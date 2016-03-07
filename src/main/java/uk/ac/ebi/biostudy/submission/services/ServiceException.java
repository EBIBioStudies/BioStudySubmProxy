package uk.ac.ebi.biostudy.submission.services;

import org.json.JSONObject;

public class ServiceException extends Exception {
	private int code;
	private JSONObject result;

	public ServiceException(int code, JSONObject result) {
		this.code = code;
		this.result = result;

	}

	public int getCode() {
		return code;
	}

	public JSONObject getResult() {
		return result;
	}
}
