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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import li.barter.R;
import li.barter.adapters.CropOptionAdapter;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.models.CropOption;
import li.barter.utils.AppConstants.FragmentTags;
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
                OnClickListener {

    private static final String TAG              = "ProfileFragment";

    private TextView            mProfileNameTextView;
    private TextView            mAboutMeTextView;
    private TextView            mPreferredLocationTextView;
    private ImageView           mProfileImageView;
    private ImageView           mEditPreferredLocationImageView;
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
        mEditPreferredLocationImageView = (ImageView) view
                        .findViewById(R.id.edit_current_location_button);

        mProfileImageView.setOnClickListener(this);
        mEditPreferredLocationImageView.setOnClickListener(this);

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
            mPreferredLocationTextView.setText(SharedPreferenceHelper
                            .getString(getActivity(), R.string.pref_location));
        }

        if (SharedPreferenceHelper
                        .contains(getActivity(), R.string.pref_profile_about_me_description)) {
            mAboutMeTextView.setText(SharedPreferenceHelper
                            .getString(getActivity(), R.string.pref_profile_about_me_description));
        } else {
            mAboutMeTextView.setVisibility(View.INVISIBLE);
        }

        setActionBarDrawerToggleEnabled(false);
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

    /*
     * (non-Javadoc)
     * @see li.barter.fragments.AbstractBarterLiFragment#getVolleyTag()
     */
    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    /*
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.edit_current_location_button: {
                showToast("Edit Preferred Location!", false);
                break;
            }
        }
    }

    @Override
    public void onSuccess(int requestId, IBlRequestContract request,
                    ResponseInfo response) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onBadRequestError(int requestId, IBlRequestContract request,
                    int errorCode, String errorMessage,
                    Bundle errorResponseBundle) {
        // TODO Auto-generated method stub
        
    }

    /**
     * Method to handle click on profile image
     */
    /*
     * private void editSetProfilePictureDialog() { final String[] items = new
     * String[] { "From Camera", "From Gallery" }; ArrayAdapter<String> adapter
     * = new ArrayAdapter<String>(getActivity(),
     * android.R.layout.select_dialog_item, items); AlertDialog.Builder builder
     * = new AlertDialog.Builder(getActivity()); builder.setAdapter(adapter, new
     * DialogInterface.OnClickListener() { public void onClick(DialogInterface
     * dialog, int item) { if (item == 0) { // Pick from camera Intent intent =
     * new Intent(MediaStore.ACTION_IMAGE_CAPTURE); mImageCaptureUri =
     * Uri.fromFile(new File(Environment .getExternalStorageDirectory(),
     * "barterli_avatar.jpg"));
     * intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
     * mImageCaptureUri); try { intent.putExtra("return-data", true);
     * startActivityForResult(intent, PICK_FROM_CAMERA); } catch
     * (ActivityNotFoundException e) { e.printStackTrace(); } } else { // pick
     * from file Intent intent = new Intent(); intent.setType("image/*");
     * intent.setAction(Intent.ACTION_GET_CONTENT);
     * startActivityForResult(Intent.createChooser(intent,
     * "Complete Action Using"), PICK_FROM_FILE); } } }); final AlertDialog
     * dialog = builder.create(); dialog.show(); } // End of
     * editSetProfilePictureDialog
     */

    /*
     * private void doCrop(final int source_of_image) { final
     * ArrayList<CropOption> cropOptions = new ArrayList<CropOption>(); String
     * source_string; if (source_of_image == PICK_FROM_FILE) { source_string =
     * "Gallery"; } else { source_string = "Camera"; } final String source =
     * source_string; Intent intent = new
     * Intent("com.android.camera.action.CROP"); intent.setType("image/*");
     * List<ResolveInfo> list = getActivity().getPackageManager()
     * .queryIntentActivities(intent, 0); int size = list.size(); if (size == 0)
     * { showToast("Could not find an App to Crop Image", false);
     * mCompressedPhoto = PhotoUtils
     * .rotateBitmapIfNeededAndCompressIfTold(getActivity(), mImageCaptureUri,
     * source, true); if (mCompressedPhoto != null) {
     * mProfileImageView.setImageBitmap(mCompressedPhoto);
     * PhotoUtils.saveImage(mCompressedPhoto, "barterli_avatar_small.png");
     * SharedPreferenceHelper.set(getActivity(),
     * R.string.pref_is_profile_pic_set, true); } return; } else {
     * intent.setData(mImageCaptureUri); intent.putExtra("outputX", 150);
     * intent.putExtra("outputY", 150); intent.putExtra("aspectX", 1);
     * intent.putExtra("aspectY", 1); intent.putExtra("scale", true);
     * intent.putExtra("return-data", true); if (size == 1) { Intent i = new
     * Intent(intent); ResolveInfo res = list.get(0); i.setComponent(new
     * ComponentName(res.activityInfo.packageName, res.activityInfo.name));
     * startActivityForResult(i, CROP_FROM_CAMERA); } else { for (ResolveInfo
     * res : list) { final CropOption co = new CropOption(); co.title =
     * getActivity().getPackageManager() .getApplicationLabel(
     * res.activityInfo.applicationInfo); co.icon =
     * getActivity().getPackageManager() .getApplicationIcon(
     * res.activityInfo.applicationInfo); co.appIntent = new Intent(intent);
     * co.appIntent .setComponent(new ComponentName(
     * res.activityInfo.packageName, res.activityInfo.name));
     * cropOptions.add(co); } CropOptionAdapter adapter = new CropOptionAdapter(
     * getActivity(), cropOptions); AlertDialog.Builder builder = new
     * AlertDialog.Builder( getActivity());
     * builder.setTitle("Choose an Application to Crop Image");
     * builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
     * public void onClick(DialogInterface dialog, int item) {
     * startActivityForResult( cropOptions.get(item).appIntent,
     * CROP_FROM_CAMERA); } }); builder.setOnCancelListener(new
     * DialogInterface.OnCancelListener() {
     * @Override public void onCancel(DialogInterface dialog) { mCompressedPhoto
     * = PhotoUtils .rotateBitmapIfNeededAndCompressIfTold( getActivity(),
     * mImageCaptureUri, source, true); if (mCompressedPhoto != null) {
     * mProfileImageView.setImageBitmap(mCompressedPhoto);
     * PhotoUtils.saveImage(mCompressedPhoto, "barterli_avatar_small.png");
     * SharedPreferenceHelper.set(getActivity(),
     * R.string.pref_is_profile_pic_set, true); } } }); AlertDialog alert =
     * builder.create(); alert.show(); } } } // End of doCrop
     */

    /*
     * public void sendImageToServer(String url) { MultiPartRequest<JSONObject>
     * multipartRequest = new MultiPartRequest<JSONObject>( Method.POST, url,
     * sListener, eListener) {
     * @Override protected Response<JSONObject> parseNetworkResponse(
     * NetworkResponse response) { // TODO Auto-generated method stub return
     * null; } }; } Response.ErrorListener eListener = new
     * Response.ErrorListener() {
     * @Override public void onErrorResponse(VolleyError error, Request<?>
     * request) { } }; Response.Listener<JSONObject> sListener = new
     * Response.Listener<JSONObject>() {
     * @Override public void onResponse(JSONObject response, Request<JSONObject>
     * request) { // TODO Auto-generated method stub } };
     */

}
