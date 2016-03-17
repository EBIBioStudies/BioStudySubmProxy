package uk.ac.ebi.biostudy.submission;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

public class AbstractServletImpl extends HttpServlet {

	protected String BS_SERVER_URL;
	protected Properties properties = new Properties();

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

	protected String readRequestBody(HttpServletRequest request) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader br = request.getReader();
		String str;
		while ((str = br.readLine()) != null) {
			sb.append(str);
		}
		return sb.toString();
	}
}
