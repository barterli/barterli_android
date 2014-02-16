package com.barterli.android;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.barterli.android.http.IVolleyHelper;
import com.barterli.android.utils.UtilityMethods;
import com.barterli.android.widgets.TypefaceCache;
import com.barterli.android.widgets.TypefacedSpan;

/**
 * @author Vinay S Shenoy Base class for inheriting all other Activities from
 */
public class AbstractBarterLiActivity extends FragmentActivity {

	private static final int ACTION_BAR_DISPLAY_MASK = ActionBar.DISPLAY_HOME_AS_UP
			| ActionBar.DISPLAY_SHOW_TITLE;

	private RequestQueue mRequestQueue;
	private ImageLoader mImageLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getActionBar() != null) {
			getActionBar().setDisplayOptions(ACTION_BAR_DISPLAY_MASK);
			setActionBarTitle(getTitle().toString());
		}

		mRequestQueue = ((IVolleyHelper) getApplication()).getRequestQueue();
		mImageLoader = ((IVolleyHelper) getApplication()).getImageLoader();
	}

	protected ImageLoader getImageLoader() {
		return mImageLoader;
	}

	protected void addRequestToQueue(Request request) {
		mRequestQueue.add(request);
	}

	@Override
	protected void onStop() {
		super.onStop();
		mRequestQueue.cancelAll(this);
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

	protected void showToast(String toastMessage, boolean isLong) {
		Toast.makeText(this, toastMessage,
				isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
	}

	protected void showToast(int toastMessageResId, boolean isLong) {
		Toast.makeText(this, toastMessageResId,
				isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
		;
	}

}
