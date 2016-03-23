package uk.ac.ebi.biostudy.submission.exceptions;

import javax.servlet.http.HttpServletResponse;

public class AuthException extends BaseException {

	private static final long serialVersionUID = 1L;

	public AuthException() {
		super(HttpServletResponse.SC_UNAUTHORIZED);
	}

	public AuthException(String message) {
		super(HttpServletResponse.SC_UNAUTHORIZED, message);
	}

}
