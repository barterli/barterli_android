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

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.haarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.GridView;

import li.barter.R;
import li.barter.R.id;
import li.barter.R.layout;
import li.barter.R.menu;
import li.barter.adapters.BooksAroundMeAdapter;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.GooglePlayClientWrapper;
import li.barter.utils.UtilityMethods;
import li.barter.widgets.FullWidthDrawerLayout;
import li.barter.widgets.FullWidthDrawerLayout.DrawerListener;

/**
 * @author Vinay S Shenoy Activity for displaying Books Around Me. Also contains
 *         a Map that the user can use to easily switch locations
 */
public class BooksAroundMeActivity extends AbstractBarterLiActivity implements
                LocationListener, SnapshotReadyCallback, CancelableCallback,
                OnMapLoadedCallback, DrawerListener {

    private static final String           TAG                 = "BooksAroundMeActivity";

    /**
     * Zoom level for the map when the location is retrieved
     */
    private static final float            MAP_ZOOM_LEVEL      = 15;

    /**
     * Blur radius for the Map. 1 <= x <= 25
     */
    private static final int              MAP_BLUR            = 20;

    /**
     * Transition to use blurring between the Map backgrounds
     */
    private static final int              TRANSITION_DURATION = 1200;

    private GooglePlayClientWrapper       mGooglePlayClientWrapper;

    /**
     * {@link MapFragment} used to display the Map
     */
    private MapFragment                   mMapFragment;

    /**
     * Background View on which the blurred Map snapshot is set
     */
    private View                          mBackgroundView;

    /**
     * Frame Layout in which the map fragment is placed
     */
    private FrameLayout                   mMapFrameLayout;

    /**
     * TextView which will provide drop down suggestions as user searches for
     * books
     */
    private AutoCompleteTextView          mBooksAroundMeAutoCompleteTextView;

    /**
     * GridView into which the book content will be placed
     */
    private GridView                      mBooksAroundMeGridView;

    /**
     * {@link BooksAroundMeAdapter} instance for the Books
     */
    private BooksAroundMeAdapter          mBooksAroundMeAdapter;

    /**
     * {@link AnimationAdapter} implementation to provide appearance animations
     * for the book items as they are brought in
     */
    private SwingBottomInAnimationAdapter mSwingBottomInAnimationAdapter;

    /**
     * Array of drawables to use for the background View transition for
     * smoothening
     */
    private Drawable[]                    mTransitionDrawableLayers;

    /**
     * Transparent color drawable to serve as the initial starting drawable for
     * the map background transition
     */
    private Drawable                      mTransparentColorDrawable;

    /**
     * Drawer Layout that contains the Books grid and autocomplete search text
     */
    private FullWidthDrawerLayout         mDrawerLayout;

    /**
     * Flag that remembers whether the drawer has been opened at least once
     * automatically
     */
    private boolean                       mDrawerOpenedAutomatically;

    /**
     * Handler for scheduling callbacks on UI thread
     */
    private Handler                       mHandler;

    /**
     * Runnable for hiding the Map container when the drawer is opened
     */
    private Runnable                      mHideMapViewRunnable;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books_around_me);

        mMapFrameLayout = (FrameLayout) findViewById(R.id.map_books_around_me);

        mDrawerLayout = (FullWidthDrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(this);
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);

        mTransparentColorDrawable = new ColorDrawable(Color.TRANSPARENT);
        mBackgroundView = findViewById(R.id.layout_books_container);
        mBooksAroundMeAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.auto_complete_books_around_me);
        mBooksAroundMeGridView = (GridView) findViewById(R.id.grid_books_around_me);

        mBooksAroundMeAdapter = new BooksAroundMeAdapter();
        mSwingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(
                        mBooksAroundMeAdapter, 150, 500);

        setActionBarDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        getActionBar().setHomeButtonEnabled(false);

        mTransitionDrawableLayers = new Drawable[2];
        mGooglePlayClientWrapper = new GooglePlayClientWrapper(this, this);

        mHandler = new Handler();

        if (savedInstanceState == null) {
            mDrawerOpenedAutomatically = false;
        } else {
            mDrawerOpenedAutomatically = savedInstanceState
                            .getBoolean(Keys.BOOL_1);
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Keys.BOOL_1, mDrawerOpenedAutomatically);
    }

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGooglePlayClientWrapper.onActivityStart();
    }

    @Override
    protected void onStop() {
        mGooglePlayClientWrapper.onActivityStop();
        super.onStop();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        /*
         * Get reference to MapFragment here because the fragment might be
         * destroyed when Activity is in background
         */
        mMapFragment = (MapFragment) getFragmentManager().findFragmentById(
                        R.id.map_books_around_me);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.menu_books_around_me, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_scan_book: {
                startActivity(new Intent(this, ScanIsbnActivity.class));
                return true;
            }

            case R.id.action_my_books: {
                // TODO do we have a screen to show here
                return true;
            }

            case R.id.action_profile: {
                // TODO Show profile screen
                return true;
            }

            case R.id.action_add_book: {
                startActivity(new Intent(this, AddOrEditBookActivity.class));
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onLocationChanged(final Location location) {

        // Very rare case, if locationClient.getLastLocation() returns null
        if (location == null) {
            return;
        }
        Log.d(TAG,
                        "Location update:" + location.getLatitude() + " "
                                        + location.getLongitude());

        UserInfo.INSTANCE.latestLocation = location;

        if ((mMapFragment != null) && mMapFragment.isVisible()) {

            final GoogleMap googleMap = mMapFragment.getMap();

            if (googleMap != null) {
                googleMap.setMyLocationEnabled(false);
                googleMap.animateCamera(
                                CameraUpdateFactory
                                                .newLatLngZoom(new LatLng(
                                                                UserInfo.INSTANCE.latestLocation
                                                                                .getLatitude(),
                                                                UserInfo.INSTANCE.latestLocation
                                                                                .getLongitude()),
                                                                MAP_ZOOM_LEVEL),
                                this);

            }
        }
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onSnapshotReady(Bitmap snapshot) {

        /* Create a blurred version of the Map snapshot */
        final BitmapDrawable backgroundDrawable = new BitmapDrawable(
                        getResources(), UtilityMethods.blurImage(this,
                                        snapshot, MAP_BLUR));

        mTransitionDrawableLayers[0] = mTransparentColorDrawable;
        mTransitionDrawableLayers[1] = backgroundDrawable;

        final TransitionDrawable transitionDrawable = new TransitionDrawable(
                        mTransitionDrawableLayers);
        transitionDrawable.setCrossFadeEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mBackgroundView.setBackground(transitionDrawable);
        } else {
            mBackgroundView.setBackgroundDrawable(transitionDrawable);
        }

        transitionDrawable.startTransition(TRANSITION_DURATION);

        snapshot.recycle();
        snapshot = null;

        /*
         * Set to prevent overdraw. You will get a moment of overdraw when the
         * transition is happening, but after the overdraw gets removed. We're
         * scheduling the hide map view transition to happen later because it
         * causes a bad effect if we hide the Map View in background while the
         * transition is still happening
         */
        scheduleHideMapTask(TRANSITION_DURATION);

        if (mBooksAroundMeGridView.getAdapter() == null) {
            mSwingBottomInAnimationAdapter
                            .setAbsListView(mBooksAroundMeGridView);
            mBooksAroundMeGridView.setAdapter(mSwingBottomInAnimationAdapter);
        }

    }

    /**
     * Schedules the task for hiding the map transition, cancelling the previous
     * one, if any
     * 
     * @param delay The delay(in milliseconds) after which the Map should be
     *            removed in the background
     */
    private void scheduleHideMapTask(final int delay) {

        unscheduleMapHideTask();
        mHideMapViewRunnable = new Runnable() {

            @Override
            public void run() {
                mMapFrameLayout.setVisibility(View.GONE);
            }
        };
        mHandler.postDelayed(mHideMapViewRunnable, delay);
    }

    /**
     * Unschedules any current(if exists) map hide transition
     */
    private void unscheduleMapHideTask() {

        if (mHideMapViewRunnable != null) {
            mHandler.removeCallbacks(mHideMapViewRunnable);
            mHideMapViewRunnable = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unscheduleMapHideTask();
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onFinish() {

        beginMapSnapshotProcess();
    }

    /**
     * Begins the process of capturing the Map snapshot by setting a Map loaded
     * callback. Once the Map is completely loaded, a snapshot is taken, and a
     * blurred bitmap is generated for the background of the screen content
     */
    private void beginMapSnapshotProcess() {
        if ((mMapFragment != null) && mMapFragment.isVisible()) {
            final GoogleMap googleMap = mMapFragment.getMap();

            if (googleMap != null) {
                Log.d(TAG, "Adding On Loaded Callback!");
                googleMap.setOnMapLoadedCallback(this);
            }
        }
    }

    @Override
    public void onMapLoaded() {

        // Create the map snapshot only if the drawer is open
        if (mDrawerLayout.isDrawerOpen(mBackgroundView)) {
            captureMapSnapshot();
        } else if (!mDrawerOpenedAutomatically) {
            // Open drawer automatically on map loaded if first launch
            mDrawerOpenedAutomatically = true;
            mDrawerLayout.openDrawer(mBackgroundView);
        }
    }

    /**
     * Sets the My location enabled for the Map
     */
    private void setMapMyLocationEnabled(final boolean enabled) {

        if ((mMapFragment != null) && mMapFragment.isVisible()) {

            final GoogleMap googleMap = mMapFragment.getMap();

            if (googleMap != null) {
                googleMap.setMyLocationEnabled(enabled);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onDrawerClosed(final View drawerView) {
        if (drawerView == mBackgroundView) {

            setMapMyLocationEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mBackgroundView.setBackground(mTransparentColorDrawable);
            } else {
                mBackgroundView.setBackgroundDrawable(mTransparentColorDrawable);
            }
        }

    }

    @Override
    public void onDrawerOpened(final View drawerView) {

        if (drawerView == mBackgroundView) {

            setMapMyLocationEnabled(false);
            beginMapSnapshotProcess();
        }

    }

    @Override
    public void onDrawerSlide(final View drawerView, final float slideOffset) {

        if (drawerView == mBackgroundView) {
            unscheduleMapHideTask(); // If drawer is slid as the transition is
                                     // happening, cancel the hide runnable
            if (mMapFrameLayout.getVisibility() == View.GONE
                            && slideOffset >= 0.9f) {
                mMapFrameLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDrawerStateChanged(final int state) {
        // TODO Auto-generated method stub

    }

    /**
     * Captures a snapshot of the map
     */
    private void captureMapSnapshot() {
        if ((mMapFragment != null) && mMapFragment.isVisible()) {
            final GoogleMap googleMap = mMapFragment.getMap();

            if (googleMap != null) {
                Log.d(TAG, "Taking Snapshot!");
                googleMap.snapshot(this);
            }
        }

    }

}
