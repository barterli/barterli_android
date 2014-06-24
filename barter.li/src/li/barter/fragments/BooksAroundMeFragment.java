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
import com.google.android.gms.analytics.HitBuilders.EventBuilder;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;

import java.util.HashMap;
import java.util.Map;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity.AlertStyle;
import li.barter.activities.ScanIsbnActivity;
import li.barter.adapters.BooksGridAdapter;
import li.barter.analytics.AnalyticsConstants.Actions;
import li.barter.analytics.AnalyticsConstants.Categories;
import li.barter.analytics.AnalyticsConstants.ParamKeys;
import li.barter.analytics.AnalyticsConstants.ParamValues;
import li.barter.analytics.AnalyticsConstants.Screens;
import li.barter.analytics.GoogleAnalyticsManager;
import li.barter.data.DBInterface;
import li.barter.data.DBInterface.AsyncDbQueryCallback;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLiteLoader;
import li.barter.data.TableSearchBooks;
import li.barter.data.ViewSearchBooksWithLocations;
import li.barter.fragments.dialogs.EnableLocationDialogFragment;
import li.barter.fragments.dialogs.SingleChoiceDialogFragment;
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
import li.barter.utils.LoadMoreHelper;
import li.barter.utils.LoadMoreHelper.LoadMoreCallbacks;
import li.barter.utils.Logger;
import li.barter.utils.SearchViewNetworkQueryHelper;
import li.barter.utils.SearchViewNetworkQueryHelper.NetworkCallbacks;
import li.barter.utils.SharedPreferenceHelper;
import li.barter.utils.Utils;

/**
 * @author Vinay S Shenoy Fragment for displaying Books Around Me. Also contains
 *         a Map that the user can use to easily switch locations
 */
public class BooksAroundMeFragment extends AbstractBarterLiFragment implements
                LoaderCallbacks<Cursor>, AsyncDbQueryCallback,
                OnItemClickListener, LoadMoreCallbacks, NetworkCallbacks,
                OnRefreshListener, OnCloseListener, OnActionExpandListener,
                OnClickListener {

    private static final String          TAG = "BooksAroundMeFragment";

    /**
     * Helper class for performing network search queries from Action Bar easily
     */
    private SearchViewNetworkQueryHelper mSearchNetworkQueryHelper;

    /**
     * GridView into which the book content will be placed
     */
    private GridView                     mBooksAroundMeGridView;

    /**
     * {@link BooksGridAdapter} instance for the Books
     */
    private BooksGridAdapter             mBooksAroundMeAdapter;

    /**
     * Current page used for load more
     */
    private int                          mCurPage;

    /**
     * Used to remember the last location so that we can avoid fetching the
     * books again if the last fetched locations, and current fetched locations
     * are close by
     */
    private Location                     mLastFetchedLocation;

    /**
     * Flag to indicate whether a load operation is in progress
     */
    private boolean                      mIsLoading;

    /**
     * Flag to indicate whether all items have been fetched
     */
    private boolean                      mHasLoadedAllItems;

    /**
     * Reference to the Dialog Fragment for selecting the book add options
     */
    private SingleChoiceDialogFragment   mAddBookDialogFragment;

    /**
     * Action Bar SearchView
     */
    private SearchView                   mSearchView;

    private View                         mEmptyView;
    
    /**
     * Flag to indigate pull to refresh so as to disable mEmptyView set for 
     * the list
     */
    private boolean						 mFromPullToRefresh;

    /**
     * {@link PullToRefreshLayout} reference
     */
    private PullToRefreshLayout          mPullToRefreshLayout;

    private EnableLocationDialogFragment mEnableLocationDialogFragment;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);

        final View contentView = inflater
                        .inflate(R.layout.fragment_books_around_me, container, false);

        setActionBarTitle(R.string.app_name);

        mPullToRefreshLayout = (PullToRefreshLayout) contentView
                        .findViewById(R.id.ptr_layout);

        mBooksAroundMeGridView = (GridView) contentView
                        .findViewById(R.id.grid_books_around_me);

        ActionBarPullToRefresh.from(getActivity()).allChildrenArePullable()
                        .listener(this).setup(mPullToRefreshLayout);
        LoadMoreHelper.init(this).on(mBooksAroundMeGridView);

        mBooksAroundMeAdapter = new BooksGridAdapter(getActivity(), true);
        mBooksAroundMeGridView.setAdapter(mBooksAroundMeAdapter);
        mBooksAroundMeGridView.setOnItemClickListener(this);
        mBooksAroundMeGridView.setVerticalScrollBarEnabled(false);

        mEmptyView = contentView.findViewById(R.id.empty_view);

        mEmptyView.findViewById(R.id.text_try_again).setOnClickListener(this);
        mEmptyView.findViewById(R.id.text_add_your_own)
                        .setOnClickListener(this);
        mEmptyView.findViewById(R.id.image_add_graphic)
                        .setOnClickListener(this);


        mCurPage = SharedPreferenceHelper
                        .getInt(getActivity(), R.string.pref_last_fetched_page, 0);
        if (savedInstanceState != null) {
            mLastFetchedLocation = savedInstanceState
                            .getParcelable(Keys.LAST_FETCHED_LOCATION);
            mHasLoadedAllItems = savedInstanceState
                            .getBoolean(Keys.HAS_LOADED_ALL_ITEMS);
        }
        mAddBookDialogFragment = (SingleChoiceDialogFragment) getFragmentManager()
                        .findFragmentByTag(FragmentTags.DIALOG_ADD_BOOK);

        mEnableLocationDialogFragment = (EnableLocationDialogFragment) getFragmentManager()
                        .findFragmentByTag(FragmentTags.DIALOG_ENABLE_LOCATION);

        loadBookSearchResults();
        setActionBarDrawerToggleEnabled(true);
        return contentView;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Keys.LAST_FETCHED_LOCATION, mLastFetchedLocation);
        outState.putBoolean(Keys.HAS_LOADED_ALL_ITEMS, mHasLoadedAllItems);

    }

    /**
     * Starts the loader for book search results
     */
    private void loadBookSearchResults() {
        getLoaderManager().restartLoader(Loaders.SEARCH_BOOKS, null, this);
    }

    /**
     * Method to fetch books around me from the server centered at a location,
     * and in a search radius
     * 
     * @param center The {@link Location} representing the center
     */
    private void fetchBooksAroundMe(final Location center) {

        if (center != null) {

            mIsLoading = true;
            final BlRequest request = new BlRequest(Method.GET, HttpConstants.getApiBaseUrl()
                            + ApiEndpoints.SEARCH, null, mVolleyCallbacks);
            request.setRequestId(RequestId.SEARCH_BOOKS);

            final Map<String, String> params = new HashMap<String, String>(2);
            params.put(HttpConstants.LATITUDE, String.valueOf(center
                            .getLatitude()));
            params.put(HttpConstants.LONGITUDE, String.valueOf(center
                            .getLongitude()));

            Logger.d(TAG, center.getLatitude() + "  " + center.getLongitude());
            final int pageToFetch = mCurPage + 1;
            params.put(HttpConstants.PAGE, String.valueOf(pageToFetch));
            params.put(HttpConstants.PERLIMIT, String
                            .valueOf(AppConstants.DEFAULT_ITEM_COUNT));
            request.addExtra(Keys.LOCATION, center);

            request.setParams(params);
            addRequestToQueue(request, true, 0, true);
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
    private void fetchBooksAroundMeForSearch(final Location center,
                    final int radius, final String bookname) {

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
                            .valueOf(AppConstants.DEFAULT_ITEM_COUNT));

            request.addExtra(Keys.LOCATION, center);

            if (radius >= 1) {
                params.put(HttpConstants.RADIUS, String.valueOf(radius));
            }
            request.setParams(params);
            addRequestToQueue(request, true, 0, true);
        }

    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_books_around_me, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_search);
        menuItem.setOnActionExpandListener(this);
        mSearchView = (SearchView) menuItem.getActionView();
        mSearchNetworkQueryHelper = new SearchViewNetworkQueryHelper(mSearchView, this);
        mSearchNetworkQueryHelper.setSuggestCountThreshold(3);
        mSearchNetworkQueryHelper.setSuggestWaitThreshold(400);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_add_book: {
                showAddBookOptions();
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    /**
     * Show dialog for adding books
     */
    private void showAddBookOptions() {

        mAddBookDialogFragment = new SingleChoiceDialogFragment();
        mAddBookDialogFragment
                        .show(AlertDialog.THEME_HOLO_LIGHT, R.array.add_book_choices, 0, R.string.add_book_dialog_head, getFragmentManager(), true, FragmentTags.DIALOG_ADD_BOOK);

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
    protected Object getTaskTag() {
        return hashCode();
    }

    public void updateLocation(final Location location) {

        if ((location.getLatitude() == 0.0) && (location.getLongitude() == 0.0)) {
            return;
        }
        fetchBooksOnLocationUpdate(location);

    }

    /**
     * When the location is updated, check to see if books need to be refreshed,
     * and refresh them
     * 
     * @param location The location at which books should be fetched
     */
    private void fetchBooksOnLocationUpdate(Location location) {

        if (shouldRefetchBooks(location)) {

            final Bundle cookie = new Bundle(1);
            cookie.putParcelable(Keys.LOCATION, location);
            //   Delete the current search results before parsing the old ones
            DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_BOOKS_SEARCH_RESULTS, getTaskTag(), cookie, TableSearchBooks.NAME, null, null, true, this);
        } else {
        	mCurPage = SharedPreferenceHelper
                    .getInt(getActivity(), R.string.pref_last_fetched_page, 0);
            mHasLoadedAllItems = false;
            fetchBooksAroundMe(location);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveLastFetchedInfoToPref();
    }

    /**
     * Saves the last fetched info to shared preferences. This will be read
     * again in onResume so as to prevent refetching of the books
     */
    private void saveLastFetchedInfoToPref() {

        SharedPreferenceHelper
                        .set(getActivity(), R.string.pref_last_fetched_page, mCurPage);
        if (mLastFetchedLocation != null) {
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

        //done to force refresh! just change the value accordingly, default value is false
        if (!SharedPreferenceHelper
                        .getBoolean(getActivity(), R.string.pref_dont_refresh_books)) {
            SharedPreferenceHelper
                            .set(getActivity(), R.string.pref_last_fetched_latitude, 0.0);
            SharedPreferenceHelper
                            .set(getActivity(), R.string.pref_last_fetched_longitude, 0.0);

            SharedPreferenceHelper
                            .set(getActivity(), R.string.pref_dont_refresh_books, true);

        }
        if (isLocationServiceEnabled()) {
            readLastFetchedInfoFromPref();
            final Location latestLocation = DeviceInfo.INSTANCE
                            .getLatestLocation();
            if ((latestLocation.getLatitude() != 0.0)
                            && (latestLocation.getLongitude() != 0.0)) {
                updateLocation(latestLocation);
            }

        } else {
            showEnableLocationDialog();
        }

    }

    /**
     * Show the dialog for the user to add his name, in case it's not already
     * added
     */
    protected void showEnableLocationDialog() {

        mEnableLocationDialogFragment = new EnableLocationDialogFragment();
        mEnableLocationDialogFragment
                        .show(AlertDialog.THEME_HOLO_LIGHT, 0, R.string.enable_location, R.string.enable_location_message, R.string.enable, R.string.cancel, 0, getFragmentManager(), true, FragmentTags.DIALOG_ENABLE_LOCATION);

    }

    /**
     * Reads the latest fetched locations from shared preferences
     */
    private void readLastFetchedInfoFromPref() {

        // Don't read from pref if already has fetched
        if (mLastFetchedLocation == null) {
            mLastFetchedLocation = new Location(LocationManager.PASSIVE_PROVIDER);
            Logger.d(TAG, mLastFetchedLocation.getLatitude() + "");
            if (mLastFetchedLocation == null) {
                mLastFetchedLocation = new Location(LocationManager.NETWORK_PROVIDER);
                Logger.d(TAG, mLastFetchedLocation.getLatitude() + "");
            }
            mLastFetchedLocation
                            .setLatitude(SharedPreferenceHelper
                                            .getDouble(getActivity(), R.string.pref_last_fetched_latitude));
            mLastFetchedLocation
                            .setLongitude(SharedPreferenceHelper
                                            .getDouble(getActivity(), R.string.pref_last_fetched_longitude));
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

            mCurPage++;

            if (response.responseBundle.getBoolean(Keys.NO_BOOKS_FLAG_KEY)) {

                mHasLoadedAllItems = true;
                mCurPage--;

            }

            /*
             * Do nothing because the loader will take care of reloading the
             * data
             */
        }
    }

    @Override
    public void onPostExecute(final IBlRequestContract request) {
        super.onPostExecute(request);
        if (request.getRequestId() == RequestId.SEARCH_BOOKS) {
            mIsLoading = false;
            mFromPullToRefresh = false;
            mPullToRefreshLayout.setRefreshComplete();
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
    public void onOtherError(int requestId, IBlRequestContract request,
                    int errorCode) {
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

        	if((cursor.getCount()==0)&&!mFromPullToRefresh)
        	{
                mBooksAroundMeGridView.setEmptyView(mEmptyView);
        	}
        	
        	if(mFromPullToRefresh)
        	{
        		mFromPullToRefresh=false;
        	}
            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            {
                mBooksAroundMeAdapter.swapCursor(cursor);
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
     * Checks if a new set of books should be fetched
     * 
     * @param center The new center point at which the books should be fetched
     * @return <code>true</code> if a new set should be fetched,
     *         <code>false</code> otherwise
     */
    private boolean shouldRefetchBooks(final Location center) {

        if (mLastFetchedLocation != null) {

            final float distanceBetweenCurAndLastFetchedLocations = Utils
                            .distanceBetween(center, mLastFetchedLocation) / 1000;

            /*
             * If there's less than 25 km distance between the current location
             * and the location where we last fetched the books we don't need to
             * fetch the books again since the current set will include
             * those(The server uses 50 km as the search radius)
             */
            if (distanceBetweenCurAndLastFetchedLocations <= 25.0f) {
                Logger.v(TAG, "Points are really close. Don't fetch");
                return false;
            }

        }

        return true;
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {
        if (parent.getId() == R.id.grid_books_around_me) {

            if (position == mBooksAroundMeAdapter.getCount() - 1) {
                showAddBookOptions();
            } else {
                final Cursor cursor = (Cursor) mBooksAroundMeAdapter
                                .getItem(position);

                final String idBook = cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.ID));

                Logger.d(TAG, "ID:" + idBook);

                final String userId = cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.USER_ID));

                final Bundle showBooksArgs = new Bundle();
                showBooksArgs.putString(Keys.ID, idBook);
                showBooksArgs.putString(Keys.USER_ID, userId);
                showBooksArgs.putInt(Keys.BOOK_POSITION, position);
                loadFragment(mContainerViewId, (AbstractBarterLiFragment) Fragment
                                .instantiate(getActivity(), BooksPagerFragment.class
                                                .getName(), showBooksArgs), FragmentTags.BOOKS_AROUND_ME, true, FragmentTags.BS_BOOK_DETAIL);
            }

        }
    }

    @Override
    public void onInsertComplete(final int token, final Object cookie,
                    final long insertRowId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeleteComplete(final int token, final Object cookie,
                    final int deleteCount) {
        if (token == QueryTokens.DELETE_BOOKS_SEARCH_RESULTS) {

            assert (cookie != null);
            DBInterface.notifyChange(TableSearchBooks.NAME);
            mCurPage = 0;
            mHasLoadedAllItems = false;
            final Bundle args = (Bundle) cookie;
            fetchBooksAroundMe((Location) args.getParcelable(Keys.LOCATION));
        }
        if (token == QueryTokens.DELETE_BOOKS_SEARCH_RESULTS_FROM_EDITTEXT) {

            assert (cookie != null);

            DBInterface.notifyChange(TableSearchBooks.NAME);
            final Bundle args = (Bundle) cookie;
            fetchBooksAroundMeForSearch((Location) args.getParcelable(Keys.LOCATION), 50, args
                            .getString(Keys.SEARCH));
        }

    }

    @Override
    public void onUpdateComplete(final int token, final Object cookie,
                    final int updateCount) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onQueryComplete(final int token, final Object cookie,
                    final Cursor cursor) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onLoadMore() {
        fetchBooksAroundMe(mLastFetchedLocation);
    }

    @Override
    public boolean isLoading() {
        return mIsLoading;
    }

    @Override
    public boolean hasLoadedAllItems() {
        return mHasLoadedAllItems;
    }

    @Override
    public boolean willHandleDialog(final DialogInterface dialog) {

        if ((mAddBookDialogFragment != null)
                        && mAddBookDialogFragment.getDialog().equals(dialog)) {
            return true;
        } else if ((mEnableLocationDialogFragment != null)
                        && mEnableLocationDialogFragment.getDialog()
                                        .equals(dialog)) {
            return true;
        }
        return super.willHandleDialog(dialog);
    }

    @Override
    public void onDialogClick(final DialogInterface dialog, final int which) {

        if ((mAddBookDialogFragment != null)
                        && mAddBookDialogFragment.getDialog().equals(dialog)) {

            if (which == 0) { // scan book
                startActivityForResult(new Intent(getActivity(), ScanIsbnActivity.class), RequestCodes.SCAN_ISBN);
                GoogleAnalyticsManager
                                .getInstance()
                                .sendEvent(new EventBuilder(Categories.USAGE, Actions.ADD_BOOK)
                                                .set(ParamKeys.TYPE, ParamValues.SCAN));

            } else if (which == 1) { // add book manually
                loadAddOrEditBookFragment(null);
                GoogleAnalyticsManager
                                .getInstance()
                                .sendEvent(new EventBuilder(Categories.USAGE, Actions.ADD_BOOK)
                                                .set(ParamKeys.TYPE, ParamValues.MANUAL));
            }
        } else if ((mEnableLocationDialogFragment != null)
                        && mEnableLocationDialogFragment.getDialog()
                                        .equals(dialog)) {

            if (which == DialogInterface.BUTTON_POSITIVE) { // enable location
                Intent locationOptionsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(locationOptionsIntent);

            } else if (which == DialogInterface.BUTTON_NEGATIVE) { // cancel
                dialog.cancel();
            }
        } else {
            super.onDialogClick(dialog, which);
        }
    }

    @Override
    public void performQuery(SearchView searchView, String query) {
        final Bundle cookie = new Bundle(2);
        cookie.putParcelable(Keys.LOCATION, mLastFetchedLocation);
        cookie.putString(Keys.SEARCH, query);
        //   Delete the current search results before parsing the old ones
        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_BOOKS_SEARCH_RESULTS_FROM_EDITTEXT, getTaskTag(), cookie, TableSearchBooks.NAME, null, null, false, BooksAroundMeFragment.this);

    }

    @Override
    public void onRefreshStarted(View view) {

        if (view.getId() == R.id.grid_books_around_me) {
        	mFromPullToRefresh=true;
            reloadNearbyBooks();
        }
    }

    /**
     * Reloads nearby books
     */
    private void reloadNearbyBooks() {
    	
    	
        mSearchView.setQuery(null, false);
        final Bundle cookie = new Bundle(2);
        cookie.putParcelable(Keys.LOCATION, mLastFetchedLocation);
        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_BOOKS_SEARCH_RESULTS, getTaskTag(), cookie, TableSearchBooks.NAME, null, null, false, this);

    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        reloadNearbyBooks();
        return true;
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        return true;
    }

    @Override
    public boolean onClose() {
        return false;
    }

    @Override
    protected String getAnalyticsScreenName() {

        return Screens.BOOKS_AROUND_ME;
    }

    @Override
    public void onClick(View v) {

        final int id = v.getId();

        switch (id) {
            case R.id.text_try_again: {
                reloadNearbyBooks();
                break;
            }

            case R.id.image_add_graphic:
            case R.id.text_add_your_own: {
                showAddBookOptions();
                break;
            }
        }
    }

}
