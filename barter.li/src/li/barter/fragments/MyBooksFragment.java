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

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import li.barter.R;
import li.barter.adapters.BooksAroundMeAdapter;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.SQLiteLoader;
import li.barter.data.ViewUserBooksWithLocations;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.Loaders;
import li.barter.utils.Logger;

/**
 * @author Anshul Kamboj
 */

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class MyBooksFragment extends AbstractBarterLiFragment implements
                LoaderCallbacks<Cursor>, OnItemClickListener {

    private static final String           TAG            = "MyBooksFragment";

    private GridView                      mBooksAroundMeGridView;

    /**
     * {@link BooksAroundMeAdapter} instance for the Books
     */
    private BooksAroundMeAdapter          mBooksAroundMeAdapter;

    private String                        mUserId;

    private final String                  mUserSelection = DatabaseColumns.USER_ID
                                                                         + SQLConstants.EQUALS_ARG;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        final View view = inflater
                        .inflate(R.layout.fragment_profile_books, null);

        mBooksAroundMeGridView = (GridView) view
                        .findViewById(R.id.list_my_books);

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

        mBooksAroundMeAdapter = new BooksAroundMeAdapter(getActivity());
        mBooksAroundMeGridView.setAdapter(mBooksAroundMeAdapter);

        mBooksAroundMeGridView.setOnItemClickListener(this);

        setActionBarDrawerToggleEnabled(false);

        return view;
    }

    @Override
    protected Object getVolleyTag() {
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
            return new SQLiteLoader(getActivity(), false, ViewUserBooksWithLocations.NAME, null, mUserSelection, new String[] {
                mUserId
            }, null, null, null, null);
        }

        else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        if (loader.getId() == Loaders.GET_MY_BOOKS) {
            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            mBooksAroundMeAdapter.swapCursor(cursor);
        }

    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        if (loader.getId() == Loaders.GET_MY_BOOKS) {
            mBooksAroundMeAdapter.swapCursor(null);
            mBooksAroundMeGridView.setAdapter(null);
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {

        if (parent.getId() == R.id.list_my_books) {
            final Cursor cursor = (Cursor) mBooksAroundMeAdapter
                            .getItem(position);

            final String bookId = cursor.getString(cursor
                            .getColumnIndex(DatabaseColumns.BOOK_ID));

            final String idBook = cursor.getString(cursor
                            .getColumnIndex(DatabaseColumns.ID));

            final Bundle showBooksArgs = new Bundle();
            showBooksArgs.putString(Keys.BOOK_ID, bookId);
            showBooksArgs.putString(Keys.ID, idBook);
            showBooksArgs.putString(Keys.USER_ID, mUserId);

            loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                            .instantiate(getActivity(), BookDetailFragment.class
                                            .getName(), showBooksArgs), FragmentTags.USER_BOOK_FROM_PROFILE, true, FragmentTags.BS_EDIT_PROFILE);
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

}
