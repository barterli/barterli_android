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

import com.squareup.picasso.Picasso;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity;
import li.barter.analytics.AnalyticsConstants.Screens;
import li.barter.data.DBInterface;
import li.barter.data.DBInterface.AsyncDbQueryCallback;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.TableSearchBooks;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.QueryTokens;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.Logger;

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class BookDetailFragment extends AbstractBarterLiFragment implements
                AsyncDbQueryCallback {

    private static final String TAG            = "BookDetailFragment";

    private TextView            mIsbnTextView;
    private TextView            mTitleTextView;
    private TextView            mAuthorTextView;
    private TextView            mDescriptionTextView;
    private TextView            mSuggestedPriceLabelTextView;
    private TextView            mSuggestedPriceTextView;
    private TextView            mNoImageTextView;
    private ImageView           mBookImageView;
    private TextView            mPublicationDateTextView;
    private TextView            mBarterTypes;

    private String              mBookId;
    private String              mId;
    private String              mUserId;
    private boolean             mOwnedByUser;
    
    
    /**
     * Whether this fragment has been loaded by itself or as part of a pager/tab setup 
     */
    private boolean mLoadedIndividually;

    private final String        mBookSelection = DatabaseColumns.BOOK_ID
                                                               + SQLConstants.EQUALS_ARG;

    public static BookDetailFragment newInstance(String userId, String bookId,String id) {
        BookDetailFragment f = new BookDetailFragment();

        Bundle args = new Bundle();
        args.putString(Keys.USER_ID, userId);
        args.putString(Keys.BOOK_ID, bookId);
        args.putString(Keys.ID, id);
        f.setArguments(args);

        return f;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setActionBarTitle(R.string.Book_Detail_fragment_title);

        final View view = inflater
                        .inflate(R.layout.fragment_book_detail, container, false);
        initViews(view);

        getActivity().getWindow()
                        .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        final Bundle extras = getArguments();

        if (extras != null) {
            mBookId = extras.getString(Keys.BOOK_ID);
            mUserId = extras.getString(Keys.USER_ID);
            mId = extras.getString(Keys.ID);
           
            if ((mUserId != null) && mUserId.equals(UserInfo.INSTANCE.getId())) {
                mOwnedByUser = true;
            } else {
                mOwnedByUser = false;
            }
        }
        
        final AbstractBarterLiFragment fragment = ((AbstractBarterLiActivity) getActivity()).getCurrentMasterFragment();
        
        if(fragment != null && fragment instanceof BooksPagerFragment) {
            mLoadedIndividually = false;
        } else {
            mLoadedIndividually = true;
        }
        
        setHasOptionsMenu(mLoadedIndividually);

        setActionBarDrawerToggleEnabled(false);
        loadBookDetails();
        return view;
    }

    /**
     * Gets references to the Views
     * 
     * @param view The content view of the fragment
     */
    private void initViews(final View view) {
        mIsbnTextView = (TextView) view.findViewById(R.id.text_isbn);
        mTitleTextView = (TextView) view.findViewById(R.id.text_title);
        mAuthorTextView = (TextView) view.findViewById(R.id.text_author);
        mBarterTypes = (TextView) view.findViewById(R.id.label_barter_types);

        mBookImageView = (ImageView) view.findViewById(R.id.book_avatar);
        mDescriptionTextView = (TextView) view
                        .findViewById(R.id.text_description);

        mSuggestedPriceTextView = (TextView) view
                        .findViewById(R.id.text_suggested_price);
        mNoImageTextView = (TextView) view
                .findViewById(R.id.image_text);
        mSuggestedPriceLabelTextView = (TextView) view
                        .findViewById(R.id.label_suggested_price);

        mPublicationDateTextView = (TextView) view
                        .findViewById(R.id.text_publication_date);

    }

    private void loadBookDetails() {

        DBInterface.queryAsync(QueryTokens.LOAD_BOOK_DETAIL_CURRENT_USER, null, false, TableSearchBooks.NAME, null, mBookSelection, new String[] {
            mBookId
        }, null, null, null, null, this);
        
    }

    @Override
    public void onBackPressed() {

        if (getTag().equals(FragmentTags.MY_BOOK_FROM_ADD_OR_EDIT)) {
            onUpNavigate();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected Object getVolleyTag() {
        return hashCode();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

        if (mOwnedByUser) {
            Logger.v(TAG, "Inflating Menu");
            inflater.inflate(R.menu.menu_profile_show, menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {
                onUpNavigate();
                return true;
            }

            case R.id.action_edit_profile: {
                final Bundle args = new Bundle(2);
                args.putString(Keys.ID, mId);
                args.putString(Keys.BOOK_ID, mBookId);
                args.putBoolean(Keys.EDIT_MODE, true);
                loadFragment(mContainerViewId, (AbstractBarterLiFragment) Fragment
                                .instantiate(getActivity(), AddOrEditBookFragment.class
                                                .getName(), args), FragmentTags.ADD_OR_EDIT_BOOK, true, FragmentTags.BS_EDIT_BOOK);

                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
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

    @Override
    public void onQueryComplete(final int token, final Object cookie,
                    final Cursor cursor) {

        if (token == QueryTokens.LOAD_BOOK_DETAIL_CURRENT_USER) {

            Logger.d(TAG, "query completed " + cursor.getCount());
            if (cursor.moveToFirst()) {
                mIsbnTextView.setText(cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.ISBN_10)));
                mTitleTextView.setText(cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.TITLE)));
                mTitleTextView.setSelected(true);
             
                mAuthorTextView.setText(cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.AUTHOR)));

                mDescriptionTextView.setText(cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.DESCRIPTION)));

                final String value = cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.VALUE));

                if (!TextUtils.isEmpty(value)) {

                    mSuggestedPriceLabelTextView.setVisibility(View.VISIBLE);
                    mSuggestedPriceTextView.setVisibility(View.VISIBLE);
                    mSuggestedPriceTextView.setText(value);
                }

                mPublicationDateTextView
                                .setText(cursor.getString(cursor
                                                .getColumnIndex(DatabaseColumns.PUBLICATION_YEAR)));

                Logger.d(TAG, cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.IMAGE_URL)), "book image");

                // Picasso.with(getActivity()).setDebugging(true);
                if(cursor.getString(cursor
                                                .getColumnIndex(DatabaseColumns.IMAGE_URL)).equals(AppConstants.FALSE))
                {
                	mBookImageView.setBackgroundColor(Color.WHITE);
                	mNoImageTextView.setVisibility(View.VISIBLE);
                }
                else
                {
               
                Picasso.with(getActivity())
                                .load(cursor.getString(cursor
                                                .getColumnIndex(DatabaseColumns.IMAGE_URL)))
                                .fit().into(mBookImageView);
                mNoImageTextView.setVisibility(View.GONE);
             	
                }

                final String barterType = cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.BARTER_TYPE));

                if (!TextUtils.isEmpty(barterType)) {
                    setBarterCheckboxes(barterType);
                }
            }

            cursor.close();
        }

    }

    /**
     * Checks the supported barter type of the book and updates the checkboxes
     * 
     * @param barterType The barter types supported by the book
     */
    private void setBarterCheckboxes(final String barterType) {

        final String[] barterTypes = barterType
                        .split(AppConstants.BARTER_TYPE_SEPARATOR);
        String barterTypeHashTag = "";
        for (final String token : barterTypes) {
            barterTypeHashTag = barterTypeHashTag + "#" + token + " ";

        }
        mBarterTypes.setText(barterTypeHashTag);
    }
    
    @Override
    protected String getAnalyticsScreenName() {
        if(mLoadedIndividually) {
            return Screens.BOOK_DETAIL;
        } else {
            return "";
        }
    }

}
