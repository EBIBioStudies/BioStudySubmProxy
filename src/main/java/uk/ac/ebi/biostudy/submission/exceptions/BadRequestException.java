package uk.ac.ebi.biostudy.submission.exceptions;

import javax.servlet.http.HttpServletResponse;

public class BadRequestException extends BaseException {

	private static final long serialVersionUID = 1L;

	public BadRequestException(String message) {
		super(HttpServletResponse.SC_BAD_REQUEST, message);
	}

	public BadRequestException() {
		super(HttpServletResponse.SC_BAD_REQUEST);

	}

}
