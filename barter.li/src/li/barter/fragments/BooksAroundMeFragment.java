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

package li.barter.fragments;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.haarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

import android.annotation.TargetApi;
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
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.FrameLayout;
import android.widget.GridView;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity;
import li.barter.activities.AddOrEditBookActivity;
import li.barter.activities.ScanIsbnActivity;
import li.barter.adapters.BooksAroundMeAdapter;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.GooglePlayClientWrapper;
import li.barter.utils.UtilityMethods;
import li.barter.widgets.FullWidthDrawerLayout;

/**
 * @author Vinay S Shenoy Fragment for displaying Books Around Me. Also contains
 *         a Map that the user can use to easily switch locations
 */
public class BooksAroundMeFragment extends AbstractBarterLiFragment implements
                LocationListener, SnapshotReadyCallback, CancelableCallback,
                OnMapLoadedCallback, DrawerListener {

    private static final String TAG = "BooksAroundMeActivity";

    /**
     * Enum that indicates the direction of the drag
     * 
     * @author Vinay S Shenoy
     */
    private enum DrawerDragState {

        OPENING,
        CLOSING
    }

    /**
     * Zoom level for the map when the location is retrieved
     */
    private static final float            MAP_ZOOM_LEVEL      = 15;

    /**
     * Blur radius for the Map. 1 <= x <= 25
     */
    private static final int              MAP_BLUR            = 20;

    /**
     * Transition time(in milliseconds) to use blurring between the Map backgrounds
     */
    private static final int              TRANSITION_DURATION = 1000;

    private GooglePlayClientWrapper       mGooglePlayClientWrapper;

    /**
     * {@link SupportMapFragment} used to display the Map
     */
    private SupportMapFragment            mMapFragment;

    /**
     * Background View on which the blurred Map snapshot is set
     */
    private View                          mBooksContentView;

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

    /**
     * Flag that indicates whether a hide runnable has also been posted to the
     * Handler to prevent multiple runnables being posted
     */
    private boolean                       mIsHideRunnablePosted;

    /**
     * The current drawer state
     */
    private DrawerDragState               mCurDirection;

    /**
     * The previous drawer state
     */
    private DrawerDragState               mPrevDirection;

    /**
     * The previous slide offset
     */
    private float                         mPrevSlideOffset;

    /**
     * Flag to indicate whether a map snapshot has been requested
     */
    private boolean                       mMapSnapshotRequested;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                    Bundle savedInstanceState) {

        setHasOptionsMenu(true);
        final View contentView = inflater.inflate(
                        R.layout.fragment_books_around_me, container, false);

        mMapFrameLayout = (FrameLayout) contentView
                        .findViewById(R.id.map_books_around_me);

        mDrawerLayout = (FullWidthDrawerLayout) contentView
                        .findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerListener(this);
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);

        mTransparentColorDrawable = new ColorDrawable(Color.TRANSPARENT);
        mBooksContentView = contentView
                        .findViewById(R.id.layout_books_container);
        mBooksAroundMeAutoCompleteTextView = (AutoCompleteTextView) contentView
                        .findViewById(R.id.auto_complete_books_around_me);
        mBooksAroundMeGridView = (GridView) contentView
                        .findViewById(R.id.grid_books_around_me);

        mBooksAroundMeAdapter = new BooksAroundMeAdapter();
        mSwingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(
                        mBooksAroundMeAdapter, 150, 500);

        mTransitionDrawableLayers = new Drawable[2];
        mGooglePlayClientWrapper = new GooglePlayClientWrapper(
                        (AbstractBarterLiActivity) getActivity(), this);

        mHandler = new Handler();

        mPrevSlideOffset = 0.0f;
        if (savedInstanceState == null) {
            mDrawerOpenedAutomatically = false;
        } else {
            mDrawerOpenedAutomatically = savedInstanceState
                            .getBoolean(Keys.BOOL_1);
        }
        return contentView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_books_around_me, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_scan_book: {
                startActivity(new Intent(getActivity(), ScanIsbnActivity.class));
                return true;
            }

            case R.id.action_add_book: {
                startActivity(new Intent(getActivity(),
                                AddOrEditBookActivity.class));
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Keys.BOOL_1, mDrawerOpenedAutomatically);
    }

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    @Override
    public void onStart() {
        super.onStart();
        mGooglePlayClientWrapper.onActivityStart();
    }

    @Override
    public void onStop() {
        mGooglePlayClientWrapper.onActivityStop();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        /*
         * Get reference to MapFragment here because the fragment might be
         * destroyed when Activity is in background
         */
        mMapFragment = (SupportMapFragment) getFragmentManager()
                        .findFragmentById(R.id.map_books_around_me);
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
                        getResources(), UtilityMethods.blurImage(getActivity(),
                                        snapshot, MAP_BLUR));

        mTransitionDrawableLayers[0] = mTransparentColorDrawable;
        mTransitionDrawableLayers[1] = backgroundDrawable;

        final TransitionDrawable transitionDrawable = new TransitionDrawable(
                        mTransitionDrawableLayers);
        transitionDrawable.setCrossFadeEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mBooksContentView.setBackground(transitionDrawable);
        } else {
            mBooksContentView.setBackgroundDrawable(transitionDrawable);
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

        mMapSnapshotRequested = false;

    }

    /**
     * Schedules the task for hiding the map transition, cancelling the previous
     * one, if any
     * 
     * @param delay The delay(in milliseconds) after which the Map should be
     *            removed in the background
     */
    private void scheduleHideMapTask(final int delay) {

        if (mIsHideRunnablePosted) {

            Log.d(TAG, "Already posted runnable, returning");
            return;
        }
        mHideMapViewRunnable = new Runnable() {

            @Override
            public void run() {
                mIsHideRunnablePosted = false;
                mMapFrameLayout.setVisibility(View.GONE);
            }
        };
        mHandler.postDelayed(mHideMapViewRunnable, delay);
        mIsHideRunnablePosted = true;
    }

    /**
     * Unschedules any current(if exists) map hide transition
     */
    private void unscheduleMapHideTask() {

        if (mHideMapViewRunnable != null) {
            mHandler.removeCallbacks(mHideMapViewRunnable);
            mHideMapViewRunnable = null;
            mIsHideRunnablePosted = false;
        }
    }

    @Override
    public void onPause() {
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
            mMapSnapshotRequested = true;
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
        if (mDrawerLayout.isDrawerOpen(mBooksContentView)) {
            captureMapSnapshot();
        } else if (!mDrawerOpenedAutomatically) {
            // Open drawer automatically on map loaded if first launch
            mDrawerOpenedAutomatically = true;
            mDrawerLayout.openDrawer(mBooksContentView);
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
        if (drawerView == mBooksContentView) {

            setMapMyLocationEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mBooksContentView.setBackground(mTransparentColorDrawable);
            } else {
                mBooksContentView
                                .setBackgroundDrawable(mTransparentColorDrawable);
            }
        }

    }

    @Override
    public void onDrawerOpened(final View drawerView) {

        if (drawerView == mBooksContentView) {

            Log.d(TAG, "Map Opened");
            setMapMyLocationEnabled(false);
            beginMapSnapshotProcess();
        }

    }

    @Override
    public void onDrawerSlide(final View drawerView, final float slideOffset) {

        if (drawerView == mBooksContentView) {

            calculateCurrentDirection(slideOffset);

            if (mCurDirection != mPrevDirection) { //Drawer state has changed

                Log.d(TAG, "Drawer drag " + mCurDirection + " from "
                                + mPrevDirection + " slide offset:"
                                + slideOffset);

                /*
                 * if (mCurDirection == DrawerDragState.OPENING) { if
                 * (slideOffset >= 0.7f && mMapFrameLayout.getVisibility() ==
                 * View.VISIBLE) { //Drawer was moved to be closed, but was
                 * brought back into opened state, in which case, we need to
                 * hide the drawer again scheduleHideMapTask(150); } } else {
                 */
                if (mCurDirection == DrawerDragState.CLOSING
                                && slideOffset >= 0.7f) { //Drawer was almost opened, but user moved it to closed again

                    if (mMapFrameLayout.getVisibility() == View.VISIBLE) {
                        unscheduleMapHideTask(); //If there's any task sceduled for hiding the map, remove it
                    } else if (mMapFrameLayout.getVisibility() == View.GONE) {
                        mMapFrameLayout.setVisibility(View.VISIBLE);
                    }
                }
                //}
            }
        }
    }

    @Override
    public void onDrawerStateChanged(final int state) {

        Log.d(TAG, "On Drawer State Change:" + state);
        if (state == FullWidthDrawerLayout.STATE_IDLE) {
            if (mDrawerLayout.isDrawerOpen(mBooksContentView)) {
                if (!mMapSnapshotRequested) { //If a map snapshot hasn't been requested, no need to call this as the map will be hidden when the snapsht is loaded
                    scheduleHideMapTask(0);
                } else {
                    beginMapSnapshotProcess();
                }
            }
        }
    }

    /**
     * Calculates the current direction of the drag
     */
    private void calculateCurrentDirection(final float slideOffset) {

        mPrevDirection = mCurDirection;

        // Drawer is being opened
        if (slideOffset >= mPrevSlideOffset) {
            mCurDirection = DrawerDragState.OPENING;
        } else {
            mCurDirection = DrawerDragState.CLOSING;
        }
        mPrevSlideOffset = slideOffset;
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
