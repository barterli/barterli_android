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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

import li.barter.R;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants.BarterType;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.Logger;

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class AddOrEditBookFragment extends AbstractBarterLiFragment implements
                OnClickListener {

    private static final String TAG = "AddOrEditBookFragment";

    private EditText            mIsbnEditText;
    private EditText            mTitleEditText;
    private EditText            mAuthorEditText;
    private EditText            mDescriptionEditText;
    private EditText            mPublicationYearEditText;
    private CheckBox            mBarterCheckBox;
    private CheckBox            mReadCheckBox;
    private CheckBox            mSellCheckBox;
    private CheckBox            mWishlistCheckBox;
    private CheckBox            mGiveAwayCheckBox;
    private CheckBox            mKeepPrivateCheckBox;
    private CheckBox[]          mBarterTypeCheckBoxes;
    private String              mBookId;
    private boolean             mHasFetchedDetails;

    /**
     * On resume, if <code>true</code> and the user has logged in, immediately
     * perform the request to add the book to server. This is to handle where
     * the case where tries to add a book without logging in and we move to the
     * login flow
     */
    private boolean             mShouldSubmitOnResume;

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
            mBookId = extras.getString(Keys.ISBN);
            Logger.d(TAG, "Book Id:" + mBookId);

            if (savedInstanceState != null) {
                mHasFetchedDetails = savedInstanceState
                                .getBoolean(Keys.HAS_FETCHED_INFO);
            }

            else {
                loadDetailsForIntent(extras);
            }
            if (!mHasFetchedDetails && !TextUtils.isEmpty(mBookId)) {
                getBookInfoFromServer(mBookId);
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
        mTitleEditText = (EditText) view.findViewById(R.id.edit_text_title);
        mAuthorEditText = (EditText) view.findViewById(R.id.edit_text_author);
        mDescriptionEditText = (EditText) view
                        .findViewById(R.id.edit_text_description);
        mPublicationYearEditText = (EditText) view
                        .findViewById(R.id.edit_text_publication_year);

        initBarterTypeCheckBoxes(view);

    }

    /**
     * Gets the references to the barter type checkboxes, set the tags to
     * simplify building the tags array when sending the request to server
     * 
     * @param view The content view of the fragment
     */
    private void initBarterTypeCheckBoxes(View view) {
        mBarterCheckBox = (CheckBox) view.findViewById(R.id.checkbox_barter);
        mReadCheckBox = (CheckBox) view.findViewById(R.id.checkbox_read);
        mSellCheckBox = (CheckBox) view.findViewById(R.id.checkbox_sell);
        mWishlistCheckBox = (CheckBox) view
                        .findViewById(R.id.checkbox_wishlist);
        mGiveAwayCheckBox = (CheckBox) view
                        .findViewById(R.id.checkbox_give_away);
        mKeepPrivateCheckBox = (CheckBox) view
                        .findViewById(R.id.checkbox_keep_private);

        //Set the barter tags
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

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onUpNavigate();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * @param args The Fragment arguments
     */
    private void loadDetailsForIntent(final Bundle args) {

        mBookId = args.getString(Keys.ISBN);
        final String title = args.getString(Keys.BOOK_TITLE);
        final String author = args.getString(Keys.AUTHOR);
        final String description = args.getString(Keys.DESCRIPTION);
        final String publicationYear = args.getString(Keys.PUBLICATION_YEAR);
        final String[] barterTypes = args.getStringArray(Keys.BARTER_TYPES);

        mIsbnEditText.setText(mBookId);
        mTitleEditText.setText(title);
        mAuthorEditText.setText(author);
        mDescriptionEditText.setText(description);
        mPublicationYearEditText.setText(publicationYear);

        setCheckBoxesForBarterTypes(barterTypes);

    }

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    /**
     * Fetches the book info from server based on the ISBN number
     * 
     * @param bookId The ISBN Id of the book to get info for
     */
    private void getBookInfoFromServer(final String bookId) {

        final BlRequest request = new BlRequest(Method.POST, HttpConstants.getApiBaseUrl()
                        + ApiEndpoints.BOOK_INFO, null, mVolleyCallbacks);
        final Map<String, String> params = new HashMap<String, String>();
        request.setRequestId(RequestId.GET_BOOK_INFO);
        params.put(HttpConstants.Q, bookId);
        request.setParams(params);
        addRequestToQueue(request, true, R.string.unable_to_fetch_book_info);
    }

    /**
     * Updates the barter types checkboxes
     * 
     * @param barterTypes An array of barter types chosen for the book
     */
    private void setCheckBoxesForBarterTypes(final String[] barterTypes) {
        if ((barterTypes != null) && (barterTypes.length >= 1)) {
            // TODO Set barter types
        }
    }

    /**
     * Add the book to the server
     */
    private void createBookOnServer() {

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
            bookJson.put(HttpConstants.PUBLICATION_YEAR, mPublicationYearEditText
                            .getText().toString());
            bookJson.put(HttpConstants.TAG_NAMES, getBarterTagsArray());
            requestObject.put(HttpConstants.BOOK, bookJson);

            final BlRequest createBookRequest = new BlRequest(Method.POST, HttpConstants.getApiBaseUrl()
                            + ApiEndpoints.BOOKS, bookJson.toString(), mVolleyCallbacks);
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
        if (mShouldSubmitOnResume) {
            createBookOnServer();
        }
    }

    @Override
    public void onClick(final View v) {

        if ((v.getId() == R.id.button_submit) && isInputValid()) {

            if (!isLoggedIn()) {

                mShouldSubmitOnResume = true;
                final Bundle loginArgs = new Bundle(1);
                loginArgs.putString(Keys.BACKSTACK_TAG, FragmentTags.BS_ADD_BOOK);

                loadFragment(mContainerViewId, (AbstractBarterLiFragment) Fragment
                                .instantiate(getActivity(), LoginFragment.class
                                                .getName(), loginArgs), FragmentTags.LOGIN_TO_ADD_BOOK, true, FragmentTags.BS_ADD_BOOK);

            } else {

                createBookOnServer();
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

        isValid &= !TextUtils.isEmpty(title);

        if (!isValid) {
            mTitleEditText.setError(getString(R.string.error_enter_title));
        }

        //Validation for at least one barter type set
        if (isValid) {

            //Flag to check if at least one of the barter checkboxes is checked
            boolean anyOneChecked = false;
            for (CheckBox checkBox : mBarterTypeCheckBoxes) {
                anyOneChecked |= checkBox.isChecked();
            }

            isValid &= anyOneChecked;
            if (!isValid) {
                showToast(R.string.select_a_barter_type, false);
            }
        }
        return isValid;
    }

    @Override
    public void onSuccess(int requestId, IBlRequestContract request,
                    ResponseInfo response) {

        if (requestId == RequestId.GET_BOOK_INFO) {
            //TODO Read book info from bundle
        } else if (requestId == RequestId.CREATE_BOOK) {
            showToast(R.string.book_added, true);
            getFragmentManager().popBackStack();
        }

    }

    @Override
    public void onBadRequestError(int requestId, IBlRequestContract request,
                    int errorCode, String errorMessage,
                    Bundle errorResponseBundle) {
        if (requestId == RequestId.GET_BOOK_INFO) {
            showToast(R.string.unable_to_fetch_book_info, false);
        }
    }

}
