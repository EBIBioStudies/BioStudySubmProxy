package uk.ac.ebi.biostudy.submission.exceptions;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class BaseException extends Exception {
	private final int code;
	private final JSONObject result;

	public BaseException(int code, JSONObject result) {
		this.code = code;
		this.result = result;
	}

	public BaseException(int code) {
		this.code = code;
		Map<String, String> map = new HashMap<String, String>();
		map.put("message", "Error");
		this.result = new JSONObject(map);
	}

	public BaseException(int code, String message) {
		this.code = code;
		Map<String, String> map = new HashMap<String, String>();
		map.put("message", message);
		this.result = new JSONObject(map);
	}

	public int getCode() {
		return code;
	}

	public JSONObject getResult() {
		return result;
	}

}
