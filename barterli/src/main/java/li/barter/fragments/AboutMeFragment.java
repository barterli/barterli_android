
package li.barter.fragments;

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

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity;
import li.barter.analytics.AnalyticsConstants.Screens;
import li.barter.data.DBInterface.AsyncDbQueryCallback;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.SQLiteLoader;
import li.barter.data.ViewUsersWithLocations;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.Loaders;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.Logger;
import li.barter.utils.SharedPreferenceHelper;

/**
 * @author Anshul Kamboj
 */

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class AboutMeFragment extends AbstractBarterLiFragment implements
                AsyncDbQueryCallback, LoaderCallbacks<Cursor>, OnClickListener {

    private static final String TAG            = "AboutMeFragment";

    private TextView            mAboutMeTextView;
    private TextView            mPreferredLocationTextView;
    private TextView            mLabelReferralCount;
    private TextView            mReferralCountTextView;

    private final String        mUserSelection = DatabaseColumns.USER_ID
                                                               + SQLConstants.EQUALS_ARG;
    private String              mLocationFormat;

    private String              mUserId;

    private boolean             mLoadedIndividually;
    private boolean             mLoggedInUser;
    private Button              mLogoutButton;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        mLocationFormat = getString(R.string.location_format);
        final View view = inflater
                        .inflate(R.layout.fragment_profile_aboutme, null);

        if (savedInstanceState != null) {
            final String savedUserId = savedInstanceState
                            .getString(Keys.USER_ID);

            if (!TextUtils.isEmpty(savedUserId)) {
                setUserId(savedUserId);
            }
        } else {
            final Bundle extras = getArguments();
            if (extras != null && extras.containsKey(Keys.USER_ID)) {

                setUserId(extras.getString(Keys.USER_ID));
            }
        }

        mLoadedIndividually = false;
        mAboutMeTextView = (TextView) view.findViewById(R.id.text_about_me);
        mPreferredLocationTextView = (TextView) view
                        .findViewById(R.id.text_current_location);
        mReferralCountTextView = (TextView) view
                        .findViewById(R.id.text_referral_count);
        mLabelReferralCount = (TextView) view
                        .findViewById(R.id.label_referral_count);
        mLogoutButton = (Button) view.findViewById(R.id.button_logout);

        mLogoutButton.setOnClickListener(this);

        setActionBarDrawerToggleEnabled(false);

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Keys.USER_ID, mUserId);
    }

    @Override
    public void onQueryComplete(final int token, final Object cookie,
                    final Cursor cursor) {

    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void onSuccess(final int requestId,
                    final IBlRequestContract request,
                    final ResponseInfo response) {

    }

    @Override
    public void onBadRequestError(final int requestId,
                    final IBlRequestContract request, final int errorCode,
                    final String errorMessage, final Bundle errorResponseBundle) {

    }

    @Override
    public void onInsertComplete(final int token, final Object cookie,
                    final long insertRowId) {

    }

    @Override
    public void onDeleteComplete(final int token, final Object cookie,
                    final int deleteCount) {

    }

    @Override
    public void onUpdateComplete(final int token, final Object cookie,
                    final int updateCount) {

    }

    /**
     * Fetches userDetails
     */

    private void loadUserDetails() {
        getLoaderManager()
                        .restartLoader(Loaders.USER_DETAILS_ABOUT_ME, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle args) {

        if (loaderId == Loaders.USER_DETAILS_ABOUT_ME) {

            return new SQLiteLoader(getActivity(), false, ViewUsersWithLocations.NAME, null, mUserSelection, new String[] {
                mUserId
            }, null, null, null, null);
        } else {

            return null;
        }
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {

        if (loader.getId() == Loaders.USER_DETAILS_ABOUT_ME) {

            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
            if (cursor.getCount() != 0) {
                cursor.moveToFirst();

                mAboutMeTextView.setText(cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.DESCRIPTION)));
                mPreferredLocationTextView
                                .setText(String.format(mLocationFormat, cursor.getString(cursor
                                                .getColumnIndex(DatabaseColumns.NAME)), cursor
                                                .getString(cursor
                                                                .getColumnIndex(DatabaseColumns.ADDRESS))));
                if (mLoggedInUser) {
                    mLabelReferralCount.setVisibility(View.VISIBLE);
                    mReferralCountTextView.setVisibility(View.VISIBLE);

                    mReferralCountTextView
                                    .setText(SharedPreferenceHelper
                                                    .getString(R.string.pref_referrer_count));
                    mAboutMeTextView.setText(SharedPreferenceHelper
                                    .getString(R.string.pref_description));

                    mLogoutButton.setVisibility(View.VISIBLE);
                } else {
                    mLabelReferralCount.setVisibility(View.GONE);
                    mReferralCountTextView.setVisibility(View.GONE);
                    mReferralCountTextView.setText(null);
                    mLogoutButton.setVisibility(View.GONE);
                }

            }

        }

    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {

    }

    /**
     * Sets the User Id for this fragment
     * 
     * @param userId
     */
    public void setUserId(String userId) {
        mUserId = userId;
        if (mUserId.equals(UserInfo.INSTANCE.getId())) {
            mLoggedInUser = true;
        } else {
            mLoggedInUser = false;
        }
        loadUserDetails();
    }

    @Override
    protected String getAnalyticsScreenName() {

        if (mLoadedIndividually) {
            return mUserId.equals(UserInfo.INSTANCE.getId()) ? Screens.ABOUT_CURRENT_USER
                            : Screens.ABOUT_OTHER_USER;
        } else {
            /*
             * We don't need to track this screen since it is loaded within a
             * viewpager. We will inform in the parent fragment
             */
            return "";
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.button_logout) {
            ((AbstractBarterLiActivity) getActivity()).logout();
        }
    }

}
