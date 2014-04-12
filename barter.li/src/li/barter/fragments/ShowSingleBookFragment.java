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
import android.provider.BaseColumns;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity.AlertStyle;
import li.barter.data.DBInterface;
import li.barter.data.DBInterface.AsyncDbQueryCallback;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.TableLocations;
import li.barter.data.TableMyBooks;
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
import li.barter.utils.Logger;
import li.barter.utils.SharedPreferenceHelper;

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class ShowSingleBookFragment extends AbstractBarterLiFragment implements
                AsyncDbQueryCallback {

    private static final String TAG = "ShowSingleBookFragment";

    private TextView            mIsbnTextView;
    private TextView            mTitleTextView;
    private TextView            mAuthorTextView;
    private TextView            mDescriptionTextView;
    private TextView            mPublicationDateTextView;
    private CheckBox            mBarterCheckBox;
    private CheckBox            mReadCheckBox;
    private CheckBox            mSellCheckBox;
    private CheckBox            mWishlistCheckBox;
    private CheckBox            mGiveAwayCheckBox;
    private CheckBox            mKeepPrivateCheckBox;
    private CheckBox[]          mBarterTypeCheckBoxes;
    private String              mBookId;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container);
        setHasOptionsMenu(true);
        final View view = inflater
                        .inflate(R.layout.fragment_show_single_book, container, false);
        initViews(view);

        getActivity().getWindow()
                        .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        final Bundle extras = getArguments();

        if (extras != null) {
            mBookId = extras.getString(Keys.BOOK_ID);
        }

        loadBookDetails();
        setActionBarDrawerToggleEnabled(false);
        return view;
    }

    private void loadBookDetails() {
        DBInterface.queryAsync(QueryTokens.LOAD_MY_BOOKS, null, false, TableMyBooks.NAME, null, DatabaseColumns.BOOK_ID
                        + SQLConstants.EQUALS_ARG, new String[] {
            mBookId
        }, null, null, null, null, this);

        //DBInterface.queryAsync(QueryTokens.LOAD_MY_BOOKS, null, false, TableMyBooks.NAME, null, null, null, null, null, null, null, this);

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
        mDescriptionTextView = (TextView) view
                        .findViewById(R.id.text_description);
        mPublicationDateTextView = (TextView) view
                        .findViewById(R.id.text_publication_date);

    }

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile_show, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {
                onUpNavigate();
                return true;
            }

            case R.id.action_edit_profile: {
                Bundle mEditBookArgs = new Bundle();
                mEditBookArgs.putString(Keys.BOOK_ID, mBookId);
                loadFragment(mContainerViewId, (AbstractBarterLiFragment) Fragment
                        .instantiate(getActivity(), AddOrEditBookFragment.class
                                        .getName(), mEditBookArgs), FragmentTags.ADD_OR_EDIT_BOOK, true, FragmentTags.BS_SINGLE_BOOK);
                
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    @Override
    public void onSuccess(int requestId, IBlRequestContract request,
                    ResponseInfo response) {
    }

    @Override
    public void onBadRequestError(int requestId, IBlRequestContract request,
                    int errorCode, String errorMessage,
                    Bundle errorResponseBundle) {
    }

    @Override
    public void onInsertComplete(int token, Object cookie, long insertRowId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeleteComplete(int token, Object cookie, int deleteCount) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpdateComplete(int token, Object cookie, int updateCount) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {

        if (token == QueryTokens.LOAD_MY_BOOKS) {
            if (cursor.moveToFirst()) {
                mIsbnTextView.setText(cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.ISBN_10)));
                mTitleTextView.setText(cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.TITLE)));
                mAuthorTextView.setText(cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.AUTHOR)));
                mDescriptionTextView.setText(cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.DESCRIPTION)));
                mPublicationDateTextView.setText(cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.PUBLICATION_YEAR)));
                //Log.v(TAG, cursor.getString(cursor
                                //.getColumnIndex(DatabaseColumns.BARTER_TYPE)));
            }
        }

    }

}
