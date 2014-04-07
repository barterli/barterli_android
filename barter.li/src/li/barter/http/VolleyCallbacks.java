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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.VolleyError.ErrorCode;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

import li.barter.http.HttpConstants.RequestId;
import li.barter.utils.AppConstants.UserInfo;

/**
 * Class to encapsulate Volley Listeners
 * 
 * @author Vinay S Shenoy
 */
public class VolleyCallbacks implements Listener<ResponseInfo>, ErrorListener {

    private final RequestQueue   mRequestQueue;
    private final IHttpCallbacks mHttpCallbacks;

    public VolleyCallbacks(RequestQueue requestQueue, IHttpCallbacks httpCallbacks) {

        mRequestQueue = requestQueue;
        mHttpCallbacks = httpCallbacks;

        assert (mRequestQueue != null);
        assert (mHttpCallbacks != null);
    }

    @Override
    public void onErrorResponse(VolleyError error, Request<?> request) {
        //Requests used in the application SHOULD implement IBlRequestContract
        assert (request instanceof IBlRequestContract);
        final IBlRequestContract blRequestContract = (IBlRequestContract) request;
        mHttpCallbacks.onPostExecute(blRequestContract);

        switch (error.errorCode) {

            case ErrorCode.AUTH_FAILURE_ERROR: { //Http 401
                mHttpCallbacks.onAuthError(blRequestContract.getRequestId(), blRequestContract);
                break;
            }

            case ErrorCode.BAD_REQUEST_ERROR: { //Http 400
                assert (error instanceof BlBadRequestError);
                final BlBadRequestError blBadRequestError = (BlBadRequestError) error;
                mHttpCallbacks.onBadRequestError(blRequestContract
                                .getRequestId(), blRequestContract, blBadRequestError.serverErrorCode, blBadRequestError.serverErrorMessage, blBadRequestError.responseBundle);
                break;
            }

            default: {
                mHttpCallbacks.onOtherError(blRequestContract.getRequestId(), blRequestContract, error.errorCode);
            }
        }

    }

    @Override
    public void onResponse(ResponseInfo response, Request<ResponseInfo> request) {

        //Requests used in the application SHOULD implement IBlRequestContract
        assert (request instanceof IBlRequestContract);
        final IBlRequestContract blRequestContract = (IBlRequestContract) request;
        mHttpCallbacks.onPostExecute(blRequestContract);
        mHttpCallbacks.onSuccess(blRequestContract.getRequestId(), blRequestContract, response);
    }

    /**
     * Adds a request to the queue
     * 
     * @param request The {@link Request} to be added to the queue. This request
     *            MUST be a version of the application's custom request types
     *            and must implement {@link IBlRequestContract}
     */
    public void queue(Request<?> request) {

        assert (request instanceof IBlRequestContract);
        addHeadersToRequest(request);
        mRequestQueue.add(request);
    }
    
    /**
     * Add Request Headers to the headers
     * 
     * @param request The request to add the headers to
     */
    private void addHeadersToRequest(Request<?> request) {

        final Map<String, String> headers = new HashMap<String, String>(1);
        headers.put(HttpConstants.HEADER_AUTHORIZATION, UserInfo.INSTANCE
                        .getAuthHeader());
        request.setHeaders(headers);
    }
    
    /**
     * Cancel any pending requests
     * @param volleyTag The tag to use for cancelling requests
     */
    public void cancelAll(Object volleyTag) {
        mRequestQueue.cancelAll(volleyTag);
    }

    /**
     * Interface for network callbacks, which splits the response methods based
     * on the type of response
     * 
     * @author Vinay S Shenoy
     */
    public static interface IHttpCallbacks {

        /**
         * Will be called before any request is placed on the queue. Start any
         * progress indicators, increment request counters here
         * 
         * @param request The request about to be placed on the queue.
         */
        public void onPreExecute(IBlRequestContract request);

        /**
         * Will be called after any request completes, irrespective of the
         * response. Decrement request counters, stop progress indicators here
         * 
         * @param request The request that was completed
         */
        public void onPostExecute(IBlRequestContract request);

        /**
         * Will be called if any request completed with HTTP Status 200(HTTP_OK)
         * 
         * @param requestId The {@link RequestId} that was made
         * @param request The Request that was completed
         * @param response The {@link ResponseInfo} which contains the response
         *            info
         */
        public void onSuccess(int requestId, IBlRequestContract request,
                        ResponseInfo response);

        /**
         * Will be called if any request completed with HTTP Status
         * 400(HTTP_BAD_REQUEST)
         * 
         * @param requestId The {@link RequestId} that was completed
         * @param request The request that was completed
         * @param errorCode The error code returned from server, which was
         *            parsed in the parser
         * @param errorMessage The error message returned from server, which was
         *            parsed in the parser. Will not be <code>null</code>
         * @param errorResponseBundle Any extra info contained in the error
         *            response. Can be <code>null</code>
         */
        public void onBadRequestError(int requestId,
                        IBlRequestContract request, int errorCode,
                        String errorMessage, Bundle errorResponseBundle);

        /**
         * Will be called if the server throws HTTP Status
         * 401(HTTP_UNAUTHORIZED)
         * 
         * @param requestId The {@link RequestId} that was completed
         * @param request The request that was completed
         */
        public void onAuthError(int requestId, IBlRequestContract request);

        /**
         * Will be called if any other error happens
         * 
         * @param requestId The {@link RequestId} that was completed
         * @param request The request that was completed
         * @param errorCode The {@link ErrorCode} that specifies the error
         */
        public void onOtherError(int requestId, IBlRequestContract request,
                        int errorCode);

    }

}
