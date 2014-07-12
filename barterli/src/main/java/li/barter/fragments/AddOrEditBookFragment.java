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

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

import com.android.volley.Request.Method;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity.AlertStyle;
import li.barter.analytics.AnalyticsConstants.Screens;
import li.barter.data.DBInterface;
import li.barter.data.DBInterface.AsyncDbQueryCallback;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.TableUserBooks;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.GoogleBookSearchKey;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.BarterType;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.QueryTokens;
import li.barter.utils.Logger;
import li.barter.utils.Utils;
import li.barter.widgets.autocomplete.INetworkSuggestCallbacks;
import li.barter.widgets.autocomplete.NetworkedAutoCompleteTextView;
import li.barter.widgets.autocomplete.Suggestion;

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out,
                    popEnterAnimation = R.anim.zoom_in,
                    popExitAnimation = R.anim.slide_out_to_right)
public class AddOrEditBookFragment extends AbstractBarterLiFragment implements
        AsyncDbQueryCallback, INetworkSuggestCallbacks,
        OnCheckedChangeListener {

    private static final String TAG = "AddOrEditBookFragment";

    private EditText                      mIsbnEditText;
    private NetworkedAutoCompleteTextView mTitleEditText;
    private EditText                      mAuthorEditText;
    private EditText                      mDescriptionEditText;
    private EditText                      mSellPriceEditText;
    private CheckBox                      mBarterCheckBox;
    private CheckBox                      mSellCheckBox;
    private CheckBox                      mLendCheckBox;
    private CheckBox[]                    mBarterTypeCheckBoxes;
    private String                        mIsbnNumber;
    private boolean                       mHasFetchedDetails;
    private boolean                       mEditMode;
    private String                        mId;

    private String mImage_Url;
    private String mPublicationYear;
    private String mGoogleBooksApiKey;
    private final String mBookSelection = DatabaseColumns.ID
            + SQLConstants.EQUALS_ARG;

    /**
     * On resume, if <code>true</code> and the user has logged in, immediately perform the request
     * to add the book to server. This is to handle where the case where tries to add a book without
     * logging in and we move to the login flow
     */
    private boolean mShouldSubmitOnResume;

    /**
     * Whether a upload request is happening
     */
    private boolean mIsUpoading;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        setActionBarTitle(R.string.editbook_title);
        final View view = inflater
                .inflate(R.layout.fragment_add_or_edit_book, container, false);
        initViews(view);
        mGoogleBooksApiKey = getString(R.string.google_maps_v2_api_key);
        getActivity().getWindow()
                     .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                                               | WindowManager.LayoutParams
                             .SOFT_INPUT_STATE_HIDDEN);
        final Bundle extras = getArguments();

        // If extras are null, it means that user has to decided to add the
        // book completely manually
        if (extras != null) {

            mEditMode = extras.getBoolean(Keys.EDIT_MODE);

            if (mEditMode) {
                mId = extras.getString(Keys.ID);
                setActionBarTitle(R.string.editbook_title2);

                //Reached here by editing current user's book
                loadBookDetails(QueryTokens.LOAD_BOOK_DETAIL_FOR_EDIT, mId);

            } else {
                mIsbnNumber = extras.getString(Keys.ISBN);
                Logger.d(TAG, "Book Isbn:" + mIsbnNumber);

                if (savedInstanceState != null) {
                    mHasFetchedDetails = savedInstanceState
                            .getBoolean(Keys.HAS_FETCHED_INFO);
                } else {
                    loadDetailsForIntent(extras);
                }
                if (!mHasFetchedDetails && !TextUtils.isEmpty(mIsbnNumber)) {
                    getBookInfoFromServer(mIsbnNumber);
                }
            }

        }

        if (savedInstanceState != null) {
            mShouldSubmitOnResume = savedInstanceState
                    .getBoolean(Keys.SUBMIT_ON_RESUME);

        }

        return view;
    }

    /**
     * Load the details of a particular book
     *
     * @param queryToken The Query token to use after the load callback returns
     * @param bookId     The id of the book to load
     */
    private void loadBookDetails(final int queryToken, final String bookId) {

        DBInterface
                .queryAsync(queryToken, getTaskTag(), null,
                            false, TableUserBooks.NAME, null, mBookSelection, new String[]{
                                bookId
                        }, null, null, null, null, this
                );
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_add_or_edit_book, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_save && !mIsUpoading) {
            checkAndFinishBookSubmitTask();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Does the job of checking if user is logged in and finishes the book add/update process
     */
    private void checkAndFinishBookSubmitTask() {
        if (isInputValid()) {

            if (!isLoggedIn()) {

                mShouldSubmitOnResume = true;
                final Bundle loginArgs = new Bundle(1);
                loginArgs.putString(Keys.UP_NAVIGATION_TAG, FragmentTags.BS_ADD_BOOK);

                loadFragment(mContainerViewId, (AbstractBarterLiFragment) Fragment
                                     .instantiate(getActivity(), LoginFragment.class
                                             .getName(), loginArgs), FragmentTags.LOGIN_TO_ADD_BOOK,
                             true,
                             FragmentTags.BS_ADD_BOOK
                );

            } else {

                if (mEditMode) {
                    mIsUpoading = true;
                    updateBookOnServer(null);
                } else {
                    if (hasFirstName()) {
                        mIsUpoading = true;
                        createBookOnServer(null);
                    } else {
                        showAddFirstNameDialog();
                    }
                }
            }
        }

    }

    /**
     * Gets references to the Views
     *
     * @param view The content view of the fragment
     */
    private void initViews(final View view) {
        mIsbnEditText = (EditText) view.findViewById(R.id.edit_text_isbn);

        mTitleEditText = (NetworkedAutoCompleteTextView) view
                .findViewById(R.id.edit_text_title);
        mTitleEditText.setNetworkSuggestCallbacks(this);
        mTitleEditText.setSuggestCountThreshold(3);
        mTitleEditText.setSuggestWaitThreshold(400);
        mAuthorEditText = (EditText) view.findViewById(R.id.edit_text_author);

        mDescriptionEditText = (EditText) view
                .findViewById(R.id.edit_text_description);

        mSellPriceEditText = (EditText) view.findViewById(R.id.edit_sell_price);

        initBarterTypeCheckBoxes(view);

    }

    /**
     * Gets the references to the barter type checkboxes, set the tags to simplify building the tags
     * array when sending the request to server
     *
     * @param view The content view of the fragment
     */
    private void initBarterTypeCheckBoxes(final View view) {
        mBarterCheckBox = (CheckBox) view.findViewById(R.id.checkbox_barter);
        mSellCheckBox = (CheckBox) view.findViewById(R.id.checkbox_sell);
        mLendCheckBox = (CheckBox) view.findViewById(R.id.checkbox_lend);

        // Set the barter tags
        mBarterCheckBox.setTag(R.string.tag_barter_type, BarterType.BARTER);
        mSellCheckBox.setTag(R.string.tag_barter_type, BarterType.SALE);
        mLendCheckBox.setTag(R.string.tag_barter_type, BarterType.LEND);

        mSellCheckBox.setOnCheckedChangeListener(this);

        mBarterTypeCheckBoxes = new CheckBox[3];
        mBarterTypeCheckBoxes[0] = mBarterCheckBox;
        mBarterTypeCheckBoxes[1] = mSellCheckBox;
        mBarterTypeCheckBoxes[2] = mLendCheckBox;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Keys.HAS_FETCHED_INFO, mHasFetchedDetails);
        outState.putBoolean(Keys.SUBMIT_ON_RESUME, mShouldSubmitOnResume);
    }

    /**
     * @param args The Fragment arguments
     */
    private void loadDetailsForIntent(final Bundle args) {

        mIsbnNumber = args.getString(Keys.ISBN);
        final String title = args.getString(Keys.BOOK_TITLE);
        final String author = args.getString(Keys.AUTHOR);
        final String description = args.getString(Keys.DESCRIPTION);

        final List<String> barterTypes = args
                .getStringArrayList(Keys.BARTER_TYPES);

        mIsbnEditText.setText(mIsbnNumber);
        mTitleEditText.setTextWithFilter(title, false);
        mAuthorEditText.setText(author);
        mDescriptionEditText.setText(description);

        setCheckBoxesForBarterTypes(barterTypes);

    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    /**
     * Fetches the book info from server based on the ISBN number or book title
     *
     * @param isbn The ISBN Number of the book to get info for
     */
    private void getBookInfoFromServer(final String isbn) {

        final BlRequest request = new BlRequest(Method.GET, HttpConstants.getGoogleBooksUrl()
                + ApiEndpoints.VOLUMES, null, mVolleyCallbacks);
        request.setRequestId(RequestId.GET_BOOK_INFO);

        final Map<String, String> params = new HashMap<String, String>(1);
        params.put(HttpConstants.Q, GoogleBookSearchKey.ISBN + isbn);

        final Map<String, String> headers = new HashMap<String, String>(1);
        headers.put(HttpConstants.KEY, mGoogleBooksApiKey);

        request.setParams(params);
        request.setHeaders(headers);

        addRequestToQueue(request, true, R.string.unable_to_fetch_book_info, false);

    }

    /**
     * Fetches the book info from server based on the Book ID. This is dependent on the service used
     * to fetch the book info
     *
     * @param bookId The unique ID of the book to get info for
     */
    private void getBookInfoById(final String bookId, final String bookName) {

        final BlRequest request = new BlRequest(Method.GET, HttpConstants.getGoogleBooksUrl()
                + ApiEndpoints.VOLUMES, null, mVolleyCallbacks);
        request.setRequestId(RequestId.GOOGLEBOOKS_SHOW_BOOK);

        final Map<String, String> params = new HashMap<String, String>(1);
        params.put(HttpConstants.Q, GoogleBookSearchKey.ID + bookId + "+"
                + GoogleBookSearchKey.INTITLE + bookName);

        final Map<String, String> headers = new HashMap<String, String>(1);
        headers.put(HttpConstants.KEY, mGoogleBooksApiKey);

        request.setParams(params);
        request.setHeaders(headers);

        addRequestToQueue(request, false, 0, false);

    }

    /**
     * Updates the barter types checkboxes
     *
     * @param barterTypes A list of barter types chosen for the book
     */
    private void setCheckBoxesForBarterTypes(final List<String> barterTypes) {
        if (barterTypes != null) {
            for (final CheckBox checkBox : mBarterTypeCheckBoxes) {

                if (barterTypes.contains(checkBox
                                                 .getTag(R.string.tag_barter_type))) {
                    checkBox.setChecked(true);
                } else {
                    checkBox.setChecked(false);
                }
            }
        }
    }

    /**
     * Add the book to the server
     *
     * @param locationObject The location at which to create the book, if <code>null</code>, uses
     *                       the user's preferred location
     */
    private void createBookOnServer(final JSONObject locationObject) {

        if (mShouldSubmitOnResume) {
            mShouldSubmitOnResume = false;
        }

        try {

            final JSONObject requestObject = new JSONObject();
            final JSONObject bookJson = new JSONObject();
            bookJson.put(HttpConstants.TITLE, mTitleEditText.getText()
                                                            .toString());
            bookJson.put(HttpConstants.AUTHOR, mAuthorEditText.getText()
                                                              .toString());
            bookJson.put(HttpConstants.DESCRIPTION, mDescriptionEditText
                    .getText().toString());
            if (!mSellPriceEditText.getText().toString().equals("")) {
                bookJson.put(HttpConstants.VALUE, mSellPriceEditText.getText()
                                                                    .toString());
            }

            bookJson.put(HttpConstants.PUBLICATION_YEAR, mPublicationYear);
            if (mIsbnEditText.getText().toString().length() == 13) {
                bookJson.put(HttpConstants.ISBN_13, mIsbnEditText.getText()
                                                                 .toString());
            } else if (mIsbnEditText.getText().toString().length() == 10) {
                bookJson.put(HttpConstants.ISBN_10, mIsbnEditText.getText()
                                                                 .toString());
            }
            bookJson.put(HttpConstants.TAG_NAMES, getBarterTagsArray());
            bookJson.put(HttpConstants.EXT_IMAGE_URL, mImage_Url);

            if (locationObject != null) {
                bookJson.put(HttpConstants.LOCATION, locationObject);
            }
            requestObject.put(HttpConstants.BOOK, bookJson);

            final BlRequest createBookRequest = new BlRequest(Method.POST,
                                                              HttpConstants.getApiBaseUrl()
                                                                      + ApiEndpoints.BOOKS,
                                                              requestObject.toString(),
                                                              mVolleyCallbacks
            );
            createBookRequest.setRequestId(RequestId.CREATE_BOOK);
            addRequestToQueue(createBookRequest, true, 0, true);

        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the book to the server
     *
     * @param locationObject The location at which to create the book, if <code>null</code>, uses
     *                       the user's preferred location
     */
    private void updateBookOnServer(final JSONObject locationObject) {
        try {

            final JSONObject requestObject = new JSONObject();
            final JSONObject bookJson = new JSONObject();
            bookJson.put(HttpConstants.TITLE, mTitleEditText.getText()
                                                            .toString());
            bookJson.put(HttpConstants.AUTHOR, mAuthorEditText.getText()
                                                              .toString());

            bookJson.put(HttpConstants.DESCRIPTION, mDescriptionEditText
                    .getText().toString());
            if (!mSellPriceEditText.getText().toString().equals("")) {
                bookJson.put(HttpConstants.VALUE, mSellPriceEditText.getText()
                                                                    .toString());
            }

            bookJson.put(HttpConstants.PUBLICATION_YEAR, mPublicationYear);
            if (mIsbnEditText.getText().toString().length() == 13) {
                bookJson.put(HttpConstants.ISBN_13, mIsbnEditText.getText()
                                                                 .toString());
            } else if (mIsbnEditText.getText().toString().length() == 10) {
                bookJson.put(HttpConstants.ISBN_10, mIsbnEditText.getText()
                                                                 .toString());
            }
            bookJson.put(HttpConstants.TAG_NAMES, getBarterTagsArray());
            bookJson.put(HttpConstants.EXT_IMAGE_URL, mImage_Url);

            if (locationObject != null) {
                bookJson.put(HttpConstants.LOCATION, locationObject);
            }
            requestObject.put(HttpConstants.BOOK, bookJson);
            requestObject.put(HttpConstants.ID, mId);
            final BlRequest updateBookRequest = new BlRequest(Method.PUT,
                                                              HttpConstants.getApiBaseUrl()
                                                                      + ApiEndpoints.BOOKS,
                                                              requestObject.toString(),
                                                              mVolleyCallbacks
            );
            updateBookRequest.setRequestId(RequestId.UPDATE_BOOK);
            addRequestToQueue(updateBookRequest, true, 0, true);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Build the tags array for books
     *
     * @return A {@link JSONArray} representing the barter tags
     */
    private JSONArray getBarterTagsArray() {

        final JSONArray tagNamesArray = new JSONArray();

        for (CheckBox checkBox : mBarterTypeCheckBoxes) {

            if (checkBox.isChecked()) {
                tagNamesArray.put(checkBox.getTag(R.string.tag_barter_type));
            }
        }

        return tagNamesArray;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mShouldSubmitOnResume && isLoggedIn()) {
            createBookOnServer(null);
        }
    }

    /**
     * Validates the current input. Sets errors for the text fields if there are any errors
     *
     * @return <code>true</code> if there are no errors, <code>fhalse</code> otherwise
     */
    private boolean isInputValid() {

        boolean isValid = true;

        final String title = mTitleEditText.getText().toString();
        final String isbn = mIsbnEditText.getText().toString();

        isValid &= !TextUtils.isEmpty(title);

        if (!isValid) {
            mTitleEditText.setError(getString(R.string.error_enter_title));
        }

        // Validation for at least one barter type set
        if (isValid) {

            // Flag to check if at least one of the barter checkboxes is checked
            boolean anyOneChecked = false;
            for (final CheckBox checkBox : mBarterTypeCheckBoxes) {
                anyOneChecked |= checkBox.isChecked();
            }

            isValid &= anyOneChecked;
            if (!isValid) {
                showCrouton(R.string.select_a_barter_type, AlertStyle.ALERT);
            }
        }

        if (!TextUtils.isEmpty(isbn)) {
            isValid &= isNumeric(isbn);
            if (!((isbn.length() == 13) || (isbn.length() == 10))) {
                isValid = false;
            }
        }

        return isValid;
    }

    @Override
    public void onSuccess(final int requestId,
                          final IBlRequestContract request,
                          final ResponseInfo response) {

        switch (requestId) {
            case RequestId.GET_BOOK_INFO: {
                mTitleEditText.setTextWithFilter(response.responseBundle
                                                         .getString(HttpConstants.TITLE), false);

                mDescriptionEditText.setText(response.responseBundle
                                                     .getString(HttpConstants.DESCRIPTION));
                mAuthorEditText.setText(response.responseBundle
                                                .getString(HttpConstants.AUTHOR));
                mPublicationYear = response.responseBundle
                        .getString(HttpConstants.PUBLICATION_YEAR);

                if (response.responseBundle.containsKey(HttpConstants.ISBN_13)) {
                    mIsbnEditText.setText(response.responseBundle
                                                  .getString(HttpConstants.ISBN_13));
                } else if (response.responseBundle
                        .containsKey(HttpConstants.ISBN_10)) {
                    mIsbnEditText.setText(response.responseBundle
                                                  .getString(HttpConstants.ISBN_10));
                }

                mImage_Url = response.responseBundle
                        .getString(HttpConstants.IMAGE_URL);

                Logger.d(TAG, "image url %s", mImage_Url);
                break;
            }

            case RequestId.GOOGLEBOOKS_SHOW_BOOK: {
                mTitleEditText.setTextWithFilter(response.responseBundle
                                                         .getString(HttpConstants.TITLE), false);

                mDescriptionEditText.setText(response.responseBundle
                                                     .getString(HttpConstants.DESCRIPTION));
                mAuthorEditText.setText(response.responseBundle
                                                .getString(HttpConstants.AUTHOR));
                mPublicationYear = response.responseBundle
                        .getString(HttpConstants.PUBLICATION_YEAR);

                if (response.responseBundle.containsKey(HttpConstants.ISBN_13)) {
                    mIsbnEditText.setText(response.responseBundle
                                                  .getString(HttpConstants.ISBN_13));
                } else if (response.responseBundle
                        .containsKey(HttpConstants.ISBN_10)) {
                    mIsbnEditText.setText(response.responseBundle
                                                  .getString(HttpConstants.ISBN_10));
                }

                mImage_Url = response.responseBundle
                        .getString(HttpConstants.IMAGE_URL);

                Logger.d(TAG, "image url %s", mImage_Url);
                break;
            }

            case RequestId.CREATE_BOOK: {

                final String bookId = response.responseBundle
                        .getString(HttpConstants.ID);
                loadBookDetails(QueryTokens.LOAD_BOOK_DETAIL_FOR_OPEN, bookId);

                break;
            }

            case RequestId.UPDATE_BOOK: {
                loadBookDetails(QueryTokens.LOAD_BOOK_DETAIL_FOR_OPEN, mId);
                break;
            }

            case RequestId.BOOK_SUGGESTIONS: {

                final Suggestion[] fetchedSuggestions = (Suggestion[]) response.responseBundle
                        .getParcelableArray(HttpConstants.RESULTS);

                if ((fetchedSuggestions != null)
                        && (fetchedSuggestions.length > 0)) {
                    mTitleEditText.onSuggestionsFetched((String) request
                            .getExtras().get(Keys.SEARCH), fetchedSuggestions, true);
                }
                break;

            }

            /*
             * This will happen in the case where the user has newly signed in
             * using email/passowrd and doesn't have a first name added yet. The
             * request is placed into the queue from
             * AbstractBarterLiFragment#onDialogClick()
             */
            case RequestId.SAVE_USER_PROFILE: {

                final Bundle userInfo = response.responseBundle;
                Utils.updateUserInfoFromBundle(userInfo, true);
                createBookOnServer(null);
                break;
            }
        }

    }

    @Override
    public void onBadRequestError(final int requestId,
                                  final IBlRequestContract request, final int errorCode,
                                  final String errorMessage, final Bundle errorResponseBundle) {
        if (requestId == RequestId.GET_BOOK_INFO) {
            showCrouton(R.string.unable_to_fetch_book_info, AlertStyle.ERROR);
        } else if (requestId == RequestId.CREATE_BOOK) {
            showCrouton(R.string.unable_to_create_book, AlertStyle.ERROR);
        }

    }

    @Override
    public void onOtherError(int requestId, IBlRequestContract request,
                             int errorCode) {
        if (requestId == RequestId.GET_BOOK_INFO) {
            showCrouton(R.string.unable_to_fetch_book_info, AlertStyle.ERROR);
        } else if (requestId == RequestId.CREATE_BOOK) {
            showCrouton(R.string.unable_to_create_book, AlertStyle.ERROR);
        }
    }

    @Override
    public void onPostExecute(IBlRequestContract request) {
        super.onPostExecute(request);
        final int requestId = request.getRequestId();
        if (requestId == RequestId.CREATE_BOOK
                || requestId == RequestId.UPDATE_BOOK) {
            mIsUpoading = false;
        }
    }

    @Override
    public void onInsertComplete(final int token, final Object cookie,
                                 final long insertRowId) {

    }

    @Override
    public void onDeleteComplete(final int token, final Object cookie,
                                 final int deleteCount) {

    }

    @Override
    public void onUpdateComplete(final int token, final Object cookie,
                                 final int updateCount) {

    }

    @Override
    public void onQueryComplete(final int token, final Object cookie,
                                final Cursor cursor) {
        if (token == QueryTokens.LOAD_BOOK_DETAIL_FOR_EDIT) {
            if (cursor.moveToFirst()) {

                /* Force a crash if trying to edit someone else's book. This should not happen in
                 production */
                final String userId = cursor
                        .getString(cursor.getColumnIndex(DatabaseColumns.USER_ID));

                if (!userId.equals(AppConstants.UserInfo.INSTANCE.getId())) {
                    throw new RuntimeException("Trying to edit someone else's book!");
                }
                mIsbnEditText.setText(cursor.getString(cursor
                                                               .getColumnIndex(
                                                                       DatabaseColumns.ISBN_10)));
                mTitleEditText.setTextWithFilter(cursor.getString(cursor
                                                                          .getColumnIndex(
                                                                                  DatabaseColumns
                                                                                          .TITLE
                                                                          )),
                                                 false
                );
                mAuthorEditText.setText(cursor.getString(cursor
                                                                 .getColumnIndex(
                                                                         DatabaseColumns.AUTHOR)));
                mDescriptionEditText.setText(cursor.getString(cursor
                                                                      .getColumnIndex(
                                                                              DatabaseColumns
                                                                                      .DESCRIPTION
                                                                      )));

                mPublicationYear = cursor
                        .getString(cursor
                                           .getColumnIndex(DatabaseColumns.PUBLICATION_YEAR));

                mImage_Url = cursor.getString(cursor
                                                      .getColumnIndex(DatabaseColumns.IMAGE_URL));

                final String value = cursor.getString(cursor
                                                              .getColumnIndex(
                                                                      DatabaseColumns.VALUE));

                if (!TextUtils.isEmpty(value)) {

                    mSellPriceEditText.setVisibility(View.VISIBLE);
                    mSellPriceEditText.setText(value);
                }

                final String barterType = cursor.getString(cursor
                                                                   .getColumnIndex(
                                                                           DatabaseColumns
                                                                                   .BARTER_TYPE
                                                                   ));

                if (!TextUtils.isEmpty(barterType)) {
                    setBarterCheckboxes(barterType);
                }
            }

            cursor.close();
        } else if (token == QueryTokens.LOAD_BOOK_DETAIL_FOR_OPEN) {

            if (cursor.moveToFirst()) {

                final Bundle showBooksArgs = Utils.cursorToBundle(cursor);
                final Intent resultIntent = new Intent();
                resultIntent.putExtras(showBooksArgs);
                getActivity().setResult(ActionBarActivity.RESULT_OK, resultIntent);
                getActivity().finish();
            }
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

    /**
     * Function to see if a string is numeric
     */

    private boolean isNumeric(final String str) {
        return str.matches("\\d+");
    }

    @Override
    public void performNetworkQuery(
            final NetworkedAutoCompleteTextView textView,
            final String query) {

        if (textView.getId() == R.id.edit_text_title) {
            Logger.v(TAG, "Perform network query %s", query);

            final BlRequest request = new BlRequest(Method.GET, HttpConstants.getGoogleBooksUrl()
                    + ApiEndpoints.VOLUMES, null, mVolleyCallbacks);
            request.setRequestId(RequestId.BOOK_SUGGESTIONS);

            final Map<String, String> params = new HashMap<String, String>(1);
            params.put(HttpConstants.Q, GoogleBookSearchKey.INTITLE + query);

            final Map<String, String> headers = new HashMap<String, String>(1);
            headers.put(HttpConstants.KEY, mGoogleBooksApiKey);

            request.setParams(params);
            request.setHeaders(headers);

            addRequestToQueue(request, false, 0, false);
        }
    }

    @Override
    public void onSuggestionClicked(
            final NetworkedAutoCompleteTextView textView,
            final Suggestion suggestion) {

        if (textView.getId() == R.id.edit_text_title) {
            Logger.v(TAG, "On Suggestion Clicked %s", suggestion);
            getBookInfoById(suggestion.id, suggestion.name);

            hideKeyBoard(textView);

        }

    }

    @Override
    public void onCheckedChanged(final CompoundButton buttonView,
                                 final boolean isChecked) {
        if (isChecked) {
            mSellPriceEditText.setVisibility(View.VISIBLE);
        } else {
            mSellPriceEditText.setVisibility(View.GONE);
        }

    }

    @Override
    protected String getAnalyticsScreenName() {

        if (mEditMode) {
            return Screens.EDIT_BOOK;
        } else {
            return Screens.ADD_BOOK;
        }
    }

}
