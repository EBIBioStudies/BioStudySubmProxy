package uk.ac.ebi.biostudy.submission;

import java.io.BufferedReader;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.mapdb.DB;
import org.mockito.Mockito;

import uk.ac.ebi.biostudy.submission.services.SubmissionService;

public class ProxyDataServletTest extends Mockito {
	HttpServletRequest request;
	HttpServletResponse response;
	HttpSession session;
	BufferedReader br;
	PrintWriter out;
	SubmissionService submissionService;
	DB db;
	UserSession userSession;

	@Before
	public void beforeEach() {
		request = mock(HttpServletRequest.class);
		response = mock(HttpServletResponse.class);
		session = mock(HttpSession.class);
		br = mock(BufferedReader.class);
		out = mock(PrintWriter.class);
		submissionService = mock(SubmissionService.class);
		db = mock(DB.class);
		userSession = new UserSession();
		userSession.setUsername("user");
	}

	@Test
	public void doDelete_BadRequest() throws Exception {
		when(request.getPathInfo()).thenReturn("/delete/E-STA-12");
		when(request.getSession(true)).thenReturn(session);
		when(response.getWriter()).thenReturn(out);

		ProxyDataServletForTest proxyDataServlet = new ProxyDataServletForTest(db);
		ProxyDataServletForTest spy = spy(proxyDataServlet);
		spy.doDelete(request, response);

		verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}

	@Test
	public void doDelete_NotAuthorized() throws Exception {
		when(request.getPathInfo()).thenReturn("/submission/delete/E-STA-12");
		when(request.getSession(true)).thenReturn(session);
		when(response.getWriter()).thenReturn(out);

		ProxyDataServletForTest proxyDataServlet = new ProxyDataServletForTest(db);
		ProxyDataServletForTest spy = spy(proxyDataServlet);
		spy.doDelete(request, response);

		verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	}

	@Test
	public void doDelete() throws Exception {
		when(request.getPathInfo()).thenReturn("/submission/E-STA-12");
		when(request.getSession(false)).thenReturn(session);
		when(session.getAttribute(Res.Session.userSession)).thenReturn(userSession);
		when(response.getWriter()).thenReturn(out);

		ProxyDataServlet proxyDataServlet = new ProxyDataServlet();
		ProxyDataServlet spy = spy(proxyDataServlet);
		doReturn(db).when(spy).getDb();
		doReturn(submissionService).when(spy).createSubmissionService();
		spy.doDelete(request, response);

		verify(response).setStatus(HttpServletResponse.SC_OK);
		// verify(submissionService).deleteSubmission(anyString(), userSession);

	}

	@Test
	public void doDelete_Temp() throws Exception {
		when(request.getPathInfo()).thenReturn("/submission/submited/TEMP-12");
		when(request.getSession(false)).thenReturn(session);
		when(session.getAttribute(Res.Session.userSession)).thenReturn(userSession);
		when(response.getWriter()).thenReturn(out);

		ProxyDataServlet proxyDataServlet = new ProxyDataServlet();
		ProxyDataServlet spy = spy(proxyDataServlet);
		doReturn(db).when(spy).getDb();
		doReturn(submissionService).when(spy).createSubmissionService();
		spy.doDelete(request, response);

		verify(response).setStatus(HttpServletResponse.SC_OK);
		// verify(submissionService).deleteSubmittedSubmission(anyString(),
		// userSession);

	}

}
