
package uk.ac.ebi.biostudy.submission;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;
import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.biostudy.submission.exceptions.AuthException;
import uk.ac.ebi.biostudy.submission.exceptions.BadRequestException;
import uk.ac.ebi.biostudy.submission.exceptions.InternalServerError;
import uk.ac.ebi.biostudy.submission.exceptions.ServiceException;
import uk.ac.ebi.biostudy.submission.services.AuthService;
import uk.ac.ebi.biostudy.submission.services.SubmissionService;

/**
 * Servlet implementation class ProxyDataServlet
 */
@WebServlet("/proxy/api/*")
public final class ProxyDataServlet extends AbstractServletImpl {

	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(ProxyDataServlet.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ProxyDataServlet() {
		super();
	}

	public DB getDb() {
		DB db = (DB) getServletContext().getAttribute("db");
		return db;
	}

	public SubmissionService createSubmissionService() {
		return new SubmissionService(BS_SERVER_URL, getDb());
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		UserSession userSession = this.getUserSession(session);
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");

		JSONObject result = new JSONObject();
		PrintWriter out = response.getWriter();

		try {
			this.isSignedIn(session);
		} catch (AuthException e1) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		String path = request.getPathInfo();
		try {
			this.processGet(path, userSession, result);
		} catch (InternalServerError | ServiceException | BadRequestException e) {
			response.setStatus(e.getCode());
			result = e.getResult();
		}
		out.print(result);

	}

	private void processGet(String path, UserSession userSession, JSONObject result)
			throws InternalServerError, ServiceException, BadRequestException {
		SubmissionService service = new SubmissionService(this.BS_SERVER_URL, getDb());

		if (path.equals("/submissions")) {
			try {
				result.put("submissions", service.listSubmissions(userSession));
			} catch (JSONException | IOException e) {
				throw new InternalServerError("Parse error");
			}

		} else if (path.contains("/submission")) {
			Pattern regExp = Pattern.compile("/submission/([A-Z0-9-]*)");
			Matcher matcherExp = regExp.matcher(path);
			if (matcherExp.matches()) {
				String acc = matcherExp.group(1);
				try {
					result = service.getSubmission(userSession, acc);
				} catch (IOException e) {
					InternalServerError error = new InternalServerError("Internal Error");
				}
			} else {
				throw new BadRequestException();
			}

		} else if (path.contains("/files/dir")) {
			try {
				result = service.getFilesDir(userSession);
			} catch (IOException e) {
				throw new InternalServerError("Internal Error");
			}

		} else {
			throw new BadRequestException();
		}

	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		JSONObject result = new JSONObject();
		HttpSession session = request.getSession(false);
		UserSession userSession = this.getUserSession(session);
		try {
			this.isSignedIn(session);
		} catch (AuthException e1) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return;
		}

		String path = request.getPathInfo();
		String queryString = request.getQueryString();
		try {
			processDelete(path, queryString, userSession, result);
		} catch (InternalServerError | ServiceException | BadRequestException e) {
			response.setStatus(e.getCode());
			result = e.getResult();
		}
		out.print(result);

	}

	private void processDelete(String path, String queryString, UserSession userSession, JSONObject result)
			throws InternalServerError, ServiceException, BadRequestException {
		SubmissionService service = new SubmissionService(this.BS_SERVER_URL, getDb());

		if (path.startsWith("/submission")) {
			String username = userSession.getUsername();
			Pattern regExp = Pattern.compile("/submission/([A-Z0-9-]*)");
			Pattern regExpSubmited = Pattern.compile("/submission/submited/([A-Z0-9-]*)");

			Matcher matcherExp = regExp.matcher(path);
			Matcher matcherExpSubm = regExpSubmited.matcher(path);
			if (matcherExp.matches()) {
				String acc = matcherExp.group(1);
				try {
					service.deleteSubmission(acc, userSession);
				} catch (IOException e) {
					throw new InternalServerError();
				}
			} else if (matcherExpSubm.matches()) {
				String acc = matcherExpSubm.group(1);
				try {
					service.deleteSubmittedSubmission(acc, userSession);
				} catch (IOException e) {
					throw new InternalServerError();
				}
			} else {
				throw new BadRequestException();
			}

		} else if (path.startsWith("/files/delete")) {
			try {
				result = service.deleteFile(userSession, queryString);
			} catch (IOException e) {
				throw new InternalServerError();
			}
		} else {
			throw new BadRequestException();
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		JSONObject result = new JSONObject();
		PrintWriter out = response.getWriter();
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		String path = request.getPathInfo();
		if (path.contains("/auth")) {
			try {
				processAuthPost(request, result);
			} catch (InternalServerError | ServiceException | BadRequestException e) {

			}
		} else {
			HttpSession session = request.getSession(false);
			UserSession userSession = this.getUserSession(session);
			try {
				this.isSignedIn(session);
			} catch (AuthException e) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setStatus(e.getCode());
				result = e.getResult();
				return;
			}
			try {
				processPost(request, userSession, result);
			} catch (InternalServerError | ServiceException | BadRequestException e) {
				response.setStatus(e.getCode());
				result = e.getResult();

			}
		}

		out.print(result);

	}

	private void processPost(HttpServletRequest request, UserSession userSession, JSONObject result)
			throws InternalServerError, ServiceException, BadRequestException {
		String path = request.getPathInfo();
		SubmissionService service = new SubmissionService(this.BS_SERVER_URL, getDb());

		if (path.startsWith("/submission/save")) {
			String username = userSession.getUsername();
			String jsonStr = "";
			try {
				jsonStr = readRequestBody(request);
			} catch (IOException e) {
				e.printStackTrace();
			}
			JSONObject obj = new JSONObject(jsonStr);
			service.saveSubmission(username, obj);
			result = obj;
		} else if (path.equals("/submission/create")) {
			String jsonStr = "";
			try {
				jsonStr = readRequestBody(request);
			} catch (IOException e) {
				e.printStackTrace();
			}
			JSONObject obj = new JSONObject(jsonStr);
			try {
				result = service.createSubmission(userSession, obj);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			throw new BadRequestException();
		}
	}

	private void processAuthPost(HttpServletRequest request, JSONObject result)
			throws InternalServerError, ServiceException, BadRequestException {
		String path = request.getPathInfo();

		if (path.equals("/auth/register")) {
			String jsonStr;
			try {
				jsonStr = readRequestBody(request);
			} catch (IOException e) {
				throw new InternalServerError();
			}
			JSONObject obj = new JSONObject(jsonStr);
			UserSession userSession = new UserSession();
			if (obj.has("username")) {
				userSession.setUsername(obj.getString("username"));
			}
			if (obj.has("sessid")) {
				userSession.setSessid(obj.getString("sessid"));
			}
			File f = new File("submission" + userSession.getUsername() + ".json");
			userSession.setSubmissionFile(f);
			HttpSession session = request.getSession(true);
			session.setAttribute("userSession", userSession);
			result.put("token", "token");
		} else if (path.equals("/auth/signup")) {
			HttpSession session = request.getSession(false);
			AuthService authService = new AuthService(BS_SERVER_URL);
			JSONObject body;
			try {
				body = readRequestBodyAsJson(request);
			} catch (IOException e1) {
				throw new InternalServerError();
			}
			String activationUrl = generateActivationUrl(request);
			JSONObject obj;
			try {
				obj = authService.singUp(body, activationUrl);
			} catch (IOException e) {
				throw new InternalServerError();
			}
			result = obj;
			// api/auth/signout
		} else if (path.equals("/auth/signout")) {
			HttpSession session = request.getSession(false);
			UserSession userSession = (UserSession) session.getAttribute(Res.Session.userSession);
			AuthService authService = new AuthService(BS_SERVER_URL);
			JSONObject obj;
			try {
				obj = authService.singOut(userSession);
			} catch (IOException e) {
				throw new InternalServerError();
			}
			logger.debug("Sign out" + obj.toString());
			session.invalidate();
		} else {
			throw new BadRequestException();
		}

	}

	private String generateActivationUrl(HttpServletRequest request) {
		String url = "http://" + request.getServerName()
				+ "/biostudies/submissions/index.html#activate/{ACTIVATION:KEY}";
		return url;
	}

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// super.doPut(req, resp);
		JSONObject result = new JSONObject();
		PrintWriter out = response.getWriter();
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		SubmissionService service = new SubmissionService(this.BS_SERVER_URL, getDb());

		logger.info("Call put method", request.getPathInfo());

		// update submission
		if (request.getPathInfo().startsWith("/submission/update")) {
			HttpSession session = request.getSession(false);
			if (session == null) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			} else {
				UserSession userSession = (UserSession) session.getAttribute("userSession");
				if (userSession == null) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				} else {
					String jsonStr = readRequestBody(request);
					System.out.println(jsonStr);
					JSONObject obj = new JSONObject(jsonStr);
					try {
						result = service.updateSubmission(userSession, obj);
					} catch (ServiceException e) {
						response.setStatus(e.getCode());
						result = e.getResult();
					}
				}
			}
		}
	}

	private JSONObject readRequestBodyAsJson(HttpServletRequest request) throws IOException {
		String body = readRequestBody(request);
		JSONObject result = new JSONObject(body);
		return result;
	}

}
