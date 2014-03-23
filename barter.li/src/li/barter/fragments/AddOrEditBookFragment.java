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
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
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
import li.barter.activities.AbstractBarterLiActivity;
import li.barter.http.BlJsonObjectRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.JsonUtils;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.SharedPreferenceHelper;

@FragmentTransition(enterAnimation = R.anim.activity_slide_in_right, exitAnimation = R.anim.activity_scale_out, popEnterAnimation = R.anim.activity_scale_in, popExitAnimation = R.anim.activity_slide_out_right)
public class AddOrEditBookFragment extends AbstractBarterLiFragment implements
                OnClickListener, Listener<JSONObject>, ErrorListener {

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
                        .inflate(R.layout.activity_edit_book, container, false);
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
            Log.d(TAG, "Book Id:" + mBookId);

            if (savedInstanceState != null) {
                mHasFetchedDetails = savedInstanceState.getBoolean(Keys.BOOL_1);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            Log.d(TAG, "On Home Pressed");
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

        final BlJsonObjectRequest request = new BlJsonObjectRequest(RequestId.GET_BOOK_INFO, HttpConstants
                        .getApiBaseUrl() + ApiEndpoints.BOOK_INFO, null, this, this);
        final Map<String, String> params = new HashMap<String, String>();
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
            final BlJsonObjectRequest createBookRequest = new BlJsonObjectRequest(RequestId.CREATE_BOOK, HttpConstants
                            .getApiBaseUrl() + ApiEndpoints.BOOKS, createBookJson, this, this);

            addRequestToQueue(createBookRequest, true, 0);
        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(final View v) {

        if ((v.getId() == R.id.button_submit) && isInputValid()) {

            if (!isLoggedIn()) {

                loadFragment(mContainerViewId, (AbstractBarterLiFragment) Fragment
                                .instantiate(getActivity(), LoginFragment.class
                                                .getName(), null), FragmentTags.LOGIN, true);

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
        return isValid;
    }

    @Override
    public void onErrorResponse(final VolleyError error,
                    final Request<?> request) {

        onRequestFinished();
        if (request instanceof BlJsonObjectRequest) {

            final int requestId = ((BlJsonObjectRequest) request)
                            .getRequestId();

            if (requestId == RequestId.GET_BOOK_INFO) {
                showToast(R.string.unable_to_fetch_book_info, false);
            }
        }

    }

    @Override
    public void onResponse(final JSONObject response,
                    final Request<JSONObject> request) {

        onRequestFinished();

        if (request instanceof BlJsonObjectRequest) {

            final int requestId = ((BlJsonObjectRequest) request)
                            .getRequestId();

            if (requestId == RequestId.GET_BOOK_INFO) {
                readBookDetailsFromResponse(response);
            } else if (requestId == RequestId.CREATE_BOOK) {
                showToast(R.string.book_added, true);
                getFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Keys.BOOL_1, mHasFetchedDetails);
    }

    /**
     * Reads the book details from the response and populates the text fields
     * 
     * @param response The {@link JSONObject} which contains the response
     */
    private void readBookDetailsFromResponse(final JSONObject response) {

        mHasFetchedDetails = true;
        final String bookTitle = JsonUtils
                        .getStringValue(response, HttpConstants.TITLE);
        final String bookDescription = JsonUtils
                        .getStringValue(response, HttpConstants.DESCRIPTION);

        JSONObject authorObject = JsonUtils
                        .getJsonObject(response, HttpConstants.AUTHORS);
        String authorName = null;
        if (authorObject != null) {
            authorObject = JsonUtils
                            .getJsonObject(authorObject, HttpConstants.AUTHOR);
            if (authorObject != null) {
                authorName = JsonUtils
                                .getStringValue(authorObject, HttpConstants.NAME);
            }
        }

        final String publicationYear = JsonUtils
                        .getStringValue(response, HttpConstants.PUBLICATION_YEAR);

        mTitleEditText.setText(bookTitle);
        mDescriptionEditText.setText(bookDescription);
        mAuthorEditText.setText(authorName);
        mPublicationYearEditText.setText(publicationYear);

    }

}
