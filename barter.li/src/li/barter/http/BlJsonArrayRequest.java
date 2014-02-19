/*******************************************************************************
 * Copyright 2014, barter.li
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
