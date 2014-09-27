/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

/*******************************************************************************
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

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.google.android.gms.analytics.HitBuilders.EventBuilder;

import li.barter.R;
import li.barter.activities.BookDetailActivity;
import li.barter.adapters.BooksGridAdapter;
import li.barter.analytics.AnalyticsConstants.Actions;
import li.barter.analytics.AnalyticsConstants.Categories;
import li.barter.analytics.AnalyticsConstants.Screens;
import li.barter.analytics.GoogleAnalyticsManager;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.SQLiteLoader;
import li.barter.data.ViewUserBooksWithLocations;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.Loaders;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.Logger;

/**
 * @author Anshul Kamboj
 */

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out,
        popEnterAnimation = R.anim.zoom_in,
        popExitAnimation = R.anim.slide_out_to_right)
public class MyBooksFragment extends AbstractBarterLiFragment implements
        LoaderCallbacks<Cursor>, OnItemClickListener {

    private static final String TAG = "MyBooksFragment";

    private GridView mGridProfileBooks;

    /**
     * {@link BooksGridAdapter} instance for the Books
     */
    private BooksGridAdapter mProfileBooksAdapter;

    private String mUserId;

    private boolean mLoadedIndividually;
    private final String mUserSelection = DatabaseColumns.USER_ID
            + SQLConstants.EQUALS_ARG;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        mLoadedIndividually = false;
        final View view = inflater
                .inflate(R.layout.fragment_profile_books, null);

        mGridProfileBooks = (GridView) view
                .findViewById(R.id.grid_profile_books);

        if (savedInstanceState != null) {
            final String savedUserId = savedInstanceState
                    .getString(Keys.USER_ID);

            if (!TextUtils.isEmpty(savedUserId)) {
                setUserId(savedUserId);
            }
        } else {
            final Bundle extras = getArguments();

            if (extras != null && extras.containsKey(Keys.USER_ID)) {
                setUserId(extras.getString(Keys.USER_ID));
            }
        }

        mProfileBooksAdapter = new BooksGridAdapter(getActivity(), false);
        mGridProfileBooks.setAdapter(mProfileBooksAdapter);

        mGridProfileBooks.setOnItemClickListener(this);

        return view;
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void onSuccess(final int requestId,
                          final IBlRequestContract request,
                          final ResponseInfo response) {

    }

    @Override
    public void onBadRequestError(final int requestId,
                                  final IBlRequestContract request, final int errorCode,
                                  final String errorMessage, final Bundle errorResponseBundle) {

    }

    /**
     * Fetches books owned by the current user
     */

    private void loadMyBooks() {
        getLoaderManager().restartLoader(Loaders.GET_MY_BOOKS, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle args) {
        if (loaderId == Loaders.GET_MY_BOOKS) {
            return new SQLiteLoader(getActivity(), false, ViewUserBooksWithLocations.NAME, null,
                    mUserSelection, new String[]{
                    mUserId
            }, null, null, null, null
            );
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        if (loader.getId() == Loaders.GET_MY_BOOKS) {
            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            mProfileBooksAdapter.swapCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        if (loader.getId() == Loaders.GET_MY_BOOKS) {
            mProfileBooksAdapter.swapCursor(null);
            mGridProfileBooks.setAdapter(null);
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view,
                            final int position, final long id) {

        if (parent.getId() == R.id.grid_profile_books) {

            if (!mUserId.equals(UserInfo.INSTANCE.getId())) {
                GoogleAnalyticsManager.getInstance().sendEvent(
                        new EventBuilder(Categories.USAGE, Actions.BOOK_PROFILE_CLICK));
            }
            final Cursor cursor = (Cursor) mProfileBooksAdapter
                    .getItem(position);

            final String bookId = cursor.getString(cursor.getColumnIndex(DatabaseColumns.ID));

            final Intent bookDetailIntent = new Intent(getActivity(), BookDetailActivity.class);
            bookDetailIntent.putExtra(Keys.ID, bookId);
            startActivity(bookDetailIntent);

            /*final Bundle showBooksArgs = Utils.cursorToBundle(cursor);

            loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                                 .instantiate(getActivity(), BookDetailFragment.class
                                         .getName(), showBooksArgs),
                         FragmentTags.USER_BOOK_FROM_PROFILE, true,
                         FragmentTags.BS_EDIT_PROFILE
            );*/
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Keys.USER_ID, mUserId);
    }

    /**
     * Sets the user id for this fragment
     *
     * @param userId
     */
    public void setUserId(String userId) {
        mUserId = userId;
        loadMyBooks();

    }

    @Override
    protected String getAnalyticsScreenName() {
        if (mLoadedIndividually) {
            return mUserId.equals(UserInfo.INSTANCE
                    .getId()) ? Screens.CURRENT_USER_BOOKS : Screens.OTHER_USER_BOOKS;
        } else {
            return "";
        }
    }

}
