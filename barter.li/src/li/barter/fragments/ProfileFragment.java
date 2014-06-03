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

import com.android.volley.Request.Method;
import com.squareup.picasso.Picasso;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity;
import li.barter.adapters.ProfileFragmentsAdapter;
import li.barter.chat.ChatService;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.SQLiteLoader;
import li.barter.data.ViewUsersWithLocations;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.SharedPreferenceHelper;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.Loaders;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.widgets.CircleImageView;

/**
 * @author Anshul Kamboj
 */

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class ProfileFragment extends AbstractBarterLiFragment implements
LoaderCallbacks<Cursor>, OnClickListener, OnTabChangeListener,
OnPageChangeListener {

	private static final String     TAG            = "ProfileFragment";

	private FragmentTabHost         mTabHost;
	private String                  mUserId;
	private String                  mImageUrl;
	private ImageView               mChatImageView;
	private CircleImageView         mOwnerImageView;
	private TextView                mOwnerNameTextView;
	private TextView                mOwnerBarterLocationTextView;
	private boolean                 mIsLoggedInUser;
	private final String            mUserSelection = DatabaseColumns.USER_ID
			+ SQLConstants.EQUALS_ARG;
	private View                    mDragHandle;
	private ViewPager               mViewPager;
	private ProfileFragmentsAdapter mProfileFragmentsAdapter;
	private String                  mLocationFormat;

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		init(container, savedInstanceState);
		mLocationFormat = getString(R.string.location_format);
		final View view = inflater.inflate(R.layout.fragment_my_profile, null);
		initViews(view);

		setActionBarDrawerToggleEnabled(false);

		final Bundle extras = getArguments();


		if (extras != null && extras.containsKey(Keys.USER_ID)) {

			setUserId(extras.getString(Keys.USER_ID));
		}

		//TODO Configure by parameters instead of checking master fragment
		final AbstractBarterLiFragment fragment = ((AbstractBarterLiActivity) getActivity())
				.getCurrentMasterFragment();

		if (fragment != null && fragment instanceof BooksPagerFragment) {
			((BooksPagerFragment) fragment).setDragHandle(view
					.findViewById(R.id.container_profile_info));

			setHasOptionsMenu(false);
		} else {
			setActionBarTitle(R.string.profilepage_title);
			setHasOptionsMenu(true);
		}

		return view;
	}

	/**
	 * Sets the User Id to be loaded into this Profile fragment
	 * 
	 * @param userId The user ID whose info has to be loaded
	 */
	public void setUserId(final String userId) {
		mUserId = userId;

		mIsLoggedInUser = mUserId.equals(UserInfo.INSTANCE.getId());
		updateViewsForUser();

		/*
		 * TODO Add check for whether info has been fetched & savedInstanceState
		 * to prevent fetch on orientation change. Also stop notifying loaders
		 * on every insert during parsing of books
		 */
		fetchUserDetailsFromServer(mUserId);

	}

	/**
	 * Updates the current selected fragment with the new user id
	 * 
	 * @param fragment The currently selected fragment
	 */
	private void updateTab(AbstractBarterLiFragment fragment) {

		if (TextUtils.isEmpty(mUserId)) {
			return;
		}

		if (fragment instanceof AboutMeFragment) {
			((AboutMeFragment) fragment).setUserId(mUserId);
		} else if (fragment instanceof MyBooksFragment) {
			((MyBooksFragment) fragment).setUserId(mUserId);
		}
	}

	private void initViews(final View view) {

		mOwnerImageView = (CircleImageView) view.findViewById(R.id.image_user);
		mChatImageView = (ImageView) view.findViewById(R.id.chat_with_owner);

		mChatImageView.setOnClickListener(this);
		mOwnerNameTextView = (TextView) view.findViewById(R.id.text_user_name);
		mOwnerBarterLocationTextView = (TextView) view
				.findViewById(R.id.text_user_location);
		mDragHandle = view.findViewById(R.id.container_profile_info);

		mTabHost = (FragmentTabHost) view.findViewById(android.R.id.tabhost);
		mTabHost.setup(getActivity(), getChildFragmentManager(), android.R.id.tabcontent);
		mTabHost.addTab(mTabHost.newTabSpec(FragmentTags.ABOUT_ME)
				.setIndicator(getString(R.string.about_me)), DummyFragment.class, null);
		mTabHost.addTab(mTabHost.newTabSpec(FragmentTags.MY_BOOKS)
				.setIndicator(getString(R.string.my_books)), DummyFragment.class, null);
		mTabHost.setOnTabChangedListener(this);

		mViewPager = (ViewPager) view.findViewById(R.id.pager_profile);
		mProfileFragmentsAdapter = new ProfileFragmentsAdapter(getChildFragmentManager());
		mViewPager.setAdapter(mProfileFragmentsAdapter);
		mViewPager.setOnPageChangeListener(this);

	}

	/**
	 * Checks whether the user is the current user or not, and updates the UI
	 * accordingly
	 */
	private void updateViewsForUser() {

		if (mIsLoggedInUser) {
			mChatImageView.setVisibility(View.GONE);
		} else {
			mChatImageView.setVisibility(View.VISIBLE);
		}

		loadUserDetails();
	}

	/**
	 * Updates the book owner user details
	 */

	private void fetchUserDetailsFromServer(final String userid) {

		final BlRequest request = new BlRequest(Method.GET, HttpConstants.getApiBaseUrl()
				+ ApiEndpoints.USERPROFILE, null, mVolleyCallbacks);
		request.setRequestId(RequestId.GET_USER_PROFILE);

		final Map<String, String> params = new HashMap<String, String>(2);

		params.put(HttpConstants.ID, String.valueOf(userid).trim());
		request.setParams(params);

		addRequestToQueue(request, true, 0, true);

	}

	/**
	 * Fetches books owned by the current user
	 */

	private void loadUserDetails() {


		getLoaderManager().restartLoader(Loaders.USER_DETAILS, null, this);


	}

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

		if (mIsLoggedInUser) {
			inflater.inflate(R.menu.menu_profile_show, menu);
		}
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
							.getName(), null), FragmentTags.EDIT_PROFILE, true, FragmentTags.BS_EDIT_PROFILE);
			return true;
		}

		default: {
			return super.onOptionsItemSelected(item);
		}
		}
	}

	@Override
	public void onBackPressed() {
		if (getTag().equals(FragmentTags.PROFILE_FROM_LOGIN)) {
			onUpNavigate();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public void onClick(final View v) {
		if (v.getId() == R.id.chat_with_owner) {

			if (isLoggedIn()) {

				if (hasFirstName()) {
					loadChatFragment();
				} else {
					showAddFirstNameDialog();
				}

			} else {

				final Bundle loginArgs = new Bundle(1);
				loginArgs.putString(Keys.UP_NAVIGATION_TAG, FragmentTags.BS_LOGIN_FROM_BOOK_DETAIL);

				loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
						.instantiate(getActivity(), LoginFragment.class
								.getName(), loginArgs), FragmentTags.LOGIN_TO_CHAT, true, FragmentTags.BS_LOGIN_FROM_BOOK_DETAIL);

			}
		}

		else {
			// Show Login Fragment
		}

	}

	/**
	 * Loads the Chat Fragment to chat with the book owner
	 */
	private void loadChatFragment() {
		final Bundle args = new Bundle(3);
		args.putString(Keys.CHAT_ID, ChatService
				.generateChatId(mUserId, UserInfo.INSTANCE.getId()));
		args.putString(Keys.USER_ID, mUserId);
		loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
				.instantiate(getActivity(), ChatDetailsFragment.class
						.getName(), args), FragmentTags.CHAT_DETAILS, true, null);

	}

	@Override
	protected Object getVolleyTag() {
		return hashCode();
	}

	@Override
	public void onSuccess(final int requestId,
			final IBlRequestContract request,
			final ResponseInfo response) {
		if (requestId == RequestId.GET_USER_PROFILE) {
			if (isAttached() && mIsLoggedInUser) {
				updateViewsForUser();
			}

		}

	}

	@Override
	public void onBadRequestError(final int requestId,
			final IBlRequestContract request, final int errorCode,
			final String errorMessage, final Bundle errorResponseBundle) {

	}

	@Override
	public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle args) {
		if (loaderId == Loaders.USER_DETAILS) {

			return new SQLiteLoader(getActivity(), false, ViewUsersWithLocations.NAME, null, mUserSelection, new String[] {
				mUserId
			}, null, null, null, null);
		} else {

			return null;
		}
	}

	@Override
	public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
		if (loader.getId() == Loaders.USER_DETAILS) {

			if(mIsLoggedInUser)
			{
			mImageUrl = UserInfo.INSTANCE.getProfilePicture();

			mOwnerNameTextView
			.setText(UserInfo.INSTANCE.getFirstName()
					);

			Picasso.with(getActivity())
			.load(mImageUrl + "?type=large")
			.resizeDimen(R.dimen.book_user_image_size_profile, R.dimen.book_user_image_size_profile)
			.centerCrop().into(mOwnerImageView.getTarget());
			
			
			}
		 if (cursor.moveToFirst()) {

				mImageUrl = cursor
						.getString(cursor
								.getColumnIndex(DatabaseColumns.PROFILE_PICTURE));

				mOwnerNameTextView
				.setText(cursor.getString(cursor
						.getColumnIndex(DatabaseColumns.FIRST_NAME))
						+ " "
						+ cursor.getString(cursor
								.getColumnIndex(DatabaseColumns.LAST_NAME)));
				mOwnerBarterLocationTextView
				.setText(String.format(mLocationFormat, cursor.getString(cursor
						.getColumnIndex(DatabaseColumns.NAME)), cursor
						.getString(cursor
								.getColumnIndex(DatabaseColumns.ADDRESS))));

				//Set selected to do marquee if text length is very long
				mOwnerBarterLocationTextView.setSelected(true);

				Picasso.with(getActivity())
				.load(mImageUrl + "?type=large")
				.resizeDimen(R.dimen.book_user_image_size_profile, R.dimen.book_user_image_size_profile)
				.centerCrop().into(mOwnerImageView.getTarget());

				updateTab(mProfileFragmentsAdapter.getFragmentAtPosition(mViewPager
						.getCurrentItem()));

			}

		}

	}
	

	@Override
	public void onLoaderReset(final Loader<Cursor> loader) {

	}

	/**
	 * @return The drag handle view for this fragment
	 */
	public View getDragHandle() {
		return mDragHandle;
	}

	/**
	 * Empty dummy fragment to provide for the TabHost
	 * 
	 * @author Vinay S Shenoy
	 */
	public static class DummyFragment extends Fragment {

		public DummyFragment() {
		}

	}

	@Override
	public void onTabChanged(String tabId) {

		final int currentViewPagerItem = mViewPager.getCurrentItem();
		if (tabId.equals(FragmentTags.ABOUT_ME)) {

			if (currentViewPagerItem != 0) {
				mViewPager.setCurrentItem(0, true);
			}
		} else if (tabId.equals(FragmentTags.MY_BOOKS)) {

			if (currentViewPagerItem != 1) {
				mViewPager.setCurrentItem(1, true);
			}
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {

	}
	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	public void onPageSelected(int position) {

		if (mTabHost.getCurrentTab() != position) {
			mTabHost.setCurrentTab(position);
		}
		final AbstractBarterLiFragment fragment = mProfileFragmentsAdapter
				.getFragmentAtPosition(position);

		updateTab(fragment);

	}
}
