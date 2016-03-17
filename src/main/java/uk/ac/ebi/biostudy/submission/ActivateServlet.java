package uk.ac.ebi.biostudy.submission;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.json.JSONObject;

/**
 * Servlet implementation class ActivateServlet
 */
@WebServlet("/activate")
public class ActivateServlet extends AbstractServletImpl {
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

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		JSONObject obj = new JSONObject(readRequestBody(request));
		if (obj.has("key")) {
			String key = obj.getString("key");
			String urlBackend = BS_SERVER_URL + "/auth/activate/" + key;
			HttpMethod httpmethod = new GetMethod(urlBackend);
			HttpClient client = new HttpClient();
			client.executeMethod(httpmethod);
			byte[] body = httpmethod.getResponseBody();
			if (httpmethod.getStatusCode() == 200) {
			} else {
			}
			String bodyStr = new String(body);
			httpmethod.releaseConnection();
		} else {

		}

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
