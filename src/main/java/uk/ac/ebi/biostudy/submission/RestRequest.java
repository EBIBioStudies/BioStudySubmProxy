package uk.ac.ebi.biostudy.submission;

import java.util.regex.Matcher;

import java.util.regex.Pattern;

import javax.servlet.ServletException;

public final class RestRequest {
	// Accommodate two requests, one for all resources, another for a specific
	// resource
	private static final Pattern regExAllPattern = Pattern.compile("/submission");
	private static final Pattern regExIdPattern = Pattern.compile("/submission/([A-Z]*)");
	private Pattern[] patterns;
	private String pathInfo;

	public RestRequest(String pathInfo, Pattern[] patterns) throws ServletException {
		// regex parse pathInfo
		Matcher matcher;

		// Check for ID case first, since the All pattern would also match
		matcher = regExIdPattern.matcher(pathInfo);
		if (matcher.find()) {
			return;
		}

		matcher = regExAllPattern.matcher(pathInfo);
		if (matcher.find())
			return;

		throw new ServletException("Invalid URI");
	}

	public void check() {

	}
}
