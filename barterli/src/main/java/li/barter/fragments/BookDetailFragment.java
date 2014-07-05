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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
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

import com.android.volley.Request.Method;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity;
import li.barter.activities.AbstractBarterLiActivity.AlertStyle;
import li.barter.analytics.AnalyticsConstants.Screens;
import li.barter.data.DBInterface;
import li.barter.data.DBInterface.AsyncDbQueryCallback;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.TableSearchBooks;
import li.barter.data.TableUserBooks;
import li.barter.fragments.dialogs.AlertDialogFragment;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.QueryTokens;
import li.barter.utils.Logger;
import li.barter.utils.SharedPreferenceHelper;
import li.barter.utils.Utils;

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out,
                    popEnterAnimation = R.anim.zoom_in,
                    popExitAnimation = R.anim.slide_out_to_right)
public class BookDetailFragment extends AbstractBarterLiFragment implements
        AsyncDbQueryCallback {

    private static final String TAG = "BookDetailFragment";

    private static final String BOOK_SELECTION = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

    private TextView  mIsbnTextView;
    private TextView  mTitleTextView;
    private TextView  mAuthorTextView;
    private TextView  mDescriptionTextView;
    private TextView  mSuggestedPriceLabelTextView;
    private TextView  mSuggestedPriceTextView;
    private TextView  mNoImageTextView;
    private ImageView mBookImageView;
    private TextView  mPublicationDateTextView;
    private TextView  mBarterTypes;

    private String              mId;
    private String              mBookTitle;
    private boolean             mOwnedByUser;
    private AlertDialogFragment mDeleteBookDialogFragment;
    private boolean             mIsDeletingBook;

    /**
     * Whether this fragment has been loaded by itself or as part of a pager/tab setup
     */
    private boolean mLoadedIndividually;

    private ShareActionProvider mShareActionProvider;

    private Bundle mBookDetails;

    public static BookDetailFragment newInstance(final Bundle bookDetails) {

        final BookDetailFragment f = new BookDetailFragment();
        f.setArguments(bookDetails);

        return f;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setActionBarTitle(R.string.Book_Detail_fragment_title);

        final View view = inflater
                .inflate(R.layout.fragment_book_detail, container, false);
        view.setVerticalScrollBarEnabled(false);
        initViews(view);

        getActivity().getWindow()
                     .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                                               | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        mBookDetails = getArguments();

        mDeleteBookDialogFragment = (AlertDialogFragment) getFragmentManager()
                .findFragmentByTag(FragmentTags.DIALOG_DELETE_BOOK);

        final AbstractBarterLiFragment fragment = ((AbstractBarterLiActivity) getActivity())
                .getCurrentMasterFragment();

        if (fragment != null && fragment instanceof BooksPagerFragment) {
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
        mNoImageTextView = (TextView) view.findViewById(R.id.image_text);
        mSuggestedPriceLabelTextView = (TextView) view
                .findViewById(R.id.label_suggested_price);

        mPublicationDateTextView = (TextView) view
                .findViewById(R.id.text_publication_date);

    }

    private void loadBookDetails() {

        mId = mBookDetails.getString(DatabaseColumns.ID);
        final String userId = mBookDetails.getString(DatabaseColumns.USER_ID);

        if(userId.equals(AppConstants.UserInfo.INSTANCE.getId())) {
            mOwnedByUser = true;
            getActivity().invalidateOptionsMenu();
        }
        mBookTitle = mBookDetails.getString(DatabaseColumns.TITLE);

        final String isbn13 = mBookDetails.getString(DatabaseColumns.ISBN_13);

        if (!TextUtils.isEmpty(isbn13)) {
            mIsbnTextView.setText(isbn13);
        } else {
            mIsbnTextView.setText(mBookDetails.getString(DatabaseColumns.ISBN_10));
        }

        mTitleTextView.setText(mBookTitle);
        mTitleTextView.setSelected(true);

        mAuthorTextView.setText(mBookDetails.getString(DatabaseColumns.AUTHOR));

        mDescriptionTextView.setText(mBookDetails.getString(DatabaseColumns.DESCRIPTION));

        final String value = mBookDetails.getString(DatabaseColumns.VALUE);

        if (!TextUtils.isEmpty(value)) {

            mSuggestedPriceLabelTextView.setVisibility(View.VISIBLE);
            mSuggestedPriceTextView.setVisibility(View.VISIBLE);
            mSuggestedPriceTextView.setText(value);
        }

        mPublicationDateTextView.setText(mBookDetails.getString(DatabaseColumns.PUBLICATION_YEAR));

        final String imageUrl = mBookDetails.getString(DatabaseColumns.IMAGE_URL);

        if (TextUtils.isEmpty(imageUrl) || imageUrl.equals(AppConstants.FALSE)) {
            mBookImageView.setBackgroundColor(Color.WHITE);
            //TODO: Set default book image instead
            mNoImageTextView.setVisibility(View.VISIBLE);
        } else {

            Picasso.with(getActivity())
                   .load(imageUrl)
                   .fit().into(mBookImageView);
            mNoImageTextView.setVisibility(View.GONE);

        }

        final String barterType = mBookDetails.getString(DatabaseColumns.BARTER_TYPE);

        if (!TextUtils.isEmpty(barterType)) {
            setBarterCheckboxes(barterType);
        }
        updateShareIntent(mBookTitle);
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
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

        if (mOwnedByUser) {
            inflater.inflate(R.menu.menu_logged_in_book_detail, menu);
        } else {
            inflater.inflate(R.menu.menu_book_detail, menu);
        }

        final MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        updateShareIntent(mBookTitle);

    }

    /**
     * @param bookTitle
     */
    private void updateShareIntent(String bookTitle) {

        if (mShareActionProvider == null) {
            return;
        }

        if (TextUtils.isEmpty(bookTitle)) {
            mShareActionProvider.setShareIntent(Utils
                                                        .createAppShareIntent(getActivity()));
            return;
        }

        final String referralId = SharedPreferenceHelper
                .getString(R.string.pref_share_token);
        String appShareUrl = getString(R.string.book_share_message, bookTitle)
                .concat(AppConstants.PLAY_STORE_LINK);

        if (!TextUtils.isEmpty(referralId)) {
            appShareUrl = appShareUrl
                    .concat(String.format(Locale.US, AppConstants.REFERRER_FORMAT, referralId));
        }

        final Intent shareIntent = Utils
                .createShareIntent(getActivity(), appShareUrl);

        mShareActionProvider.setShareIntent(shareIntent);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {
                onUpNavigate();
                return true;
            }

            case R.id.action_edit_book: {
                final Bundle args = new Bundle(2);
                args.putString(Keys.ID, mId);
                args.putBoolean(Keys.EDIT_MODE, true);
                args.putString(Keys.UP_NAVIGATION_TAG, FragmentTags.BOOKS_AROUND_ME);
                loadFragment(mContainerViewId, (AbstractBarterLiFragment) Fragment
                                     .instantiate(getActivity(), AddOrEditBookFragment.class
                                             .getName(), args), FragmentTags.ADD_OR_EDIT_BOOK, true,
                             FragmentTags.BS_EDIT_BOOK
                );

                return true;
            }

            case R.id.action_delete_book: {
                deleteBook();
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    /**
     * Shows the dialog to delete the book
     */
    private void deleteBook() {

        mDeleteBookDialogFragment = new AlertDialogFragment();
        mDeleteBookDialogFragment
                .show(AlertDialog.THEME_HOLO_LIGHT, 0, R.string.delete,
                      R.string.confirm_delete_book, R.string.delete, R.string.cancel, 0,
                      getFragmentManager(), true, FragmentTags.DIALOG_DELETE_BOOK);
    }

    @Override
    public void onSuccess(final int requestId,
                          final IBlRequestContract request,
                          final ResponseInfo response) {

        if (requestId == RequestId.DELETE_BOOK) {

            DBInterface.deleteAsync(AppConstants.QueryTokens.DELETE_MY_BOOK, getTaskTag(), null,
                                    TableUserBooks.NAME, BOOK_SELECTION, new String[]{
                            mId
                    }, true, this
            );
        }

    }

    /**
     * Deletes the book to the server
     */
    private void deleteBookOnServer() {

        /*
         * TODO Investigate a better way to fix the hardcoding of Api endpoints.
         * Do we need the ".json" suffix? That will help with this. Or pass the
         * book id in params.
         */
        final BlRequest deleteBookRequest = new BlRequest(Method.DELETE,
                                                          HttpConstants.getApiBaseUrl()
                                                                  + "/books/" + mId, null,
                                                          mVolleyCallbacks
        );
        deleteBookRequest.setRequestId(RequestId.DELETE_BOOK);
        addRequestToQueue(deleteBookRequest, true, 0, true);
        mIsDeletingBook = true;
    }

    @Override
    public void onBadRequestError(final int requestId,
                                  final IBlRequestContract request, final int errorCode,
                                  final String errorMessage, final Bundle errorResponseBundle) {

        if (requestId == RequestId.DELETE_BOOK) {
            showCrouton(errorMessage, AlertStyle.ERROR);
        }
    }

    @Override
    public void onPostExecute(IBlRequestContract request) {
        super.onPostExecute(request);
        if (request.getRequestId() == RequestId.DELETE_BOOK) {
            mIsDeletingBook = false;
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
        if (token == QueryTokens.DELETE_MY_BOOK) {
            DBInterface
                    .deleteAsync(AppConstants.QueryTokens.DELETE_MY_BOOK_FROM_SEARCH, getTaskTag(),
                                 null, TableSearchBooks.NAME, BOOK_SELECTION, new String[]{
                                    mId
                            }, true, this
                    );

        } else if (token == QueryTokens.DELETE_MY_BOOK_FROM_SEARCH) {
            if (isAttached()) {
                getFragmentManager().popBackStack();
            }

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

        if (token == QueryTokens.LOAD_BOOK_DETAIL_CURRENT_USER) {

            Logger.d(TAG, "query completed " + cursor.getCount());
            if (cursor.moveToFirst()) {
                mIsbnTextView.setText(cursor.getString(cursor
                                                               .getColumnIndex(
                                                                       DatabaseColumns.ISBN_10)));
                mBookTitle = cursor.getString(cursor
                                                      .getColumnIndex(DatabaseColumns.TITLE));
                mTitleTextView.setText(mBookTitle);
                mTitleTextView.setSelected(true);

                mAuthorTextView.setText(cursor.getString(cursor
                                                                 .getColumnIndex(
                                                                         DatabaseColumns.AUTHOR)));

                mDescriptionTextView.setText(cursor.getString(cursor
                                                                      .getColumnIndex(
                                                                              DatabaseColumns.DESCRIPTION)));

                final String value = cursor.getString(cursor
                                                              .getColumnIndex(
                                                                      DatabaseColumns.VALUE));

                if (!TextUtils.isEmpty(value)) {

                    mSuggestedPriceLabelTextView.setVisibility(View.VISIBLE);
                    mSuggestedPriceTextView.setVisibility(View.VISIBLE);
                    mSuggestedPriceTextView.setText(value);
                }

                mPublicationDateTextView
                        .setText(cursor.getString(cursor
                                                          .getColumnIndex(
                                                                  DatabaseColumns.PUBLICATION_YEAR)));

                // Picasso.with(getActivity()).setDebugging(true);
                if (cursor.getString(cursor.getColumnIndex(DatabaseColumns.IMAGE_URL))
                          .equals(AppConstants.FALSE)) {
                    mBookImageView.setBackgroundColor(Color.WHITE);
                    mNoImageTextView.setVisibility(View.VISIBLE);
                } else {

                    Picasso.with(getActivity())
                           .load(cursor.getString(cursor
                                                          .getColumnIndex(
                                                                  DatabaseColumns.IMAGE_URL)))
                           .fit().into(mBookImageView);
                    mNoImageTextView.setVisibility(View.GONE);

                }

                final String barterType = cursor.getString(cursor
                                                                   .getColumnIndex(
                                                                           DatabaseColumns.BARTER_TYPE));

                if (!TextUtils.isEmpty(barterType)) {
                    setBarterCheckboxes(barterType);
                }
            }
            updateShareIntent(mBookTitle);
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
        if (mLoadedIndividually) {
            return Screens.BOOK_DETAIL;
        } else {
            return "";
        }
    }

    @Override
    public boolean willHandleDialog(DialogInterface dialog) {

        if (mDeleteBookDialogFragment != null
                && mDeleteBookDialogFragment.getDialog().equals(dialog)) {
            return true;
        }
        return super.willHandleDialog(dialog);
    }

    @Override
    public void onDialogClick(DialogInterface dialog, int which) {

        if (mDeleteBookDialogFragment != null
                && mDeleteBookDialogFragment.getDialog().equals(dialog)) {
            if (!mIsDeletingBook) {
                deleteBookOnServer();
            }
        }
    }

}
