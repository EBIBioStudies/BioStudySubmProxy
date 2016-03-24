package uk.ac.ebi.biostudy.submission.proxy;

import uk.ac.ebi.biostudy.submission.MyContext;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static uk.ac.ebi.biostudy.submission.MyContext.getProxy;
import static uk.ac.ebi.biostudy.submission.proxy.Transformers.transformActivationReq;


@WebServlet("/activate")
public class ActivateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Proxy proxy = getProxy(getServletContext());
        proxy.executeMethod(transformActivationReq(), request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
