package li.barter.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.analytics.HitBuilders;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity;
import li.barter.adapters.NavDrawerAdapter;
import li.barter.analytics.AnalyticsConstants;
import li.barter.analytics.GoogleAnalyticsManager;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;
import li.barter.utils.Utils;

/**
 * Fragment to load in the Navigation Drawer
 * Created by vinaysshenoy on 29/6/14.
 */
public class NavDrawerFragment extends AbstractBarterLiFragment implements AdapterView.OnItemClickListener {

    /**
     * ListView to provide Nav drawer content
     */
    private ListView mListView;

    /**
     * Drawer Adapter to provide the list view options
     */
    private NavDrawerAdapter mDrawerAdapter;

    /**
     * Callback will be triggered whenever the Nav drawer takes an action.  Use to close the drawer layout
     */
    private INavDrawerActionCallback mNavDrawerActionCallback;

    /**
     * Callback for delaying the running of nav drawer actions. This is so that the drawer can be closed without jank
     */
    private Handler mHandler;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mHandler = new Handler();
        mListView = (ListView) inflater.inflate(R.layout.fragment_nav_drawer, container, false);
        mDrawerAdapter = new NavDrawerAdapter(getActivity(), R.array.nav_drawer_titles, R.array.nav_drawer_descriptions);
        mListView.setAdapter(mDrawerAdapter);
        mListView.setOnItemClickListener(this);
        return mListView;
    }

    @Override
    protected String getAnalyticsScreenName() {
        //We don't want this fragment tracked
        return null;
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void onSuccess(int requestId, IBlRequestContract request, ResponseInfo response) {

    }

    @Override
    public void onBadRequestError(int requestId, IBlRequestContract request, int errorCode, String errorMessage, Bundle errorResponseBundle) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (parent == mListView) {
            final Runnable launchRunnable = makeRunnableForNavDrawerClick(position);
            if (launchRunnable != null) {
                //Give time for drawer to close before performing the action
                mHandler.postDelayed(launchRunnable, 250);
            }
            if (mNavDrawerActionCallback != null) {
                mNavDrawerActionCallback.onActionTaken();
            }
        }
    }

    /**
     * Creates a {@link Runnable} for positing to the Handler for launching the
     * Navigation Drawer click
     *
     * @param position The nav drawer item that was clicked
     * @return a {@link Runnable} to be posted to the Handler thread
     */
    private Runnable makeRunnableForNavDrawerClick(final int position) {

        Runnable runnable = null;
        final AbstractBarterLiFragment masterFragment = ((AbstractBarterLiActivity) getActivity()).getCurrentMasterFragment();

        switch (position) {

            //My Profile
            case 0: {

                GoogleAnalyticsManager
                        .getInstance()
                        .sendEvent(new HitBuilders.EventBuilder(AnalyticsConstants.Categories.USAGE, AnalyticsConstants.Actions.NAVIGATION_OPTION)
                                .set(AnalyticsConstants.ParamKeys.TYPE, AnalyticsConstants.ParamValues.PROFILE));
                /*
                 * If the master fragment is already the login fragment, don't
                 * load it again. TODO Check for Profile Fragment also
                 */
                if ((masterFragment != null)
                        && (masterFragment instanceof LoginFragment)) {
                    return null;
                }
                runnable = new Runnable() {

                    @Override
                    public void run() {
                        if (isLoggedIn()) {
                            Bundle args = new Bundle();
                            args.putString(AppConstants.Keys.USER_ID, AppConstants.UserInfo.INSTANCE
                                    .getId());

                            loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                                    .instantiate(getActivity(), ProfileFragment.class
                                            .getName(), args), AppConstants.FragmentTags.PROFILE_FROM_NAV_DRAWER, true, null);

                        } else {

                            final Bundle loginArgs = new Bundle(1);
                            loginArgs.putString(AppConstants.Keys.UP_NAVIGATION_TAG, AppConstants.FragmentTags.BS_BOOKS_AROUND_ME);

                            loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                                    .instantiate(getActivity(), LoginFragment.class
                                            .getName(), loginArgs), AppConstants.FragmentTags.LOGIN_FROM_NAV_DRAWER, true, AppConstants.FragmentTags.BS_BOOKS_AROUND_ME);
                        }

                    }
                };
                break;

            }

            //My Chats
            case 1: {

                GoogleAnalyticsManager
                        .getInstance()
                        .sendEvent(new HitBuilders.EventBuilder(AnalyticsConstants.Categories.USAGE, AnalyticsConstants.Actions.NAVIGATION_OPTION)
                                .set(AnalyticsConstants.ParamKeys.TYPE, AnalyticsConstants.ParamValues.CHATS));
                if ((masterFragment != null)
                        && (masterFragment instanceof ChatsFragment)) {
                    return null;
                }

                //TODO Check for login
                runnable = new Runnable() {

                    @Override
                    public void run() {
                        loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                                .instantiate(getActivity(), ChatsFragment.class
                                        .getName(), null), AppConstants.FragmentTags.CHATS, true, AppConstants.FragmentTags.BS_CHATS);
                    }
                };
                break;
            }

            //Report Bug
            case 2: {
                GoogleAnalyticsManager
                        .getInstance()
                        .sendEvent(new HitBuilders.EventBuilder(AnalyticsConstants.Categories.USAGE, AnalyticsConstants.Actions.NAVIGATION_OPTION)
                                .set(AnalyticsConstants.ParamKeys.TYPE, AnalyticsConstants.ParamValues.REPORT_BUG));
                if ((masterFragment != null)
                        && (masterFragment instanceof ReportBugFragment)) {
                    return null;
                }

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                                .instantiate(getActivity(), ReportBugFragment.class
                                        .getName(), null), AppConstants.FragmentTags.REPORT_BUGS, true, null);
                    }
                };
                break;
            }

            //Share
            case 3: {
                GoogleAnalyticsManager
                        .getInstance()
                        .sendEvent(new HitBuilders.EventBuilder(AnalyticsConstants.Categories.USAGE, AnalyticsConstants.Actions.NAVIGATION_OPTION)
                                .set(AnalyticsConstants.ParamKeys.TYPE, AnalyticsConstants.ParamValues.SHARE));
                runnable = new Runnable() {

                    @Override
                    public void run() {

                        Intent shareIntent = Utils
                                .createAppShareIntent(getActivity());
                        try {
                            startActivity(Intent
                                    .createChooser(shareIntent, getString(R.string.share_via)));
                        } catch (ActivityNotFoundException e) {
                            //Shouldn't happen
                        }

                    }
                };

                break;
            }

            //Rate Us
            case 4: {
                GoogleAnalyticsManager
                        .getInstance()
                        .sendEvent(new HitBuilders.EventBuilder(AnalyticsConstants.Categories.USAGE, AnalyticsConstants.Actions.NAVIGATION_OPTION)
                                .set(AnalyticsConstants.ParamKeys.TYPE, AnalyticsConstants.ParamValues.RATE_US));

                runnable = new Runnable() {

                    @Override
                    public void run() {
                        Uri marketUri = Uri
                                .parse(AppConstants.PLAY_STORE_MARKET_LINK);
                        Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
                        startActivity(marketIntent);

                    }
                };

                break;
            }

            //About Us
            case 5: {
                GoogleAnalyticsManager
                        .getInstance()
                        .sendEvent(new HitBuilders.EventBuilder(AnalyticsConstants.Categories.USAGE, AnalyticsConstants.Actions.NAVIGATION_OPTION)
                                .set(AnalyticsConstants.ParamKeys.TYPE, AnalyticsConstants.ParamValues.ABOUT_US));
                if ((masterFragment != null)
                        && (masterFragment instanceof TeamFragment)) {
                    return null;
                }

                runnable = new Runnable() {
                    @Override
                    public void run() {
                        loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                                .instantiate(getActivity(), AboutUsPagerFragment.class
                                        .getName(), null), AppConstants.FragmentTags.TEAM, true, null);
                    }
                };

                break;
            }

            default: {
                runnable = null;
            }
        }

        return runnable;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (!(activity instanceof INavDrawerActionCallback)) {
            throw new IllegalArgumentException("Activity " + activity.toString() + " must implement INavDrawerActionCallback");
        }

        mNavDrawerActionCallback = (INavDrawerActionCallback) activity;
    }

    /**
     * Interface that is called when the Navigation Drawer performs an Action
     */
    public static interface INavDrawerActionCallback {
        public void onActionTaken();
    }

}
