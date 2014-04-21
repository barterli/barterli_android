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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.haarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;

import java.util.HashMap;
import java.util.Map;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity.AlertStyle;
import li.barter.activities.ScanIsbnActivity;
import li.barter.adapters.BooksAroundMeAdapter;
import li.barter.data.DBInterface;
import li.barter.data.DBInterface.AsyncDbQueryCallback;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLiteLoader;
import li.barter.data.TableSearchBooks;
import li.barter.data.ViewSearchBooksWithLocations;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.DeviceInfo;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.Loaders;
import li.barter.utils.AppConstants.QueryTokens;
import li.barter.utils.AppConstants.RequestCodes;
import li.barter.utils.AppConstants.ResultCodes;
import li.barter.utils.Logger;
import li.barter.utils.MapDrawerInteractionHelper;
import li.barter.utils.SharedPreferenceHelper;
import li.barter.utils.Utils;
import li.barter.widgets.FullWidthDrawerLayout;

/**
 * @author Vinay S Shenoy Fragment for displaying Books Around Me. Also contains
 *         a Map that the user can use to easily switch locations
 */
public class BooksAroundMeFragment extends AbstractBarterLiFragment implements

LoaderCallbacks<Cursor>, DrawerListener, AsyncDbQueryCallback,
                OnItemClickListener, OnScrollListener,TextWatcher {

    private static final String           TAG            = "BooksAroundMeFragment";

    /**
     * Zoom level for the map when the location is retrieved
     */
    private static final float            MAP_ZOOM_LEVEL = 15;

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

    /**
     * Default page count value which is incremented on scrolling
     * {@link GridView}
     */
    private int                           mPageCount      = 1;

    /**
     * Flag to stop onScroll method to call when fragment loads
     */
    private boolean                       mUserScrolled   = false;

    /**
     * Holds the value of the previous search radius to prevent querying for
     * books from server again
     */
    private int                           mPrevSearchRadius;

    /**
     * Used to remember the last location so that we can avoid fetching the
     * books again if the last fetched locations, and current fetched locations
     * are close by
     */
    private Location                      mLastFetchedLocation;
    
    /**
     * This string holds the search bar text for books
     */
    private String 						  mBookName;

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
        if (savedInstanceState == null) {
            MapsInitializer.initialize(getActivity());
        }

        mMapView = (MapView) contentView.findViewById(R.id.map_books_around_me);
        mMapView.onCreate(savedInstanceState);
        mDrawerLayout = (FullWidthDrawerLayout) contentView
                        .findViewById(R.id.drawer_layout);
        mDrawerLayout.setScrimColor(Color.TRANSPARENT);

        mBooksDrawerView = contentView
                        .findViewById(R.id.layout_books_container);
        mBooksAroundMeAutoCompleteTextView = (AutoCompleteTextView) contentView
                        .findViewById(R.id.auto_complete_books_around_me);
      
      mBooksAroundMeAutoCompleteTextView.addTextChangedListener(this);
        mBooksAroundMeGridView = (GridView) contentView
                        .findViewById(R.id.grid_books_around_me);

        mMapDrawerBlurHelper = new MapDrawerInteractionHelper(getActivity(), mDrawerLayout, mBooksDrawerView, mMapView);
        mMapDrawerBlurHelper.init(this);

        mBooksAroundMeGridView.setOnScrollListener(this);

        mBooksAroundMeAdapter = new BooksAroundMeAdapter(getActivity());
        mSwingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(mBooksAroundMeAdapter, 150, 500);
        mSwingBottomInAnimationAdapter.setAbsListView(mBooksAroundMeGridView);
        mBooksAroundMeGridView.setAdapter(mSwingBottomInAnimationAdapter);
        mBooksAroundMeGridView.setOnItemClickListener(this);

        if (savedInstanceState == null) {
            mDrawerOpenedAutomatically = false;
            mMapAlreadyMovedOnce = false;

        } else {
            mDrawerOpenedAutomatically = savedInstanceState
                            .getBoolean(Keys.DRAWER_OPENED_ONCE);
            mMapAlreadyMovedOnce = savedInstanceState
                            .getBoolean(Keys.MAP_MOVED_ONCE);
            mLastFetchedLocation = savedInstanceState
                            .getParcelable(Keys.LAST_FETCHED_LOCATION);
            mPrevSearchRadius = savedInstanceState
                            .getInt(Keys.LAST_FETCHED_SEARCH_RADIUS);
            mPageCount=savedInstanceState
            		.getInt(Keys.LAST_FETCHED_PAGENUMBER);
        }

        loadBookSearchResults();
        setActionBarDrawerToggleEnabled(true);
        return contentView;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // userScrolled is set to true in order to prevent auto scrolling on page load
        if (scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL
                        || scrollState == OnScrollListener.SCROLL_STATE_FLING) {
            mUserScrolled = true;
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {

        //TODO
        boolean loadMore = /* maybe add a padding */
        firstVisibleItem + visibleItemCount >= totalItemCount
                        - AppConstants.DEFAULT_LOAD_BEFORE_COUNT;
        Logger.d(TAG, "visible count: %d", visibleItemCount);

        if (loadMore && mUserScrolled) {
            mPageCount++;
            loadMore = false;
            mUserScrolled = false;
            fetchBooksAroundMe(Utils.getCenterLocationOfMap(getMap()), (int) (Utils
                            .getShortestRadiusFromCenter(mMapView) / 1000));

        }

    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Keys.DRAWER_OPENED_ONCE, mDrawerOpenedAutomatically);
        outState.putBoolean(Keys.MAP_MOVED_ONCE, mMapAlreadyMovedOnce);
        outState.putParcelable(Keys.LAST_FETCHED_LOCATION, mLastFetchedLocation);
        outState.putInt(Keys.LAST_FETCHED_SEARCH_RADIUS, mPrevSearchRadius);
        outState.putInt(Keys.LAST_FETCHED_PAGENUMBER, mPageCount);
        
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

        if (center != null) {

            final BlRequest request = new BlRequest(Method.GET, HttpConstants.getApiBaseUrl()
                            + ApiEndpoints.SEARCH, null, mVolleyCallbacks);
            request.setRequestId(RequestId.SEARCH_BOOKS);

            final Map<String, String> params = new HashMap<String, String>(2);
            params.put(HttpConstants.LATITUDE, String.valueOf(center
                            .getLatitude()));
            params.put(HttpConstants.LONGITUDE, String.valueOf(center
                            .getLongitude()));
            params.put(HttpConstants.PAGE, String.valueOf(mPageCount));
            if (mPageCount == 1) {
                params.put(HttpConstants.PERLIMIT, String
                                .valueOf(AppConstants.DEFAULT_PERPAGE_LIMIT));
            } else {
                params.put(HttpConstants.PERLIMIT, String
                                .valueOf(AppConstants.DEFAULT_PERPAGE_LIMIT_ONSCROLL));
            }
            request.addExtra(Keys.LOCATION, center);
            request.addExtra(Keys.SEARCH_RADIUS, radius);

            if (radius >= 1) {
                params.put(HttpConstants.RADIUS, String.valueOf(radius));
            }
            request.setParams(params);
            addRequestToQueue(request, true, 0);
        }

    }
    
    /**
     * Method to fetch books around me from the server centered at a location,
     * and in a search radius and the search field
     * 
     * @param center The {@link Location} representing the center
     * @param radius The radius(in kilometers) to search in
     * @param bookname The book name to search for
     */
    private void fetchBooksAroundMeForSearch(final Location center, final int radius,String bookname) {

        if (center != null) {

            final BlRequest request = new BlRequest(Method.GET, HttpConstants.getApiBaseUrl()
                            + ApiEndpoints.SEARCH, null, mVolleyCallbacks);
            request.setRequestId(RequestId.SEARCH_BOOKS);

            final Map<String, String> params = new HashMap<String, String>(2);
            params.put(HttpConstants.LATITUDE, String.valueOf(center
                            .getLatitude()));
            params.put(HttpConstants.LONGITUDE, String.valueOf(center
                            .getLongitude()));
            params.put(HttpConstants.SEARCH, bookname);
                params.put(HttpConstants.PERLIMIT, String
                                .valueOf(AppConstants.DEFAULT_PERPAGE_LIMIT_FOR_SEARCH));
            
            request.addExtra(Keys.LOCATION, center);
            request.addExtra(Keys.SEARCH_RADIUS, radius);
         

            if (radius >= 1) {
                params.put(HttpConstants.RADIUS, String.valueOf(radius));
            }
            request.setParams(params);
            addRequestToQueue(request, true, 0);
        }

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
            case R.id.send_email: {
                Utils.emailDatabase(getActivity());
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

    public void updateLocation(final Location location) {

        if ((location.getLatitude() == 0.0) && (location.getLongitude() == 0.0)) {
            return;
        }
        if (!mMapAlreadyMovedOnce) {

            /*
             * For the initial launch, move the Map to the user's current
             * position as soon as the location is fetched
             */
            final GoogleMap googleMap = getMap();

            if (googleMap != null) {
                googleMap.setMyLocationEnabled(false);

                /*
                 * For Simple Effect
                 */
                googleMap.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(new LatLng(DeviceInfo.INSTANCE
                                                .getLatestLocation()
                                                .getLatitude(), DeviceInfo.INSTANCE
                                                .getLatestLocation()
                                                .getLongitude()), MAP_ZOOM_LEVEL));
                /*
                 * As there was no callback option in moveCamera , so i used
                 * piece of code here for updating the results when location
                 * changes
                 */
                fetchBooksOnLocationUpdate();
            }

        }
    }

    /**
     * When the location is updated, check to see if books need to be refreshed,
     * and refresh them
     */
    private void fetchBooksOnLocationUpdate() {

        final Location center = Utils.getCenterLocationOfMap(getMap());
        final int searchRadius = Math.round(Utils
                        .getShortestRadiusFromCenter(mMapView) / 1000);

        if (searchRadius >= 25) {
            return;
        }

        mMapAlreadyMovedOnce = true;

        if (shouldRefetchBooks(center, searchRadius)) {

            final Bundle cookie = new Bundle(2);
            cookie.putParcelable(Keys.LOCATION, center);
            cookie.putInt(Keys.SEARCH_RADIUS, searchRadius);
            //   Delete the current search results before parsing the old ones
            DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_BOOKS_SEARCH_RESULTS, cookie, TableSearchBooks.NAME, null, null, true, this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveLastFetchedInfoToPref();
        mMapView.onPause();
        mMapDrawerBlurHelper.onPause();
    }

    /**
     * Saves the last fetched info to shared preferences. This will be read
     * again in onResume so as to prevent refetching of the books
     */
    private void saveLastFetchedInfoToPref() {

        if ((mPrevSearchRadius > 0) && (mLastFetchedLocation != null)) {
            SharedPreferenceHelper
                            .set(getActivity(), R.string.pref_last_search_radius, mPrevSearchRadius);
            SharedPreferenceHelper
                            .set(getActivity(), R.string.pref_last_fetched_latitude, mLastFetchedLocation
                                            .getLatitude());
            SharedPreferenceHelper
                            .set(getActivity(), R.string.pref_last_fetched_longitude, mLastFetchedLocation
                                            .getLongitude());

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        readLastFetchedInfoFromPref();
        mMapView.onResume();
        mMapDrawerBlurHelper.onResume();
        final Location latestLocation = DeviceInfo.INSTANCE.getLatestLocation();
        if ((latestLocation.getLatitude() != 0.0)
                        && (latestLocation.getLongitude() != 0.0)) {
            updateLocation(latestLocation);
        }
    }

    /**
     * Reads the latest fetched locations from shared preferences
     */
    private void readLastFetchedInfoFromPref() {

        // Don't read from pref if already has fetched
        if ((mPrevSearchRadius == 0) && (mLastFetchedLocation == null)) {
            mPrevSearchRadius = SharedPreferenceHelper
                            .getInt(getActivity(), R.string.pref_last_search_radius);
            mLastFetchedLocation = new Location(LocationManager.PASSIVE_PROVIDER);
            mLastFetchedLocation
                            .setLatitude(SharedPreferenceHelper
                                            .getDouble(getActivity(), R.string.pref_last_fetched_latitude));
            mLastFetchedLocation
                            .setLongitude(SharedPreferenceHelper
                                            .getDouble(getActivity(), R.string.pref_last_fetched_longitude));
        }

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
    public void onSuccess(final int requestId,
                    final IBlRequestContract request,
                    final ResponseInfo response) {

        if (requestId == RequestId.SEARCH_BOOKS) {

            mLastFetchedLocation = (Location) request.getExtras()
                            .get(Keys.LOCATION);
            mPrevSearchRadius = (Integer) request.getExtras()
                            .get(Keys.SEARCH_RADIUS);
       
        
        
            /*
             * Do nothing because the loader will take care of reloading the
             * data
             */
        }
        
       
    }

    @Override
    public void onBadRequestError(final int requestId,
                    final IBlRequestContract request, final int errorCode,
                    final String errorMessage, final Bundle errorResponseBundle) {
        if (requestId == RequestId.SEARCH_BOOKS) {
            showCrouton(R.string.unable_to_fetch_books, AlertStyle.ERROR);
        }
        if (requestId == RequestId.SEARCH_BOOKS_FROM_EDITTEXT) {
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

            {
                mBooksAroundMeAdapter.swapCursor(cursor);
            }
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
    	
    	//it makes the keyboard hide when drawer closed
    	InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
  		      Context.INPUT_METHOD_SERVICE);
  		imm.hideSoftInputFromWindow(mBooksAroundMeAutoCompleteTextView.getWindowToken(), 0);
    }

    @Override
    public void onDrawerOpened(final View drawerView) {
    
        if (drawerView == mBooksDrawerView) {
            final int searchRadius = Math.round(Utils
                            .getShortestRadiusFromCenter(mMapView) / 1000);
            final Location center = Utils.getCenterLocationOfMap(getMap());

            if (shouldRefetchBooks(center, searchRadius)) {

                final Bundle cookie = new Bundle(2);
                cookie.putParcelable(Keys.LOCATION, center);
                cookie.putInt(Keys.SEARCH_RADIUS, searchRadius);
                //   Delete the current search results before parsing the old ones
                DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_BOOKS_SEARCH_RESULTS, cookie, TableSearchBooks.NAME, null, null, true, this);
            }

        }
    }

    /**
     * Checks if a new set of books should be fetched
     * 
     * @param center The new center point at which the books should be fetched
     * @param searchRadius The new search radius for which the books are being
     *            fetched
     * @return <code>true</code> if a new set should be fetched,
     *         <code>false</code> otherwise
     */
    private boolean shouldRefetchBooks(final Location center,
                    final int searchRadius) {

        if (mLastFetchedLocation != null) {

            final float distanceBetweenCurAndLastFetchedLocations = Utils
                            .distanceBetween(center, mLastFetchedLocation) / 1000;

            /*
             * If there's less than 1 km distance between the current location
             * and the location where we last fetched the books AND the search
             * radius is lesser than the older search radius, we don't need to
             * fetch the books again since the current set will include those
             */
            if ((distanceBetweenCurAndLastFetchedLocations <= 1.0f)
                            && (searchRadius <= mPrevSearchRadius)) {
                Logger.v(TAG, "Points are really close. Don't fetch");
                return false;
            }

        }

        return true;
    }

    @Override
    public void onDrawerSlide(final View drawerView, final float slideOffset) {

    }

    @Override
    public void onDrawerStateChanged(final int state) {

    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {
        if (parent.getId() == R.id.grid_books_around_me) {

            final Cursor cursor = (Cursor) mBooksAroundMeAdapter
                            .getItem(position);

            final String bookId = cursor.getString(cursor
                            .getColumnIndex(DatabaseColumns.BOOK_ID));
            final String userId = cursor.getString(cursor
                            .getColumnIndex(DatabaseColumns.USER_ID));

            final Bundle showBooksArgs = new Bundle();
            showBooksArgs.putString(Keys.BOOK_ID, bookId);
            showBooksArgs.putString(Keys.USER_ID, userId);

            loadFragment(mContainerViewId, (AbstractBarterLiFragment) Fragment
                            .instantiate(getActivity(), BookDetailFragment.class
                                            .getName(), showBooksArgs), FragmentTags.BOOK_FROM_BOOKS_AROUND_ME, true, null);
        }
    }

    @Override
    public void onInsertComplete(int token, Object cookie, long insertRowId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeleteComplete(int token, Object cookie, int deleteCount) {
        if (token == QueryTokens.DELETE_BOOKS_SEARCH_RESULTS) {

            assert (cookie != null);

            mPageCount = 1;
            final Bundle args = (Bundle) cookie;
            fetchBooksAroundMe((Location) args.getParcelable(Keys.LOCATION), args
                            .getInt(Keys.SEARCH_RADIUS));
        }
        if (token == QueryTokens.DELETE_BOOKS_SEARCH_RESULTS_FROM_EDITTEXT) {

            assert (cookie != null);

            final Bundle args = (Bundle) cookie;
            fetchBooksAroundMeForSearch((Location) args.getParcelable(Keys.LOCATION), args
                            .getInt(Keys.SEARCH_RADIUS),mBookName);
        }
        
        
       
		

    }

    @Override
    public void onUpdateComplete(int token, Object cookie, int updateCount) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        // TODO Auto-generated method stub

    }

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub
		
	}

	//Implemented for search bar..
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		
		
		
		final Bundle cookie = new Bundle(2);
        cookie.putParcelable(Keys.LOCATION, mLastFetchedLocation);
        cookie.putInt(Keys.SEARCH_RADIUS, mPrevSearchRadius);
        //   Delete the current search results before parsing the old ones
        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_BOOKS_SEARCH_RESULTS_FROM_EDITTEXT, cookie, TableSearchBooks.NAME, null, null, true, BooksAroundMeFragment.this);
		 
        mBookName=mBooksAroundMeAutoCompleteTextView.getText().toString();
		
	}

	@Override
	public void afterTextChanged(Editable s) {
		// TODO Auto-generated method stub
		
	}

}
