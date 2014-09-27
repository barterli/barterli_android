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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import li.barter.R;
import li.barter.activities.SelectPreferredLocationActivity;
import li.barter.analytics.AnalyticsConstants.Screens;
import li.barter.data.DBInterface;
import li.barter.data.DBInterface.AsyncDbQueryCallback;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.TableLocations;
import li.barter.fragments.dialogs.SingleChoiceDialogFragment;
import li.barter.http.BlMultiPartRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.QueryTokens;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.AvatarBitmapTransformation;
import li.barter.utils.PhotoUtils;
import li.barter.utils.SharedPreferenceHelper;
import li.barter.utils.Utils;
import li.barter.widgets.RoundedCornerImageView;

/**
 * @author Sharath Pandeshwar
 */

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out,
                    popEnterAnimation = R.anim.zoom_in,
                    popExitAnimation = R.anim.slide_out_to_right)
public class EditProfileFragment extends AbstractBarterLiFragment implements
        OnClickListener, AsyncDbQueryCallback {

    private static final String TAG = "EditProfileFragment";

    private TextView               mFirstNameTextView;
    private TextView               mLastNameTextView;
    private TextView               mAboutMeTextView;
    private TextView               mPreferredLocationTextView;
    private RoundedCornerImageView mProfileImageView;
    private boolean mWasProfileImageChanged = false;

    private Bitmap mCompressedPhoto;
    private File   mAvatarfile;
    private Uri    mCameraImageCaptureUri;
    private Uri    mGalleryImageCaptureUri;
    private final String mAvatarFileName = "barterli_avatar_small.png";

    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;
    private static final int PICK_FROM_FILE   = 3;

    private AvatarBitmapTransformation mAvatarBitmapTransformation;


    /**
     * Reference to the Dialog Fragment for selecting the picture type
     */
    private SingleChoiceDialogFragment mChoosePictureDialogFragment;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        final View view = inflater
                .inflate(R.layout.fragment_profile_edit, null);
        setActionBarTitle(R.string.text_edit_profile);
        mAvatarBitmapTransformation = new AvatarBitmapTransformation(AvatarBitmapTransformation.AvatarSize.LARGE);

        mFirstNameTextView = (TextView) view.findViewById(R.id.text_first_name);
        mLastNameTextView = (TextView) view.findViewById(R.id.text_last_name);
        mAboutMeTextView = (TextView) view.findViewById(R.id.text_about_me);
        mPreferredLocationTextView = (TextView) view
                .findViewById(R.id.text_current_location);
        mProfileImageView = (RoundedCornerImageView) view
                .findViewById(R.id.image_profile_pic);

        mPreferredLocationTextView.setOnClickListener(this);

        mProfileImageView.setOnClickListener(this);
        mAvatarfile = new File(Environment.getExternalStorageDirectory(), mAvatarFileName);

        if (mAvatarfile.exists()) {
            final Bitmap bmp = BitmapFactory.decodeFile(mAvatarfile
                                                                .getAbsolutePath());
            mProfileImageView.setImageBitmap(bmp);
        }

        if (SharedPreferenceHelper
                .contains(R.string.pref_first_name)) {
            mFirstNameTextView
                    .setText(SharedPreferenceHelper
                                     .getString(R.string.pref_first_name));
        } else {
            mFirstNameTextView
                    .setText(UserInfo.INSTANCE.getFirstName());
        }

        if (SharedPreferenceHelper
                .contains(R.string.pref_last_name)) {
            mLastNameTextView.setText(SharedPreferenceHelper
                                              .getString(R.string.pref_last_name));
        }

        if (SharedPreferenceHelper
                .contains(R.string.pref_description)) {
            mAboutMeTextView.setText(SharedPreferenceHelper
                                             .getString(R.string.pref_description));
        }

        if (SharedPreferenceHelper
                .contains(R.string.pref_location)) {
            loadPreferredLocation();
        }

        // for loading profile image

        String mProfileImageUrl = "";
        if (SharedPreferenceHelper
                .contains(R.string.pref_profile_image)) {
            mProfileImageUrl = SharedPreferenceHelper
                    .getString(R.string.pref_profile_image);

        }
        Picasso.with(getActivity()).load(mProfileImageUrl)
               .transform(mAvatarBitmapTransformation)
               .error(R.drawable.pic_avatar)
               .into(mProfileImageView.getTarget());

        mCameraImageCaptureUri = Uri.fromFile(new File(android.os.Environment
                                                               .getExternalStorageDirectory(),
                                                       "barterli_avatar.jpg"
        ));

        mChoosePictureDialogFragment = (SingleChoiceDialogFragment) getFragmentManager()
                .findFragmentByTag(FragmentTags.DIALOG_TAKE_PICTURE);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.menu_profile_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {
                getActivity().finish();
                return true;
            }

            case R.id.action_profile_save: {

                if (isInputValid()) {
                    final String firstName = mFirstNameTextView.getText()
                                                               .toString();
                    final String lastName = mLastNameTextView.getText()
                                                             .toString();
                    final String aboutMe = mAboutMeTextView.getText()
                                                           .toString();

                    saveProfileInfoToServer(firstName, lastName, aboutMe, mWasProfileImageChanged,
                                            mAvatarfile
                                                    .getAbsolutePath()
                    );
                }
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    /**
     * Used to validate input before sending to server. Also sets the error messages on the
     * respective fields.
     *
     * @return <code>true</code> if input is valid, <code>false</code> otherwise
     */
    private boolean isInputValid() {

        boolean isValid = false;

        final String firstName = mFirstNameTextView.getText().toString();

        isValid = !TextUtils.isEmpty(firstName);

        if (!isValid) {
            mFirstNameTextView.setError(getString(R.string.error_required));
        }
        return isValid;
    }

    /**
     * Load the user's preferred location from DB Table and show it in the profile page.
     */

    private void loadPreferredLocation() {
        DBInterface.queryAsync(QueryTokens.LOAD_LOCATION_FROM_PROFILE_EDIT_PAGE, getTaskTag(), null,
                               false, TableLocations.NAME, null, DatabaseColumns.LOCATION_ID
                        + SQLConstants.EQUALS_ARG, new String[]{
                        SharedPreferenceHelper
                                .getString(R.string.pref_location)
                }, null, null, null, null, this
        );
    }

    @Override
    public void onQueryComplete(final int token, final Object cookie,
                                final Cursor cursor) {
        if (token == QueryTokens.LOAD_LOCATION_FROM_PROFILE_EDIT_PAGE) {

            if (cursor.moveToFirst()) {
                final String mPrefPlaceName = cursor.getString(cursor
                                                                       .getColumnIndex(
                                                                               DatabaseColumns
                                                                                       .NAME
                                                                       ));
                final String mPrefPlaceAddress = cursor.getString(cursor
                                                                          .getColumnIndex(
                                                                                  DatabaseColumns
                                                                                          .ADDRESS
                                                                          ));

                if (!TextUtils.isEmpty(mPrefPlaceName)) {
                    final String preferredLocation = getString(R.string.format_address_underline,
                                                               mPrefPlaceName,
                                                               (TextUtils.isEmpty(
                                                                       mPrefPlaceAddress) ? "" :
                                                                       mPrefPlaceAddress)
                    );
                    mPreferredLocationTextView.setText(preferredLocation);
                }


            }

            cursor.close();

        }
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.text_current_location: {

                final Intent editLocationIntent = new Intent(getActivity(),
                                                             SelectPreferredLocationActivity.class);
                startActivityForResult(editLocationIntent,
                                       AppConstants.RequestCodes.EDIT_PREFERRED_LOCATION);
                break;
            }

            case R.id.image_profile_pic: {
                showChoosePictureSourceDialog();
                break;
            }
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                                 final Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        switch (requestCode) {
            case PICK_FROM_CAMERA:
                // doCrop(PICK_FROM_CAMERA);
                setAndSaveImage(mCameraImageCaptureUri, PICK_FROM_CAMERA);
                break;

            case PICK_FROM_FILE:
                mGalleryImageCaptureUri = data.getData();
                setAndSaveImage(mGalleryImageCaptureUri, PICK_FROM_FILE);
                // doCrop(PICK_FROM_FILE);
                break;

            case CROP_FROM_CAMERA:
                final Bundle extras = data.getExtras();
                if (extras != null) {
                    mCompressedPhoto = extras.getParcelable("data");
                    mProfileImageView.setImageBitmap(mCompressedPhoto);
                }
                PhotoUtils.saveImage(mCompressedPhoto, "barterli_avatar_small.png");
                break;

            case AppConstants.RequestCodes.EDIT_PREFERRED_LOCATION: {
                loadPreferredLocation();
                break;
            }

        }
    } // End of onActivityResult

    /**
     * Method to handle click on profile image
     */
    private void showChoosePictureSourceDialog() {

        mChoosePictureDialogFragment = new SingleChoiceDialogFragment();
        mChoosePictureDialogFragment
                .show(AlertDialog.THEME_HOLO_LIGHT, R.array.take_photo_choices, 0,
                      R.string.take_picture, getFragmentManager(), true,
                      FragmentTags.DIALOG_TAKE_PICTURE);

    }

    /**
     * Set the Profile Image and Save it locally
     *
     * @param uri             URI of the image to be saved.
     * @param source_of_image If the image was from Gallery or Camera
     */

    private void setAndSaveImage(final Uri uri, final int source_of_image) {
        String source_string;
        if (source_of_image == PICK_FROM_FILE) {
            source_string = "Gallery";
        } else {
            source_string = "Camera";
        }

        mCompressedPhoto = PhotoUtils
                .rotateBitmapIfNeededAndCompressIfTold(getActivity(), uri, source_string, true);

        if (mCompressedPhoto != null) {
            mProfileImageView.setImageBitmap(mCompressedPhoto);
            PhotoUtils.saveImage(mCompressedPhoto, mAvatarFileName);
        }
        mWasProfileImageChanged = true;
    }

    /**
     * Method to update the user profile.
     *
     * @param firstName          First Name of the person
     * @param lastName           Last Name of the person
     * @param aboutMeDescription A Brief Introduction about the person
     * @param shouldIncludePic   Should the Profile picture be sent.
     * @param profilePicPath     Path of the image file to be sent, if should be sent.
     */

    private void saveProfileInfoToServer(final String firstName,
                                         final String lastName, final String aboutMeDescription,
                                         final Boolean shouldIncludePic,
                                         final String profilePicPath) {

        final String url = HttpConstants.getApiBaseUrl()
                + ApiEndpoints.UPDATE_USER_INFO;

        final JSONObject mUserProfileObject = new JSONObject();
        final JSONObject mUserProfileMasterObject = new JSONObject();
        try {
            mUserProfileObject.put(HttpConstants.FIRST_NAME, firstName);
            mUserProfileObject.put(HttpConstants.LAST_NAME, lastName);
            mUserProfileObject
                    .put(HttpConstants.DESCRIPTION, aboutMeDescription);
            mUserProfileMasterObject
                    .put(HttpConstants.USER, mUserProfileObject);

            final BlMultiPartRequest updateUserProfileRequest = new BlMultiPartRequest(Method.PUT,
                                                                                       url, null,
                                                                                       mVolleyCallbacks);

            updateUserProfileRequest
                    .addMultipartParam(HttpConstants.USER, "application/json",
                                       mUserProfileMasterObject
                                               .toString()
                    );
            if (shouldIncludePic) {
                updateUserProfileRequest
                        .addFile(HttpConstants.PROFILE_PIC, profilePicPath);
            }

            updateUserProfileRequest.setRequestId(RequestId.SAVE_USER_PROFILE);
            addRequestToQueue(updateUserProfileRequest, true, 0, true);

        } catch (final JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSuccess(final int requestId,
                          final IBlRequestContract request,
                          final ResponseInfo response) {

        if (requestId == RequestId.SAVE_USER_PROFILE) {

            SharedPreferenceHelper.set(R.string.pref_force_user_refetch, true);

            final Bundle userInfo = response.responseBundle;
            Utils.updateUserInfoFromBundle(userInfo, true);
            getActivity().setResult(ActionBarActivity.RESULT_OK);
            getActivity().finish();
        }

    }

    @Override
    public void onBadRequestError(final int requestId,
                                  final IBlRequestContract request, final int errorCode,
                                  final String errorMessage, final Bundle errorResponseBundle) {
        Log.v(TAG, "Volley error");

    }

    @Override
    public void onInsertComplete(final int token, final Object cookie,
                                 final long insertRowId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDeleteComplete(final int token, final Object cookie,
                                 final int deleteCount) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpdateComplete(final int token, final Object cookie,
                                 final int updateCount) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean willHandleDialog(final DialogInterface dialog) {

        if ((mChoosePictureDialogFragment != null)
                && mChoosePictureDialogFragment.getDialog()
                                               .equals(dialog)) {
            return true;
        }
        return super.willHandleDialog(dialog);
    }

    @Override
    public void onDialogClick(final DialogInterface dialog, final int which) {

        if ((mChoosePictureDialogFragment != null)
                && mChoosePictureDialogFragment.getDialog()
                                               .equals(dialog)) {

            if (which == 0) { // Pick from camera
                final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraImageCaptureUri);

                try {
                    startActivityForResult(
                            Intent.createChooser(intent, getString(R.string.complete_action_using)),
                            PICK_FROM_CAMERA);
                } catch (final ActivityNotFoundException e) {
                    e.printStackTrace();
                }

            } else if (which == 1) { // pick from file
                final Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(
                        Intent.createChooser(intent, getString(R.string.complete_action_using)),
                        PICK_FROM_FILE);
            }
        } else {
            super.onDialogClick(dialog, which);
        }
    }

    @Override
    protected String getAnalyticsScreenName() {
        return Screens.EDIT_PROFILE;
    }

}
