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

package li.barter.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ClearCacheRequest;

import java.util.concurrent.atomic.AtomicInteger;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import li.barter.BarterLiApplication;
import li.barter.R;
import li.barter.analytics.GoogleAnalyticsManager;
import li.barter.chat.ChatService;
import li.barter.data.DBInterface;
import li.barter.data.DBInterface.AsyncDbQueryCallback;
import li.barter.data.TableChatMessages;
import li.barter.data.TableChats;
import li.barter.fragments.AbstractBarterLiFragment;
import li.barter.fragments.FragmentTransition;
import li.barter.http.IBlRequestContract;
import li.barter.http.IVolleyHelper;
import li.barter.http.ResponseInfo;
import li.barter.http.VolleyCallbacks;
import li.barter.http.VolleyCallbacks.IHttpCallbacks;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.DeviceInfo;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.QueryTokens;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.Logger;
import li.barter.utils.SharedPreferenceHelper;
import li.barter.utils.Utils;
import li.barter.widgets.TypefaceCache;
import li.barter.widgets.TypefacedSpan;

/**
 * @author Vinay S Shenoy Base class for inheriting all other Activities from
 */
public abstract class AbstractBarterLiActivity extends ActionBarActivity
        implements IHttpCallbacks, AsyncDbQueryCallback,
        OnClickListener {

    private static final String TAG = "BaseBarterLiActivity";

    private static final int ACTION_BAR_DISPLAY_MASK = ActionBar.DISPLAY_HOME_AS_UP
            | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_HOME;
    /**
     * {@link VolleyCallbacks} for encapsulating Volley request responses
     */
    protected VolleyCallbacks    mVolleyCallbacks;
    private   AtomicInteger      mRequestCounter;
    private   ActivityTransition mActivityTransition;

    /**
     * Whether a screen hit should be reported to analytics
     */
    private boolean mShouldReportScreenHit;

    /**
     * Creates a Crouton View based on the style
     *
     * @param context {@link Context} reference to get the {@link LayoutInflater} reference
     * @param message The message to display
     * @param style   The style of Crouton
     * @return A View to display as a Crouton
     */
    private static View getCroutonViewForStyle(final Context context,
                                               final String message, final AlertStyle style) {
        int layoutResId = R.layout.crouton_info; //Default layout
        switch (style) {

            case ALERT: {
                layoutResId = R.layout.crouton_alert;
                break;
            }

            case ERROR: {
                layoutResId = R.layout.crouton_error;
                break;
            }

            case INFO: {
                layoutResId = R.layout.crouton_info;
            }
        }
        final View croutonText = LayoutInflater.from(context)
                                               .inflate(layoutResId, null);
        ((TextView) croutonText.findViewById(R.id.text_message))
                .setText(message);
        return croutonText;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        /* Here, getClass() might show an Ambiguous method call bug. It's a bug in IntelliJ IDEA 13
        * http://youtrack.jetbrains.com/issue/IDEA-72835 */
        mActivityTransition = getClass()
                .getAnnotation(ActivityTransition.class);

        long lastScreenTime = 0l;
        if (savedInstanceState == null) {
            if (mActivityTransition != null) {
                overridePendingTransition(mActivityTransition.createEnterAnimation(),
                                          mActivityTransition
                                                  .createExitAnimation()
                );
            }
        } else {
            lastScreenTime = savedInstanceState.getLong(Keys.LAST_SCREEN_TIME);
        }

        if (Utils.shouldReportScreenHit(lastScreenTime)) {
            mShouldReportScreenHit = true;
        } else {
            mShouldReportScreenHit = false;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(ACTION_BAR_DISPLAY_MASK);
            setActionBarTitle(getTitle().toString());
        }

        final RequestQueue requestQueue = ((IVolleyHelper) getApplication())
                .getRequestQueue();

        mVolleyCallbacks = new VolleyCallbacks(requestQueue, this);
        mRequestCounter = new AtomicInteger(0);
        setProgressBarIndeterminateVisibility(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(Keys.LAST_SCREEN_TIME, Utils.getCurrentEpochTime());
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAndReportScreenHit();
    }

    /**
     * Reports a screen hit
     */
    public void checkAndReportScreenHit() {
        if (mShouldReportScreenHit) {
            final String analyticsScreenName = getAnalyticsScreenName();

            if (!TextUtils.isEmpty(analyticsScreenName)) {
                GoogleAnalyticsManager.getInstance()
                                      .sendScreenHit(getAnalyticsScreenName());
            }
        }

    }

    /**
     * Gets the screen name for reporting to google analytics. Send empty string, or
     * <code>null</code> if you don't want the Activity tracked
     */
    protected abstract String getAnalyticsScreenName();

    /**
     * Disconnects the chat service, clears any local data
     */
    public void logout() {

        if (isLoggedIn()) {

            final RequestQueue requestQueue = ((BarterLiApplication) getApplication())
                    .getRequestQueue();
            requestQueue.add(new ClearCacheRequest(requestQueue.getCache(), new Runnable() {
                @Override
                public void run() {
                    UserInfo.INSTANCE.reset();
                    DBInterface.deleteAsync(QueryTokens.DELETE_CHATS, getTaskTag(), null,
                                            TableChats.NAME, null, null, true,
                                            AbstractBarterLiActivity.this);
                    DBInterface.deleteAsync(QueryTokens.DELETE_CHAT_MESSAGES, getTaskTag(), null,
                                            TableChatMessages.NAME, null, null, true,
                                            AbstractBarterLiActivity.this);
                    SharedPreferenceHelper
                            .removeKeys(AbstractBarterLiActivity.this, R.string.pref_auth_token,
                                        R.string.pref_email, R.string.pref_description,
                                        R.string.pref_location, R.string.pref_first_name,
                                        R.string.pref_last_name, R.string.pref_user_id,
                                        R.string.pref_profile_image, R.string.pref_share_token,
                                        R.string.pref_referrer, R.string.pref_referrer_count);
                    final Intent disconnectChatIntent = new Intent(AbstractBarterLiActivity.this,
                                                                   ChatService.class);
                    disconnectChatIntent.setAction(AppConstants.ACTION_DISCONNECT_CHAT);
                    startService(disconnectChatIntent);
                    getSupportFragmentManager()
                            .popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    LocalBroadcastManager.getInstance(BarterLiApplication.getStaticContext())
                                         .sendBroadcast(
                                                 new Intent(AppConstants.ACTION_USER_INFO_UPDATED));
                }
            }));

        }
    }

    @Override
    public void onInsertComplete(final int token, final Object cookie,
                                 final long insertRowId) {

    }

    @Override
    public void onDeleteComplete(final int token, final Object cookie,
                                 final int deleteCount) {

        switch (token) {
            case QueryTokens.DELETE_CHAT_MESSAGES: {
                Logger.v(TAG, "Deleted %d messages", deleteCount);
                break;
            }

            case QueryTokens.DELETE_CHATS: {
                Logger.v(TAG, "Deleted %d chats", deleteCount);
                break;
            }

            case QueryTokens.DELETE_MY_BOOKS: {
                Logger.v(TAG, "Deleted %d books", deleteCount);
                break;
            }

            default:
                break;
        }
    }

    @Override
    public void onQueryComplete(final int token, final Object cookie,
                                final Cursor cursor) {

    }

    @Override
    public void onUpdateComplete(final int token, final Object cookie,
                                 final int updateCount) {

    }

    /**
     * Add a request on the network queue
     *
     * @param request              The {@link Request} to add
     * @param showErrorOnNoNetwork Whether an error toast should be displayed on no internet
     *                             connection
     * @param errorMsgResId        String resource Id for error message to show if no internet
     *                             connection, 0 for a default error message
     */
    protected void addRequestToQueue(final Request<?> request,
                                     final boolean showErrorOnNoNetwork,
                                     final int errorMsgResId, boolean addHeader) {
        if (isConnectedToInternet()) {
            request.setTag(getTaskTag());
            mVolleyCallbacks.queue(request, addHeader);
        } else if (showErrorOnNoNetwork) {
            showCrouton(errorMsgResId != 0 ? errorMsgResId
                                : R.string.no_network_connection, AlertStyle.ERROR);
        }
    }

    /**
     * A Tag to add to all async requests. This must be unique for all Activity types
     *
     * @return An Object that's the tag for this fragment
     */
    protected abstract Object getTaskTag();

    @Override
    protected void onStop() {
        super.onStop();
        // Cancel all pending requests because they shouldn't be delivered
        mVolleyCallbacks.cancelAll(getTaskTag());
        DBInterface.cancelAll(getTaskTag());
        setProgressBarIndeterminateVisibility(false);
    }

    public void setActionBarDisplayOptions(final int displayOptions) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayOptions(displayOptions, ACTION_BAR_DISPLAY_MASK);
        }
    }

    /**
     * Is the device connected to a network or not.
     *
     * @return <code>true</code> if connected, <code>false</code> otherwise
     */
    public boolean isConnectedToInternet() {
        return DeviceInfo.INSTANCE.isNetworkConnected();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {


        //Fetch the current primary fragment. If that will handle the Menu click, pass it to that one
        final AbstractBarterLiFragment currentMainFragment = (AbstractBarterLiFragment) getSupportFragmentManager()
                .findFragmentById(R.id.frame_content);

        boolean handled = false;
        if (currentMainFragment != null) {
            handled = currentMainFragment.onOptionsItemSelected(item);
        }

        if (!handled) {
            // To provide Up navigation
            if (item.getItemId() == android.R.id.home) {

                doUpNavigation();
                return true;
            } else {
                return super.onOptionsItemSelected(item);
            }

        }

        return handled;


    }

    /**
     * Moves up in the hierarchy using the Support meta data specified in manifest
     */
    private void doUpNavigation() {
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

    }

    /**
     * Sets the Action bar title, using the desired {@link Typeface} loaded from {@link
     * TypefaceCache}
     *
     * @param title The title to set for the Action Bar
     */

    public final void setActionBarTitle(final String title) {

        final SpannableString s = new SpannableString(title);
        s.setSpan(new TypefacedSpan(this, TypefaceCache.SLAB_REGULAR), 0, s.length(),
                  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Update the action bar title with the TypefaceSpan instance
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(s);
    }

    /**
     * Sets the Action bar title, using the desired {@link Typeface} loaded from {@link
     * TypefaceCache}
     *
     * @param titleResId The title string resource Id to set for the Action Bar
     */
    public final void setActionBarTitle(final int titleResId) {
        setActionBarTitle(getString(titleResId));
    }

    /**
     * Display an alert, with a string message
     *
     * @param message The message to display
     * @param style   The {@link AlertStyle} of message to display
     */
    public void showCrouton(final String message, final AlertStyle style) {
        //Crouton.make(activity, customView, viewGroupResId, configuration)
        //Crouton.make(this, getCroutonViewForStyle(this, message, style)).show();
        Crouton.make(this, getCroutonViewForStyle(this, message, style))
               .setConfiguration(
                       new de.keyboardsurfer.android.widget.crouton.Configuration.Builder()
                               .setDuration(
                                       de.keyboardsurfer.android.widget.crouton.Configuration.DURATION_SHORT)
                               .build()
               ).show();
    }

    /**
     * Display an alert, with a string message
     *
     * @param messageResId The message to display
     * @param style        The {@link AlertStyle} of message to display
     */
    public void showCrouton(final int messageResId, final AlertStyle style) {
        showCrouton(getString(messageResId), style);
    }

    /**
     * Display an alert, with a string message with infinite time
     *
     * @param message The message to display
     * @param style   The {@link AlertStyle} of message to display
     */
    public void showInfiniteCrouton(final String message, final AlertStyle style) {
        Crouton.make(this, getCroutonViewForStyle(this, message, style))
               .setConfiguration(
                       new de.keyboardsurfer.android.widget.crouton.Configuration.Builder()
                               .setDuration(
                                       de.keyboardsurfer.android.widget.crouton.Configuration.DURATION_INFINITE)
                               .build()
               ).show();

    }

    /**
     * Display an alert, with a string message with infinite time
     *
     * @param messageResId The message to display
     * @param style        The {@link AlertStyle} of message to display
     */
    public void showInfiniteCrouton(final int messageResId,
                                    final AlertStyle style) {
        showInfiniteCrouton(getString(messageResId), style);
    }

    /**
     * Cancels all queued {@link Crouton}s. If there is a {@link Crouton} displayed currently, it
     * will be the last one displayed.
     */
    public void cancelAllCroutons() {
        Crouton.cancelAllCroutons();
    }

    /**
     * Finish the Activity, specifying whether to use custom or default animations
     *
     * @param defaultAnimation <code>true</code> to use Activity default animation,
     *                         <code>false</code> to use custom Animation. In order for the custom
     *                         Animation to be applied, however, you must add the {@link
     *                         ActivityTransition} Annotation to the Activity declaration
     */
    public void finish(final boolean defaultAnimation) {
        super.finish();
        if ((mActivityTransition != null) && !defaultAnimation) {
            overridePendingTransition(mActivityTransition.destroyEnterAnimation(),
                                      mActivityTransition
                                              .destroyExitAnimation()
            );
        }
    }

    @Override
    public void finish() {
        finish(false);
    }

    /**
     * Helper method to load fragments into layout
     *
     * @param containerResId The container resource Id in the content view into which to load the
     *                       fragment
     * @param fragment       The fragment to load
     * @param tag            The fragment tag
     * @param addToBackStack Whether the transaction should be addded to the backstack
     * @param backStackTag   The tag used for the backstack tag
     */
    public void loadFragment(final int containerResId,
                             final AbstractBarterLiFragment fragment, final String tag,
                             final boolean addToBackStack, final String backStackTag) {

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager
                .beginTransaction();
        final FragmentTransition fragmentTransition = fragment.getClass()
                                                              .getAnnotation(
                                                                      FragmentTransition.class);
        if (fragmentTransition != null) {

            transaction.setCustomAnimations(fragmentTransition.enterAnimation(), fragmentTransition
                    .exitAnimation(), fragmentTransition
                                                    .popEnterAnimation(), fragmentTransition
                                                    .popExitAnimation());

        }

        transaction.replace(containerResId, fragment, tag);

        if (addToBackStack) {
            transaction.addToBackStack(backStackTag);
        }
        transaction.commit();
    }

    /**
     * Is the user logged in
     */
    protected boolean isLoggedIn() {
        return !TextUtils.isEmpty(UserInfo.INSTANCE.getAuthToken());
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        //Reset background to reduce overdaw
        getWindow().setBackgroundDrawable(null);
    }

    @Override
    public void onBackPressed() {

        final AbstractBarterLiFragment masterFragment = getCurrentMasterFragment();

        final AbstractBarterLiFragment chatDetailFragment = (AbstractBarterLiFragment) getSupportFragmentManager()
                .findFragmentByTag(FragmentTags.CHAT_DETAILS);

        final AbstractBarterLiFragment chatFragment = (AbstractBarterLiFragment) getSupportFragmentManager()
                .findFragmentByTag(FragmentTags.CHATS);

        if ((masterFragment != null)
                && (getSupportFragmentManager()
                .getBackStackEntryCount() > 0)) {

            masterFragment.onBackPressed();

        } else if ((chatDetailFragment != null)
                && (getSupportFragmentManager()
                .getBackStackEntryCount() == 0)) {
            chatDetailFragment.onBackPressed();
        } else if ((chatFragment != null)
                && (getSupportFragmentManager()
                .getBackStackEntryCount() == 0)) {
            chatFragment.onBackPressed();
        } else {
            super.onBackPressed();
        }

    }

    /**
     * Returns the current master fragment. In single pane layout, this is the fragment in the main
     * content. In a multi-pane layout, returns the fragment in the master container, which is the
     * one responsible for coordination
     *
     * @return <code>null</code> If no fragment is loaded,the {@link AbstractBarterLiFragment}
     * implementation which is the current master fragment otherwise
     */
    public AbstractBarterLiFragment getCurrentMasterFragment() {

        return (AbstractBarterLiFragment) getSupportFragmentManager()
                .findFragmentById(R.id.frame_content);

    }

    @Override
    public void onPreExecute(final IBlRequestContract request) {
        mRequestCounter.incrementAndGet();
        setProgressBarIndeterminateVisibility(true);
    }

    @Override
    public void onPostExecute(final IBlRequestContract request) {
        if (mRequestCounter.decrementAndGet() == 0) {
            setProgressBarIndeterminateVisibility(false);
        }
    }

    @Override
    public abstract void onSuccess(int requestId, IBlRequestContract request,
                                   ResponseInfo response);

    @Override
    public abstract void onBadRequestError(int requestId,
                                           IBlRequestContract request, int errorCode,
                                           String errorMessage, Bundle errorResponseBundle);

    @Override
    public void onAuthError(final int requestId,
                            final IBlRequestContract request) {
        //TODO Show Login Fragment and ask user to login again
    }

    @Override
    public void onOtherError(final int requestId,
                             final IBlRequestContract request, final int errorCode) {
        //TODO Show generic network error message
    }

    @Override
    public void onClick(final DialogInterface dialog, final int which) {

        final AbstractBarterLiFragment fragment = getCurrentMasterFragment();

        if ((fragment != null) && fragment.isVisible()) {
            if (fragment.willHandleDialog(dialog)) {
                fragment.onDialogClick(dialog, which);
            }
        }
    }



    /**
     * @author Vinay S Shenoy Enum to handle the different types of Alerts that can be shown
     */

    public enum AlertStyle {
        ALERT,
        INFO,
        ERROR
    }
}
