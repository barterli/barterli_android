package li.barter;

import java.util.concurrent.atomic.AtomicInteger;

import li.barter.R;
import li.barter.http.IVolleyHelper;
import li.barter.utils.UtilityMethods;
import li.barter.widgets.TypefaceCache;
import li.barter.widgets.TypefacedSpan;
import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;

/**
 * @author Vinay S Shenoy Base class for inheriting all other Activities from
 */
public class AbstractBarterLiActivity extends FragmentActivity {

	private static final int ACTION_BAR_DISPLAY_MASK = ActionBar.DISPLAY_HOME_AS_UP
			| ActionBar.DISPLAY_SHOW_TITLE;

	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;
	private AtomicInteger mRequestCounter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);
		if (getActionBar() != null) {
			getActionBar().setDisplayOptions(ACTION_BAR_DISPLAY_MASK);
			setActionBarTitle(getTitle().toString());
		}

		mRequestQueue = ((IVolleyHelper) getApplication()).getRequestQueue();
		mImageLoader = ((IVolleyHelper) getApplication()).getImageLoader();
		mRequestCounter = new AtomicInteger(0);
		setProgressBarIndeterminateVisibility(false);
	}

	protected ImageLoader getImageLoader() {
		return mImageLoader;
	}

	/**
	 * Add a request on the network queue
	 * 
	 * @param request
	 *            The {@link Request} to add
	 * @param showErrorOnNoNetwork
	 *            Whether an error toast should be displayed on no internet
	 *            connection
	 * @param errorMsgResId
	 *            String resource Id for error message to show if no internet
	 *            connection, 0 for a default error message
	 */
	protected void addRequestToQueue(Request<?> request,
			boolean showErrorOnNoNetwork, final int errorMsgResId) {

		if (isConnectedToInternet()) {
			mRequestCounter.incrementAndGet();
			setProgressBarIndeterminateVisibility(true);
			mRequestQueue.add(request);
		} else if (showErrorOnNoNetwork) {
			showToast(errorMsgResId != 0 ? errorMsgResId
					: R.string.no_network_connection, false);
		}
	}

	/**
	 * Call this whenever a request has finished, whether successfully or error
	 */
	protected void onRequestFinished() {

		if (mRequestCounter.decrementAndGet() == 0) {
			setProgressBarIndeterminateVisibility(false);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		mRequestQueue.cancelAll(this);
		setProgressBarIndeterminateVisibility(false);
	}

	protected void setActionBarDisplayOptions(int displayOptions) {
		if (getActionBar() != null) {
			getActionBar().setDisplayOptions(displayOptions,
					ACTION_BAR_DISPLAY_MASK);
		}
	}

	protected boolean isConnectedToInternet() {
		return UtilityMethods.isNetworkConnected(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// To provide Up navigation
		if (item.getItemId() == android.R.id.home) {

			Intent upIntent = NavUtils.getParentActivityIntent(this);

			if (upIntent == null) {

				NavUtils.navigateUpFromSameTask(this);

			} else {
				if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
					// This activity is NOT part of this app's task, so create a
					// new
					// task
					// when navigating up, with a synthesized back stack.
					TaskStackBuilder.create(this)
					// Add all of this activity's parents to the back stack
							.addNextIntentWithParentStack(upIntent)
							// Navigate up to the closest parent
							.startActivities();
				} else {
					// This activity is part of this app's task, so simply
					// navigate up to the logical parent activity.
					NavUtils.navigateUpTo(this, upIntent);
				}
			}

			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	protected final void setActionBarTitle(final String title) {

		final SpannableString s = new SpannableString(title);
		s.setSpan(new TypefacedSpan(this, TypefaceCache.ALEGREYA_BLACK_ITALIC),
				0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		// Update the action bar title with the TypefaceSpan instance
		final ActionBar actionBar = getActionBar();
		actionBar.setTitle(s);
	}

	public void showToast(String toastMessage, boolean isLong) {
		Toast.makeText(this, toastMessage,
				isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
	}

	public void showToast(int toastMessageResId, boolean isLong) {
		Toast.makeText(this, toastMessageResId,
				isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
	}

}
