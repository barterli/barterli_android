/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and
  * limitations under the License.
 */

package li.barter.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.facebook.LoggingBehavior;
import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.google.android.gms.analytics.HitBuilders.EventBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import li.barter.BarterLiApplication;
import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity.AlertStyle;
import li.barter.activities.AuthActivity;
import li.barter.activities.PasswordResetActivity;
import li.barter.activities.SelectPreferredLocationActivity;
import li.barter.analytics.AnalyticsConstants.Actions;
import li.barter.analytics.AnalyticsConstants.Categories;
import li.barter.analytics.AnalyticsConstants.ParamKeys;
import li.barter.analytics.AnalyticsConstants.ParamValues;
import li.barter.analytics.AnalyticsConstants.Screens;
import li.barter.analytics.GoogleAnalyticsManager;
import li.barter.fragments.dialogs.AddSingleEditTextDialogFragment;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.Logger;
import li.barter.utils.Utils;

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out,
                    popEnterAnimation = R.anim.zoom_in,
                    popExitAnimation = R.anim.slide_out_to_right)
public class LoginFragment extends AbstractBarterLiFragment implements
        OnClickListener, StatusCallback {

    private static final String TAG = "LoginFragment";

    /**
     * Minimum length of the entered password
     */
    private final int mMinPasswordLength = 8;
    private Button                          mFacebookLoginButton;
    private Button                          mGoogleLoginButton;
    private Button                          mSubmitButton;
    private EditText                        mEmailEditText;
    private EditText                        mPasswordEditText;
    private TextView                        mForgotPassword;
    private AddSingleEditTextDialogFragment mAddSingleEditTextDialogFragment;
    private String                          mEmailForPasswordChange;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                             final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        final View view = inflater.inflate(R.layout.fragment_login, null);

        mFacebookLoginButton = (Button) view
                .findViewById(R.id.button_facebook_login);
        mGoogleLoginButton = (Button) view
                .findViewById(R.id.button_google_login);
        mSubmitButton = (Button) view.findViewById(R.id.button_submit);
        mEmailEditText = (EditText) view.findViewById(R.id.edit_text_email);
        mPasswordEditText = (EditText) view
                .findViewById(R.id.edit_text_password);
        mForgotPassword = (TextView) view.findViewById(R.id.forgot_password);

        mForgotPassword.setOnClickListener(this);

        Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);

        Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session
                        .restoreSession(getActivity(), null, this, savedInstanceState);
            }
            if (session == null) {
                session = new Session(getActivity());
            }
            Session.setActiveSession(session);

        }
        mFacebookLoginButton.setOnClickListener(this);
        mGoogleLoginButton.setOnClickListener(this);
        mSubmitButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        final Session session = Session.getActiveSession();
        Session.saveSession(session, outState);
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                                 final Intent data) {

        if (requestCode == AppConstants.RequestCodes.RESET_PASSWORD && resultCode ==
                ActionBarActivity.RESULT_OK) {

            getActivity().setResult(ActionBarActivity.RESULT_OK, data);
            getActivity().finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
        Session.getActiveSession()
               .onActivityResult(getActivity(), requestCode, resultCode, data);


    }

    @Override
    public void onClick(final View v) {

        switch (v.getId()) {

            case R.id.button_facebook_login: {
                GoogleAnalyticsManager
                        .getInstance()
                        .sendEvent(new EventBuilder(Categories.CONVERSION, Actions.SIGN_IN_ATTEMPT)
                                           .set(ParamKeys.TYPE, ParamValues.FACEBOOK));
                final Session session = Session.getActiveSession();
                if (!session.isOpened() && !session.isClosed()) {
                    session.openForRead(new Session.OpenRequest(this)
                                                .setPermissions(Arrays
                                                                        .asList(AppConstants
                                                                                        .FBPERMISSIONS))
                                                .setCallback(this));
                } else {
                    Session.openActiveSession(getActivity(), this, true, this);
                }
                break;
            }

            case R.id.button_google_login: {
                GoogleAnalyticsManager
                        .getInstance()
                        .sendEvent(new EventBuilder(Categories.CONVERSION, Actions.SIGN_IN_ATTEMPT)
                                           .set(ParamKeys.TYPE, ParamValues.GOOGLE));
                ((AuthActivity) getActivity()).getPlusManager().login();
                break;
            }

            case R.id.button_submit: {
                if (isInputValid()) {
                    GoogleAnalyticsManager
                            .getInstance()
                            .sendEvent(
                                    new EventBuilder(Categories.CONVERSION, Actions.SIGN_IN_ATTEMPT)
                                            .set(ParamKeys.TYPE, ParamValues.EMAIL)
                            );
                    login(mEmailEditText.getText().toString(), mPasswordEditText
                            .getText().toString());
                }
                break;
            }

            case R.id.forgot_password: {
                showForgotPasswordDialog();

            }
        }
    }

    /**
     * Show the dialog for the user to enter his email address
     */
    private void showForgotPasswordDialog() {

        mAddSingleEditTextDialogFragment = new AddSingleEditTextDialogFragment();
        mAddSingleEditTextDialogFragment
                .show(AlertDialog.THEME_HOLO_LIGHT, 0, R.string.forgot_password, R.string.submit,
                      R.string.cancel, 0, R.string.email_label, getFragmentManager(), true,
                      FragmentTags.DIALOG_FORGOT_PASSWORD);

    }

    @Override
    public boolean willHandleDialog(final DialogInterface dialog) {

        if ((mAddSingleEditTextDialogFragment != null)
                && mAddSingleEditTextDialogFragment.getDialog()
                                                   .equals(dialog)) {
            return true;
        }
        return false;
    }

    @Override
    public void onDialogClick(final DialogInterface dialog, final int which) {

        if ((mAddSingleEditTextDialogFragment != null)
                && mAddSingleEditTextDialogFragment.getDialog()
                                                   .equals(dialog)) {

            if (which == DialogInterface.BUTTON_POSITIVE) {
                callForgotPassword(mAddSingleEditTextDialogFragment.getName());
            }
        }
    }

    /**
     * Call the password_reset Api
     *
     * @param email The entered email
     */

    private void callForgotPassword(String email) {
        final BlRequest request = new BlRequest(Method.GET, HttpConstants.getApiBaseUrl()
                + ApiEndpoints.PASSWORD_RESET, null, mVolleyCallbacks);
        request.setRequestId(RequestId.REQUEST_RESET_TOKEN);
        mEmailForPasswordChange = email;
        final Map<String, String> params = new HashMap<String, String>(1);
        params.put(HttpConstants.EMAIL, email);
        request.setParams(params);
        addRequestToQueue(request, true, 0, true);
    }

    /**
     * Call the login Api
     *
     * @param email    The entered email
     * @param password The entered password
     */
    private void login(final String email, final String password) {

        final JSONObject requestObject = new JSONObject();

        try {
            requestObject.put(HttpConstants.PROVIDER, AppConstants.MANUAL);
            requestObject.put(HttpConstants.EMAIL, email);
            requestObject.put(HttpConstants.PASSWORD, password);
            requestObject.put(HttpConstants.DEVICE_ID, UserInfo.INSTANCE
                    .getDeviceId());
            final BlRequest request = new BlRequest(Method.POST, HttpConstants.getApiBaseUrl()
                    + ApiEndpoints.CREATE_USER, requestObject.toString(), mVolleyCallbacks);
            request.setRequestId(RequestId.CREATE_USER);
            addRequestToQueue(request, true, 0, true);
        } catch (final JSONException e) {
            // Should never happen
            Logger.e(TAG, e, "Error building create user json");
        }

    }

    /**
     * Call the login Api
     *
     * @param token    oath token we get from providers
     * @param provider facebook or google in our case
     */
    private void loginWithProvider(final String token, final String provider) {

        final JSONObject requestObject = new JSONObject();

        try {
            requestObject.put(HttpConstants.PROVIDER, provider);
            requestObject.put(HttpConstants.ACCESS_TOKEN, token);
            requestObject.put(HttpConstants.DEVICE_ID, UserInfo.INSTANCE
                    .getDeviceId());
            final BlRequest request = new BlRequest(Method.POST, HttpConstants.getApiBaseUrl()
                    + ApiEndpoints.CREATE_USER, requestObject.toString(), mVolleyCallbacks);
            request.setRequestId(RequestId.CREATE_USER);
            addRequestToQueue(request, true, 0, true);
        } catch (final JSONException e) {
            // Should never happen
            Logger.e(TAG, e, "Error building create user json");
        }

    }

    /**
     * Validates the text fields for creating a user. Automatically sets the error messages for the
     * text fields
     *
     * @return <code>true</code> If the input is valid, <code>false</code> otherwise
     */
    private boolean isInputValid() {

        final String email = mEmailEditText.getText().toString();
        boolean isValid = !TextUtils.isEmpty(email);

        if (!isValid) {
            mEmailEditText.setError(getString(R.string.error_enter_email));
        } else {
            //Using a regex for email comparison is pointless
            isValid &= email.contains("@");
            if (!isValid) {
                mEmailEditText.setError(getString(R.string.error_invalid_email));
            }
        }

        if (isValid) {
            final String password = mPasswordEditText.getText().toString();
            isValid &= !TextUtils.isEmpty(password);

            if (!isValid) {
                mPasswordEditText
                        .setError(getString(R.string.error_enter_password));
            } else {
                isValid &= (password.length() >= mMinPasswordLength);
                if (!isValid) {
                    mPasswordEditText
                            .setError(getString(R.string.error_password_minimum_length,
                                                mMinPasswordLength));
                }
            }
        }

        return isValid;
    }

    @Override
    public void onSuccess(final int requestId,
                          final IBlRequestContract request,
                          final ResponseInfo response) {
        if (requestId == RequestId.CREATE_USER) {

            final Bundle userInfo = response.responseBundle;

            Utils.updateUserInfoFromBundle(userInfo, true);
            BarterLiApplication.startChatService();

            final String locationId = userInfo
                    .getString(HttpConstants.LOCATION);
            Intent returnIntent = getDefaultOnwardIntent();
            if (TextUtils.isEmpty(locationId)) {
                returnIntent = new Intent(getActivity(), SelectPreferredLocationActivity.class);
                returnIntent.putExtra(Keys.ONWARD_INTENT, getDefaultOnwardIntent());
            }

            final Intent data = new Intent();
            data.putExtra(Keys.ONWARD_INTENT, returnIntent);
            getActivity().setResult(Activity.RESULT_OK, returnIntent);
            getActivity().finish();

        } else if (requestId == RequestId.REQUEST_RESET_TOKEN) {

            final Intent resetPasswordIntent = new Intent(getActivity(),
                                                          PasswordResetActivity.class);
            resetPasswordIntent.putExtra(Keys.EMAIL, mEmailForPasswordChange);

            if(getArguments() != null) {
                resetPasswordIntent.putExtras(getArguments());
            }

            startActivityForResult(resetPasswordIntent, AppConstants.RequestCodes.RESET_PASSWORD);
        }

    }

    @Override
    public void onBadRequestError(final int requestId,
                                  final IBlRequestContract request, final int errorCode,
                                  final String errorMessage, final Bundle errorResponseBundle) {

        if (requestId == RequestId.CREATE_USER) {
            showCrouton(errorMessage, AlertStyle.ERROR);
        }
    }

    @Override
    public void call(final Session session, final SessionState state,
                     final Exception exception) {
        // TODO session returns the user_token
        Logger.e(TAG, session.getAccessToken() + " token" + state.toString());
        //exception.printStackTrace();
        if (!session.getAccessToken().equals("")) {
            loginWithProvider(session.getAccessToken(), AppConstants.FACEBOOK);
        }

    }

    /**
     * Method called when google login completes
     */
    public void onGoogleLogin() {

        final String googleAccessToken = ((AuthActivity) getActivity())
                .getPlusManager().getAccessToken();
        Logger.v(TAG, "Google Access Token: %s", googleAccessToken);

        if (!TextUtils.isEmpty(googleAccessToken)) {
            loginWithProvider(googleAccessToken, AppConstants.GOOGLE);
        }
    }

    /**
     * Method called when there is an error while google login
     *
     * @param error The {@link Exception} that occured
     */
    public void onGoogleLoginError(final Exception error) {

        showCrouton(R.string.error_unable_to_login, AlertStyle.ERROR);
    }

    /**
     * Method called when google logout happens
     */
    public void onGoogleLogout() {

    }

    @Override
    protected String getAnalyticsScreenName() {
        return Screens.LOGIN;
    }

    /**
     * If an onward intent has been specified, provides that.
     * <p/>
     * Otherwise, just creates a default intent to open the user's profile
     */
    private Intent getDefaultOnwardIntent() {

        final Bundle extras = getArguments();

        if (extras != null && extras.containsKey(Keys.ONWARD_INTENT)) {
            return extras.getParcelable(Keys.ONWARD_INTENT);
        } else {
            return null;
        }
    }

}
