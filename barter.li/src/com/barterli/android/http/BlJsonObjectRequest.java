package com.barterli.android.http;

import org.json.JSONObject;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;

/**
 * Custom {@link JsonObjectRequest} extension to carry an extra request Id
 * 
 * @author Vinay S Shenoy
 * 
 */
public class BlJsonObjectRequest extends JsonObjectRequest {

	private final int mRequestId;

	public BlJsonObjectRequest(int method, int requestId, String url,
			JSONObject jsonRequest, Listener<JSONObject> listener,
			ErrorListener errorListener) {
		super(method, url, jsonRequest, listener, errorListener);
		mRequestId = requestId;
	}

	public BlJsonObjectRequest(int requestId, String url,
			JSONObject jsonRequest, Listener<JSONObject> listener,
			ErrorListener errorListener) {
		super(url, jsonRequest, listener, errorListener);
		mRequestId = requestId;
	}

	public int getRequestId() {
		return mRequestId;
	}

}
