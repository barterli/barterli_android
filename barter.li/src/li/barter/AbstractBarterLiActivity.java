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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.MenuItem;
import android.view.Window;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicInteger;

import li.barter.http.IVolleyHelper;
import li.barter.utils.ActivityTransition;
import li.barter.utils.UtilityMethods;
import li.barter.widgets.TypefaceCache;
import li.barter.widgets.TypefacedSpan;

/**
 * @author Vinay S Shenoy Base class for inheriting all other Activities from
 */
public class AbstractBarterLiActivity extends FragmentActivity {

    private static final int   ACTION_BAR_DISPLAY_MASK = ActionBar.DISPLAY_HOME_AS_UP
                                                                       | ActionBar.DISPLAY_SHOW_TITLE;

    private RequestQueue       mRequestQueue;
    private ImageLoader        mImageLoader;
    private AtomicInteger      mRequestCounter;

    private ActivityTransition mActivityTransition;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        mActivityTransition = getClass().getAnnotation(ActivityTransition.class);
        if (savedInstanceState == null) {
            if (mActivityTransition != null) {
                overridePendingTransition(
                                mActivityTransition.createEnterAnimation(),
                                mActivityTransition.createExitAnimation());
            }
        }
        if (getActionBar() != null) {
            getActionBar().setDisplayOptions(ACTION_BAR_DISPLAY_MASK);
            setActionBarTitle(getTitle().toString());
        }
        mRequestQueue = ((IVolleyHelper) getApplication()).getRequestQueue();
        mImageLoader = ((IVolleyHelper) getApplication()).getImageLoader();
        mRequestCounter = new AtomicInteger(0);
        setProgressBarIndeterminateVisibility(false);
    }

    /**
     * Reference to the {@link ImageLoader}
     * 
     * @return The {@link ImageLoader} for loading images from ntwork
     */
    protected ImageLoader getImageLoader() {
        return mImageLoader;
    }

    /**
     * Add a request on the network queue
     * 
     * @param request The {@link Request} to add
     * @param showErrorOnNoNetwork Whether an error toast should be displayed on
     *            no internet connection
     * @param errorMsgResId String resource Id for error message to show if no
     *            internet connection, 0 for a default error message
     */
    protected void addRequestToQueue(final Request<?> request,
                    final boolean showErrorOnNoNetwork, final int errorMsgResId) {
        // TODO Add Headers to request objects
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
        // Cancel all pending requests because they shouldn't be delivered
        mRequestQueue.cancelAll(this);
        setProgressBarIndeterminateVisibility(false);
    }

    protected void setActionBarDisplayOptions(final int displayOptions) {
        if (getActionBar() != null) {
            getActionBar().setDisplayOptions(displayOptions,
                            ACTION_BAR_DISPLAY_MASK);
        }
    }

    /**
     * Is the device connected to a network or not.
     * 
     * @return <code>true</code> if connected, <code>false</code> otherwise
     */
    protected boolean isConnectedToInternet() {
        return UtilityMethods.isNetworkConnected(this);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        // To provide Up navigation
        if (item.getItemId() == android.R.id.home) {

            final Intent upIntent = NavUtils.getParentActivityIntent(this);

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

    /**
     * Sets the Action bar title, using the desired {@link Typeface} loaded from
     * {@link TypefaceCache}
     * 
     * @param title The title to set for the Action Bar
     */
    protected final void setActionBarTitle(final String title) {

        final SpannableString s = new SpannableString(title);
        s.setSpan(new TypefacedSpan(this, TypefaceCache.ALEGREYA_BLACK_ITALIC),
                        0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        final ActionBar actionBar = getActionBar();
        actionBar.setTitle(s);
    }

    /**
     * Sets the Action bar title, using the desired {@link Typeface} loaded from
     * {@link TypefaceCache}
     * 
     * @param titleResId The title string resource Id to set for the Action Bar
     */
    protected final void setActionBarTitle(final int titleResId) {
        setActionBarTitle(getString(titleResId));
    }

    /**
     * Display a {@link Toast} message
     * 
     * @param toastMessage The message to display
     * @param isLong Whether it is a long toast
     */
    public void showToast(final String toastMessage, final boolean isLong) {
        Toast.makeText(this, toastMessage,
                        isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    /**
     * Display a {@link Toast} message
     * 
     * @param toastMessageResId The message string resource Id to display
     * @param isLong Whether it is a long toast
     */
    public void showToast(final int toastMessageResId, final boolean isLong) {
        Toast.makeText(this, toastMessageResId,
                        isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    /**
     * Finish the Activity, specifying whether to use custom or default
     * animations
     * 
     * @param defaultAnimation <code>true</code> to use Activity default
     *            animation, <code>false</code> to use custom Animation. In
     *            order for the custom Animation to be applied, however, you
     *            must add the {@link ActivityTransition} Annotation to the
     *            Activity declaration
     */
    public void finish(boolean defaultAnimation) {
        super.finish();
        if (mActivityTransition != null && !defaultAnimation) {
            overridePendingTransition(
                            mActivityTransition.destroyEnterAnimation(),
                            mActivityTransition.destroyExitAnimation());
        }
    }

    @Override
    public void finish() {
        finish(false);
    }

}
