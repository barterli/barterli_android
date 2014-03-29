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

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

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
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

import li.barter.R;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.JsonUtils;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.Logger;

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class AddOrEditBookFragment extends AbstractBarterLiFragment implements
                OnClickListener, Listener<ResponseInfo>, ErrorListener {

    private static final String TAG = "AddOrEditBookFragment";

    private EditText            mIsbnEditText;
    private EditText            mTitleEditText;
    private EditText            mAuthorEditText;
    private EditText            mDescriptionEditText;
    private EditText            mPublicationYearEditText;
    private String              mBookId;
    private boolean             mHasFetchedDetails;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container);
        final View view = inflater
                        .inflate(R.layout.fragment_add_or_edit_book, container, false);
        mIsbnEditText = (EditText) view.findViewById(R.id.edit_text_isbn);
        mTitleEditText = (EditText) view.findViewById(R.id.edit_text_title);
        mAuthorEditText = (EditText) view.findViewById(R.id.edit_text_author);
        mDescriptionEditText = (EditText) view
                        .findViewById(R.id.edit_text_description);
        mPublicationYearEditText = (EditText) view
                        .findViewById(R.id.edit_text_publication_year);

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

        setActionBarDrawerToggleEnabled(false);
        return view;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Keys.HAS_FETCHED_INFO, mHasFetchedDetails);
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
                        + ApiEndpoints.BOOK_INFO, null, this, this);
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

        final JSONObject createBookJson = new JSONObject();

        try {
            createBookJson.put(HttpConstants.TITLE, mTitleEditText.getText()
                            .toString());
            createBookJson.put(HttpConstants.AUTHOR, mAuthorEditText.getText()
                            .toString());
            createBookJson.put(HttpConstants.DESCRIPTION, mDescriptionEditText
                            .getText().toString());
            createBookJson.put(HttpConstants.PUBLICATION_YEAR, mPublicationYearEditText
                            .getText().toString());

            // TODO Add barter types
            final BlRequest createBookRequest = new BlRequest(Method.POST, HttpConstants.getApiBaseUrl()
                            + ApiEndpoints.BOOKS, createBookJson.toString(), this, this);
            createBookRequest.setRequestId(RequestId.CREATE_BOOK);
            addRequestToQueue(createBookRequest, true, 0);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(final View v) {

        if ((v.getId() == R.id.button_submit) && isInputValid()) {

            if (!isLoggedIn()) {

                final Bundle loginArgs = new Bundle(1);
                loginArgs.putString(Keys.BACKSTACK_TAG, FragmentTags.BS_ADD_BOOK);

                loadFragment(mContainerViewId, (AbstractBarterLiFragment) Fragment
                                .instantiate(getActivity(), LoginFragment.class
                                                .getName(), loginArgs), FragmentTags.LOGIN_TO_ADD_BOOK, true, FragmentTags.BS_ADD_BOOK);

            } else {

                loadFragment(mContainerViewId, (AbstractBarterLiFragment) Fragment
                                .instantiate(getActivity(), SelectPreferredLocationFragment.class
                                                .getName(), null), FragmentTags.SELECT_PREFERRED_LOCATION_FROM_ADD_OR_EDIT_BOOK, true, FragmentTags.BS_PREFERRED_LOCATION);
                //createBookOnServer();
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
        return isValid;
    }

    @Override
    public void onErrorResponse(final VolleyError error,
                    final Request<?> request) {

        onRequestFinished();
        if (request instanceof BlRequest) {

            final int requestId = ((BlRequest) request).getRequestId();

            if (requestId == RequestId.GET_BOOK_INFO) {
                showToast(R.string.unable_to_fetch_book_info, false);
            }
        }

    }

    @Override
    public void onResponse(final ResponseInfo response,
                    final Request<ResponseInfo> request) {

        onRequestFinished();

        if (request instanceof BlRequest) {

            //TODO Read book details from response
            final int requestId = ((BlRequest) request).getRequestId();

            /*
             * if (requestId == RequestId.GET_BOOK_INFO) {
             * readBookDetailsFromResponse(response); } else if (requestId ==
             * RequestId.CREATE_BOOK) { showToast(R.string.book_added, true);
             * getFragmentManager().popBackStack(); }
             */
        }
    }

    /**
     * Reads the book details from the response and populates the text fields
     * 
     * @param response The {@link JSONObject} which contains the response
     */
    private void readBookDetailsFromResponse(final JSONObject response) {

        mHasFetchedDetails = true;
        final String bookTitle = JsonUtils
                        .readString(response, HttpConstants.TITLE);
        final String bookDescription = JsonUtils
                        .readString(response, HttpConstants.DESCRIPTION);

        JSONObject authorObject = JsonUtils
                        .readJSONObject(response, HttpConstants.AUTHORS);
        String authorName = null;
        if (authorObject != null) {
            authorObject = JsonUtils
                            .readJSONObject(authorObject, HttpConstants.AUTHOR);
            if (authorObject != null) {
                authorName = JsonUtils
                                .readString(authorObject, HttpConstants.NAME);
            }
        }

        final String publicationYear = JsonUtils
                        .readString(response, HttpConstants.PUBLICATION_YEAR);

        mTitleEditText.setText(bookTitle);
        mDescriptionEditText.setText(bookDescription);
        mAuthorEditText.setText(authorName);
        mPublicationYearEditText.setText(publicationYear);

    }

}
