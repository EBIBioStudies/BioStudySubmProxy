package uk.ac.ebi.biostudy.submission.services;

import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.JSONObject;

import uk.ac.ebi.biostudy.submission.Res;
import uk.ac.ebi.biostudy.submission.UserSession;

public class AuthService {
	private final String bsServerUrl;

	public AuthService(String bsServerUrl) {
		this.bsServerUrl = bsServerUrl;
	}

	public JSONObject singOut(UserSession userSession) throws HttpException, IOException {
		PostMethod httpmethod = new PostMethod(this.bsServerUrl + "/auth/signout");
		httpmethod.setQueryString(Res.BsServer.SESSION_PARAM + "=" + userSession.getSessid());
		HttpClient client = new HttpClient();
		JSONObject requestObj = new JSONObject();
		requestObj.put("username", userSession.getUsername());
		requestObj.put("sessid", userSession.getSessid());
		StringRequestEntity requestEntity = new StringRequestEntity(requestObj.toString(), "application/json", "UTF-8");
		httpmethod.setRequestEntity(requestEntity);

		client.executeMethod(httpmethod);
		byte[] body = httpmethod.getResponseBody();
		httpmethod.releaseConnection();
		JSONObject array = new JSONObject();
		System.out.println("httpmethod.getStatusCode()" + httpmethod.getStatusCode());
		JSONObject object = new JSONObject(new String(body));
		return object;
	}

	public JSONObject singUp(JSONObject signUpReq, String activationUrl) throws HttpException, IOException {
		PostMethod httpmethod = new PostMethod(this.bsServerUrl + "/auth/signup");
		HttpClient client = new HttpClient();
		signUpReq.put("activationURL", activationUrl);
		StringRequestEntity requestEntity = new StringRequestEntity(signUpReq.toString(), "application/json", "UTF-8");
		httpmethod.setRequestEntity(requestEntity);

		client.executeMethod(httpmethod);
		byte[] body = httpmethod.getResponseBody();
		httpmethod.releaseConnection();
		JSONObject array = new JSONObject();
		System.out.println("httpmethod.getStatusCode()" + httpmethod.getStatusCode());
		JSONObject object = new JSONObject(new String(body));
		return object;
	}
}
