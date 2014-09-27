/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package li.barter.http;

import com.android.volley.Request;

import java.util.Map;

/**
 * @author Vinay S Shenoy Interface that represents the common methods all
 *         custom implementations of {@link Request}s in the app must honour
 */
public interface IBlRequestContract {

    /**
     * Sets a request Id for the request
     */
    public void setRequestId(final int requestId);

    /**
     * @return The request Id
     */
    public int getRequestId();

    /**
     * Gets the extras associated with this request
     * 
     * @return The extras returned with this request. Will not be
     *         <code>null</code>
     */
    public Map<String, Object> getExtras();

    /**
     * Add an extra to the request
     * 
     * @param key The key
     * @param value The value to map to the key
     */
    public void addExtra(String key, Object value);

}
