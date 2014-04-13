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

package li.barter.chat;

import com.android.volley.Request.Method;

import li.barter.http.BlRequest;
import li.barter.http.VolleyCallbacks;

/**
 * Custom implementation of {@link BlRequest} to enable sending receiving chat
 * messages with acknowledgements TODO: Investigate removing these by using
 * RabbitMQ ACKs
 * 
 * @author Vinay S Shenoy
 */
public class ChatRequest extends BlRequest {

    /**
     * {@link ChatAcknowledge} to receive the callback when the chat request
     * completes
     */
    private ChatAcknowledge mAcknowledge;

    /**
     * @param method One of the constants from {@link Method} class to identify
     *            the request type
     * @param url The API endpoint
     * @param requestBody A string represention of the Json request body
     * @param volleyCallbacks A {@link VolleyCallbacks} reference to receive the
     *            volley callbacks
     * @param acknowledge A {@link ChatAcknowledge} callback for when the
     *            request completes
     */
    public ChatRequest(int method, String url, String requestBody, VolleyCallbacks volleyCallbacks, ChatAcknowledge acknowledge) {
        super(method, url, requestBody, volleyCallbacks);
        mAcknowledge = acknowledge;
    }

    /**
     * @return The {@link ChatAcknowledge} to be notified when the chat request
     *         completes
     */
    public ChatAcknowledge getAcknowledge() {
        return mAcknowledge;
    }

}
