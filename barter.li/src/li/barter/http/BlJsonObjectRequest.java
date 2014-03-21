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

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

/**
 * {@link JsonObjectRequest} extension
 * 
 * @author Vinay S Shenoy
 */
public class BlJsonObjectRequest extends JsonObjectRequest {

    /**
     * An identifier for the request that was made
     */
    private final int mRequestId;

    /**
     * Build a request to read a {@link JSONObject} response
     * 
     * @param method One of the constants from {@link Method} class to identify
     *            the request type
     * @param requestId One of the {@linkplain HttpConstants.RequestId}
     *            constants
     * @param url The API endpoint
     * @param jsonRequest A {@link JSONObject} to use as the request body
     * @param listener The {@link Listener} for the response
     * @param errorListener The {@link ErrorListener} for the error response
     */
    public BlJsonObjectRequest(final int method, final int requestId, final String url, final JSONObject jsonRequest, final Listener<JSONObject> listener, final ErrorListener errorListener) {
        super(method, url, jsonRequest, listener, errorListener);
        mRequestId = requestId;
    }

    /**
     * Build a request to read a {@link JSONObject} response
     * 
     * @param requestId One of the {@linkplain HttpConstants.RequestId}
     *            constants
     * @param url The API endpoint
     * @param jsonRequest A {@link JSONObject} to use as the request body.
     *            Request type will be set to GET if <code>null</code>, POST if
     *            not
     * @param listener The {@link Listener} for the response
     * @param errorListener The {@link ErrorListener} for the error response
     */
    public BlJsonObjectRequest(final int requestId, final String url, final JSONObject jsonRequest, final Listener<JSONObject> listener, final ErrorListener errorListener) {
        super(url, jsonRequest, listener, errorListener);
        mRequestId = requestId;
    }

    /**
     * Gets the request Id associated with this request
     * 
     * @return An integer representing the request Id
     */
    public int getRequestId() {
        return mRequestId;
    }

}
