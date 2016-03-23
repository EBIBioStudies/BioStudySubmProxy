package uk.ac.ebi.biostudy.submission.exceptions;

import javax.servlet.http.HttpServletResponse;

public class InternalServerError extends BaseException {

	private static final long serialVersionUID = 1L;

	public InternalServerError(String message) {
		super(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
	}

	public InternalServerError() {
		super(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

	}

}
