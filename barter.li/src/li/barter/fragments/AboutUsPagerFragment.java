
package li.barter.fragments;

import java.util.ArrayList;

import li.barter.R;
import li.barter.fragments.BooksPagerFragment.BookPageAdapter;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.viewpagerindicator.IconPagerAdapter;
import com.viewpagerindicator.TitlePageIndicator;

/**
 * @author Anshul Kamboj Fragment for Paging {@link CollaborateFragment}, {@link AboutUsPagerFragment}
 * , {@link OssLicenseFragment}, {@link TributeFragment}
 */

public class AboutUsPagerFragment extends AbstractBarterLiFragment {

	private static final String  TAG          = "AboutUsPagerFragment";

	/**
	 * {@link BookPageAdapter} holds the {@link BookDetailFragment} as viewpager
	 */
	private AboutUsPageAdapter      mAdapter;

	/**
	 * ViewPager which holds the fragment
	 */
	private ViewPager            mAboutUsPager;

	private TitlePageIndicator      mIndicator;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		init(container, savedInstanceState);
		setHasOptionsMenu(true);
		setActionBarTitle(getString(R.string.Aboutus_fragment_title));
		final View view = inflater
				.inflate(R.layout.fragment_aboutus_pager, container, false);

		mAboutUsPager = (ViewPager) view.findViewById(R.id.pager_aboutus);
		
		 mAdapter = new AboutUsPageAdapter(getChildFragmentManager(),AppConstants.ABOUTUS_FRAGMENT_TITLES);

		 mAboutUsPager.setAdapter(mAdapter);
		 
		 mIndicator = (TitlePageIndicator)view.findViewById(R.id.aboutUsTitlesIndicator);
		 mIndicator.setViewPager( mAboutUsPager );

		return view;
	}




	public class AboutUsPageAdapter extends FragmentStatePagerAdapter implements IconPagerAdapter{

		private int mCount;

		@SuppressLint("UseSparseArrays")
		/*
		 * The benefits of SparseArrays are not noticeable unless the data size
		 * is huge(~10k) and the API to use them is cumbersome compared to a Map
		 */
		public AboutUsPageAdapter(FragmentManager fm,ArrayList<String> content) {
			super(fm);
			
			mCount = AppConstants.ABOUTUS_FRAGMENT_TITLES.size();
		}
		

		@Override
		public int getCount() {
			return mCount;
		}
		
		

		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				final TributeFragment tributeFragment = TributeFragment
				.newInstance();
				return tributeFragment;

			case 1:
				final TeamFragment teamFragment = TeamFragment
				.newInstance();
				return teamFragment;


			case 2:
				final CollaborateFragment collaborateFragment = CollaborateFragment
				.newInstance();
				return collaborateFragment;


			case 3:
				final OssLicenseFragment ossLicenseFragment = OssLicenseFragment
				.newInstance();
				return ossLicenseFragment;


			default:
				break;
			}
			return null;
			
			

		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
		}

		@Override
		public int getIconResId(int index) {
			// TODO Auto-generated method stub
			return 0;
		}
		@Override
		public CharSequence getPageTitle(int position) {
			return AppConstants.ABOUTUS_FRAGMENT_TITLES.get(position % AppConstants.ABOUTUS_FRAGMENT_TITLES.size());
		}

		
	}




	@Override
	protected Object getVolleyTag() {
		 return hashCode();
	}




	@Override
	public void onSuccess(int requestId, IBlRequestContract request,
			ResponseInfo response) {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void onBadRequestError(int requestId, IBlRequestContract request,
			int errorCode, String errorMessage, Bundle errorResponseBundle) {
		// TODO Auto-generated method stub
		
	}
	 

	

}
