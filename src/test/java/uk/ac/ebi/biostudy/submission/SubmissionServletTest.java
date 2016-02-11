package uk.ac.ebi.biostudy.submission;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;
import org.mockito.Mockito;

public class SubmissionServletTest extends Mockito {
	
	@Test
	public void test() throws ServletException, IOException {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		//SubmissionServlet submissionServlet = spy(SubmissionServlet.class);
		//submissionServlet.doDelete(request, response);
	}
}
