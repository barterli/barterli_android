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
