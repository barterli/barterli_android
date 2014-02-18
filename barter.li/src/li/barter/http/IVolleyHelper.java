package li.barter.http;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;

/**
 * @author Vinay S Shenoy Interface to hold the methods to represent the volley
 *         helpers
 */
public interface IVolleyHelper {

	public RequestQueue getRequestQueue();

	public ImageLoader getImageLoader();
}
