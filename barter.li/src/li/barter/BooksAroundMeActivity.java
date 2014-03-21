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
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;

import li.barter.adapters.BooksAroundMeAdapter;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.GooglePlayClientWrapper;
import li.barter.utils.UtilityMethods;

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
    private DrawerLayout                  mDrawerLayout;

    /**
     * Flag that remembers whether the drawer has been opened at least once
     * automatically
     */
    private boolean                       mDrawerOpenedAutomatically;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_books_around_me);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
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

        if (savedInstanceState == null) {
            mDrawerOpenedAutomatically = false;
        } else {
            mDrawerOpenedAutomatically = savedInstanceState
                            .getBoolean(Keys.BOOL_1);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Keys.BOOL_1, mDrawerOpenedAutomatically);
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

        if (mBooksAroundMeGridView.getAdapter() == null) {
            mSwingBottomInAnimationAdapter
                            .setAbsListView(mBooksAroundMeGridView);
            mBooksAroundMeGridView.setAdapter(mSwingBottomInAnimationAdapter);
        }

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
    public void onDrawerClosed(View drawerView) {
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
    public void onDrawerOpened(View drawerView) {

        if (drawerView == mBackgroundView) {
            setMapMyLocationEnabled(false);
            beginMapSnapshotProcess();
        }

    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDrawerStateChanged(int state) {
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
