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

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;

/**
 * @author Vinay S Shenoy Interface to hold the methods to represent the volley
 *         helpers. Putting in a separate interface to use when Unit Testing
 */
public interface IVolleyHelper {

	/**
	 * Gets an instance of the Application's {@link RequestQueue}. Which should only be 1 per application
	 * @return The Application's {@link RequestQueue}
	 */
	public RequestQueue getRequestQueue();

	/**
	 * Gets an instance of the Application's {@link ImageLoader}
	 * @return the Application's {@link ImageLoader}
	 */
	public ImageLoader getImageLoader();
}
