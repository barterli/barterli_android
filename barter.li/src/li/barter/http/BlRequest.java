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

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyError.ErrorCode;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.apache.http.protocol.HTTP;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Request class for submitting requests to Volley
 * 
 * @author Vinay S Shenoy
 */
public class BlRequest extends JsonRequest<ResponseInfo> implements
                IBlRequestContract {

    /**
     * An identifier for the request that was made
     */
    private int                 mRequestId;

    /**
     * Any extras that can be passed into the request
     */
    private Map<String, Object> mExtras;

    /**
     * @param method One of the constants from {@link Method} class to identify
     *            the request type
     * @param url The API endpoint
     * @param requestBody A string represention of the Json request body
     * @param volleyCallbacks A {@link VolleyCallbacks} reference to receive the
     *            volley callbacks
     */
    public BlRequest(final int method, final String url, final String requestBody, final VolleyCallbacks volleyCallbacks) {
        super(method, url, requestBody, volleyCallbacks, volleyCallbacks);
    }

    @Override
    protected Response<ResponseInfo> parseNetworkResponse(
                    final NetworkResponse response) {

        final HttpResponseParser parser = new HttpResponseParser();
        try {
            final ResponseInfo responseInfo = parser
                            .getSuccessResponse(mRequestId, new String(response.data, HTTP.UTF_8));

            if (responseInfo.success) {
                return Response.success(responseInfo, HttpHeaderParser
                                .parseCacheHeaders(response));
            }

            return Response.error(new ParseError("Unable to parse and store data!"));
        } catch (final JSONException e) {
            return Response.error(new ParseError(e));
        } catch (final UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    protected VolleyError parseNetworkError(final VolleyError volleyError) {

        if (volleyError.errorCode == ErrorCode.BAD_REQUEST_ERROR) {
            try {

                final HttpResponseParser parser = new HttpResponseParser();
                return parser.getErrorResponse(mRequestId, new String(volleyError.networkResponse.data, HTTP.UTF_8));
            } catch (final UnsupportedEncodingException e) {
                return new ParseError(e);
            } catch (final JSONException e) {
                return new ParseError(e);
            }
        } else {
            return super.parseNetworkError(volleyError);
        }
    }

    /**
     * Gets the extras associated with this request
     * 
     * @return The extras returned with this request. Will not be
     *         <code>null</code>
     */
    @Override
    public Map<String, Object> getExtras() {

        if (mExtras != null) {
            return mExtras;
        }

        return Collections.<String, Object> emptyMap();
    }

    /**
     * Add an extra to the request
     * 
     * @param key The key
     * @param value The value to map to the key
     */
    @Override
    public void addExtra(final String key, final Object value) {

        if (mExtras == null) {
            mExtras = new HashMap<String, Object>();
        }
        mExtras.put(key, value);
    }

    @Override
    public void setRequestId(final int requestId) {
        mRequestId = requestId;
    }

    @Override
    public int getRequestId() {
        return mRequestId;
    }

}
