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
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;

/**
 * {@link JsonObjectRequest} extension
 * 
 * @author Vinay S Shenoy
 */
public class BlJsonArrayRequest extends JsonArrayRequest {

    /**
     * An identifier for the request that was made
     */
    private final int mRequestId;

    /**
     * Build a request to read a {@link JSONArray} response
     * 
     * @param requestId One of the {@linkplain HttpConstants.RequestId}
     *            constants
     * @param url The API endpoint
     * @param listener The {@link Listener} for the response
     * @param errorListener The {@link ErrorListener} for the error response
     */
    public BlJsonArrayRequest(final int requestId, final String url, final Listener<JSONArray> listener, final ErrorListener errorListener) {
        super(url, listener, errorListener);
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
