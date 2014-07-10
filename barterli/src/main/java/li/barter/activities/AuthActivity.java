package li.barter.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;

import li.barter.R;
import li.barter.fragments.AbstractBarterLiFragment;
import li.barter.fragments.LoginFragment;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;
import li.barter.utils.GooglePlusManager;

/**
 * Activity to perform methods to Login & reset the user's password.
 * <p/>
 * Use the {@linkplain #ACTION_LOGIN} and {@linkplain #ACTION_RESET_PASSWORD} to decide what to
 * start it as. Depending on what is selected, different fragments will be loaded.
 * <p/>
 * Created by vinay.shenoy on 09/07/14.
 */
public class AuthActivity extends AbstractDrawerActivity implements GooglePlusManager.GooglePlusAuthCallback {

    public static final String TAG = "AuthActivity";

    public static final String ACTION_LOGIN          = "li.barter.ACTION_LOGIN";
    public static final String ACTION_RESET_PASSWORD = "li.barter.ACTION_RESET_PASSWORD";

    /**
     * Helper class for connecting to GooglePlus for login
     */
    private GooglePlusManager mGooglePlusManager;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);
        initDrawer(R.id.drawer_layout, R.id.frame_nav_drawer);
        mGooglePlusManager = new GooglePlusManager(this, this);
        if (savedInstanceState == null) {

            final String action = getIntent().getAction();

            if (TextUtils.isEmpty(action)) {
                throw new RuntimeException("No action set for Login Activity");
            }

            handleAction(action);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGooglePlusManager.onActivityStarted();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGooglePlusManager.onActivityStopped();
    }

    /** Looks at the intent action and loads the appropriate fragment */
    private void handleAction(final String action) {

        if (action.equals(ACTION_LOGIN)) {
            loadLoginFragment();
        } else if (action.equals(ACTION_RESET_PASSWORD)) {
            loadResetPasswordFragment();
        } else {
            throw new RuntimeException("Invalid action for login activity:" + action);
        }
    }

    /** Load the fragment for resetting the password */
    private void loadResetPasswordFragment() {

    }

    /** Load the fragment for login */
    private void loadLoginFragment() {

        loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                .instantiate(this, LoginFragment.class.getName()),
                     AppConstants.FragmentTags.LOGIN, false, null);
    }


    @Override
    protected boolean isDrawerActionBarToggleEnabled() {
        return false;
    }

    @Override
    protected String getAnalyticsScreenName() {
        return null;
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void onSuccess(final int requestId, final IBlRequestContract request, final ResponseInfo response) {

    }

    @Override
    public void onBadRequestError(final int requestId, final IBlRequestContract request, final int errorCode, final String errorMessage, final Bundle errorResponseBundle) {

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == GooglePlusManager.CONNECTION_UPDATE_ERROR)
                && (resultCode == RESULT_OK)) {
            mGooglePlusManager.onActivityResult();
        }
    }

    /**
     * Gets a reference to the Google Plus Manager
     */
    public GooglePlusManager getPlusManager() {

        return mGooglePlusManager;
    }

    @Override
    public void onLogin() {

        final LoginFragment fragment = ((LoginFragment) getSupportFragmentManager().findFragmentByTag(
                AppConstants.FragmentTags.LOGIN));

        if(fragment != null && fragment.isResumed()) {
            fragment.onGoogleLogin();
        }
    }

    @Override
    public void onLoginError(final Exception error) {

        final LoginFragment fragment = ((LoginFragment) getSupportFragmentManager().findFragmentByTag(
                AppConstants.FragmentTags.LOGIN));

        if(fragment != null && fragment.isResumed()) {
            fragment.onGoogleLoginError(error);
        }

    }

    @Override
    public void onLogout() {

        final LoginFragment fragment = ((LoginFragment) getSupportFragmentManager().findFragmentByTag(
                AppConstants.FragmentTags.LOGIN));

        if(fragment != null && fragment.isResumed()) {
            fragment.onGoogleLogout();
        }
    }
}
