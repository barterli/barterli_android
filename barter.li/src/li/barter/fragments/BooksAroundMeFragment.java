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

import com.android.volley.Request.Method;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.haarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;

import java.util.HashMap;
import java.util.Map;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity;
import li.barter.activities.AbstractBarterLiActivity.AlertStyle;
import li.barter.activities.ScanIsbnActivity;
import li.barter.adapters.BooksAroundMeAdapter;
import li.barter.data.SQLiteLoader;
import li.barter.data.ViewSearchBooksWithLocations;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants.DeviceInfo;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.Loaders;
import li.barter.utils.AppConstants.RequestCodes;
import li.barter.utils.AppConstants.ResultCodes;
import li.barter.utils.GooglePlayClientWrapper;
import li.barter.utils.Logger;
import li.barter.utils.MapDrawerInteractionHelper;
import li.barter.utils.Utils;
import li.barter.widgets.FullWidthDrawerLayout;

/**
 * @author Vinay S Shenoy Fragment for displaying Books Around Me. Also contains
 *         a Map that the user can use to easily switch locations
 */
public class BooksAroundMeFragment extends AbstractBarterLiFragment implements
                LocationListener, LoaderCallbacks<Cursor>, CancelableCallback,
                DrawerListener {

    private static final String           TAG            = "BooksAroundMeFragment";

    /**
     * Zoom level for the map when the location is retrieved
     */
    private static final float            MAP_ZOOM_LEVEL = 15;

    /**
     * Helper for connecting to Google Play Services
     */
    private GooglePlayClientWrapper       mGooglePlayClientWrapper;

    /**
     * {@link MapView} used to display the Map
     */
    private MapView                       mMapView;

    /**
     * Drawer View on which the blurred Map snapshot is set
     */
    private View                          mBooksDrawerView;

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
     * Drawer Layout that contains the Books grid and autocomplete search text
     */
    private FullWidthDrawerLayout         mDrawerLayout;

    /**
     * Flag that remembers whether the drawer has been opened at least once
     * automatically
     */
    private boolean                       mDrawerOpenedAutomatically;

    /**
     * Flag to indicate whether the Map has already been moved at least once to
     * the user position
     */
    private boolean                       mMapAlreadyMovedOnce;

    /**
     * Class to manage the transitions for drawer events the map behind it
     */
    private MapDrawerInteractionHelper    mMapDrawerBlurHelper;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container);
        setHasOptionsMenu(true);

        final View contentView = inflater
                        .inflate(R.layout.fragment_books_around_me, container, false);

        /*
         * The Google Maps V2 API states that when using MapView, we need to
         * forward the onCreate(Bundle) method to the MapView, but since we are
         * in a fragment, the onCreateView() gets called AFTER the
         * onCreate(Bundle) method, which makes forwarding that method
         * impossible. This is the workaround for that
         */
        MapsInitializer.initialize(getActivity());
        mMapView = (MapView) contentView.findViewById(R.id.map_books_around_me);
        mMapView.onCreate(savedInstanceState);
        mDrawerLayout = (FullWidthDrawerLayout) contentView
                        .findViewById(R.id.drawer_layout);
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);

        mBooksDrawerView = contentView
                        .findViewById(R.id.layout_books_container);
        mBooksAroundMeAutoCompleteTextView = (AutoCompleteTextView) contentView
                        .findViewById(R.id.auto_complete_books_around_me);
        mBooksAroundMeGridView = (GridView) contentView
                        .findViewById(R.id.grid_books_around_me);

        mMapDrawerBlurHelper = new MapDrawerInteractionHelper(getActivity(), mDrawerLayout, mBooksDrawerView, mMapView);
        mMapDrawerBlurHelper.init(this);

        mBooksAroundMeAdapter = new BooksAroundMeAdapter(getActivity());
        mSwingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(mBooksAroundMeAdapter, 150, 500);
        mSwingBottomInAnimationAdapter.setAbsListView(mBooksAroundMeGridView);
        mBooksAroundMeGridView.setAdapter(mSwingBottomInAnimationAdapter);

        mGooglePlayClientWrapper = new GooglePlayClientWrapper((AbstractBarterLiActivity) getActivity(), this);

        if (savedInstanceState == null) {
            mDrawerOpenedAutomatically = false;
            mMapAlreadyMovedOnce = false;
            fetchBooksAroundMe(DeviceInfo.INSTANCE.getLatestLocation(), 1);
        } else {
            mDrawerOpenedAutomatically = savedInstanceState
                            .getBoolean(Keys.DRAWER_OPENED_ONCE);
            mMapAlreadyMovedOnce = savedInstanceState
                            .getBoolean(Keys.MAP_MOVED_ONCE);
        }

        loadBookSearchResults();
        setActionBarDrawerToggleEnabled(true);
        return contentView;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Keys.DRAWER_OPENED_ONCE, mDrawerOpenedAutomatically);
        outState.putBoolean(Keys.MAP_MOVED_ONCE, mMapAlreadyMovedOnce);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onViewStateRestored(final Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        mMapDrawerBlurHelper.onRestoreState();
    }

    /**
     * Starts the loader for book search results
     */
    private void loadBookSearchResults() {
        //TODO Add filters for search results
        getLoaderManager().restartLoader(Loaders.SEARCH_BOOKS, null, this);
    }

    /**
     * Method to fetch books around me from the server centered at a location,
     * and in a search radius
     * 
     * @param center The {@link Location} representing the center
     * @param radius The radius(in kilometers) to search in
     */
    private void fetchBooksAroundMe(final Location center, final int radius) {

        final BlRequest request = new BlRequest(Method.GET, HttpConstants.getApiBaseUrl()
                        + ApiEndpoints.SEARCH, null, mVolleyCallbacks);
        request.setRequestId(RequestId.SEARCH_BOOKS);

        if (center != null) {
            final Map<String, String> params = new HashMap<String, String>(2);
            params.put(HttpConstants.LATITUDE, String.valueOf(center
                            .getLatitude()));
            params.put(HttpConstants.LONGITUDE, String.valueOf(center
                            .getLongitude()));

            if (radius >= 1) {
                params.put(HttpConstants.RADIUS, String.valueOf(radius));
            }
            request.setParams(params);
        }
        addRequestToQueue(request, true, 0);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_books_around_me, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_scan_book: {
                startActivityForResult(new Intent(getActivity(), ScanIsbnActivity.class), RequestCodes.SCAN_ISBN);
                return true;
            }

            case R.id.action_add_book: {
                loadAddOrEditBookFragment(null);
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                    final Intent data) {
        if ((requestCode == RequestCodes.SCAN_ISBN)
                        && (resultCode == ResultCodes.SUCCESS)) {

            Bundle args = null;
            if (data != null) {
                args = data.getExtras();
            }

            loadAddOrEditBookFragment(args);

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    @Override
    public void onStart() {
        super.onStart();
        mGooglePlayClientWrapper.onStart();
    }

    @Override
    public void onStop() {
        mGooglePlayClientWrapper.onStop();
        super.onStop();
    }

    @Override
    public void onLocationChanged(final Location location) {

        DeviceInfo.INSTANCE.setLatestLocation(location);

        if (!mMapAlreadyMovedOnce) {

            /*
             * For the initial launch, move the Map to the user's current
             * position as soon as the location is fetched
             */
            final GoogleMap googleMap = getMap();

            if (googleMap != null) {
                googleMap.setMyLocationEnabled(false);
                googleMap.animateCamera(CameraUpdateFactory
                                .newLatLngZoom(new LatLng(DeviceInfo.INSTANCE
                                                .getLatestLocation()
                                                .getLatitude(), DeviceInfo.INSTANCE
                                                .getLatestLocation()
                                                .getLongitude()), MAP_ZOOM_LEVEL), this);

            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        mMapDrawerBlurHelper.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null) {
            mMapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onFinish() {
        if (!mMapAlreadyMovedOnce) {
            mMapAlreadyMovedOnce = true;
            final int searchRadius = Math.round(Utils
                            .getShortestRadiusFromCenter(mMapView) / 1000);
            fetchBooksAroundMe(DeviceInfo.INSTANCE.getLatestLocation(), searchRadius);
        }
        mMapDrawerBlurHelper.onMapZoomedIn();
    }

    /**
     * Loads the Fragment to Add Or Edit Books
     * 
     * @param bookInfo The book info to load. Can be <code>null</code>, in which
     *            case, it treats it as Add a new book flow
     */
    private void loadAddOrEditBookFragment(final Bundle bookInfo) {

        loadFragment(mContainerViewId, (AbstractBarterLiFragment) Fragment
                        .instantiate(getActivity(), AddOrEditBookFragment.class
                                        .getName(), bookInfo), FragmentTags.ADD_OR_EDIT_BOOK, true, FragmentTags.BS_BOOKS_AROUND_ME);
    }

    @Override
    public void onSuccess(int requestId, IBlRequestContract request,
                    ResponseInfo response) {

        if (requestId == RequestId.SEARCH_BOOKS) {
            /*
             * Do nothing because the loader will take care of reloading the
             * data
             */
        }
    }

    @Override
    public void onBadRequestError(int requestId, IBlRequestContract request,
                    int errorCode, String errorMessage,
                    Bundle errorResponseBundle) {
        if (requestId == RequestId.SEARCH_BOOKS) {
            showCrouton(R.string.unable_to_fetch_books, AlertStyle.ERROR);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle args) {

        if (loaderId == Loaders.SEARCH_BOOKS) {
            return new SQLiteLoader(getActivity(), false, ViewSearchBooksWithLocations.NAME, null, null, null, null, null, null, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {

        if (loader.getId() == Loaders.SEARCH_BOOKS) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            mBooksAroundMeAdapter.swapCursor(cursor);
            if (!mDrawerOpenedAutomatically) {
                // Open drawer automatically on map loaded if first launch
                mDrawerOpenedAutomatically = true;
                mDrawerLayout.openDrawer(mBooksDrawerView);
            }
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        if (loader.getId() == Loaders.SEARCH_BOOKS) {
            mBooksAroundMeAdapter.swapCursor(null);
        }
    }

    /**
     * Gets a reference of the Google Map
     */
    private GoogleMap getMap() {

        return mMapView.getMap();
    }

    @Override
    public void onDrawerClosed(final View drawerView) {

    }

    @Override
    public void onDrawerOpened(final View drawerView) {

        if (drawerView == mBooksDrawerView) {
            final int searchRadius = Math.round(Utils
                            .getShortestRadiusFromCenter(mMapView) / 1000);
            fetchBooksAroundMe(Utils.getCenterLocationOfMap(getMap()), searchRadius);
        }
    }

    @Override
    public void onDrawerSlide(final View drawerView, final float slideOffset) {

    }

    @Override
    public void onDrawerStateChanged(final int state) {

    }

}
