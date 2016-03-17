package uk.ac.ebi.biostudy.submission;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
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

import uk.ac.ebi.biostudy.submission.services.AuthService;
import uk.ac.ebi.biostudy.submission.services.ServiceException;
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

		JSONObject result = new JSONObject();
		PrintWriter out = response.getWriter();
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		SubmissionService service = new SubmissionService(this.BS_SERVER_URL, getDb());
		String path = request.getPathInfo();
		if (path.equals("/submissions")) {
			HttpSession session = request.getSession(false);
			if (session == null) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			} else {
				UserSession userSession = (UserSession) session.getAttribute("userSession");
				if (userSession == null) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				} else {
					String username = userSession.getUsername();
					try {
						result.put("submissions", service.listSubmissions(userSession));
					} catch (JSONException | ParseException e) {
						e.printStackTrace();
						response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
					}
				}
			}

		} else if (request.getPathInfo().contains("/submission")) {
			HttpSession session = request.getSession(false);
			if (session == null) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			} else {
				UserSession userSession = (UserSession) session.getAttribute("userSession");
				if (userSession == null) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				} else {
					Pattern regExp = Pattern.compile("/submission/([A-Z0-9-]*)");

					Matcher matcherExp = regExp.matcher(request.getPathInfo());
					if (matcherExp.matches()) {
						String acc = matcherExp.group(1);
						try {
							result = service.getSubmission(userSession, acc);
						} catch (ServiceException e) {
							response.setStatus(e.getCode());
							result = e.getResult();
						}
					} else {
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					}
				}
			}

		} else if (request.getPathInfo().contains("/files/dir")) {
			System.out.println("API DIR");
			HttpSession session = request.getSession(false);
			if (session == null) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			} else {
				UserSession userSession = (UserSession) session.getAttribute("userSession");
				if (userSession == null) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				} else {

					try {
						result = service.getFilesDir(userSession);
					} catch (ServiceException e) {
						response.setStatus(e.getCode());
						result = e.getResult();
					}
				}
			}

		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		out.print(result);

	}

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String path = request.getPathInfo();

		PrintWriter out = response.getWriter();
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		JSONObject result = new JSONObject();
		SubmissionService service = this.createSubmissionService();

		// Enumeration<String> str = getServletContext().getAttributeNames();
		if (path.startsWith("/submission")) {

			HttpSession session = request.getSession(false);
			if (session == null) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			} else {
				System.out.println("asdsada 1");

				UserSession userSession = (UserSession) session.getAttribute("userSession");
				if (userSession == null) {
					System.out.println("Delete" + request.getPathInfo());
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				} else {
					String username = userSession.getUsername();
					Pattern regExp = Pattern.compile("/submission/([A-Z0-9-]*)");
					Pattern regExpSubmited = Pattern.compile("/submission/submited/([A-Z0-9-]*)");

					Matcher matcherExp = regExp.matcher(request.getPathInfo());
					Matcher matcherExpSubm = regExpSubmited.matcher(request.getPathInfo());
					System.out.println("asdsada 2" + request.getPathInfo());
					if (matcherExp.matches()) {
						String acc = matcherExp.group(1);
						service.deleteSubmission(acc, userSession);
					} else if (matcherExpSubm.matches()) {
						String acc = matcherExpSubm.group(1);
						service.deleteSubmittedSubmission(acc, userSession);
					} else {
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					}
				}
			}

		} else if (path.startsWith("/files/delete")) {

			HttpSession session = request.getSession(false);
			if (session == null) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			} else {
				System.out.println("files delete" + request.getQueryString());

				UserSession userSession = (UserSession) session.getAttribute("userSession");
				if (userSession == null) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				} else {
					try {
						result = service.deleteFile(userSession, request.getQueryString());
					} catch (ServiceException e) {
					}
					response.setStatus(HttpServletResponse.SC_OK);
				}
			}
		}

		else {
			System.out.println("Data: delete" + path);
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}
		out.print(result);

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
		SubmissionService service = new SubmissionService(this.BS_SERVER_URL, getDb());

		logger.info("Call post method", request.getPathInfo());

		if (request.getPathInfo().equals("/auth/register")) {
			String jsonStr = readRequestBody(request);
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

			// obj.get("userid");
		} else if (request.getPathInfo().equals("/auth/signup")) {
			HttpSession session = request.getSession(false);
			AuthService authService = new AuthService(BS_SERVER_URL);
			JSONObject body = readRequestBodyAsJson(request);
			String activationUrl = generateActivationUrl(request);
			JSONObject obj = authService.singUp(body, activationUrl);
			result = obj;
			// api/auth/signout
		} else if (request.getPathInfo().equals("/auth/signout")) {
			HttpSession session = request.getSession(false);
			UserSession userSession = (UserSession) session.getAttribute(Res.Session.userSession);
			AuthService authService = new AuthService(BS_SERVER_URL);
			JSONObject obj = authService.singOut(userSession);
			logger.debug("Sign out" + obj.toString());
			session.invalidate();
			// api/auth/signout
		} else if (request.getPathInfo().equals("/set")) { // Delete this
			HttpSession session = request.getSession(false);
			if (session == null) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			} else {
				UserSession userSession = (UserSession) session.getAttribute("userSession");
				if (userSession == null) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				} else {
					String username = userSession.getUsername();
					String jsonStr = readRequestBody(request);
					System.out.println(username + jsonStr);
					JSONObject obj = new JSONObject(jsonStr);
					service.saveSubmission(username, obj);
					result = obj;
				}
			}

		} else if (request.getPathInfo().startsWith("/submission/save")) {
			HttpSession session = request.getSession(false);
			if (session == null) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			} else {
				UserSession userSession = (UserSession) session.getAttribute("userSession");
				if (userSession == null) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				} else {
					String username = userSession.getUsername();
					String jsonStr = readRequestBody(request);
					System.out.println(username + jsonStr);
					JSONObject obj = new JSONObject(jsonStr);
					service.saveSubmission(username, obj);
					result = obj;
				}
			}

		} else if (request.getPathInfo().equals("/submission/create")) {
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
						result = service.createSubmission(userSession, obj);
					} catch (ServiceException e) {
						response.setStatus(e.getCode());
						result = e.getResult();
					}
				}
			}

		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}

		out.print(result);
	}

	private String generateActivationUrl(HttpServletRequest request) {
		String url = request.getServerName() + "/biostudies/submissions/index.html#activate/{ACTIVATION:KEY}";
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
