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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity.AlertStyle;
import li.barter.data.DBInterface;
import li.barter.data.DBInterface.AsyncDbQueryCallback;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.TableLocations;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants.BarterType;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.QueryTokens;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.Logger;
import li.barter.utils.SharedPreferenceHelper;
import li.barter.widgets.autocomplete.NetworkedAutoCompleteTextView;
import li.barter.widgets.autocomplete.NetworkedAutoCompleteTextView.NetworkSuggestCallbacks;
import li.barter.widgets.autocomplete.NetworkedAutoCompleteTextView.Suggestion;

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class AddOrEditBookFragment extends AbstractBarterLiFragment implements
                OnClickListener, AsyncDbQueryCallback, NetworkSuggestCallbacks {

    private static final String           TAG = "AddOrEditBookFragment";

    private EditText                      mIsbnEditText;
    private NetworkedAutoCompleteTextView mTitleEditText;
    private EditText                      mAuthorEditText;
    private EditText                      mDescriptionEditText;
    private CheckBox                      mBarterCheckBox;
    private CheckBox                      mReadCheckBox;
    private CheckBox                      mSellCheckBox;
    private CheckBox                      mWishlistCheckBox;
    private CheckBox                      mGiveAwayCheckBox;
    private CheckBox                      mKeepPrivateCheckBox;
    private CheckBox[]                    mBarterTypeCheckBoxes;
    private String                        mIsbnNumber;
    private boolean                       mHasFetchedDetails;
    private boolean                       mEditMode;
    private String                        mBookId;
    private String                        mImage_Url;
    private String                        mPublicationYear;

    /**
     * On resume, if <code>true</code> and the user has logged in, immediately
     * perform the request to add the book to server. This is to handle where
     * the case where tries to add a book without logging in and we move to the
     * login flow
     */
    private boolean                       mShouldSubmitOnResume;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container);
        final View view = inflater
                        .inflate(R.layout.fragment_add_or_edit_book, container, false);
        initViews(view);
        view.findViewById(R.id.button_submit).setOnClickListener(this);

        getActivity().getWindow()
                        .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        final Bundle extras = getArguments();

        // If extras are null, it means that user has to decided to add the
        // book completely manually
        if (extras != null) {

            mEditMode = extras.getBoolean(Keys.EDIT_MODE);

            if (mEditMode) {
                mBookId = extras.getString(Keys.BOOK_ID);
                //TODO Load book details from DB
            } else {
                mIsbnNumber = extras.getString(Keys.ISBN);
                Logger.d(TAG, "Book Isbn:" + mIsbnNumber);

                if (savedInstanceState != null) {
                    mHasFetchedDetails = savedInstanceState
                                    .getBoolean(Keys.HAS_FETCHED_INFO);
                }

                else {
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

        setActionBarDrawerToggleEnabled(false);
        return view;
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
        mTitleEditText.setText(title);
        mAuthorEditText.setText(author);
        mDescriptionEditText.setText(description);

        setCheckBoxesForBarterTypes(barterTypes);

    }

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    /**
     * Fetches the book info from server based on the ISBN number
     * 
     * @param bookIsbn The ISBN Id of the book to get info for
     */
    private void getBookInfoFromServer(final String bookIsbn) {

        final BlRequest request = new BlRequest(Method.GET, HttpConstants.getApiBaseUrl()
                        + ApiEndpoints.BOOK_INFO, null, mVolleyCallbacks);
        final Map<String, String> params = new HashMap<String, String>();
        request.setRequestId(RequestId.GET_BOOK_INFO);
        params.put(HttpConstants.Q, bookIsbn);
        request.setParams(params);
        addRequestToQueue(request, true, R.string.unable_to_fetch_book_info);
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
     * @param locationObject The location at which to create the book, if
     *            <code>null</code>, uses the user's preferred location
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

            bookJson.put(HttpConstants.PUBLICATION_YEAR, mPublicationYear);
            //            bookJson.put(HttpConstants.DESCRIPTION, mDescriptionEditText
            //                    .getText().toString());
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

            final BlRequest createBookRequest = new BlRequest(Method.POST, HttpConstants.getApiBaseUrl()
                            + ApiEndpoints.BOOKS, requestObject.toString(), mVolleyCallbacks);
            createBookRequest.setRequestId(RequestId.CREATE_BOOK);
            addRequestToQueue(createBookRequest, true, 0);
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

        for (final CheckBox checkBox : mBarterTypeCheckBoxes) {

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

            if (mEditMode) {
                //TODO Edit book
            } else {
                createBookOnServer(null);
            }
        }
    }

    /**
     * Loads the user's preferred location from the DB
     */
    private void loadPreferredLocation() {

        DBInterface.queryAsync(QueryTokens.LOAD_LOCATION_FROM_ADD_OR_EDIT_BOOK, null, false, TableLocations.NAME, null, DatabaseColumns.LOCATION_ID
                        + SQLConstants.EQUALS_ARG, new String[] {
            SharedPreferenceHelper
                            .getString(getActivity(), R.string.pref_location)
        }, null, null, null, null, this);
    }

    @Override
    public void onClick(final View v) {

        if ((v.getId() == R.id.button_submit) && isInputValid()) {

            if (!isLoggedIn()) {

                mShouldSubmitOnResume = true;
                final Bundle loginArgs = new Bundle(1);
                loginArgs.putString(Keys.UP_NAVIGATION_TAG, FragmentTags.BS_ADD_BOOK);

                loadFragment(mContainerViewId, (AbstractBarterLiFragment) Fragment
                                .instantiate(getActivity(), LoginFragment.class
                                                .getName(), loginArgs), FragmentTags.LOGIN_TO_ADD_BOOK, true, FragmentTags.BS_ADD_BOOK);

            } else {

                if (mEditMode) {
                    //TODO Update book
                } else {
                    createBookOnServer(null);
                }
            }
        }
    }

    /**
     * Validates the current input. Sets errors for the text fields if there are
     * any errors
     * 
     * @return <code>true</code> if there are no errors, <code>fhalse</code>
     *         otherwise
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

        if (requestId == RequestId.GET_BOOK_INFO) {

            mTitleEditText.setText(response.responseBundle
                            .getString(HttpConstants.TITLE));
            mDescriptionEditText.setText(response.responseBundle
                            .getString(HttpConstants.DESCRIPTION));
            mAuthorEditText.setText(response.responseBundle
                            .getString(HttpConstants.AUTHOR));
            mPublicationYear = response.responseBundle
                            .getString(HttpConstants.PUBLICATION_YEAR);

            mImage_Url = response.responseBundle
                            .getString(HttpConstants.IMAGE_URL);

            Logger.d(TAG, "image url %s", mImage_Url);
        } else if (requestId == RequestId.CREATE_BOOK) {
            Logger.v(TAG, "Created Book Id %s", response.responseBundle
                            .getString(HttpConstants.ID_BOOK));

            final String bookId = response.responseBundle
                            .getString(HttpConstants.ID_BOOK);

            final Bundle showBooksArgs = new Bundle(6);
            showBooksArgs.putString(Keys.BOOK_ID, bookId);
            showBooksArgs.putString(Keys.UP_NAVIGATION_TAG, FragmentTags.BS_BOOKS_AROUND_ME);
            showBooksArgs.putString(Keys.USER_ID, UserInfo.INSTANCE.getId());
            loadFragment(mContainerViewId, (AbstractBarterLiFragment) Fragment
                            .instantiate(getActivity(), BookDetailFragment.class
                                            .getName(), showBooksArgs), FragmentTags.MY_BOOK_FROM_ADD_OR_EDIT, true, FragmentTags.BS_BOOKS_AROUND_ME);
        }

    }

    @Override
    public void onBadRequestError(final int requestId,
                    final IBlRequestContract request, final int errorCode,
                    final String errorMessage, final Bundle errorResponseBundle) {
        if (requestId == RequestId.GET_BOOK_INFO) {
            showCrouton(R.string.unable_to_fetch_book_info, AlertStyle.ERROR);
        } else if (requestId == RequestId.GET_BOOK_INFO) {
            showCrouton(R.string.unable_to_create_book, AlertStyle.ERROR);
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

        if (token == QueryTokens.LOAD_LOCATION_FROM_ADD_OR_EDIT_BOOK) {

            try {
                if (cursor.moveToFirst()) {
                    final JSONObject locationObject = new JSONObject();
                    locationObject.put(HttpConstants.NAME, cursor.getString(cursor
                                    .getColumnIndex(DatabaseColumns.NAME)));
                    locationObject.put(HttpConstants.ADDRESS, cursor.getString(cursor
                                    .getColumnIndex(DatabaseColumns.ADDRESS)));
                    locationObject.put(HttpConstants.LATITUDE, cursor.getDouble(cursor
                                    .getColumnIndex(DatabaseColumns.LATITUDE)));
                    locationObject.put(HttpConstants.LONGITUDE, cursor.getDouble(cursor
                                    .getColumnIndex(DatabaseColumns.LONGITUDE)));

                    // TODO Show location address
                }

            } catch (final JSONException e) {
                Logger.e(TAG, e, "Unable to build location object");
            } finally {
                cursor.close();
            }

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        DBInterface.cancelAsyncQuery(QueryTokens.LOAD_LOCATION_FROM_ADD_OR_EDIT_BOOK);
    }

    /**
     * Function to see if a string is numeric
     * 
     * @param str
     * @return
     */

    private boolean isNumeric(final String str) {
        return str.matches("\\d+");
    }

    @Override
    public void performNetworkQuery(NetworkedAutoCompleteTextView textView,
                    String query) {

        if (textView.getId() == R.id.edit_text_title) {
            Logger.v(TAG, "Perform network query %s", query);
        }
    }

    @Override
    public void onSuggestionClicked(NetworkedAutoCompleteTextView textView,
                    Suggestion suggestion) {

    }

}
