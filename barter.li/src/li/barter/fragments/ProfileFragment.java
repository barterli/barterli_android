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

import com.haarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.squareup.picasso.Picasso;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.Locale;

import li.barter.R;
import li.barter.adapters.BooksAroundMeAdapter;
import li.barter.data.DBInterface;
import li.barter.data.DBInterface.AsyncDbQueryCallback;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.SQLiteLoader;
import li.barter.data.TableLocations;
import li.barter.data.ViewMyBooksWithLocations;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.Loaders;
import li.barter.utils.AppConstants.QueryTokens;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.Logger;
import li.barter.utils.SharedPreferenceHelper;

/**
 * @author Sharath Pandeshwar
 */

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class ProfileFragment extends AbstractBarterLiFragment implements
                AsyncDbQueryCallback, LoaderCallbacks<Cursor>,
                OnItemClickListener {

    private static final String           TAG          = "ProfileFragment";

    private TextView                      mProfileNameTextView;
    private TextView                      mAboutMeTextView;
    private TextView                      mPreferredLocationTextView;
    private ImageView                     mProfileImageView;
    private final String                  mDefaultName = "Your Name";

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

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container);
        setHasOptionsMenu(true);
        final View view = inflater
                        .inflate(R.layout.fragment_profile_show, null);

        setActionBarTitle(R.string.profilepage_title);

        mProfileNameTextView = (TextView) view
                        .findViewById(R.id.text_profile_name);
        mAboutMeTextView = (TextView) view.findViewById(R.id.text_about_me);
        mPreferredLocationTextView = (TextView) view
                        .findViewById(R.id.text_current_location);
        mProfileImageView = (ImageView) view
                        .findViewById(R.id.image_profile_pic);
        mBooksAroundMeGridView = (GridView) view
                        .findViewById(R.id.grid_my_books);

        final File mAvatarfile = new File(Environment.getExternalStorageDirectory(), "barterli_avatar_small.png");
        if (mAvatarfile.exists()) {
            final Bitmap bmp = BitmapFactory.decodeFile(mAvatarfile
                            .getAbsolutePath());
            mProfileImageView.setImageBitmap(bmp);
        }

        if (SharedPreferenceHelper
                        .contains(getActivity(), R.string.pref_first_name)
                        && !(TextUtils.isEmpty(SharedPreferenceHelper
                                        .getString(getActivity(), R.string.pref_first_name)))) {

            final String mFirstName = SharedPreferenceHelper
                            .getString(getActivity(), R.string.pref_first_name);

            String mLastName = "";

            if (SharedPreferenceHelper
                            .contains(getActivity(), R.string.pref_last_name)) {
                mLastName = SharedPreferenceHelper
                                .getString(getActivity(), R.string.pref_last_name);
            }

            // for loading profile image

            String mProfileImageUrl = "";
            if (SharedPreferenceHelper
                            .contains(getActivity(), R.string.pref_profile_image)) {
                mProfileImageUrl = SharedPreferenceHelper
                                .getString(getActivity(), R.string.pref_profile_image);

            }

            //append "?type=large" for getting large clear image for the profile
            Picasso.with(getActivity()).load(mProfileImageUrl + "?type=large")
                            .fit().error(R.drawable.pic_avatar)
                            .into(mProfileImageView);

            String mFullName = String.format(Locale.US, mFirstName + " "
                            + mLastName);
            if (TextUtils.isEmpty(mFullName)) {
                mFullName = mDefaultName;
            }
            mProfileNameTextView.setText(mFullName);
        } else {
            mProfileNameTextView.setText(mDefaultName);
        }

        if (SharedPreferenceHelper
                        .contains(getActivity(), R.string.pref_location)) {
            loadPreferredLocation();
        }

        if (SharedPreferenceHelper
                        .contains(getActivity(), R.string.pref_description)) {
            mAboutMeTextView.setText(SharedPreferenceHelper
                            .getString(getActivity(), R.string.pref_description));
        } else {
            mAboutMeTextView.setVisibility(View.INVISIBLE);
        }

        mBooksAroundMeAdapter = new BooksAroundMeAdapter(getActivity());
        mSwingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(mBooksAroundMeAdapter, 150, 500);
        mSwingBottomInAnimationAdapter.setAbsListView(mBooksAroundMeGridView);
        mBooksAroundMeGridView.setAdapter(mSwingBottomInAnimationAdapter);

        mBooksAroundMeGridView.setOnItemClickListener(this);

        setActionBarDrawerToggleEnabled(false);
        loadMyBooks();
        return view;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile_show, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {

                if(getTag().equals(FragmentTags.PROFILE_FROM_LOGIN)) {
                    loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                                    .instantiate(getActivity(), BooksAroundMeFragment.class
                                                    .getName(), null), FragmentTags.BOOKS_AROUND_ME, true, null);
                }
                else {
                    onUpNavigate(); 
                }
                
                return true;
            }

            case R.id.action_edit_profile: {
                loadFragment(mContainerViewId, (AbstractBarterLiFragment) Fragment
                                .instantiate(getActivity(), EditProfileFragment.class
                                                .getName(), null), FragmentTags.EDIT_PROFILE, true, FragmentTags.BS_EDIT_PROFILE);
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (getTag().equals(FragmentTags.PROFILE_FROM_LOGIN)) {
            loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                            .instantiate(getActivity(), BooksAroundMeFragment.class
                                            .getName(), null), FragmentTags.BOOKS_AROUND_ME, true, null);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Load the user's preferred location from DB Table and show it in the
     * profile page.
     */

    private void loadPreferredLocation() {
        DBInterface.queryAsync(QueryTokens.LOAD_LOCATION_FROM_PROFILE_SHOW_PAGE, null, false, TableLocations.NAME, null, DatabaseColumns.LOCATION_ID
                        + SQLConstants.EQUALS_ARG, new String[] {
            SharedPreferenceHelper
                            .getString(getActivity(), R.string.pref_location)
        }, null, null, null, null, this);
    }

    @Override
    public void onQueryComplete(final int token, final Object cookie,
                    final Cursor cursor) {
        if (token == QueryTokens.LOAD_LOCATION_FROM_PROFILE_SHOW_PAGE) {

            if (cursor.moveToFirst()) {
                final String mPrefAddressName = cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.NAME))
                                + ", "
                                + cursor.getString(cursor
                                                .getColumnIndex(DatabaseColumns.ADDRESS));

                mPreferredLocationTextView.setText(mPrefAddressName);
            }

            cursor.close();

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        DBInterface.cancelAsyncQuery(QueryTokens.LOAD_LOCATION_FROM_PROFILE_SHOW_PAGE);
    }

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    @Override
    public void onSuccess(final int requestId,
                    final IBlRequestContract request,
                    final ResponseInfo response) {
        // TODO Auto-generated method stub
        if (requestId == RequestId.GET_USER_PROFILE) {
        }

    }

    @Override
    public void onBadRequestError(final int requestId,
                    final IBlRequestContract request, final int errorCode,
                    final String errorMessage, final Bundle errorResponseBundle) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onInsertComplete(final int token, final Object cookie,
                    final long insertRowId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeleteComplete(final int token, final Object cookie,
                    final int deleteCount) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpdateComplete(final int token, final Object cookie,
                    final int updateCount) {
        // TODO Auto-generated method stub

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
            return new SQLiteLoader(getActivity(), false, ViewMyBooksWithLocations.NAME, null, null, null, null, null, null, null);
        } else {
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
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view,
                    final int position, final long id) {

        if (parent.getId() == R.id.grid_my_books) {
            final Cursor cursor = (Cursor) mBooksAroundMeAdapter
                            .getItem(position);

            final String bookId = cursor.getString(cursor
                            .getColumnIndex(DatabaseColumns.BOOK_ID));

            final Bundle showBooksArgs = new Bundle();
            showBooksArgs.putString(Keys.BOOK_ID, bookId);
            showBooksArgs.putString(Keys.USER_ID, UserInfo.INSTANCE.getId());

            loadFragment(mContainerViewId, (AbstractBarterLiFragment) Fragment
                            .instantiate(getActivity(), BookDetailFragment.class
                                            .getName(), showBooksArgs), FragmentTags.MY_BOOK_FROM_PROFILE, true, FragmentTags.BS_EDIT_PROFILE);
        }
    }

}
