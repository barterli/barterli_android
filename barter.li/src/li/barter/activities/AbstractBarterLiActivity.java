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

import com.android.volley.Request;
import com.android.volley.RequestQueue;

import de.keyboardsurfer.android.widget.crouton.Crouton;

import android.app.ActionBar;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.DrawerLayout;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import li.barter.R;
import li.barter.adapters.HomeNavDrawerAdapter;
import li.barter.analytics.GoogleAnalyticsManager;
import li.barter.chat.ChatService;
import li.barter.data.DBInterface;
import li.barter.data.DBInterface.AsyncDbQueryCallback;
import li.barter.data.TableChatMessages;
import li.barter.data.TableChats;
import li.barter.fragments.AboutUsPagerFragment;
import li.barter.fragments.AbstractBarterLiFragment;
import li.barter.fragments.ChatsFragment;
import li.barter.fragments.FragmentTransition;
import li.barter.fragments.LoginFragment;
import li.barter.fragments.ProfileFragment;
import li.barter.fragments.ReportBugFragment;
import li.barter.fragments.TeamFragment;
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
import li.barter.widgets.TypefaceCache;
import li.barter.widgets.TypefacedSpan;

/**
 * @author Vinay S Shenoy Base class for inheriting all other Activities from
 */
public abstract class AbstractBarterLiActivity extends FragmentActivity
                implements IHttpCallbacks, AsyncDbQueryCallback,
                OnClickListener {

    private static final String TAG                     = "BaseBarterLiActivity";

    private static final int    ACTION_BAR_DISPLAY_MASK = ActionBar.DISPLAY_HOME_AS_UP
                                                                        | ActionBar.DISPLAY_SHOW_TITLE;

    /**
     * @author Vinay S Shenoy Enum to handle the different types of Alerts that
     *         can be shown
     */
    public enum AlertStyle {
        ALERT,
        INFO,
        ERROR
    }

    /**
     * {@link VolleyCallbacks} for encapsulating Volley request responses
     */
    protected VolleyCallbacks         mVolleyCallbacks;
    private AtomicInteger             mRequestCounter;

    private ActivityTransition        mActivityTransition;

    /**
     * Drawer Layout that contains the Navigation Drawer
     */
    private DrawerLayout              mDrawerLayout;

    /**
     * Drawer toggle for Action Bar
     */
    private ActionBarDrawerToggle     mDrawerToggle;

    /**
     * {@link ListView} that provides the navigation items
     */
    private ListView                  mNavListView;

    /**
     * {@link BaseAdapter} implementation for Navigation drawer item
     */
    private HomeNavDrawerAdapter      mNavDrawerAdapter;

    /**
     * Whether the current activity has a Navigation drawer or not
     */
    private boolean                   mHasNavigationDrawer;

    /**
     * Whether the nav drawer associated with this activity is also associated
     * with the drawer toggle. Is valid only if
     * <code>mHasNavigationDrawer</code> is <code>true</code>
     */
    private boolean                   mIsActionBarNavDrawerToggleEnabled;

    /**
     * Handler for posting callbacks back to the main thread. Used for delaying
     * launching of nav drawer item until the drawer is closed
     */
    private Handler                   mHandler;

    /**
     * Navigation Drawer Item Click Listener. The Nav list items are loaded from
     * R.array.nav_drawer_titles
     */
    private final OnItemClickListener mNavDrawerItemClickListener = new OnItemClickListener() {
                                                                      @Override
                                                                      public void onItemClick(
                                                                                      final AdapterView<?> parent,
                                                                                      final View view,
                                                                                      final int position,
                                                                                      final long id) {

                                                                          final Runnable launchRunnable = makeRunnableForNavDrawerClick(position);
                                                                          if (launchRunnable != null) {
                                                                              //Give time for drawer to close before performing the action
                                                                              mHandler.postDelayed(launchRunnable, 250);
                                                                          }
                                                                          mDrawerLayout.closeDrawer(mNavListView);

                                                                      }

                                                                  };

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        mHasNavigationDrawer = false;
        mIsActionBarNavDrawerToggleEnabled = false;
        mActivityTransition = getClass()
                        .getAnnotation(ActivityTransition.class);
        if (savedInstanceState == null) {
            if (mActivityTransition != null) {
                overridePendingTransition(mActivityTransition.createEnterAnimation(), mActivityTransition
                                .createExitAnimation());
            }
        }
        if (getActionBar() != null) {
            getActionBar().setDisplayOptions(ACTION_BAR_DISPLAY_MASK);
            setActionBarTitle(getTitle().toString());
        }

        final RequestQueue requestQueue = ((IVolleyHelper) getApplication())
                        .getRequestQueue();

        mVolleyCallbacks = new VolleyCallbacks(requestQueue, this);
        mRequestCounter = new AtomicInteger(0);
        setProgressBarIndeterminateVisibility(false);
        mHandler = new Handler();
    }

    @Override
    protected void onResume() {
        super.onResume();

        final String analyticsScreenName = getAnalyticsScreenName();

        if (!TextUtils.isEmpty(analyticsScreenName)) {
            GoogleAnalyticsManager.getInstance()
                            .sendScreenHit(getAnalyticsScreenName());
        }
    }

    /**
     * Gets the screen name for reporting to google analytics. Send empty
     * string, or <code>null</code> if you don't want the Activity tracked
     */
    protected abstract String getAnalyticsScreenName();

    /**
     * Creates a {@link Runnable} for positing to the Handler for launching the
     * Navigation Drawer click
     * 
     * @param position The nav drawer item that was clicked
     * @return a {@link Runnable} to be posted to the Handler thread
     */
    private Runnable makeRunnableForNavDrawerClick(final int position) {

        Runnable runnable = null;
        final AbstractBarterLiFragment masterFragment = getCurrentMasterFragment();
        switch (position) {

        //My Profile
            case 0: {

                /*
                 * If the master fragment is already the login fragment, don't
                 * load it again. TODO Check for Profile Fragment also
                 */
                if ((masterFragment != null)
                                && (masterFragment instanceof LoginFragment)) {
                    return null;
                }
                runnable = new Runnable() {

                    @Override
                    public void run() {
                        if (isLoggedIn()) {
                            Bundle args = new Bundle();
                            args.putString(Keys.USER_ID, UserInfo.INSTANCE
                                            .getId());
                            loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                                            .instantiate(AbstractBarterLiActivity.this, ProfileFragment.class
                                                            .getName(), args), FragmentTags.PROFILE_FROM_NAV_DRAWER, true, null);

                        } else {

                            final Bundle loginArgs = new Bundle(1);
                            loginArgs.putString(Keys.UP_NAVIGATION_TAG, FragmentTags.BS_BOOKS_AROUND_ME);

                            loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                                            .instantiate(AbstractBarterLiActivity.this, LoginFragment.class
                                                            .getName(), loginArgs), FragmentTags.LOGIN_FROM_NAV_DRAWER, true, FragmentTags.BS_BOOKS_AROUND_ME);
                        }

                    }
                };
                break;

            }

            //My Chats
            case 1: {

                if ((masterFragment != null)
                                && (masterFragment instanceof ChatsFragment)) {
                    return null;
                }

                //TODO Check for login
                runnable = new Runnable() {

                    @Override
                    public void run() {
                        loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                                        .instantiate(AbstractBarterLiActivity.this, ChatsFragment.class
                                                        .getName(), null), FragmentTags.CHATS, true, FragmentTags.BS_CHATS);
                    }
                };
                break;
            }

            //Report Bug
            case 2: {
                if ((masterFragment != null)
                                && (masterFragment instanceof ReportBugFragment)) {
                    return null;
                }

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                                        .instantiate(AbstractBarterLiActivity.this, ReportBugFragment.class
                                                        .getName(), null), FragmentTags.REPORT_BUGS, true, null);
                    }
                };
                break;
            }

            //Share
            case 3: {

                final String referralId = SharedPreferenceHelper
                                .getString(this, R.string.pref_share_token);
                String appShareUrl = getString(R.string.app_share_message);

                if (!TextUtils.isEmpty(referralId)) {
                    appShareUrl = appShareUrl
                                    .concat(String.format(Locale.US, AppConstants.REFERRER_FORMAT, referralId));
                }

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.subject));
                shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, appShareUrl);

                shareIntent.setType("text/plain");
                try {
                    startActivity(Intent
                                    .createChooser(shareIntent, getString(R.string.share_via)));
                } catch (ActivityNotFoundException e) {
                    //Shouldn't happen
                }

                break;
            }
            //About Us
            case 4: {
                if ((masterFragment != null)
                                && (masterFragment instanceof TeamFragment)) {
                    return null;
                }

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                                        .instantiate(AbstractBarterLiActivity.this, AboutUsPagerFragment.class
                                                        .getName(), null), FragmentTags.TEAM, true, null);
                    }
                };

                break;
            }

            //Logout
            case 5: {

                runnable = new Runnable() {

                    @Override
                    public void run() {
                        logout();
                    }
                };
                break;
            }

            default: {
                runnable = null;
            }
        }

        return runnable;
    };

    /**
     * Disconnects the chat service, clears any local data
     */
    protected void logout() {

        if (isLoggedIn()) {
            UserInfo.INSTANCE.reset();
            DBInterface.deleteAsync(QueryTokens.DELETE_CHATS, null, TableChats.NAME, null, null, true, this);
            DBInterface.deleteAsync(QueryTokens.DELETE_CHAT_MESSAGES, null, TableChatMessages.NAME, null, null, true, this);
            SharedPreferenceHelper
                            .removeKeys(this, R.string.pref_auth_token, R.string.pref_email, R.string.pref_description, R.string.pref_location, R.string.pref_first_name, R.string.pref_last_name, R.string.pref_user_id, R.string.pref_profile_image);
            final Intent disconnectChatIntent = new Intent(this, ChatService.class);
            disconnectChatIntent.setAction(AppConstants.ACTION_DISCONNECT_CHAT);
            startService(disconnectChatIntent);
            getSupportFragmentManager()
                            .popBackStack(FragmentTags.BS_BOOKS_AROUND_ME, FragmentManager.POP_BACK_STACK_INCLUSIVE);
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
     * @param request The {@link Request} to add
     * @param showErrorOnNoNetwork Whether an error toast should be displayed on
     *            no internet connection
     * @param errorMsgResId String resource Id for error message to show if no
     *            internet connection, 0 for a default error message
     */
    protected void addRequestToQueue(final Request<?> request,
                    final boolean showErrorOnNoNetwork,
                    final int errorMsgResId, boolean addHeader) {
        if (isConnectedToInternet()) {
            request.setTag(getVolleyTag());
            mVolleyCallbacks.queue(request, addHeader);
        } else if (showErrorOnNoNetwork) {
            showCrouton(errorMsgResId != 0 ? errorMsgResId
                            : R.string.no_network_connection, AlertStyle.ERROR);
        }
    }

    /**
     * A Tag to add to all Volley requests. This must be unique for all
     * Fragments types
     * 
     * @return An Object that's the tag for this fragment
     */
    protected abstract Object getVolleyTag();

    @Override
    protected void onStop() {
        super.onStop();
        // Cancel all pending requests because they shouldn't be delivered
        mVolleyCallbacks.cancelAll(getVolleyTag());
        setProgressBarIndeterminateVisibility(false);
    }

    public void setActionBarDisplayOptions(final int displayOptions) {
        if (getActionBar() != null) {
            getActionBar().setDisplayOptions(displayOptions, ACTION_BAR_DISPLAY_MASK);
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

        if (isActionBarNavDrawerToggleEnabled()
                        && mDrawerToggle.onOptionsItemSelected(item)) {
            // Pass the event to ActionBarDrawerToggle, if it returns
            // true, then it has handled the app icon touch event
            return true;
        }

        else {

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

    }

    /**
     * Moves up in the hierarchy using the Support meta data specified in
     * manifest
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
     * Sets the Action bar title, using the desired {@link Typeface} loaded from
     * {@link TypefaceCache}
     * 
     * @param title The title to set for the Action Bar
     */

    public final void setActionBarTitle(final String title) {

        final SpannableString s = new SpannableString(title);
        s.setSpan(new TypefacedSpan(this, TypefaceCache.BOLD), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

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
    public final void setActionBarTitle(final int titleResId) {
        setActionBarTitle(getString(titleResId));
    }

    /**
     * Display an alert, with a string message
     * 
     * @param message The message to display
     * @param style The {@link AlertStyle} of message to display
     */
    public void showCrouton(final String message, final AlertStyle style) {
        Crouton.make(this, getCroutonViewForStyle(this, message, style)).show();

    }

    /**
     * Display an alert, with a string message
     * 
     * @param messageResId The message to display
     * @param style The {@link AlertStyle} of message to display
     */
    public void showCrouton(final int messageResId, final AlertStyle style) {
        showCrouton(getString(messageResId), style);
    }

    /**
     * Display an alert, with a string message with infinite time
     * 
     * @param message The message to display
     * @param style The {@link AlertStyle} of message to display
     */
    public void showInfiniteCrouton(final String message, final AlertStyle style) {
        Crouton.make(this, getCroutonViewForStyle(this, message, style))
                        .setConfiguration(new de.keyboardsurfer.android.widget.crouton.Configuration.Builder()
                                        .setDuration(de.keyboardsurfer.android.widget.crouton.Configuration.DURATION_INFINITE)
                                        .build()).show();

    }

    /**
     * Display an alert, with a string message with infinite time
     * 
     * @param messageResId The message to display
     * @param style The {@link AlertStyle} of message to display
     */
    public void showInfiniteCrouton(final int messageResId,
                    final AlertStyle style) {
        showInfiniteCrouton(getString(messageResId), style);
    }

    /**
     * Cancels all queued {@link Crouton}s. If there is a {@link Crouton}
     * displayed currently, it will be the last one displayed.
     */
    public void cancelAllCroutons() {
        Crouton.cancelAllCroutons();
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
    public void finish(final boolean defaultAnimation) {
        super.finish();
        if ((mActivityTransition != null) && !defaultAnimation) {
            overridePendingTransition(mActivityTransition.destroyEnterAnimation(), mActivityTransition
                            .destroyExitAnimation());
        }
    }

    @Override
    public void finish() {
        finish(false);
    }

    /**
     * Helper method to load fragments into layout
     * 
     * @param containerResId The container resource Id in the content view into
     *            which to load the fragment
     * @param fragment The fragment to load
     * @param tag The fragment tag
     * @param addToBackStack Whether the transaction should be addded to the
     *            backstack
     * @param backStackTag The tag used for the backstack tag
     */
    public void loadFragment(final int containerResId,
                    final AbstractBarterLiFragment fragment, final String tag,
                    final boolean addToBackStack, final String backStackTag) {

        final FragmentManager fragmentManager = getSupportFragmentManager();
        final FragmentTransaction transaction = fragmentManager
                        .beginTransaction();
        final FragmentTransition fragmentTransition = fragment.getClass()
                        .getAnnotation(FragmentTransition.class);
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
     * Initialize the Navigation Drawer. Call after the content view is set, in
     * onCreate()
     * 
     * @param navDrawerResId The resource Id of the navigation drawer
     * @param navListResId The resource id of the list view in the layout which
     *            is the drawer content
     * @param attachToActionBar Whether the navigation should be associated with
     *            the Action Bar drawer toggle
     */
    protected void initDrawer(final int navDrawerResId, final int navListResId,
                    final boolean attachToActionBar) {

        mDrawerLayout = (DrawerLayout) findViewById(navDrawerResId);

        if (mDrawerLayout == null) {
            throw new IllegalArgumentException("Drawer Layout not found. Check your layout/resource id being sent");
        }
        mNavListView = (ListView) findViewById(navListResId);

        if (mNavListView == null) {
            throw new IllegalArgumentException("Drawer content not found. Check the layout/resource id being sent");
        }

        mHasNavigationDrawer = true;

        if (attachToActionBar) {
            mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_navigation_drawer, R.string.drawer_open, R.string.drawer_closed) {

                @Override
                public void onDrawerOpened(final View drawerView) {
                    super.onDrawerOpened(drawerView);
                    //  mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    invalidateOptionsMenu();

                }

                @Override
                public void onDrawerClosed(final View drawerView) {
                    super.onDrawerClosed(drawerView);
                    //   mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                    invalidateOptionsMenu();

                }

            };

            mDrawerLayout.setDrawerListener(mDrawerToggle);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            mIsActionBarNavDrawerToggleEnabled = true;
        }
        // mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerLayout.setScrimColor(getResources()
                        .getColor(R.color.overlay_black_40p));
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        mNavDrawerAdapter = new HomeNavDrawerAdapter(this, R.array.nav_drawer_titles, R.array.nav_drawer_descriptions);
        mNavListView.setAdapter(mNavDrawerAdapter);
        mNavListView.setOnItemClickListener(mNavDrawerItemClickListener);

    }

    @Override
    protected void onPostCreate(final Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        if (mIsActionBarNavDrawerToggleEnabled) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mIsActionBarNavDrawerToggleEnabled) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(final Menu menu) {
        if (mIsActionBarNavDrawerToggleEnabled) {
            setOptionsGroupHidden(menu, mDrawerLayout.isDrawerOpen(mNavListView));
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void setOptionsGroupHidden(final Menu menu, final boolean drawerOpen) {

        menu.setGroupEnabled(R.id.group_hide_on_drawer_open, !drawerOpen);
        menu.setGroupVisible(R.id.group_hide_on_drawer_open, !drawerOpen);
    }

    /**
     * Whether the current Activity has a Navigation Drawer
     */
    public boolean hasNavigationDrawer() {
        return mHasNavigationDrawer;
    }

    /**
     * Whether the current Activity's Navigation drawer is also associated with
     * the Action Bar
     */
    public boolean isActionBarNavDrawerToggleEnabled() {
        return mDrawerToggle == null ? mIsActionBarNavDrawerToggleEnabled
                        : mDrawerToggle.isDrawerIndicatorEnabled();
    }

    /**
     * Use to dynamically enable/disable the Action Bar drawer toggle for the
     * Activity. Has no effect if the Activity never has a Navigation Drawer to
     * begin with. Mainly used to control The Action Bar Drawer toggle from
     * fragments
     * 
     * @param enabled <code>true</code> to enable the Action Bar drawer toggle,
     *            <code>false</code> to disable it
     */
    public void setActionBarDrawerToggleEnabled(final boolean enabled) {

        if (mHasNavigationDrawer) {

            if (mDrawerToggle != null) {
                mDrawerToggle.setDrawerIndicatorEnabled(enabled);
            }

        }
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
        if ((masterFragment != null)
                        && (getSupportFragmentManager()
                                        .getBackStackEntryCount() > 0)) {
            masterFragment.onBackPressed();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Returns the current master fragment. In single pane layout, this is the
     * fragment in the main content. In a multi-pane layout, returns the
     * fragment in the master container, which is the one responsible for
     * coordination
     * 
     * @return <code>null</code> If no fragment is loaded,the
     *         {@link AbstractBarterLiFragment} implementation which is the
     *         current master fragment otherwise
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

    /**
     * Creates a Crouton View based on the style
     * 
     * @param context {@link Context} reference to get the
     *            {@link LayoutInflater} reference
     * @param message The message to display
     * @param style The style of Crouton
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
    public void onClick(final DialogInterface dialog, final int which) {

        final AbstractBarterLiFragment fragment = getCurrentMasterFragment();

        if ((fragment != null) && fragment.isVisible()) {
            if (fragment.willHandleDialog(dialog)) {
                fragment.onDialogClick(dialog, which);
            }
        }
    }

}
