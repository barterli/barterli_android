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
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;

import org.apache.http.protocol.HTTP;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;

/**
 * Request class for submitting requests to Volley
 * 
 * @author Vinay S Shenoy
 */
public class BlRequest extends JsonRequest<ResponseInfo> {

    /**
     * An identifier for the request that was made
     */
    private final int mRequestId;

    /**
     * @param method One of the constants from {@link Method} class to identify
     *            the request type
     * @param requestId One of the {@linkplain HttpConstants.RequestId}
     *            constants
     * @param url The API endpoint
     * @param requestBody A string represention of the Json request body
     * @param listener The {@link Listener} for the response
     * @param errorListener The {@link ErrorListener} for the error response
     */
    public BlRequest(final int method, final int requestId, final String url, final String requestBody, final Listener<ResponseInfo> listener, final ErrorListener errorListener) {
        super(method, url, requestBody, listener, errorListener);
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

    @Override
    protected Response<ResponseInfo> parseNetworkResponse(
                    NetworkResponse response) {
        
        final HttpResponseParser parser = new HttpResponseParser();
        try {
            return Response.success(parser
                            .getSuccessResponse(mRequestId, new String(response.data, HTTP.UTF_8)), HttpHeaderParser
                            .parseCacheHeaders(response));
        } catch (JSONException e) {
            return Response.error(new ParseError(e));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        }
    }

}
