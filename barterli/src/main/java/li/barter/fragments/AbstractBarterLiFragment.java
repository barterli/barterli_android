/**
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
 */

package li.barter.fragments;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;

import de.keyboardsurfer.android.widget.crouton.Crouton;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import java.util.concurrent.atomic.AtomicInteger;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity;
import li.barter.activities.AbstractBarterLiActivity.AlertStyle;
import li.barter.analytics.GoogleAnalyticsManager;
import li.barter.fragments.dialogs.AddUserInfoDialogFragment;
import li.barter.http.BlMultiPartRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.IVolleyHelper;
import li.barter.http.ResponseInfo;
import li.barter.http.VolleyCallbacks;
import li.barter.http.VolleyCallbacks.IHttpCallbacks;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.Utils;
import li.barter.widgets.TypefaceCache;

/**
 * Base fragment class to encapsulate common functionality. Call the init()
 * method in the onCreateView() of your fragments
 * 
 * @author Vinay S Shenoy
 */
public abstract class AbstractBarterLiFragment extends Fragment implements
                IHttpCallbacks {

    private static final String       TAG = "AbstractBarterLiFragment";

    /**
     * Flag that indicates that this fragment is attached to an Activity
     */
    private boolean                   mIsAttached;

    /**
     * Stores the id for the container view
     */
    protected int                     mContainerViewId;

    /**
     * {@link VolleyCallbacks} for encapsulating the Volley response flow
     */
    protected VolleyCallbacks         mVolleyCallbacks;

    private AtomicInteger             mRequestCounter;

    /**
     * Whether a screen hit should be reported to analytics
     */
    private boolean                   mShouldReportScreenHit;
    
    public boolean				  mRefreshBooks=false;				 

    /**
     * {@link AddUserInfoDialogFragment} for
     */
    private AddUserInfoDialogFragment mAddUserInfoDialogFragment;

    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        mIsAttached = true;
        final RequestQueue requestQueue = ((IVolleyHelper) activity
                        .getApplication()).getRequestQueue();
        mVolleyCallbacks = new VolleyCallbacks(requestQueue, this);
        mRequestCounter = new AtomicInteger(0);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(Keys.LAST_SCREEN_TIME, Utils.getCurrentEpochTime());
    }

    /**
     * Call this method in the onCreateView() of any subclasses
     * 
     * @param container The container passed into onCreateView()
     * @param savedInstanceState The Instance state bundle passed into the
     *            onCreateView() method
     */
    protected void init(final ViewGroup container,
                    final Bundle savedInstanceState) {
        mContainerViewId = container.getId();
        long lastScreenTime = 0l;
        if (savedInstanceState != null) {
            mAddUserInfoDialogFragment = (AddUserInfoDialogFragment) getFragmentManager()
                            .findFragmentByTag(FragmentTags.DIALOG_ADD_NAME);
            lastScreenTime = savedInstanceState.getLong(Keys.LAST_SCREEN_TIME);
        }

        if (Utils.shouldReportScreenHit(lastScreenTime)) {
            mShouldReportScreenHit = true;
        } else {
            mShouldReportScreenHit = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAndReportScreenHit();
    }

    /**
     * Reports a screen hit
     */
    private void checkAndReportScreenHit() {
        if (mShouldReportScreenHit) {
            final String analyticsScreenName = getAnalyticsScreenName();

            if (!TextUtils.isEmpty(analyticsScreenName)) {
                GoogleAnalyticsManager.getInstance()
                                .sendScreenHit(getAnalyticsScreenName());
            }
        }

    }

    /**
     * Gets the screen name for reporting to google analytics. Send empty
     * string, or <code>null</code> if you don't want the Fragment tracked
     */
    protected abstract String getAnalyticsScreenName();

    protected void setActionBarDrawerToggleEnabled(final boolean enabled) {
        final AbstractBarterLiActivity activity = (AbstractBarterLiActivity) getActivity();
        if (activity.hasNavigationDrawer()) {
            activity.setActionBarDrawerToggleEnabled(enabled);
        }
    }

    /**
     * Helper method to load fragments into layout
     * 
     * @param containerResId The container resource Id in the content view into
     *            which to load the fragment
     * @param fragment The fragment to load
     * @param tag The fragment tag
     * @param addToBackStack Whether the transaction should be addded to the
     *            backstack
     * @param backStackTag The tag used for the backstack tag
     */
    public void loadFragment(final int containerResId,
                    final AbstractBarterLiFragment fragment, final String tag,
                    final boolean addToBackStack, final String backStackTag) {

        if (mIsAttached) {
            ((AbstractBarterLiActivity) getActivity())
                            .loadFragment(containerResId, fragment, tag, addToBackStack, backStackTag);
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mContainerViewId = 0;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mIsAttached = false;
        mVolleyCallbacks = null;
        mRequestCounter = null;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onUpNavigate();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Is the device connected to a network or not.
     * 
     * @return <code>true</code> if connected, <code>false</code> otherwise
     */
    public boolean isConnectedToInternet() {
        return ((AbstractBarterLiActivity) getActivity())
                        .isConnectedToInternet();
    }

    public void setActionBarDisplayOptions(final int displayOptions) {
        if (mIsAttached) {

            ((AbstractBarterLiActivity) getActivity())
                            .setActionBarDisplayOptions(displayOptions);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Crouton.clearCroutonsForActivity(getActivity());
        mVolleyCallbacks.cancelAll(getVolleyTag());
        getActivity().setProgressBarIndeterminateVisibility(false);
    }

    /**
     * Add a request on the network queue
     * 
     * @param request The {@link Request} to add
     * @param showErrorOnNoNetwork Whether an error toast should be displayed on
     *            no internet connection
     * @param errorMsgResId String resource Id for error message to show if no
     *            internet connection, 0 for a default error message
     */
    protected void addRequestToQueue(final Request<?> request,
                    final boolean showErrorOnNoNetwork,
                    final int errorMsgResId, boolean addHeader) {

        if (mIsAttached) {
            request.setTag(getVolleyTag());
            if (isConnectedToInternet()) {
                mRequestCounter.incrementAndGet();
                getActivity().setProgressBarIndeterminateVisibility(true);
                mVolleyCallbacks.queue(request, addHeader);
            } else if (showErrorOnNoNetwork) {
                showCrouton(errorMsgResId != 0 ? errorMsgResId
                                : R.string.no_network_connection, AlertStyle.ERROR);
            }
        }
    }

    /**
     * A Tag to add to all Volley requests. This must be unique for all
     * Fragments types
     * 
     * @return An Object that's the tag for this fragment
     */
    protected abstract Object getVolleyTag();

    /**
     * Display an alert, with a string message
     * 
     * @param message The message to display
     * @param style The {@link AlertStyle} of message to display
     */
    public void showCrouton(final String message, final AlertStyle style) {
        if (mIsAttached) {
            ((AbstractBarterLiActivity) getActivity())
                            .showCrouton(message, style);
        }
    }

    /**
     * Display an alert, with a string message
     * 
     * @param messageResId The message to display
     * @param style The {@link AlertStyle} of message to display
     */
    public void showCrouton(final int messageResId, final AlertStyle style) {
        if (mIsAttached) {
            showCrouton(getString(messageResId), style);
        }
    }

    /**
     * Display an alert, with a string message infinitely
     * 
     * @param messageResId The message to display
     * @param style The {@link AlertStyle} of message to display
     */
    public void showInfiniteCrouton(final int messageResId,
                    final AlertStyle style) {
        if (mIsAttached) {
            showInfiniteCrouton(getString(messageResId), style);
        }
    }

    public void cancelAllCroutons() {
        if (mIsAttached) {
            ((AbstractBarterLiActivity) getActivity()).cancelAllCroutons();
        }
    }

    /**
     * Display an alert, with a string message infinitely
     * 
     * @param message The message to display
     * @param style The {@link AlertStyle} of message to display
     */
    public void showInfiniteCrouton(final String message, final AlertStyle style) {
        if (mIsAttached) {
            ((AbstractBarterLiActivity) getActivity())
                            .showInfiniteCrouton(message, style);
        }
    }

    /**
     * Whether this Fragment is currently attached to an Activity
     * 
     * @return <code>true</code> if attached, <code>false</code> otherwise
     */
    public boolean isAttached() {
        return mIsAttached;
    }

    /**
     * Sets the Action bar title, using the desired {@link Typeface} loaded from
     * {@link TypefaceCache}
     * 
     * @param title The title to set for the Action Bar
     */
    public final void setActionBarTitle(final String title) {

        if (mIsAttached) {
            ((AbstractBarterLiActivity) getActivity()).setActionBarTitle(title);
        }
    }

    /**
     * Sets the Action bar title, using the desired {@link Typeface} loaded from
     * {@link TypefaceCache}
     * 
     * @param titleResId The title string resource Id to set for the Action Bar
     */
    public final void setActionBarTitle(final int titleResId) {
        setActionBarTitle(getString(titleResId));
    }

    /**
     * Sets the Action bar title, using the desired {@link Typeface} loaded from
     * {@link TypefaceCache}
     * 
     * @param titleResId The title string resource Id to set for the Action Bar
     */
    public final int getActionBarHeight() {
        int actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getActivity()
                        .getTheme()
                        .resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue
                            .complexToDimensionPixelSize(tv.data, getResources()
                                            .getDisplayMetrics());
        }
        return actionBarHeight;
    }

    /**
     * Is the user logged in
     */
    protected boolean isLoggedIn() {
        return !TextUtils.isEmpty(UserInfo.INSTANCE.getAuthToken());
    }
    
   

    /**
     * Does the user have a first name
     */
    protected boolean hasFirstName() {
        return !TextUtils.isEmpty(UserInfo.INSTANCE.getFirstName());
    }

    /**
     * Show the dialog for the user to add his name, in case it's not already
     * added
     */
    protected void showAddFirstNameDialog() {

        mAddUserInfoDialogFragment = new AddUserInfoDialogFragment();
        mAddUserInfoDialogFragment
                        .show(AlertDialog.THEME_HOLO_LIGHT, 0, R.string.update_info, R.string.submit, R.string.cancel, 0, getFragmentManager(), true, FragmentTags.DIALOG_ADD_NAME);
    }

    /**
     * Pops the fragment from the backstack, checking to see if the bundle args
     * have {@linkplain Keys#UP_NAVIGATION_TAG} which gives the name of the
     * backstack tag to pop to. This is mainly for providing Up navigation
     */
    public void onUpNavigate() {
        final Bundle args = getArguments();

        if ((args != null) && args.containsKey(Keys.UP_NAVIGATION_TAG)) {
            getFragmentManager()
                            .popBackStack(args.getString(Keys.UP_NAVIGATION_TAG), FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else {
            getFragmentManager().popBackStack();
        }
    }

    /**
     * Handles the behaviour for onBackPressed() Default behavious is to pop the
     * frament manager's backstack. Child fragments must override this if they
     * wish to provide custom behaviour
     */
    public void onBackPressed() {

        getFragmentManager().popBackStack();
        
    }

    
    @Override
    public void onPreExecute(final IBlRequestContract request) {
        mRequestCounter.incrementAndGet();
        if (mIsAttached) {
            getActivity().setProgressBarIndeterminateVisibility(true);
        }
    }

    @Override
    public void onPostExecute(final IBlRequestContract request) {
        assert (mRequestCounter != null);
        if (mIsAttached && (mRequestCounter.decrementAndGet() == 0)) {
            getActivity().setProgressBarIndeterminateVisibility(false);
        }
    }

    @Override
    public abstract void onSuccess(int requestId, IBlRequestContract request,
                    ResponseInfo response);

    @Override
    public abstract void onBadRequestError(int requestId,
                    IBlRequestContract request, int errorCode,
                    String errorMessage, Bundle errorResponseBundle);

    @Override
    public void onAuthError(final int requestId,
                    final IBlRequestContract request) {
        //TODO Show Login Fragment and ask user to login again
    }

    @Override
    public void onOtherError(final int requestId,
                    final IBlRequestContract request, final int errorCode) {
        //TODO Show generic network error message
    }

    /**
     * Whether this fragment will handle the particular dialog click or not
     * 
     * @param dialog The dialog that was interacted with
     * @return <code>true</code> If the fragment will handle it,
     *         <code>false</code> otherwise
     */
    public boolean willHandleDialog(final DialogInterface dialog) {

        if ((mAddUserInfoDialogFragment != null)
                        && mAddUserInfoDialogFragment.getDialog()
                                        .equals(dialog)) {
            return true;
        }
        return false;
    }

    /**
     * Handle the click for the dialog. The fragment will receive this call,
     * only if {@link #willHandleDialog(DialogInterface)} returns
     * <code>true</code>
     * 
     * @param dialog The dialog that was interacted with
     * @param which The button that was clicked
     */
    public void onDialogClick(final DialogInterface dialog, final int which) {

        if ((mAddUserInfoDialogFragment != null)
                        && mAddUserInfoDialogFragment.getDialog()
                                        .equals(dialog)) {

            if (which == DialogInterface.BUTTON_POSITIVE) {
                final String firstName = mAddUserInfoDialogFragment
                                .getFirstName();
                final String lastName = mAddUserInfoDialogFragment
                                .getLastName();

                if (!TextUtils.isEmpty(firstName)) {
                    updateUserInfo(firstName, lastName);
                }
            }
        }
    }

    /**
     * Updates the user info with just the first name and last name
     * 
     * @param firstName The user's first name
     * @param lastName The user's last name
     */
    public void updateUserInfo(final String firstName, final String lastName) {

        final String url = HttpConstants.getApiBaseUrl()
                        + ApiEndpoints.UPDATE_USER_INFO;

        final JSONObject mUserProfileObject = new JSONObject();
        final JSONObject mUserProfileMasterObject = new JSONObject();
        try {
            mUserProfileObject.put(HttpConstants.FIRST_NAME, firstName);
            mUserProfileObject.put(HttpConstants.LAST_NAME, lastName);
            mUserProfileMasterObject
                            .put(HttpConstants.USER, mUserProfileObject);

            final BlMultiPartRequest updateUserProfileRequest = new BlMultiPartRequest(Method.PUT, url, null, mVolleyCallbacks);

            updateUserProfileRequest
                            .addMultipartParam(HttpConstants.USER, "application/json", mUserProfileMasterObject
                                            .toString());

            updateUserProfileRequest.setRequestId(RequestId.SAVE_USER_PROFILE);
            addRequestToQueue(updateUserProfileRequest, true, 0, true);

        } catch (final JSONException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Hides the keyboard
     * 
     * @param view the view from which keyboard was open
     */
    
    public void hideKeyBoard(View view)
    {
    	InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(
			      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    
    public boolean isLocationServiceEnabled() {
        LocationManager lm = (LocationManager)
                getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria=new Criteria();
       
        String provider = lm.getBestProvider(criteria, true);
        return ((provider!=null) &&
                !LocationManager.PASSIVE_PROVIDER.equals(provider));
    }

}
