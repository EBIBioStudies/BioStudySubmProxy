package uk.ac.ebi.biostudy.submission;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

import static uk.ac.ebi.biostudy.submission.Transformers.transformActivationReq;


@WebServlet("/activate")
public class ActivateServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        MyProxy proxy = MyContext.getProxy(request.getServletContext());
        proxy.executeMethod(transformActivationReq(), request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
