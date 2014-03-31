/*******************************************************************************
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

import java.io.File;
import java.util.Locale;

import li.barter.R;
import li.barter.data.DBInterface;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.TableLocations;
import li.barter.data.DBInterface.AsyncDbQueryCallback;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.QueryTokens;
import li.barter.utils.SharedPreferenceHelper;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request.Method;

/**
 * @author Sharath Pandeshwar
 */

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class ProfileFragment extends AbstractBarterLiFragment implements
                AsyncDbQueryCallback {

    private static final String TAG          = "ProfileFragment";

    private TextView            mProfileNameTextView;
    private TextView            mAboutMeTextView;
    private TextView            mPreferredLocationTextView;
    private ImageView           mProfileImageView;
    private String              mDefaultName = "Your Name";

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container);
        setHasOptionsMenu(true);
        final View view = inflater
                        .inflate(R.layout.fragment_profile_show, null);

        mProfileNameTextView = (TextView) view
                        .findViewById(R.id.text_profile_name);
        mAboutMeTextView = (TextView) view.findViewById(R.id.text_about_me);
        mPreferredLocationTextView = (TextView) view
                        .findViewById(R.id.text_current_location);
        mProfileImageView = (ImageView) view
                        .findViewById(R.id.image_profile_pic);

        File mAvatarfile = new File(Environment.getExternalStorageDirectory(), "barterli_avatar_small.png");
        if (mAvatarfile.exists()) {
            Bitmap bmp = BitmapFactory
                            .decodeFile(mAvatarfile.getAbsolutePath());
            mProfileImageView.setImageBitmap(bmp);
        }

        if (SharedPreferenceHelper
                        .contains(getActivity(), R.string.pref_first_name)
                        && !(TextUtils.isEmpty(SharedPreferenceHelper
                                        .getString(getActivity(), R.string.pref_first_name)))) {

            String mFirstName = SharedPreferenceHelper
                            .getString(getActivity(), R.string.pref_first_name);

            Log.v(TAG, mFirstName);

            String mLastName = "";

            if (SharedPreferenceHelper
                            .contains(getActivity(), R.string.pref_last_name)) {
                mLastName = SharedPreferenceHelper
                                .getString(getActivity(), R.string.pref_last_name);
            }

            String mFullName = String.format(Locale.US, mFirstName + " "
                            + mLastName);
            if (TextUtils.isEmpty(mFullName)) {
                mFullName = mDefaultName;
            }
            mProfileNameTextView.setText(mFullName);
        } else {
            mProfileNameTextView.setText(mDefaultName);
        }

        if (SharedPreferenceHelper
                        .contains(getActivity(), R.string.pref_location)) {
            loadPreferredLocation();
        }

        if (SharedPreferenceHelper
                        .contains(getActivity(), R.string.pref_description)) {
            mAboutMeTextView.setText(SharedPreferenceHelper
                            .getString(getActivity(), R.string.pref_description));
        } else {
            mAboutMeTextView.setVisibility(View.INVISIBLE);
        }

        setActionBarDrawerToggleEnabled(false);
        fetchMyBooks();
        return view;
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
                loadFragment(mContainerViewId, (AbstractBarterLiFragment) Fragment
                                .instantiate(getActivity(), EditProfileFragment.class
                                                .getName(), null), FragmentTags.EDIT_PROFILE, true, FragmentTags.BS_PROFILE);
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    /**
     * Load the user's preferred location from DB Table and show it in the
     * profile page.
     */

    private void loadPreferredLocation() {

        DBInterface.queryAsync(QueryTokens.LOAD_LOCATION_FROM_PROFILE_SHOW_PAGE, null, false, TableLocations.NAME, null, DatabaseColumns.LOCATION_ID
                        + SQLConstants.EQUALS_ARG, new String[] {
            SharedPreferenceHelper
                            .getString(getActivity(), R.string.pref_location)
        }, null, null, null, null, this);
    }

    public void onQueryComplete(int token, Object cookie, Cursor cursor) {
        if (token == QueryTokens.LOAD_LOCATION_FROM_PROFILE_SHOW_PAGE) {

            if (cursor.moveToFirst()) {
                String mPrefAddressName = cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.NAME))
                                + ", "
                                + cursor.getString(cursor
                                                .getColumnIndex(DatabaseColumns.ADDRESS));

                mPreferredLocationTextView.setText(mPrefAddressName);
            }

            cursor.close();

        }
    }

    @Override
    public void onStop() {
        super.onStop();
        DBInterface.cancelAsyncQuery(QueryTokens.LOAD_LOCATION_FROM_PROFILE_SHOW_PAGE);
    }

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    @Override
    public void onSuccess(int requestId, IBlRequestContract request,
                    ResponseInfo response) {
        // TODO Auto-generated method stub
        if (requestId == RequestId.GET_USER_PROFILE) {
            Log.v(TAG, response.toString());
        }

    }

    @Override
    public void onBadRequestError(int requestId, IBlRequestContract request,
                    int errorCode, String errorMessage,
                    Bundle errorResponseBundle) {
        // TODO Auto-generated method stub

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

    /**
     * Fetches books owned by the current user
     */

    private void fetchMyBooks() {
        final BlRequest request = new BlRequest(Method.GET, HttpConstants.getApiBaseUrl()
                        + ApiEndpoints.GET_USER_INFO, null, mVolleyCallbacks);
        request.setRequestId(RequestId.GET_USER_PROFILE);
        addRequestToQueue(request, true, 0);
    }

}
