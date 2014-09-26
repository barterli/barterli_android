/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package li.barter.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity;
import li.barter.activities.AbstractBarterLiActivity.AlertStyle;
import li.barter.activities.AddOrEditBookActivity;
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
import li.barter.utils.SharedPreferenceHelper;
import li.barter.utils.Utils;

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out,
        popEnterAnimation = R.anim.zoom_in,
        popExitAnimation = R.anim.slide_out_to_right)
public class BookDetailFragment extends AbstractBarterLiFragment implements
        AsyncDbQueryCallback, View.OnClickListener {

    private static final String TAG = "BookDetailFragment";

    private static final String BOOK_SELECTION = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

    private TextView mIsbnTextView;
    private TextView mTitleTextView;
    private TextView mAuthorTextView;
    private TextView mDescriptionTextView;
    private ImageView mBookImageView;
    private TextView mPublicationDateTextView;
    private View mBarterOptionsContainer;
    private Button mBarterButton;
    private Button mBuyButton;
    private Button mBorrowButton;
    private RatingBar mRatingBar;

    private List<String> mSupportedBarterOptions;
    private String mId;
    private String mBookTitle;
    private boolean mOwnedByUser;
    private AlertDialogFragment mDeleteBookDialogFragment;
    private boolean mIsDeletingBook;
    private String mUserId;

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
                        | WindowManager.LayoutParams
                        .SOFT_INPUT_STATE_HIDDEN);
        mDeleteBookDialogFragment = (AlertDialogFragment) getFragmentManager()
                .findFragmentByTag(FragmentTags.DIALOG_DELETE_BOOK);

        final AbstractBarterLiFragment fragment = ((AbstractBarterLiActivity) getActivity())
                .getCurrentMasterFragment();

        if (fragment != null && fragment instanceof BooksPagerFragment) {
            mLoadedIndividually = false;
        } else {
            mLoadedIndividually = true;
        }

        if (savedInstanceState == null) {
            mBookDetails = getArguments();
        } else {
            mBookDetails = savedInstanceState.getBundle(Keys.BOOK_DETAILS);
        }
        setHasOptionsMenu(mLoadedIndividually);
        loadBookDetails();
        return view;
    }

    /**
     * Updates the book details being displayed
     */
    public void updateBookDetails(final Bundle bookDetails) {
        mBookDetails = bookDetails;
        loadBookDetails();
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(Keys.BOOK_DETAILS, mBookDetails);
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
        mBarterOptionsContainer = view.findViewById(R.id.container_barter_options);
        mBookImageView = (ImageView) view.findViewById(R.id.book_avatar);
        mDescriptionTextView = (TextView) view
                .findViewById(R.id.text_description);

        mBarterButton = (Button) mBarterOptionsContainer.findViewById(R.id.button_barter);
        mBarterButton.setOnClickListener(this);
        mBuyButton = (Button) mBarterOptionsContainer.findViewById(R.id.button_buy);
        mBuyButton.setOnClickListener(this);
        mBorrowButton = (Button) mBarterOptionsContainer.findViewById(R.id.button_borrow);

        mRatingBar = (RatingBar) view.findViewById(R.id.rating_book);

        mPublicationDateTextView = (TextView) view
                .findViewById(R.id.text_publication_date);

    }

    /**
     * Loads the book details from the arguments bundle
     */
    private void loadBookDetails() {

        if (mBookDetails == null) {
            return;
        }
        mId = mBookDetails.getString(DatabaseColumns.ID);
        mUserId = mBookDetails.getString(DatabaseColumns.USER_ID);

        if (mUserId.equals(AppConstants.UserInfo.INSTANCE.getId())) {
            mOwnedByUser = true;
            getActivity().invalidateOptionsMenu();
        }


        final String isbnFormat = getString(R.string.isbn_number);
        final String isbn13 = mBookDetails.getString(DatabaseColumns.ISBN_13);

        if (!TextUtils.isEmpty(isbn13)) {
            mIsbnTextView.setText(String.format(isbnFormat, isbn13));
        } else {

            final String isbn10 = mBookDetails.getString(DatabaseColumns.ISBN_10);
            if (!TextUtils.isEmpty(isbn10)) {
                mIsbnTextView.setText(String.format(isbnFormat, isbn10));
            }
        }

        mBookTitle = mBookDetails.getString(DatabaseColumns.TITLE);
        if (!TextUtils.isEmpty(mBookTitle)) {
            mTitleTextView.setText(mBookTitle);
        }

        final String author = mBookDetails.getString(DatabaseColumns.AUTHOR);
        if (!TextUtils.isEmpty(author)) {
            mAuthorTextView.setText(getString(R.string.by_author, author));
        }

        final String description = mBookDetails.getString(DatabaseColumns.DESCRIPTION);
        if (!TextUtils.isEmpty(description)) {
            mDescriptionTextView.setText(description);
        }

        final String publicationDate = mBookDetails.getString(DatabaseColumns.PUBLICATION_YEAR);
        if (!TextUtils.isEmpty(publicationDate)) {
            mPublicationDateTextView.setText(getString(R.string.published_on, publicationDate));
        }

        final String imageUrl = mBookDetails.getString(DatabaseColumns.IMAGE_URL);

        if (TextUtils.isEmpty(imageUrl) || imageUrl.equals(AppConstants.FALSE)) {
            mBookImageView.setImageBitmap(null);
            //TODO: Set default book image
        } else {

            Picasso.with(getActivity())
                    .load(imageUrl)
                    .fit().into(mBookImageView);
        }

        final String barterType = mBookDetails.getString(DatabaseColumns.BARTER_TYPE);

        if (!TextUtils.isEmpty(barterType)) {
            mSupportedBarterOptions = Arrays
                    .asList(barterType.split(AppConstants.BARTER_TYPE_SEPARATOR));
        } else {
            mSupportedBarterOptions = Collections.emptyList();
        }

        setBarterOptions();
        updateShareIntent(mBookTitle);
    }

    /**
     * Sets the barter options, based on the supported barter typer
     */
    private void setBarterOptions() {

        if (!mOwnedByUser) {
            mBarterOptionsContainer.setVisibility(View.VISIBLE);

            if (mSupportedBarterOptions.contains(AppConstants.BarterType.BARTER)) {
                mBarterButton.setEnabled(true);
            } else {
                mBarterButton.setEnabled(false);
            }

            if (mSupportedBarterOptions.contains(AppConstants.BarterType.SALE)) {
                mBuyButton.setEnabled(true);

                final String value = mBookDetails.getString(DatabaseColumns.VALUE);
                if (!TextUtils.isEmpty(value)) {
                    mBuyButton.setText(getString(R.string.buy_for, value));
                }
            } else {
                mBuyButton.setEnabled(false);
            }

            if (mSupportedBarterOptions.contains(AppConstants.BarterType.LEND)) {
                mBorrowButton.setEnabled(true);
            } else {
                mBorrowButton.setEnabled(false);
            }
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
                getActivity().finish();
                return true;
            }

            case R.id.action_edit_book: {
                final Bundle args = new Bundle(2);
                args.putString(Keys.ID, mId);
                args.putBoolean(Keys.EDIT_MODE, true);

                final Intent editBookIntent = new Intent(getActivity(),
                        AddOrEditBookActivity.class);
                editBookIntent.putExtra(Keys.ID, mId);
                editBookIntent.putExtra(Keys.EDIT_MODE, true);

                startActivityForResult(editBookIntent, AppConstants.RequestCodes.EDIT_BOOK);

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

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        if (requestCode == AppConstants.RequestCodes.EDIT_BOOK && resultCode == ActionBarActivity
                .RESULT_OK) {
            updateBookDetails(data.getExtras());
        } else {
            super.onActivityResult(requestCode, resultCode, data);
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
                getActivity().finish();
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

    @Override
    public void onClick(View view) {

        final int id = view.getId();

        if (id == R.id.button_barter) {

            final Intent chatIntent = new Intent(AppConstants.ACTION_LAUNCH_CHAT);
            chatIntent.putExtra(Keys.CHAT_MESSAGE, getString(R.string.barter_book));
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(chatIntent);

        } else if (id == R.id.button_buy) {

            final Intent chatIntent = new Intent(AppConstants.ACTION_LAUNCH_CHAT);
            chatIntent.putExtra(Keys.CHAT_MESSAGE, getString(R.string.buy_book));
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(chatIntent);
        }
    }
}
