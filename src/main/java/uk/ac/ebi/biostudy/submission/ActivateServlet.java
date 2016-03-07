package uk.ac.ebi.biostudy.submission;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class ActivateServlet
 */
@WebServlet("/activate")
public class ActivateServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String BS_SERVER_URL;
	private Properties properties = new Properties();

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public ActivateServlet() {
		super();
		// TODO Auto-generated constructor stub
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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String query = request.getQueryString();
		String[] params = query.split("&");
		Map<String, String> map = new HashMap<String, String>();
		for (String param : params) {
			String name = param.split("=")[0];
			String value = param.split("=")[1];
			map.put(name, value);
		}
		String value = map.get("key");
		System.out.println(value);
		String urlServer = request.getServerName() + "/biostudies/submissions";
		String urlSucces = urlServer + "/registered.html";
		String urlFailure = urlServer + "/registeredError.html";

		String urlBackend = BS_SERVER_URL + "/auth/activate/" + value + "?activationSuccessURL=" + urlSucces
				+ "&activationFailURL=" + urlFailure;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
