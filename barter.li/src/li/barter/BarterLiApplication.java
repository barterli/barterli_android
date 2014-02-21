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
package li.barter;

import li.barter.http.IVolleyHelper;
import li.barter.utils.AppConstants;
import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Custom Application class which holds some common functionality for the
 * Application
 * 
 * @author Vinay S Shenoy
 * 
 */
public class BarterLiApplication extends Application implements IVolleyHelper {

	private RequestQueue mRequestQueue;

	private ImageLoader mImageLoader;

	public void onCreate() {

		VolleyLog.sDebug = AppConstants.DEBUG;
		mRequestQueue = Volley.newRequestQueue(this);
		mImageLoader = new ImageLoader(mRequestQueue);
	};

	@Override
	public RequestQueue getRequestQueue() {
		return mRequestQueue;
	}

	@Override
	public ImageLoader getImageLoader() {
		return mImageLoader;
	}

}
