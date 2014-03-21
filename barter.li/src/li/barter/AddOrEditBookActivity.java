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

package li.barter;

import com.android.volley.Request;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

import java.util.HashMap;
import java.util.Map;

import li.barter.http.BlJsonObjectRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.JsonUtils;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.ActivityTransition;
import li.barter.utils.SharedPreferenceHelper;

@ActivityTransition(createEnterAnimation = R.anim.activity_slide_in_right, createExitAnimation = R.anim.activity_scale_out, destroyEnterAnimation = R.anim.activity_scale_in, destroyExitAnimation = R.anim.activity_slide_out_right)
public class AddOrEditBookActivity extends AbstractBarterLiActivity implements
                OnClickListener, Listener<JSONObject>, ErrorListener {

    private static final String TAG = "AddOrEditBookActivity";

    private EditText            mIsbnEditText;
    private EditText            mTitleEditText;
    private EditText            mAuthorEditText;
    private EditText            mDescriptionEditText;
    private EditText            mPublicationYearEditText;
    private String              mBookId;
    private boolean             mHasFetchedDetails;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);
        mIsbnEditText = (EditText) findViewById(R.id.edit_text_isbn);
        mTitleEditText = (EditText) findViewById(R.id.edit_text_title);
        mAuthorEditText = (EditText) findViewById(R.id.edit_text_author);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_text_description);
        mPublicationYearEditText = (EditText) findViewById(R.id.edit_text_publication_year);

        findViewById(R.id.button_submit).setOnClickListener(this);

        final Bundle extras = getIntent().getExtras();

        // If extras are null, it means that user has to decided to add the
        // book completely manually
        if (extras != null) {
            mBookId = extras.getString(Keys.BOOK_ID);
            Log.d(TAG, "Book Id:" + mBookId);

            if (savedInstanceState != null) {
                mHasFetchedDetails = savedInstanceState
                                .getBoolean(Keys.BOOL_1);
            }

            else {
                loadDetailsForIntent(extras);
            }
            if (!mHasFetchedDetails && !TextUtils.isEmpty(mBookId)) {
                getBookInfoFromServer(mBookId);
            }
        }

        // TODO Set up TextWatchers for Autocomplete ISBN and Title
    } // End of oncreate

    /**
     * @param extras The intent extras
     */
    private void loadDetailsForIntent(final Bundle extras) {

        mBookId = extras.getString(Keys.BOOK_ID);
        final String title = extras.getString(Keys.BOOK_TITLE);
        final String author = extras.getString(Keys.AUTHOR);
        final String description = extras.getString(Keys.DESCRIPTION);
        final String publicationYear = extras
                        .getString(Keys.PUBLICATION_YEAR);
        final String[] barterTypes = extras
                        .getStringArray(Keys.BARTER_TYPES);

        mIsbnEditText.setText(mBookId);
        mTitleEditText.setText(title);
        mAuthorEditText.setText(author);
        mDescriptionEditText.setText(description);
        mPublicationYearEditText.setText(publicationYear);

        setCheckBoxesForBarterTypes(barterTypes);

    }

    /**
     * Fetches the book info from server based on the ISBN number
     * 
     * @param bookId The ISBN Id of the book to get info for
     */
    private void getBookInfoFromServer(final String bookId) {

        final BlJsonObjectRequest request = new BlJsonObjectRequest(
                        RequestId.GET_BOOK_INFO, HttpConstants.getApiBaseUrl()
                                        + ApiEndpoints.BOOK_INFO, null, this,
                        this);
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
            createBookJson.put(HttpConstants.PUBLICATION_YEAR,
                            mPublicationYearEditText.getText().toString());

            // TODO Add barter types
            final BlJsonObjectRequest createBookRequest = new BlJsonObjectRequest(
                            RequestId.CREATE_BOOK,
                            HttpConstants.getApiBaseUrl() + ApiEndpoints.BOOKS,
                            createBookJson, this, this);

            addRequestToQueue(createBookRequest, true, 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(final View v) {

        if ((v.getId() == R.id.button_submit) && isInputValid()) {

            if (TextUtils.isEmpty(SharedPreferenceHelper.getString(this,
                            R.string.pref_auth_token))) {

                startActivity(new Intent(this, LoginActivity.class));
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
        
        if(!isValid) {
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
                finish();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
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
        final String bookTitle = JsonUtils.getStringValue(response,
                        HttpConstants.TITLE);
        final String bookDescription = JsonUtils.getStringValue(response,
                        HttpConstants.DESCRIPTION);

        JSONObject authorObject = JsonUtils.getJsonObject(response,
                        HttpConstants.AUTHORS);
        String authorName = null;
        if (authorObject != null) {
            authorObject = JsonUtils.getJsonObject(authorObject,
                            HttpConstants.AUTHOR);
            if (authorObject != null) {
                authorName = JsonUtils.getStringValue(authorObject,
                                HttpConstants.NAME);
            }
        }

        final String publicationYear = JsonUtils.getStringValue(response,
                        HttpConstants.PUBLICATION_YEAR);

        mTitleEditText.setText(bookTitle);
        mDescriptionEditText.setText(bookDescription);
        mAuthorEditText.setText(authorName);
        mPublicationYearEditText.setText(publicationYear);

    }

}
