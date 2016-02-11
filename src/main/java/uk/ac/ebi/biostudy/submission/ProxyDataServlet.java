package uk.ac.ebi.biostudy.submission;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mapdb.DB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class ProxyDataServlet
 */
@WebServlet("/proxy/api/*")
public class ProxyDataServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String SESSION_PARAM = "BIOSTDSESS";
	private String BS_SERVER_URL;
	private Properties properties = new Properties();
	private static final Logger logger = LoggerFactory.getLogger(ProxyDataServlet.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ProxyDataServlet() {
		super();
	}

	@Override
	public void init() throws ServletException {
		System.out.println("Servlet started");
		super.init();
		try {
			properties.load(getServletContext().getResourceAsStream("/WEB-INF/classes/config.properties"));
			BS_SERVER_URL = properties.getProperty("BS_SERVER_URL");

		} catch (IOException e) {
			throw new ServletException("Problem to load properties from ");
		}
	}

	@Override
	public void destroy() {
		super.destroy();
	}

	private DB getDb() {
		DB db = (DB) getServletContext().getAttribute("db");
		return db;
	}

	private void saveSubmission(String username, JSONObject obj) {
		DB db = getDb();
		ConcurrentNavigableMap<String, String> map = getDb().treeMap("submissions" + username);

		map.size();
		String acc = obj.getString("accno");
		if (acc.equals("!{S-STA}")) {
			acc = "TEMP-" + map.size() + 1;
			obj.put("accno", acc);
			System.out.println(obj);
			// new one
		}
		map.remove(acc);
		map.put(acc, obj.toString());
		db.commit();

	}

	private void deleteSubmission(final String username, final String acc, final UserSession userSession)
			throws HttpException, IOException {
		DB db = getDb();
		ConcurrentNavigableMap<String, String> map = getDb().treeMap("submissions" + username);
		map.remove(acc);
		db.commit();

	}

	private void deleteSubmittedSubmission(final String username, final String acc, final UserSession userSession)
			throws HttpException, IOException {

		HttpMethod httpmethod = new GetMethod(BS_SERVER_URL + "/submit/delete");
		httpmethod.setQueryString(SESSION_PARAM + "=" + userSession.getSessid() + "&id=" + acc);
		HttpClient client = new HttpClient();
		client.executeMethod(httpmethod);
		byte[] body = httpmethod.getResponseBody();
		httpmethod.releaseConnection();

	}

	private JSONArray listSubmissions(UserSession userSession) throws HttpException, IOException {
		HttpMethod httpmethod = new GetMethod(BS_SERVER_URL + "/sbmlist");
		httpmethod.setQueryString(SESSION_PARAM + "=" + userSession.getSessid());
		HttpClient client = new HttpClient();
		client.executeMethod(httpmethod);
		byte[] body = httpmethod.getResponseBody();
		httpmethod.releaseConnection();
		JSONArray array = new JSONArray();

		if (httpmethod.getStatusCode() == 200) {
			System.out.println("httpmethod.getStatusCode()" + httpmethod.getStatusCode());
			JSONObject object = new JSONObject(new String(body));
			if (object.getString("status").equals("OK")) {
				array = object.getJSONArray("submissions");
			}
		}

		DB db = getDb();
		ConcurrentNavigableMap<String, String> map = getDb().treeMap("submissions" + userSession.getUsername());
		Iterator<String> it = map.values().iterator();
		while (it.hasNext()) {
			JSONObject o = new JSONObject(it.next());
			array.put(o);
		}
		System.out.println("Returned array" + array.toString());

		return array;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("save 1");
		JSONObject result = new JSONObject();
		PrintWriter out = response.getWriter();
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		Enumeration<String> str = getServletContext().getAttributeNames();
		if (request.getPathInfo().equals("/submissions")) {
			HttpSession session = request.getSession(false);
			if (session == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				UserSession userSession = (UserSession) session.getAttribute("userSession");
				if (userSession == null) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				} else {
					String username = userSession.getUsername();
					result.put("submissions", listSubmissions(userSession));
				}
			}

		} else if (request.getPathInfo().contains("/get")) {
			System.out.println("get");
			HttpSession session = request.getSession(false);
			if (session == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				UserSession userSession = (UserSession) session.getAttribute("userSession");
				if (userSession == null) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				} else {
					String username = userSession.getUsername();
					result.put("submissions", listSubmissions(userSession));
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
		System.out.println("Delete" + request.getPathInfo());
		String path = request.getPathInfo();

		PrintWriter out = response.getWriter();
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		JSONObject result = new JSONObject();

		Enumeration<String> str = getServletContext().getAttributeNames();
		if (request.getPathInfo().startsWith("/submission")) {
			HttpSession session = request.getSession(false);
			if (session == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				UserSession userSession = (UserSession) session.getAttribute("userSession");
				if (userSession == null) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				} else {
					String username = userSession.getUsername();
					Pattern regExp = Pattern.compile("/submission/([A-Z0-9-]*)");
					Pattern regExpSubmited = Pattern.compile("/submission/submitted/([A-Z0-9-]*)");

					Matcher matcherExp = regExp.matcher(request.getPathInfo());
					Matcher matcherExpSubm = regExpSubmited.matcher(request.getPathInfo());

					if (matcherExp.matches()) {
						String acc = matcherExp.group(1);
						System.out.println("ACC" + acc);
						deleteSubmission(username, acc, userSession);
					} else if (matcherExpSubm.matches()) {
						String acc = matcherExp.group(1);
						System.out.println("ACC" + acc);
						deleteSubmittedSubmission(username, acc, userSession);
					} else {
						System.out.println("Data: ");
						response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					}
				}
			}

		} else {
			System.out.println("Data: " + path);
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
		System.out.println("Data: " + request.getPathInfo());

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
		} else if (request.getPathInfo().equals("/set")) {
			HttpSession session = request.getSession(false);
			if (session == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				UserSession userSession = (UserSession) session.getAttribute("userSession");
				if (userSession == null) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				} else {
					String username = userSession.getUsername();
					String jsonStr = readRequestBody(request);
					System.out.println(username + jsonStr);
					JSONObject obj = new JSONObject(jsonStr);
					saveSubmission(username, obj);
					result = obj;
				}
			}

		} else if (request.getPathInfo().equals("/submission/create")) {
			HttpSession session = request.getSession(false);
			if (session == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			} else {
				UserSession userSession = (UserSession) session.getAttribute("userSession");
				if (userSession == null) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				} else {
					String jsonStr = readRequestBody(request);
					JSONObject obj = new JSONObject(jsonStr);
					createSubmission(userSession, obj);
					result = obj;
				}
			}

		} else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		}

		out.print(result);
	}

	private void createSubmission(UserSession userSession, JSONObject obj) throws HttpException, IOException {
		System.out.println(obj.toString());
		PostMethod httpmethod = new PostMethod(BS_SERVER_URL + "/submit/create");
		httpmethod.setQueryString(SESSION_PARAM + "=" + userSession.getSessid());
		StringRequestEntity requestEntity = new StringRequestEntity(obj.toString(), "application/json", "UTF-8");
		httpmethod.setRequestEntity(requestEntity);
		HttpClient client = new HttpClient();
		client.executeMethod(httpmethod);
		byte[] body = httpmethod.getResponseBody();
		httpmethod.releaseConnection();
		JSONArray array = new JSONArray();

		if (httpmethod.getStatusCode() == 200) {
			System.out.println("httpmethod.getStatusCode()" + httpmethod.getStatusCode());
			JSONObject object = new JSONObject(new String(body));
			System.out.println("httpmethod.getStatusCode()" + object.toString());

			if (object.getString("status").equals("OK")) {
				// array=object.getJSONArray("submissions");
			}
		}

	}

	private String readRequestBody(HttpServletRequest request) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = request.getReader();
		String str;
		while ((str = br.readLine()) != null) {
			sb.append(str);
		}
		return sb.toString();
	}

}
