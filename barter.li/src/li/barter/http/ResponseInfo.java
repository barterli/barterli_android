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

import android.os.Bundle;

/**
 * Class that encapsulates metadata about the response
 * 
 * @author Vinay S Shenoy
 */
public class ResponseInfo {

    /** Whether the request was successful or not */
    public boolean success;

    /**
     * Any information in the response that needs to passed back to the
     * Component that launched the request
     */
    public Bundle  responseBundle;

    public ResponseInfo() {
        success = true;
    }

    public ResponseInfo(boolean success) {
        this.success = success;
    }

    public ResponseInfo(boolean success, Bundle responseBundle) {
        this.success = success;
        this.responseBundle = responseBundle;
    }
}
