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
import java.util.ArrayList;
import java.util.List;

import li.barter.R;
import li.barter.adapters.CropOptionAdapter;
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
import li.barter.models.CropOption;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.QueryTokens;
import li.barter.utils.PhotoUtils;
import li.barter.utils.SharedPreferenceHelper;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.MultiPartRequest;

/**
 * @author Sharath Pandeshwar
 */

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class ProfileFragment extends AbstractBarterLiFragment implements
                AsyncDbQueryCallback {

    private static final String TAG              = "ProfileFragment";

    private TextView            mProfileNameTextView;
    private TextView            mAboutMeTextView;
    private TextView            mPreferredLocationTextView;
    private ImageView           mProfileImageView;
    //private ImageView           mEditPreferredLocationImageView;
    private Uri                 mImageCaptureUri;
    private Bitmap              mCompressedPhoto;
    // private Boolean mHasAboutMeDescriptionChanged = false;

    private static final int    PICK_FROM_CAMERA = 1;
    private static final int    CROP_FROM_CAMERA = 2;
    private static final int    PICK_FROM_FILE   = 3;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container);
        setHasOptionsMenu(true);
        final View view = inflater
                        .inflate(R.layout.fragment_profile_show, null);

        mProfileNameTextView = (TextView) view.findViewById(R.id.profile_name);
        mAboutMeTextView = (TextView) view.findViewById(R.id.about_me);
        mPreferredLocationTextView = (TextView) view
                        .findViewById(R.id.current_location_text);
        mProfileImageView = (ImageView) view
                        .findViewById(R.id.profile_pic_thumbnail);

        if (SharedPreferenceHelper
                        .getBoolean(getActivity(), R.string.pref_is_profile_pic_set)) {
            File mAvatarfile = new File(Environment.getExternalStorageDirectory(), "barterli_avatar_small.png");
            if (mAvatarfile.exists()) {
                Bitmap bmp = BitmapFactory.decodeFile(mAvatarfile
                                .getAbsolutePath());
                mProfileImageView.setImageBitmap(bmp);
            }
        }

        if (SharedPreferenceHelper
                        .contains(getActivity(), R.string.pref_profile_first_name)) {

            String fullName = SharedPreferenceHelper
                            .getString(getActivity(), R.string.pref_profile_first_name)
                            + " ";

            if (SharedPreferenceHelper
                            .contains(getActivity(), R.string.pref_profile_last_name)) {
                fullName += SharedPreferenceHelper
                                .getString(getActivity(), R.string.pref_profile_last_name);
            }
            mProfileNameTextView.setText(fullName);
        }

        if (SharedPreferenceHelper
                        .contains(getActivity(), R.string.pref_location)) {
            loadPreferredLocation();
        }

        if (SharedPreferenceHelper
                        .contains(getActivity(), R.string.pref_profile_about_me_description)) {
            mAboutMeTextView.setText(SharedPreferenceHelper
                            .getString(getActivity(), R.string.pref_profile_about_me_description));
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

    /*
     * (non-Javadoc)
     * @see li.barter.fragments.AbstractBarterLiFragment#getVolleyTag()
     */
    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    @Override
    public void onSuccess(int requestId, IBlRequestContract request,
                    ResponseInfo response) {
        // TODO Auto-generated method stub
        if(requestId == RequestId.GET_USER_PROFILE){
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

    private void fetchMyBooks(){
        final BlRequest request = new BlRequest(Method.GET, HttpConstants.getApiBaseUrl()
                        + ApiEndpoints.GET_USER_INFO, null, mVolleyCallbacks);
        request.setRequestId(RequestId.GET_USER_PROFILE);
        addRequestToQueue(request, true, 0);
    }


}
