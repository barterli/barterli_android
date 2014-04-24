/**
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
 */

package li.barter.utils;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.MapView;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.view.View;

/**
 * Class to encapsulate the functionality for handling the opening/closing of
 * the content drawer and the Map view behind it
 * 
 * @author Vinay S Shenoy
 */
public class MapDrawerInteractionHelper implements DrawerListener,
                SnapshotReadyCallback, OnMapLoadedCallback {

    private static final String TAG                 = "MapDrawBlurHelper";
    private static final Object LOCK                = new Object();

    /**
     * A reference to the context
     */
    private final Context       mContext;

    /**
     * Blur radius for the Map. 1 <= x <= 25
     */
    private static final int    MAP_BLUR            = 20;

    /**
     * Transition time(in milliseconds) to use blurring between the Map
     * backgrounds
     */
    private static final int    TRANSITION_DURATION = 1000;

    /**
     * Array of drawables to use for the background View transition for
     * smoothening
     */
    private Drawable[]          mTransitionDrawableLayers;

    /**
     * Transparent color drawable to serve as the initial starting drawable for
     * the map background transition
     */
    private Drawable            mTransparentColorDrawable;

    /**
     * The current drawer state
     */
    private DrawerDragState     mCurDirection;

    /**
     * The previous drawer state
     */
    private DrawerDragState     mPrevDirection;

    /**
     * The previous slide offset
     */
    private float               mPrevSlideOffset;

    /**
     * Flag to indicate whether a map snapshot has been requested
     */
    private boolean             mMapSnapshotRequested;

    /**
     * Handler for scheduling callbacks on UI thread
     */
    private Handler             mHandler;

    /**
     * Runnable for hiding the Map container when the drawer is opened
     */
    private Runnable            mHideMapViewRunnable;

    /**
     * Flag that indicates whether a hide runnable has also been posted to the
     * Handler to prevent multiple runnables being posted
     */
    private boolean             mIsHideRunnablePosted;

    /**
     * External drawer listener to listen for drawer open/close events
     */
    private DrawerListener      mExternalDrawerListener;

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
     * Reference to the Drawer Layout
     */
    private DrawerLayout mDrawerLayout;

    /**
     * Reference to the drawer View which is drawn on top of the Map
     */
    private View         mDrawerView;

    /**
     * Reference to the MapView
     */
    private MapView      mMapView;

    /**
     * Renderscript reference for blurring the bitmap
     */
    private RenderScript mRenderScript;

    /**
     * Construct a {@link MapDrawerInteractionHelper} with the drawer layout,
     * drawer view and the map view. call init to begin tracking the Map and
     * Drawer events
     * 
     * @param context A {@link Context} reference
     * @param drawerLayout The Drawer Layout which contains the drawer view
     * @param drawerView The reference to the Drawer view
     * @param mapView The MapView which is to be tied to the drawer events
     */
    public MapDrawerInteractionHelper(final Context context, final DrawerLayout drawerLayout, final View drawerView, final MapView mapView) {

        mContext = context;
        mDrawerLayout = drawerLayout;
        mDrawerView = drawerView;
        mMapView = mapView;
    }

    /**
     * Call this either in {@linkplain Activity#onRestoreInstanceState(Bundle)}
     * or in {@linkplain Fragment#onViewStateRestored(Bundle)} to ensure proper
     * redrawing of the transition effect if the drawer is open
     */
    public void onRestoreState() {

        if (mDrawerLayout.isDrawerOpen(mDrawerView)) {
            beginMapSnapshotProcess();
        }
    }

    /**
     * Call in {@link Activity#onPause()} or in {@link Fragment#onPause()}
     */
    public void onPause() {

        unscheduleMapHideTask();
        mRenderScript.destroy();
    }

    /**
     * Call in {@link Activity#onResume()} or in {@link Fragment#onResume()}
     */
    public void onResume() {
        mRenderScript = RenderScript.create(mContext);

    }

    /**
     * Start tracking the events for the Map/Drawer
     * 
     * @param externalDrawerListener The external drawer listener to listen for
     *            events whenever the drawer opens/closes
     */
    public void init(final DrawerListener externalDrawerListener) {
        mTransparentColorDrawable = new ColorDrawable(Color.TRANSPARENT);
        mTransitionDrawableLayers = new Drawable[2];
        mPrevSlideOffset = 0.0f;
        mHandler = new Handler();
        mDrawerLayout.setDrawerListener(this);
        setExternalDrawerListener(externalDrawerListener);
    }

    public DrawerListener getExternalDrawerListener() {
        return mExternalDrawerListener;
    }

    public void setExternalDrawerListener(final DrawerListener drawerListener) {
        mExternalDrawerListener = drawerListener;
    }

    /**
     * @return the mDrawerLayout
     */
    public DrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    /**
     * @param drawerLayout the drawerLayout to set
     */
    public void setDrawerLayout(final DrawerLayout drawerLayout) {
        this.mDrawerLayout = drawerLayout;
    }

    /**
     * @return the drawerView
     */
    public View getDrawerView() {
        return mDrawerView;
    }

    /**
     * @param drawerView the drawerView to set
     */
    public void setDrawerView(final View drawerView) {
        this.mDrawerView = drawerView;
    }

    /**
     * @return the mapView
     */
    public MapView getMapView() {
        return mMapView;
    }

    /**
     * @param mapView the mapView to set
     */
    public void setmMapView(final MapView mapView) {
        this.mMapView = mapView;
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onDrawerClosed(final View drawerView) {

        if (mExternalDrawerListener != null) {
            mExternalDrawerListener.onDrawerClosed(drawerView);
        }
        if (drawerView == mDrawerView) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                // mDrawerView.setBackground(mTransparentColorDrawable);
            } else {
                // mDrawerView.setBackgroundDrawable(mTransparentColorDrawable);
            }
        }

    }

    @Override
    public void onDrawerOpened(final View drawerView) {

        if (mExternalDrawerListener != null) {
            mExternalDrawerListener.onDrawerOpened(drawerView);
        }
        if (drawerView == mDrawerView) {

            Logger.d(TAG, "Map Opened");
            beginMapSnapshotProcess();
        }

    }

    @Override
    public void onDrawerSlide(final View drawerView, final float slideOffset) {

        if (mExternalDrawerListener != null) {
            mExternalDrawerListener.onDrawerSlide(drawerView, slideOffset);
        }
        if (drawerView == mDrawerView) {

            calculateCurrentDirection(slideOffset);

            if (mCurDirection != mPrevDirection) { //Drawer state has changed

                Logger.d(TAG, "Drawer drag " + mCurDirection + " from "
                                + mPrevDirection + " slide offset:"
                                + slideOffset);

                if ((mCurDirection == DrawerDragState.CLOSING)
                                && (slideOffset >= 0.7f)) { //Drawer was almost opened, but user moved it to closed again

                    if (mMapView.getVisibility() == View.VISIBLE) {
                        unscheduleMapHideTask(); //If there's any task sceduled for hiding the map, remove it
                    } else if (mMapView.getVisibility() == View.GONE) {
                        mMapView.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }

    @Override
    public void onDrawerStateChanged(final int state) {

        if (mExternalDrawerListener != null) {
            mExternalDrawerListener.onDrawerStateChanged(state);
        }
        if (state == DrawerLayout.STATE_IDLE) {
            if (mDrawerLayout.isDrawerOpen(mDrawerView)) {
                setMapMyLocationEnabled(false);
                if (!mMapSnapshotRequested) { //If a map snapshot hasn't been requested, no need to call this as the map will be hidden when the snapsht is loaded
                    scheduleHideMapTask(0);
                } else {
                    beginMapSnapshotProcess();
                }
            } else {
                setMapMyLocationEnabled(true);
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
     * Schedules the task for hiding the map transition, cancelling the previous
     * one, if any
     * 
     * @param delay The delay(in milliseconds) after which the Map should be
     *            removed in the background
     */
    private void scheduleHideMapTask(final int delay) {

        if (mIsHideRunnablePosted) {

            Logger.d(TAG, "Already posted runnable, returning");
            return;
        }
        mHideMapViewRunnable = new Runnable() {

            @Override
            public void run() {
                mIsHideRunnablePosted = false;
                mMapView.setVisibility(View.GONE);
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

    /**
     * Begins the process of capturing the Map snapshot by setting a Map loaded
     * callback. Once the Map is completely loaded, a snapshot is taken, and a
     * blurred bitmap is generated for the background of the screen content
     */
    private void beginMapSnapshotProcess() {

        //        mMapSnapshotRequested = true;
        //        final GoogleMap googleMap = getMap();
        //
        //        if (googleMap != null) {
        //            Logger.d(TAG, "Adding On Loaded Callback!");
        //            googleMap.setOnMapLoadedCallback(this);
        //        }
    }

    /**
     * Call for when the Map is zoomed into a location and the zoom in
     * completes. It triggers the Map/Drawer interaction to begin
     */
    public void onMapZoomedIn() {
        beginMapSnapshotProcess();
    }

    @Override
    public void onMapLoaded() {

        // Create the map snapshot only if the drawer is open
        if (mDrawerLayout.isDrawerOpen(mDrawerView)) {
            captureMapSnapshot();
        }
    }

    /**
     * Sets the My location enabled for the Map
     */
    private void setMapMyLocationEnabled(final boolean enabled) {

        final GoogleMap googleMap = getMap();

        if (googleMap != null) {
            googleMap.setMyLocationEnabled(enabled);
        }
    }

    /**
     * Captures a snapshot of the map
     */
    private void captureMapSnapshot() {
        final GoogleMap googleMap = getMap();

        if (googleMap != null) {
            Logger.d(TAG, "Taking Snapshot!");
            googleMap.snapshot(this);
        }

    }

    /**
     * Gets a reference of the Google Map
     */
    private GoogleMap getMap() {

        return mMapView.getMap();
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onSnapshotReady(Bitmap snapshot) {

        /* Create a blurred version of the Map snapshot */
        final BitmapDrawable backgroundDrawable = new BitmapDrawable(mContext.getResources(), blurImage(mContext, snapshot, MAP_BLUR));

        mTransitionDrawableLayers[0] = mTransparentColorDrawable;
        mTransitionDrawableLayers[1] = backgroundDrawable;

        final TransitionDrawable transitionDrawable = new TransitionDrawable(mTransitionDrawableLayers);
        transitionDrawable.setCrossFadeEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            //  mDrawerView.setBackground(transitionDrawable);
        } else {
            //  mDrawerView.setBackgroundDrawable(transitionDrawable);
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

        mMapSnapshotRequested = false;

    }

    /**
     * Generate a blurred Bitmap from an input Bitmap
     * 
     * @param context
     * @param input The bitmap to be blurred
     * @param blurRadius The blur radius, between 1 & 25, inclusive
     * @return The blurred Bitmap
     */
    private Bitmap blurImage(final Context context, final Bitmap input,
                    final int blurRadius) {
        final Allocation alloc = Allocation
                        .createFromBitmap(mRenderScript, input);

        final ScriptIntrinsicBlur blur = ScriptIntrinsicBlur
                        .create(mRenderScript, alloc.getElement());
        blur.setRadius(blurRadius);
        blur.setInput(alloc);

        final Bitmap result = Bitmap.createBitmap(input.getWidth(), input
                        .getHeight(), input.getConfig());
        final Allocation outAlloc = Allocation
                        .createFromBitmap(mRenderScript, result);
        blur.forEach(outAlloc);
        outAlloc.copyTo(result);

        alloc.destroy();
        outAlloc.destroy();
        return result;
    }

}
