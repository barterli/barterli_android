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

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity.AlertStyle;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.Logger;

/**
 * Fragment to Collect/Report Bugs
 * 
 * @author Sharath Pandeshwar
 */
@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class ReportBugFragment extends AbstractBarterLiFragment implements
                OnClickListener {

    private static final String TAG = "ReportBugFragment";
    private EditText            mReportedBugTextView;
    private Button              mReportBugButton;
    private RadioButton         mReportBugSelect;
    private RadioButton         mReportSuggestionSelect;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setActionBarDrawerToggleEnabled(false);
        setActionBarTitle(R.string.Report_fragment_title);
        final View view = inflater.inflate(R.layout.fragment_report_bug, null);
        mReportedBugTextView = (EditText) view
                        .findViewById(R.id.text_bug_description);
        mReportBugSelect = (RadioButton) view
                        .findViewById(R.id.radio_reportbug);
        mReportSuggestionSelect = (RadioButton) view
                        .findViewById(R.id.radio_suggestfeature);
        mReportBugButton = (Button) view.findViewById(R.id.button_report_bug);
        mReportBugButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.button_report_bug: {

                if (mReportBugSelect.isChecked()) {
                    final String ReportedBugTitle = getResources()
                                    .getString(R.string.text_bug_title);
                    final String mReportedBugText = mReportedBugTextView
                                    .getText().toString();

                    if (TextUtils.isEmpty(mReportedBugText)) {
                        showCrouton("Please Enter a Valid Text", AlertStyle.ERROR);
                        return;
                    }
                    //Make a NetWork request Here
                    final JSONObject requestObject = new JSONObject();

                    try {
                        requestObject.put(HttpConstants.BUG_TITLE, ReportedBugTitle);
                        requestObject.put(HttpConstants.BUG_BODY, mReportedBugText);
                        requestObject.put(HttpConstants.BUG_LABEL, HttpConstants.LABEL_FOR_BUG);
                        final BlRequest request = new BlRequest(Method.POST, HttpConstants.getApiBaseUrl()
                                        + ApiEndpoints.REPORT_BUG, requestObject
                                        .toString(), mVolleyCallbacks);
                        request.setRequestId(RequestId.REPORT_BUG);
                        addRequestToQueue(request, true, 0);
                    } catch (final JSONException e) {
                        // Should never happen
                        Logger.e(TAG, e, "Error building report bug json");
                    }

                } else if (mReportSuggestionSelect.isChecked()) {
                    final String mSuggestedFeatureTitle = getResources()
                                    .getString(R.string.text_suggest_feature_title);
                    final String mSuggestedFeatureText = mReportedBugTextView
                                    .getText().toString();
                    if (TextUtils.isEmpty(mSuggestedFeatureText)) {
                        showCrouton("Please Enter a Valid Text", AlertStyle.ERROR);
                        return;
                    }
                    //Make a NetWork request Here
                    final JSONObject requestObject = new JSONObject();

                    try {
                        requestObject.put(HttpConstants.BUG_TITLE, mSuggestedFeatureTitle);
                        requestObject.put(HttpConstants.BUG_BODY, mSuggestedFeatureText);
                        requestObject.put(HttpConstants.BUG_LABEL, HttpConstants.LABEL_FOR_FEATURE);
                        final BlRequest request = new BlRequest(Method.POST, HttpConstants.getApiBaseUrl()
                                        + ApiEndpoints.REPORT_BUG, requestObject
                                        .toString(), mVolleyCallbacks);
                        request.setRequestId(RequestId.SUGGEST_FEATURE);
                        addRequestToQueue(request, true, 0);
                    } catch (final JSONException e) {
                        // Should never happen
                        Logger.e(TAG, e, "Error building create suggest feature json");
                    }

                } else {
                    showCrouton("Please Select Atleast One Option", AlertStyle.ERROR);
                }
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

        if (requestId == RequestId.REPORT_BUG) {
            final String mStatus = response.responseBundle
                            .getString(HttpConstants.STATUS);
            if (mStatus.equals(HttpConstants.SUCCESS)) {
                String mMessage = "Thanks for reporting bug. ";
                if (!isLoggedIn()) {
                    mMessage += "Make the most by registering and adding books.";
                }
                showCrouton(mMessage, AlertStyle.INFO);
                mReportedBugTextView.setText("");
            } else {
                showCrouton("Something went Wrong.", AlertStyle.ERROR);
            }

        }
        if (requestId == RequestId.SUGGEST_FEATURE) {
            final String mStatus = response.responseBundle
                            .getString(HttpConstants.STATUS);
            if (mStatus.equals(HttpConstants.SUCCESS)) {
                String mMessage = "Thanks for your invaluable feedback. ";
                if (!isLoggedIn()) {
                    mMessage += "Make the most by registering and adding books.";
                }
                showCrouton(mMessage, AlertStyle.INFO);
                mReportedBugTextView.setText("");
            } else {
                showCrouton("Something went Wrong.", AlertStyle.ERROR);
            }

        }

    }

    @Override
    public void onBadRequestError(final int requestId,
                    final IBlRequestContract request, final int errorCode,
                    final String errorMessage, final Bundle errorResponseBundle) {
    }

}
