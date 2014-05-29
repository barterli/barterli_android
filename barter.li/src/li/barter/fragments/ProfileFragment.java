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

import java.util.HashMap;
import java.util.Map;

import li.barter.R;
import li.barter.chat.ChatService;
import li.barter.data.DBInterface;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.SQLiteLoader;
import li.barter.data.TableUsers;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.Loaders;
import li.barter.utils.AppConstants.QueryTokens;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.Logger;
import li.barter.widgets.CircleImageView;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request.Method;
import com.squareup.picasso.Picasso;

/**
 * @author Anshul Kamboj
 */

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class ProfileFragment extends AbstractBarterLiFragment implements
               LoaderCallbacks<Cursor>,
                OnClickListener {

    private static final String          TAG          = "ProfileFragment";

    private FragmentTabHost				 mTabHost;
    private String             			 mUserId;
	private String             			 mImageUrl;
	private String            		  	 mId;
	private boolean             		 mOwnedByUser;
	private boolean            			 mCameFromOtherProfile;
	private ImageView					 mChatLinkImageView;
	private CircleImageView				 mOwnerImageViewslide;
	private TextView					 mOwnerNameSlide;
	
	
 

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        final View view = inflater.inflate(R.layout.fragment_my_profile, null);
        initViews(view);

        setActionBarTitle(R.string.profilepage_title);

        setActionBarDrawerToggleEnabled(false);
        
        final Bundle extras = getArguments();

		if (extras != null) {
			mUserId = extras.getString(Keys.USER_ID);

			mCameFromOtherProfile = extras.getBoolean(Keys.OTHER_PROFILE_FLAG);
			if ((mUserId != null) && mUserId.equals(UserInfo.INSTANCE.getId())) {
				mOwnedByUser = true;
				
			} else {
				mOwnedByUser = false;
			}
		}

		updateViewForUser();

		
		
        mTabHost = (FragmentTabHost) view.findViewById(android.R.id.tabhost);
        mTabHost.setup(getActivity(), getChildFragmentManager(), android.R.id.tabcontent);
		mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.aboutMeSpec)).setIndicator(getString(R.string.aboutMe)),
				AboutMeFragment.class, getArguments());

		mTabHost.addTab(mTabHost.newTabSpec(getString(R.string.myBooksSpec)).setIndicator(getString(R.string.myBooks)),
				MyBooksFragment.class, getArguments());
        
		
		getUserDetails(mUserId);
		if(mOwnedByUser)
		{
			mImageUrl = UserInfo.INSTANCE.getProfilePicture();

			mOwnerNameSlide.setText(UserInfo.INSTANCE.getFirstName());

			Picasso.with(getActivity())
			.load(mImageUrl + "?type=large")
			.resizeDimen(R.dimen.book_user_image_size_profile, R.dimen.book_user_image_size_profile).centerCrop()
			.into(mOwnerImageViewslide.getTarget());
		}
		else
		{
		loadUserDetails();
		}
        return view;
    }
    
    private void initViews(final View view) {
		
		mOwnerImageViewslide=(CircleImageView)view.findViewById(R.id.image_user_circular);
		mChatLinkImageView= (ImageView) view.findViewById(R.id.chatwithowner);
		
		mChatLinkImageView.setOnClickListener(this);
		mOwnerNameSlide = (TextView) view.findViewById(R.id.name);

		// initBarterTypeCheckBoxes(view);

	}
    
    /**
	 * Checks whether the book belongs to the current user or not, and updates
	 * the UI accordingly
	 */
	private void updateViewForUser() {

		if (mOwnedByUser) {
			mChatLinkImageView.setVisibility(View.GONE);

		}

		if (mCameFromOtherProfile) {

		}
	}
	
	/**
	 * Updates the book owner user details
	 */

	private void getUserDetails(final String userid) {

		final BlRequest request = new BlRequest(Method.GET, HttpConstants.getApiBaseUrl()
				+ ApiEndpoints.USERPROFILE, null, mVolleyCallbacks);
		request.setRequestId(RequestId.GET_USER_PROFILE);

		final Map<String, String> params = new HashMap<String, String>(2);

		params.put(HttpConstants.ID, String.valueOf(userid).trim());
		request.setParams(params);

		addRequestToQueue(request, true, 0,true);

	}
	
	/**
	 * Fetches books owned by the current user
	 */

	private void loadUserDetails() {
		getLoaderManager().restartLoader(Loaders.USER_DETAILS, null, this);

	}

	

	@Override
	public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {

		if (mOwnedByUser) {
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
		if (v.getId() == R.id.chatwithowner) {

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
		if(mOwnedByUser)
		{
		args.putString(Keys.BOOK_TITLE, "");
		}
		else
		{
			//TODO add argument from bundle
			args.putString(Keys.BOOK_TITLE, "");	
		}
		loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
				.instantiate(getActivity(), ChatDetailsFragment.class
						.getName(), args), FragmentTags.CHAT_DETAILS, true, null);

	}
    @Override
    public void onStop() {
        super.onStop();
        DBInterface.cancelAsyncQuery(QueryTokens.LOAD_LOCATION_FROM_PROFILE_SHOW_PAGE);
    }

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    @Override
    public void onSuccess(final int requestId,
                    final IBlRequestContract request,
                    final ResponseInfo response) {
        // TODO Auto-generated method stub
        if (requestId == RequestId.GET_USER_PROFILE) {
        }

    }

    @Override
    public void onBadRequestError(final int requestId,
                    final IBlRequestContract request, final int errorCode,
                    final String errorMessage, final Bundle errorResponseBundle) {
        // TODO Auto-generated method stub

    }

   


	@Override
	public Loader<Cursor> onCreateLoader(final int loaderId, final Bundle args) {
		if(loaderId == Loaders.USER_DETAILS)
		{
			final String selection = DatabaseColumns.USER_ID
					+ SQLConstants.EQUALS_ARG;
			final String[] argsId = new String[1];
			argsId[0]=mUserId;
			Logger.d(TAG,"load = "+ mUserId);
			return new SQLiteLoader(getActivity(), false, TableUsers.NAME, null, selection, argsId, null, null, null, null);
		}
		else {


			return null;
		}
	}

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
    	if (loader.getId() == Loaders.USER_DETAILS) {

			Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
			if(cursor.getCount()!=0)
			{
				cursor.moveToFirst();

				mImageUrl = cursor.getString(cursor
						.getColumnIndex(DatabaseColumns.PROFILE_PICTURE));

				mOwnerNameSlide.setText(cursor.getString(cursor
						.getColumnIndex(DatabaseColumns.FIRST_NAME))+" "+cursor.getString(cursor
								.getColumnIndex(DatabaseColumns.LAST_NAME)));

				Picasso.with(getActivity())
				.load(mImageUrl + "?type=large")
				.resizeDimen(R.dimen.book_user_image_size_profile, R.dimen.book_user_image_size_profile).centerCrop()
				.into(mOwnerImageViewslide.getTarget());



				//				mAboutMeTextView.setText(cursor.getString(cursor
				//						.getColumnIndex(DatabaseColumns.DESCRIPTION)));
				//				mPreferredLocationTextView.setText(cursor.getString(cursor
				//						.getColumnIndex(DatabaseColumns.NAME))+","+cursor.getString(cursor
				//								.getColumnIndex(DatabaseColumns.ADDRESS)));

			}

		}

    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
       
    }

   
    
  

}
