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

/**
 * Interface callback to be notified when the chat request completes
 * 
 * @author Vinay S Shenoy
 */
public interface ChatAcknowledge {

    /**
     * Callback when the chat request completes
     * 
     * @param success <code>true</code> if the chat message was sent
     *            suceessfully, <code>false</code> if it fails
     */
    public void onChatRequestComplete(final boolean success);
}
