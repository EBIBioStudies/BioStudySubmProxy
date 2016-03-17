/*
 * Copyright (c) 2016 European Molecular Biology Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or impl
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.biostudy.submission.services;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.concurrent.ConcurrentNavigableMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.json.JSONArray;
import org.json.JSONObject;
import org.mapdb.DB;

import uk.ac.ebi.biostudy.submission.Res;
import uk.ac.ebi.biostudy.submission.UserSession;

public class SubmissionService {
	private final String bsServerUrl;
	private final DB db;

	public SubmissionService(String bsServerUrl, DB db) {
		this.bsServerUrl = bsServerUrl;
		this.db = db;
	}

	public void saveSubmission(String username, JSONObject obj) {
		ConcurrentNavigableMap<String, String> map = this.db.treeMap("submissions" + username);

		String acc = "";
		if (obj.has("accno")) {
			acc = obj.getString("accno");
		}
		if (acc.equals("!{S-STA}")) {
			acc = "TEMP-" + map.size() + 1;
			obj.put("accno", acc);
		}
		if (acc != null && !acc.equals("")) {
			map.remove(acc);
			map.put(acc, obj.toString());
		}
		db.commit();

	}

	public void deleteSubmission(final String acc, final UserSession userSession) throws HttpException, IOException {
		ConcurrentNavigableMap<String, String> map = this.db.treeMap("submissions" + userSession.getUsername());
		Iterator<String> it = map.keySet().iterator();
		while (it.hasNext()) {
			System.out.println("key " + it.next());

		}
		if (map.remove(acc) == null) {
			// nothing to delete
			System.out.println("Nothing to delete for " + acc);

		}

		db.commit();
		db.compact();

	}

	public void deleteSubmittedSubmission(final String acc, final UserSession userSession)
			throws HttpException, IOException {

		HttpMethod httpmethod = new GetMethod(this.bsServerUrl + "/submit/delete");
		httpmethod.setQueryString(Res.BsServer.SESSION_PARAM + "=" + userSession.getSessid() + "&id=" + acc);
		HttpClient client = new HttpClient();
		client.executeMethod(httpmethod);
		byte[] body = httpmethod.getResponseBody();
		httpmethod.releaseConnection();

	}

	public JSONArray listSubmissions(UserSession userSession) throws HttpException, IOException, ParseException {
		HttpMethod httpmethod = new GetMethod(this.bsServerUrl + "/sbmlist");
		httpmethod.setQueryString(Res.BsServer.SESSION_PARAM + "=" + userSession.getSessid());
		HttpClient client = new HttpClient();
		client.executeMethod(httpmethod);
		byte[] body = httpmethod.getResponseBody();
		httpmethod.releaseConnection();
		JSONArray array = new JSONArray();

		if (httpmethod.getStatusCode() == 200) {
			System.out.println("httpmethod.getStatusCode()" + httpmethod.getStatusCode());
			JSONObject object = new JSONObject(new String(body));
			if (object.getString("status").equals("OK")) {
				array = object.getJSONArray("submissions");
			}
		}

		ConcurrentNavigableMap<String, String> map = this.db.treeMap("submissions" + userSession.getUsername());
		Iterator<String> it = map.keySet().iterator();
		DateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

		while (it.hasNext()) {
			String key = it.next();
			if (map.containsKey(key) && key != null && !key.equals("")) {
				JSONObject o = new JSONObject(map.get(key));
				JSONArray attrs = o.getJSONArray("attributes");
				for (int i = 0; i < attrs.length(); i++) {
					JSONObject attr = attrs.getJSONObject(i);
					if (attr.getString("name").equals("Title")) {
						o.put("title", attr.getString("value"));
					}
					if (attr.getString("name").equals("ReleaseDate")) {
						String sdate = attr.getString("value");

						// formatDate.parse(sdate).getTime();
						// Date drdate = new Date(new Long(sdate));

						// o.put("rtime", attr.getString("value"));
					}
				}
				System.out.println("Get" + key + "key" + map.get(key));
				array.put(o);
			}
		}
		// System.out.println("Returned array" + array.toString());

		return array;
	}

	public JSONObject getSubmission(final UserSession userSession, final String acc)
			throws HttpException, IOException, ServiceException {

		HttpMethod httpmethod = new GetMethod(this.bsServerUrl + "/submission/" + acc);
		httpmethod.setQueryString(Res.BsServer.SESSION_PARAM + "=" + userSession.getSessid());
		HttpClient client = new HttpClient();
		client.executeMethod(httpmethod);
		byte[] body = httpmethod.getResponseBody();
		httpmethod.releaseConnection();
		JSONObject result = new JSONObject(new String(body));

		if (httpmethod.getStatusCode() == 200) {
			System.out.println("httpmethod.getStatusCode()" + httpmethod.getStatusCode());
			return result;
		} else {
			throw new ServiceException(httpmethod.getStatusCode(), result);
		}

	}

	public JSONObject createSubmission(UserSession userSession, JSONObject obj)
			throws HttpException, IOException, ServiceException {
		// replace accno by
		String acc = obj.getJSONArray("submissions").getJSONObject(0).getString("accno");
		obj.getJSONArray("submissions").getJSONObject(0).put("accno", "!{S-STA}");
		PostMethod httpmethod = new PostMethod(this.bsServerUrl + "/submit/create");
		httpmethod.setQueryString(Res.BsServer.SESSION_PARAM + "=" + userSession.getSessid());
		StringRequestEntity requestEntity = new StringRequestEntity(obj.toString(), "application/json", "UTF-8");
		httpmethod.setRequestEntity(requestEntity);
		HttpClient client = new HttpClient();
		client.executeMethod(httpmethod);
		byte[] body = httpmethod.getResponseBody();
		httpmethod.releaseConnection();
		JSONObject result = new JSONObject(new String(body));

		System.out.println("httpmethod.getStatusCode()" + httpmethod.getStatusCode());
		System.out.println("httpmethod.getStatusCode()" + result.toString());

		if (httpmethod.getStatusCode() != 200) {
			throw new ServiceException(httpmethod.getStatusCode(), result);
		}
		// delete temporary
		deleteSubmission(acc, userSession);
		return result;

	}

	public JSONObject updateSubmission(UserSession userSession, JSONObject obj)
			throws HttpException, IOException, ServiceException {
		// String acc =
		// obj.getJSONArray("submissions").getJSONObject(0).getString("accno");
		PostMethod httpmethod = new PostMethod(this.bsServerUrl + "/submit/update");
		httpmethod.setQueryString(Res.BsServer.SESSION_PARAM + "=" + userSession.getSessid());
		StringRequestEntity requestEntity = new StringRequestEntity(obj.toString(), "application/json", "UTF-8");
		httpmethod.setRequestEntity(requestEntity);
		HttpClient client = new HttpClient();
		client.executeMethod(httpmethod);
		byte[] body = httpmethod.getResponseBody();
		httpmethod.releaseConnection();
		JSONObject result = new JSONObject(new String(body));

		System.out.println("httpmethod.getStatusCode()" + httpmethod.getStatusCode());
		System.out.println("httpmethod.getStatusCode()" + result.toString());

		if (httpmethod.getStatusCode() != 200) {
			throw new ServiceException(httpmethod.getStatusCode(), result);
		}
		// delete temporary
		// deleteSubmission(acc, userSession);
		return result;
	}

	public JSONObject getFilesDir(UserSession userSession) throws HttpException, IOException, ServiceException {
		HttpMethod httpmethod = new GetMethod(this.bsServerUrl + "/dir");
		httpmethod.setQueryString(Res.BsServer.SESSION_PARAM + "=" + userSession.getSessid());
		HttpClient client = new HttpClient();
		client.executeMethod(httpmethod);
		byte[] body = httpmethod.getResponseBody();
		httpmethod.releaseConnection();
		JSONObject result = new JSONObject(new String(body));

		if (httpmethod.getStatusCode() == 200) {
			System.out.println("httpmethod.getStatusCode()" + httpmethod.getStatusCode());
			return result;
		} else {
			throw new ServiceException(httpmethod.getStatusCode(), result);
		}

	}

	public JSONObject deleteFile(UserSession userSession, String queryFile)
			throws HttpException, IOException, ServiceException {
		PostMethod httpmethod = new PostMethod(this.bsServerUrl + "/dir");
		httpmethod.setQueryString(
				Res.BsServer.SESSION_PARAM + "=" + userSession.getSessid() + "&command=delete&" + queryFile);
		HttpClient client = new HttpClient();
		client.executeMethod(httpmethod);
		byte[] body = httpmethod.getResponseBody();
		httpmethod.releaseConnection();
		System.out.println(new String(body));
		JSONObject result = new JSONObject(new String(body));

		System.out.println("httpmethod.getStatusCode()" + httpmethod.getStatusCode());
		System.out.println("httpmethod.getStatusCode()" + result.toString());

		if (httpmethod.getStatusCode() != 200) {
			throw new ServiceException(httpmethod.getStatusCode(), result);
		}
		return result;

	}

}
