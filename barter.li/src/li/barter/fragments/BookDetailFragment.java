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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import li.barter.R;
import li.barter.chat.ChatService;
import li.barter.data.DBInterface;
import li.barter.data.DBInterface.AsyncDbQueryCallback;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.TableMyBooks;
import li.barter.data.TableSearchBooks;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.BarterType;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.QueryTokens;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.Logger;

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class BookDetailFragment extends AbstractBarterLiFragment implements
                AsyncDbQueryCallback, OnClickListener {

    private static final String TAG = "ShowSingleBookFragment";

    private TextView            mIsbnTextView;
    private TextView            mTitleTextView;
    private TextView            mAuthorTextView;
    private TextView            mDescriptionTextView;
    private ImageView           mBookImageView;
    private TextView            mPublicationDateTextView;
    private CheckBox            mBarterCheckBox;
    private CheckBox            mReadCheckBox;
    private CheckBox            mSellCheckBox;
    private CheckBox            mWishlistCheckBox;
    private CheckBox            mGiveAwayCheckBox;
    private CheckBox            mKeepPrivateCheckBox;
    private CheckBox[]          mBarterTypeCheckBoxes;
    private Button              mChatWithOwnerButton;
    private Button              mOwnerProfileButton;

    private String              mBookId;
    private String              mUserId;
    private boolean             mOwnedByUser;
    private boolean             mCameFromOtherProfile;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container);
        setHasOptionsMenu(true);
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
            mCameFromOtherProfile = extras.getBoolean(Keys.OTHER_PROFILE_FLAG);
            if ((mUserId != null) && mUserId.equals(UserInfo.INSTANCE.getId())) {
                mOwnedByUser = true;
            } else {
                mOwnedByUser = false;
            }
        }

        updateViewForUser();
        loadBookDetails();
        setActionBarDrawerToggleEnabled(false);
        return view;
    }

    /**
     * Checks whether the book belongs to the current user or not, and updates
     * the UI accordingly
     */
    private void updateViewForUser() {

        if (mOwnedByUser) {
            mChatWithOwnerButton.setEnabled(false);
            mChatWithOwnerButton.setVisibility(View.GONE);
            
        }
        
        if(mCameFromOtherProfile)
        {
            mOwnerProfileButton.setVisibility(View.GONE);
        }
    }

    private void loadBookDetails() {

        if (mOwnedByUser) {
            //Reached here either by creating a new book, OR by tapping a book item in My Profile
            DBInterface.queryAsync(QueryTokens.LOAD_BOOK_DETAIL_CURRENT_USER, null, false, TableMyBooks.NAME, null, DatabaseColumns.BOOK_ID
                            + SQLConstants.EQUALS_ARG, new String[] {
                mBookId
            }, null, null, null, null, this);
        } else {
            //Reached here by tapping a book item in Books Around Me screen
            DBInterface.queryAsync(QueryTokens.LOAD_BOOK_DETAIL_OTHER_USER, null, false, TableSearchBooks.NAME, null, DatabaseColumns.BOOK_ID
                            + SQLConstants.EQUALS_ARG, new String[] {
                mBookId
            }, null, null, null, null, this);
        }

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
        mBookImageView = (ImageView) view.findViewById(R.id.book_avatar);
        mDescriptionTextView = (TextView) view
                        .findViewById(R.id.text_description);
        mPublicationDateTextView = (TextView) view
                        .findViewById(R.id.text_publication_date);
        mChatWithOwnerButton = (Button) view.findViewById(R.id.button_chat);
        mChatWithOwnerButton.setOnClickListener(this);

        mOwnerProfileButton = (Button) view.findViewById(R.id.button_profile);
        mOwnerProfileButton.setOnClickListener(this);

        initBarterTypeCheckBoxes(view);

    }

    /**
     * Gets the references to the barter type checkboxes, set the tags to
     * simplify building the tags array when sending the request to server
     * 
     * @param view The content view of the fragment
     */
    private void initBarterTypeCheckBoxes(final View view) {
        mBarterCheckBox = (CheckBox) view.findViewById(R.id.checkbox_barter);
        mReadCheckBox = (CheckBox) view.findViewById(R.id.checkbox_read);
        mSellCheckBox = (CheckBox) view.findViewById(R.id.checkbox_sell);
        mWishlistCheckBox = (CheckBox) view
                        .findViewById(R.id.checkbox_wishlist);
        mGiveAwayCheckBox = (CheckBox) view
                        .findViewById(R.id.checkbox_give_away);
        mKeepPrivateCheckBox = (CheckBox) view
                        .findViewById(R.id.checkbox_keep_private);

        // Set the barter tags
        mBarterCheckBox.setTag(R.string.tag_barter_type, BarterType.BARTER);
        mReadCheckBox.setTag(R.string.tag_barter_type, BarterType.READ);
        mSellCheckBox.setTag(R.string.tag_barter_type, BarterType.SALE);
        mWishlistCheckBox.setTag(R.string.tag_barter_type, BarterType.RENT);
        mGiveAwayCheckBox.setTag(R.string.tag_barter_type, BarterType.FREE);
        mKeepPrivateCheckBox
                        .setTag(R.string.tag_barter_type, BarterType.PRIVATE);

        mBarterTypeCheckBoxes = new CheckBox[6];
        mBarterTypeCheckBoxes[0] = mBarterCheckBox;
        mBarterTypeCheckBoxes[1] = mReadCheckBox;
        mBarterTypeCheckBoxes[2] = mSellCheckBox;
        mBarterTypeCheckBoxes[3] = mWishlistCheckBox;
        mBarterTypeCheckBoxes[4] = mGiveAwayCheckBox;
        mBarterTypeCheckBoxes[5] = mKeepPrivateCheckBox;
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
        return TAG;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

        if (mOwnedByUser) {
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
                args.putString(Keys.BOOK_ID, mBookId);
                args.putBoolean(Keys.EDIT_MODE, true);
                loadFragment(mContainerViewId, (AbstractBarterLiFragment) Fragment
                                .instantiate(getActivity(), AddOrEditBookFragment.class
                                                .getName(), args), FragmentTags.ADD_OR_EDIT_BOOK, true, FragmentTags.BS_SINGLE_BOOK);

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

        if ((token == QueryTokens.LOAD_BOOK_DETAIL_CURRENT_USER)
                        || (token == QueryTokens.LOAD_BOOK_DETAIL_OTHER_USER)) {
            if (cursor.moveToFirst()) {
                mIsbnTextView.setText(cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.ISBN_10)));
                mTitleTextView.setText(cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.TITLE)));
                mAuthorTextView.setText(cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.AUTHOR)));
                mDescriptionTextView
                                .setText(Html.fromHtml(cursor.getString(cursor
                                                .getColumnIndex(DatabaseColumns.DESCRIPTION))));
                mPublicationDateTextView
                                .setText(cursor.getString(cursor
                                                .getColumnIndex(DatabaseColumns.PUBLICATION_YEAR)));

                Logger.d(TAG, cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.IMAGE_URL)), "book image");

                // Picasso.with(getActivity()).setDebugging(true);
                Picasso.with(getActivity())
                                .load(cursor.getString(cursor
                                                .getColumnIndex(DatabaseColumns.IMAGE_URL)))
                                .fit().into(mBookImageView);

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

        for (final String token : barterTypes) {

            for (final CheckBox eachCheckBox : mBarterTypeCheckBoxes) {

                if (eachCheckBox.getTag(R.string.tag_barter_type).equals(token)) {
                    eachCheckBox.setChecked(true);
                }
            }
        }
    }

    @Override
    public void onClick(final View v) {
        if (v.getId() == R.id.button_chat) {

            if (isLoggedIn()) {
                final Bundle args = new Bundle(3);
                args.putString(Keys.CHAT_ID, ChatService
                                .generateChatId(mUserId, UserInfo.INSTANCE
                                                .getId()));
                args.putString(Keys.USER_ID, mUserId);
                args.putString(Keys.BOOK_TITLE, mTitleTextView.getText()
                                .toString());

                loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                                .instantiate(getActivity(), ChatDetailsFragment.class
                                                .getName(), args), FragmentTags.CHAT_DETAILS, true, null);
            } else {
                loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                                .instantiate(getActivity(), LoginFragment.class
                                                .getName(), null), FragmentTags.LOGIN_TO_CHAT, true, null);
            }
        }

        else if (v.getId() == R.id.button_profile) {

            final Bundle args = new Bundle(1);

            args.putString(Keys.USER_ID, mUserId);

            loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                            .instantiate(getActivity(), OtherProfileFragment.class
                                            .getName(), args), FragmentTags.OTHER_USER_PROFILE, true, null);

        } else {
            // Show Login Fragment
        }

    }

}
