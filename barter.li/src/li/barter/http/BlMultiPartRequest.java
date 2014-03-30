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
import com.android.volley.toolbox.MultiPartRequest;

import org.apache.http.protocol.HTTP;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;

/**
 * Request class for submitting multipart requests to Volley
 * 
 * @author Vinay S Shenoy
 */
public class BlMultiPartRequest extends MultiPartRequest<ResponseInfo>
                implements IBlRequestContract {

    /**
     * An identifier for the request that was made
     */
    private int mRequestId;

    /**
     * @param method One of the constants from {@link Method} class to identify
     *            the request type
     * @param url The API endpoint
     * @param volleyCallbacks The {@link VolleyCallbacks} reference to receive
     *            the callbacks
     */
    public BlMultiPartRequest(final int method, final String url, final String requestBody, final VolleyCallbacks volleyCallbacks) {
        super(method, url, volleyCallbacks, volleyCallbacks);
    }

    @Override
    protected Response<ResponseInfo> parseNetworkResponse(
                    final NetworkResponse response) {

        final HttpResponseParser parser = new HttpResponseParser();
        try {
            return Response.success(parser
                            .getSuccessResponse(mRequestId, new String(response.data, HTTP.UTF_8)), HttpHeaderParser
                            .parseCacheHeaders(response));
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

    @Override
    public void setRequestId(int requestId) {
        mRequestId = requestId;
    }

    @Override
    public int getRequestId() {
        return mRequestId;
    }

}
