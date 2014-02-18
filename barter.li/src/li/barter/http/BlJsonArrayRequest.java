package li.barter.http;

import org.json.JSONArray;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonArrayRequest;

/**
 * Custom {@link JsonArrayRequest} extension to carry an extra request Id
 * 
 * @author Vinay S Shenoy
 * 
 */
public class BlJsonArrayRequest extends JsonArrayRequest {

	private final int mRequestId;

	public BlJsonArrayRequest(int requestId, String url,
			Listener<JSONArray> listener, ErrorListener errorListener) {
		super(url, listener, errorListener);
		mRequestId = requestId;
	}

	public int getRequestId() {
		return mRequestId;
	}
}
