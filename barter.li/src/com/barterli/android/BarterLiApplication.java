package com.barterli.android;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.barterli.android.http.IVolleyHelper;

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
