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
import com.haarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import li.barter.utils.SharedPreferenceHelper;
import li.barter.utils.Utils;

/**
 * @author Vinay S Shenoy Fragment for displaying Books Around Me. Also contains
 *         a Map that the user can use to easily switch locations
 */
public class BooksAroundMeFragment extends AbstractBarterLiFragment implements
                LoaderCallbacks<Cursor>, AsyncDbQueryCallback,
                OnItemClickListener, TextWatcher, LoadMoreCallbacks {

    private static final String           TAG                     = "BooksAroundMeFragment";

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
     * Current page used for load more
     */
    private int                           mCurPage;

    /**
     * Flag to Display Crouton Message on empty book search result
     */
    private boolean                       mEmptySearchCroutonFlag = true;

    /**
     * Used to remember the last location so that we can avoid fetching the
     * books again if the last fetched locations, and current fetched locations
     * are close by
     */
    private Location                      mLastFetchedLocation;

    /**
     * This string holds the search bar text for books
     */
    private String                        mBookName;

    /**
     * Flag to indicate whether a load operation is in progress
     */
    private boolean                       mIsLoading;

    /**
     * Flag to indicate whether all items have been fetched
     */
    private boolean                       mHasLoadedAllItems;

    /**
     * Reference to the Dialog Fragment for selecting the book add options
     */
    private SingleChoiceDialogFragment    mAddBookDialogFragment;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);

        final View contentView = inflater
                        .inflate(R.layout.fragment_books_around_me, container, false);

        setActionBarTitle(R.string.app_name);
        mBooksAroundMeAutoCompleteTextView = (AutoCompleteTextView) contentView
                        .findViewById(R.id.auto_complete_books_around_me);

        mBooksAroundMeGridView = (GridView) contentView
                        .findViewById(R.id.grid_books_around_me);

        LoadMoreHelper.init(this).on(mBooksAroundMeGridView);

        mBooksAroundMeAdapter = new BooksAroundMeAdapter(getActivity());
        mSwingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(mBooksAroundMeAdapter, 150, 500);
        mSwingBottomInAnimationAdapter.setAbsListView(mBooksAroundMeGridView);
        mBooksAroundMeGridView.setAdapter(mSwingBottomInAnimationAdapter);
        mBooksAroundMeGridView.setOnItemClickListener(this);

        if (savedInstanceState == null) {
            mCurPage = 0;

        } else {
            mLastFetchedLocation = savedInstanceState
                            .getParcelable(Keys.LAST_FETCHED_LOCATION);
            mCurPage = savedInstanceState.getInt(Keys.CUR_PAGE);
            mHasLoadedAllItems = savedInstanceState
                            .getBoolean(Keys.HAS_LOADED_ALL_ITEMS);
        }
        mAddBookDialogFragment = (SingleChoiceDialogFragment) getFragmentManager()
                        .findFragmentByTag(FragmentTags.DIALOG_ADD_BOOK);

        loadBookSearchResults();
        setActionBarDrawerToggleEnabled(true);
        return contentView;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Keys.LAST_FETCHED_LOCATION, mLastFetchedLocation);
        outState.putInt(Keys.CUR_PAGE, mCurPage);
        outState.putBoolean(Keys.HAS_LOADED_ALL_ITEMS, mHasLoadedAllItems);

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
            final int pageToFetch = mCurPage + 1;
            params.put(HttpConstants.PAGE, String.valueOf(pageToFetch));
            if (pageToFetch == 1) {
                params.put(HttpConstants.PERLIMIT, String
                                .valueOf(AppConstants.DEFAULT_PERPAGE_LIMIT));
            } else {
                params.put(HttpConstants.PERLIMIT, String
                                .valueOf(AppConstants.DEFAULT_PERPAGE_LIMIT_ONSCROLL));
            }
            request.addExtra(Keys.LOCATION, center);

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
                            .valueOf(AppConstants.DEFAULT_PERPAGE_LIMIT_FOR_SEARCH));

            request.addExtra(Keys.LOCATION, center);

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

            case R.id.action_refresh_books: {

                final Bundle cookie = new Bundle(2);
                cookie.putParcelable(Keys.LOCATION, mLastFetchedLocation);
                mEmptySearchCroutonFlag = false;
                DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_BOOKS_SEARCH_RESULTS, cookie, TableSearchBooks.NAME, null, null, true, this);
                return true;
            }

            case R.id.action_add_book: {

                showAddBookOptions();

                return true;
            }
            /*
             * case R.id.send_email: { Utils.emailDatabase(getActivity());
             * return true; }
             */

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    /**
     * Method to handle click on profile image
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
    protected Object getVolleyTag() {
        return TAG;
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
            DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_BOOKS_SEARCH_RESULTS, cookie, TableSearchBooks.NAME, null, null, true, this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        saveLastFetchedInfoToPref();
        mBooksAroundMeAutoCompleteTextView.removeTextChangedListener(this);
    }

    /**
     * Saves the last fetched info to shared preferences. This will be read
     * again in onResume so as to prevent refetching of the books
     */
    private void saveLastFetchedInfoToPref() {

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
        readLastFetchedInfoFromPref();
        mBooksAroundMeAutoCompleteTextView.addTextChangedListener(this);
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
        if (mLastFetchedLocation == null) {
            mLastFetchedLocation = new Location(LocationManager.PASSIVE_PROVIDER);
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

                showCrouton(mEmptySearchCroutonFlag ? R.string.no_books_found
                                : R.string.no_more_books_found, AlertStyle.INFO);
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
             * fetch the books again since the current set will include those(The server uses 50 km as the search radius)
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

            final InputMethodManager imm = (InputMethodManager) getActivity()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mBooksAroundMeAutoCompleteTextView
                            .getWindowToken(), 0);

            final Cursor cursor = (Cursor) mBooksAroundMeAdapter
                            .getItem(position);

            final String idBook = cursor.getString(cursor
                            .getColumnIndex(DatabaseColumns.ID));

            Logger.d(TAG, "ID:" + idBook);

            final String bookId = cursor.getString(cursor
                            .getColumnIndex(DatabaseColumns.BOOK_ID));
            final String userId = cursor.getString(cursor
                            .getColumnIndex(DatabaseColumns.USER_ID));

            final Bundle showBooksArgs = new Bundle();
            showBooksArgs.putString(Keys.BOOK_ID, bookId);
            showBooksArgs.putString(Keys.ID, idBook);
            showBooksArgs.putString(Keys.USER_ID, userId);

            loadFragment(mContainerViewId, (AbstractBarterLiFragment) Fragment
                            .instantiate(getActivity(), BookDetailFragment.class
                                            .getName(), showBooksArgs), FragmentTags.BOOK_FROM_BOOKS_AROUND_ME, true, FragmentTags.BS_BOOK_DETAIL);
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

            mCurPage = 0;
            mHasLoadedAllItems = false;
            final Bundle args = (Bundle) cookie;
            fetchBooksAroundMe((Location) args.getParcelable(Keys.LOCATION));
        }
        if (token == QueryTokens.DELETE_BOOKS_SEARCH_RESULTS_FROM_EDITTEXT) {

            assert (cookie != null);

            final Bundle args = (Bundle) cookie;
            fetchBooksAroundMeForSearch((Location) args.getParcelable(Keys.LOCATION), 50, mBookName);
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
    public void beforeTextChanged(final CharSequence s, final int start,
                    final int count, final int after) {
        // TODO Auto-generated method stub

    }

    //Implemented for search bar..
    @Override
    public void onTextChanged(final CharSequence s, final int start,
                    final int before, final int count) {

        final Bundle cookie = new Bundle(2);
        cookie.putParcelable(Keys.LOCATION, mLastFetchedLocation);
        //   Delete the current search results before parsing the old ones
        mBookName = mBooksAroundMeAutoCompleteTextView.getText().toString();
        DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_BOOKS_SEARCH_RESULTS_FROM_EDITTEXT, cookie, TableSearchBooks.NAME, null, null, true, BooksAroundMeFragment.this);

    }

    @Override
    public void afterTextChanged(final Editable s) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onLoadMore() {
        mEmptySearchCroutonFlag = false;
        fetchBooksAroundMe( mLastFetchedLocation);
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
        }
        return super.willHandleDialog(dialog);
    }

    @Override
    public void onDialogClick(final DialogInterface dialog, final int which) {

        if ((mAddBookDialogFragment != null)
                        && mAddBookDialogFragment.getDialog().equals(dialog)) {

            if (which == 0) { // scan book
                startActivityForResult(new Intent(getActivity(), ScanIsbnActivity.class), RequestCodes.SCAN_ISBN);

            } else if (which == 1) { // add book manually
                loadAddOrEditBookFragment(null);
            }
        } else {
            super.onDialogClick(dialog, which);
        }
    }

}
