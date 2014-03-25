/**
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
 */

package li.barter.http;

import com.android.volley.BadRequestError;
import com.android.volley.NetworkResponse;

import android.os.Bundle;

/**
 * Custom class to encapsulate {@link BadRequestError} class to hold extra
 * response info
 * 
 * @author Vinay S Shenoy
 */
public class BlBadRequestError extends BadRequestError {

    private static final long serialVersionUID = 1L;

    public final int          requestId;
    public final int          errorCode;
    private Bundle            mResponseBundle;

    public BlBadRequestError(int requestId, int errorCode) {
        super();
        this.requestId = requestId;
        this.errorCode = errorCode;
    }

    public BlBadRequestError(int requestId, int errorCode, NetworkResponse networkResponse) {
        super(networkResponse);
        this.requestId = requestId;
        this.errorCode = errorCode;
    }

    public BlBadRequestError(int requestId, int errorCode, String exceptionMessage, Throwable reason) {
        super(exceptionMessage, reason);
        this.requestId = requestId;
        this.errorCode = errorCode;
    }

    public BlBadRequestError(int requestId, int errorCode, String exceptionMessage) {
        super(exceptionMessage);
        this.requestId = requestId;
        this.errorCode = errorCode;
    }

    public BlBadRequestError(int requestId, int errorCode, Throwable cause) {
        super(cause);
        this.requestId = requestId;
        this.errorCode = errorCode;
    }

    /**
     * @return the Response Bundle
     */
    public Bundle getResponseBundle() {
        return mResponseBundle;
    }

    /**
     * @param responseBundle the Response info bundle to set
     */
    public void setResponseBundle(Bundle responseBundle) {
        mResponseBundle = responseBundle;
    }

}
