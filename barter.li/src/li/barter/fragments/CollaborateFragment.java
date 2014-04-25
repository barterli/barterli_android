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

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity.AlertStyle;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;

/**
 * Fragment to Collect Collaboration enlistings.
 * 
 * @author Sharath Pandeshwar
 */
@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class CollaborateFragment extends AbstractBarterLiFragment implements
                OnClickListener {

    private static final String TAG = "CollaborateFragment";
    private EditText            mAboutMeTextView;
    private Button              mSubmitEnlistingButton;
    private RadioButton         mEnlistAsSponsorRadioButton;
    private RadioButton         mEnlistAsDesignerRadioButton;
    private RadioButton         mEnlistAsDeveloperRadioButton;
    private RadioButton         mEnlistAsUserRadioButton;
    private RadioButton         mEnlistAsVolunteerRadioButton;
    private RadioGroup          mEnlistOptionsRadioGroup;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setActionBarDrawerToggleEnabled(false);
        setActionBarTitle(R.string.Collaborate_fragment_title);
        final View view = inflater
                        .inflate(R.layout.fragment_collaborate_with_us, null);
        mAboutMeTextView = (EditText) view.findViewById(R.id.text_about_me);
        mEnlistAsSponsorRadioButton = (RadioButton) view
                        .findViewById(R.id.radio_sponsor);

        mEnlistAsDesignerRadioButton = (RadioButton) view
                        .findViewById(R.id.radio_designer);

        mEnlistAsDeveloperRadioButton = (RadioButton) view
                        .findViewById(R.id.radio_developer);

        mEnlistAsUserRadioButton = (RadioButton) view
                        .findViewById(R.id.radio_user);

        mEnlistAsVolunteerRadioButton = (RadioButton) view
                        .findViewById(R.id.radio_volunteer);

        mEnlistOptionsRadioGroup = (RadioGroup) view
                        .findViewById(R.id.radio_enlist_options);

        mSubmitEnlistingButton = (Button) view
                        .findViewById(R.id.button_submit_enlisting);
        mSubmitEnlistingButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.button_submit_enlisting: {
                final String mAboutMeText = mAboutMeTextView.getText()
                                .toString();

                if (!(mEnlistAsSponsorRadioButton.isChecked()
                                || mEnlistAsDesignerRadioButton.isChecked()
                                || mEnlistAsDeveloperRadioButton.isChecked()
                                || mEnlistAsUserRadioButton.isChecked() || mEnlistAsVolunteerRadioButton
                                    .isChecked())) {
                    showCrouton("Please Select in what role you would like to work with us", AlertStyle.ERROR);
                    return;
                }

                final int mRadioButtonID = mEnlistOptionsRadioGroup
                                .getCheckedRadioButtonId();
                final RadioButton mChosenRadioButton = (RadioButton) mEnlistOptionsRadioGroup
                                .findViewById(mRadioButtonID);
                final String mSelectedRole = mChosenRadioButton.getText()
                                .toString();

                if (TextUtils.isEmpty(mAboutMeText)) {
                    showCrouton("Please include a short description about you.", AlertStyle.ERROR);
                    return;
                }

                final String[] mRecipients = new String[1];
                mRecipients[0] = getResources()
                                .getString(R.string.barterli_email);
                final String mSubject = "Collaborate Request for Barter.Li in the role of "
                                + mSelectedRole;
                sendEmail(mRecipients, mSubject, mAboutMeText);

                //Make a NetWork request Here
                //                final JSONObject requestObject = new JSONObject();
                //
                //                try {
                //                    requestObject.put(HttpConstants.REGISTER_TYPE, mSelectedRole);
                //                    requestObject.put(HttpConstants.BODY, mAboutMeText);
                //
                //                    final BlRequest request = new BlRequest(Method.POST, HttpConstants.getApiBaseUrl()
                //                                    + ApiEndpoints.COLLABORATE_REQUEST, requestObject
                //                                    .toString(), mVolleyCallbacks);
                //                    request.setRequestId(RequestId.COLLABORATE_REQUEST);
                //                    addRequestToQueue(request, true, 0);
                //                } catch (final JSONException e) {
                //                    // Should never happen
                //                    Logger.e(TAG, e, "Error building report bug json");
                //                }

            }
        }
    }

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    @Override
    public void onSuccess(final int requestId,
                    final IBlRequestContract request,
                    final ResponseInfo response) {
        //        if (requestId == RequestId.COLLABORATE_REQUEST) {
        //            String mStatus = response.responseBundle
        //                            .getString(HttpConstants.STATUS);
        //            if (mStatus.equals(HttpConstants.SUCCESS)) {
        //                showCrouton("Thanks for registering with us. We shall get back to you shortly.", AlertStyle.INFO);
        //                mAboutMeTextView.setText("");
        //                mEnlistOptionsRadioGroup.clearCheck();
        //            } else {
        //                showCrouton("Something went Wrong.", AlertStyle.ERROR);
        //            }
        //
        //        }
    }

    @Override
    public void onBadRequestError(final int requestId,
                    final IBlRequestContract request, final int errorCode,
                    final String errorMessage, final Bundle errorResponseBundle) {
    }

    private void sendEmail(final String[] recipients, final String subject,
                    final String message) {
        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, recipients);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, message);
        emailIntent.setType("text/plain");
        startActivity(Intent
                        .createChooser(emailIntent, "Choose an Email client :"));
    }

}
