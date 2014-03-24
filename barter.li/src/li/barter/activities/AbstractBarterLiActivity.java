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
import com.android.volley.toolbox.ImageLoader;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.DrawerLayout;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.concurrent.atomic.AtomicInteger;

import li.barter.R;
import li.barter.adapters.HomeNavDrawerAdapter;
import li.barter.fragments.AbstractBarterLiFragment;
import li.barter.fragments.FragmentTransition;
import li.barter.http.IVolleyHelper;
import li.barter.utils.AppConstants.NetworkDetails;
import li.barter.widgets.TypefaceCache;
import li.barter.widgets.TypefacedSpan;

/**
 * @author Vinay S Shenoy Base class for inheriting all other Activities from
 */
public abstract class AbstractBarterLiActivity extends FragmentActivity {

    private static final String   TAG                     = "BaseBarterLiActivity";

    private static final int      ACTION_BAR_DISPLAY_MASK = ActionBar.DISPLAY_HOME_AS_UP
                                                                          | ActionBar.DISPLAY_SHOW_TITLE;

    private RequestQueue          mRequestQueue;
    private ImageLoader           mImageLoader;
    private AtomicInteger         mRequestCounter;

    private ActivityTransition    mActivityTransition;

    /**
     * Drawer Layout that contains the Navigation Drawer
     */
    private DrawerLayout          mDrawerLayout;

    /**
     * Drawer toggle for Action Bar
     */
    private ActionBarDrawerToggle mDrawerToggle;

    /**
     * {@link ListView} that provides the navigation items
     */
    private ListView              mNavListView;

    /**
     * {@link BaseAdapter} implementation for Navigation drawer item
     */
    private HomeNavDrawerAdapter  mNavDrawerAdapter;

    /**
     * Whether the current activity has a Navigation drawer or not
     */
    private boolean               mHasNavigationDrawer;

    /**
     * Whether the nav drawer associated with this activity is also associated
     * with the drawer togglw. Is valid only if
     * <code>mHasNavigationDrawer</code> is <code>true</code>
     */
    private boolean               mIsActionBarNavDrawerToggleEnabled;

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
            request.setTag(getVolleyTag());
            mRequestCounter.incrementAndGet();
            setProgressBarIndeterminateVisibility(true);
            mRequestQueue.add(request);
        } else if (showErrorOnNoNetwork) {
            showToast(errorMsgResId != 0 ? errorMsgResId
                            : R.string.no_network_connection, false);
        }
    }

    /**
     * A Tag to add to all Volley requests. This must be unique for all
     * Fragments types
     * 
     * @return An Object that's the tag for this fragment
     */
    protected abstract Object getVolleyTag();

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
        return NetworkDetails.INSTANCE.isNetworkConnected;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        if (mIsActionBarNavDrawerToggleEnabled
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
        s.setSpan(new TypefacedSpan(this, TypefaceCache.ALEGREYA_BLACK_ITALIC), 0, s
                        .length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

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
     * Display a {@link Toast} message
     * 
     * @param toastMessage The message to display
     * @param isLong Whether it is a long toast
     */
    public void showToast(final String toastMessage, final boolean isLong) {
        Toast.makeText(this, toastMessage, isLong ? Toast.LENGTH_LONG
                        : Toast.LENGTH_SHORT).show();
    }

    /**
     * Display a {@link Toast} message
     * 
     * @param toastMessageResId The message string resource Id to display
     * @param isLong Whether it is a long toast
     */
    public void showToast(final int toastMessageResId, final boolean isLong) {
        Toast.makeText(this, toastMessageResId, isLong ? Toast.LENGTH_LONG
                        : Toast.LENGTH_SHORT).show();
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
                    boolean attachToActionBar) {

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
                    invalidateOptionsMenu();
                }

                @Override
                public void onDrawerClosed(final View drawerView) {
                    super.onDrawerClosed(drawerView);
                    invalidateOptionsMenu();
                }

            };

            mDrawerLayout.setDrawerListener(mDrawerToggle);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            mIsActionBarNavDrawerToggleEnabled = true;
        }

        mDrawerLayout.setScrimColor(getResources()
                        .getColor(R.color.overlay_black_40p));
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);

        mNavDrawerAdapter = new HomeNavDrawerAdapter(this, R.array.nav_drawer_titles, R.array.nav_drawer_descriptions);
        mNavListView.setAdapter(mNavDrawerAdapter);

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
    public void setActionBarDrawerToggleEnabled(boolean enabled) {

        if (mHasNavigationDrawer) {

            if (mDrawerToggle != null) {
                mDrawerToggle.setDrawerIndicatorEnabled(enabled);
            }

        }
    }

}
