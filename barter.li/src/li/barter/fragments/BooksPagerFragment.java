
package li.barter.fragments;

import com.google.android.gms.analytics.HitBuilders.EventBuilder;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ShareActionProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import li.barter.R;
import li.barter.analytics.AnalyticsConstants.Actions;
import li.barter.analytics.AnalyticsConstants.Categories;
import li.barter.analytics.AnalyticsConstants.ParamKeys;
import li.barter.analytics.AnalyticsConstants.ParamValues;
import li.barter.analytics.AnalyticsConstants.Screens;
import li.barter.analytics.GoogleAnalyticsManager;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLiteLoader;
import li.barter.data.TableSearchBooks;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.Loaders;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.Logger;
import li.barter.utils.SharedPreferenceHelper;
import li.barter.utils.Utils;

/**
 * @author Anshul Kamboj Fragment for Paging Books Around Me. Also contains a
 *         Profile that the user can chat directly with the owner
 */

public class BooksPagerFragment extends AbstractBarterLiFragment implements
                LoaderCallbacks<Cursor>, OnPageChangeListener,
                PanelSlideListener {

    private static final String  TAG                     = "BookDetailPagerFragment";

    /**
     * {@link BookPageAdapter} holds the {@link BookDetailFragment} as viewpager
     */
    private BookPageAdapter      mAdapter;

    /**
     * ViewPager which holds the fragment
     */
    private ViewPager            mBookDetailPager;

    /**
     * Counter to load the current number of pages for
     * {@link BookDetailFragment}
     */
    private int                  mBookCounter;

    /**
     * It holds the Book which is clicked
     */
    private int                  mBookPosition;

    /**
     * These arrays holds bookids, userids and book titles for all the books in
     * the pager to map them
     */
    private ArrayList<String>    mIdArray                = new ArrayList<String>();
    private ArrayList<String>    mUserIdArray            = new ArrayList<String>();
    private ArrayList<String>    mBookTitleArray         = new ArrayList<String>();

    /**
     * Used to provide a slide up UI companent to place the user's profile
     * fragment
     */
    private SlidingUpPanelLayout mSlidingLayout;

    /**
     * Intent filter for chat button click events
     */
    private final IntentFilter   mChatButtonIntentFilter = new IntentFilter(AppConstants.ACTION_CHAT_BUTTON_CLICKED);

    /**
     * Receiver for chat button click events
     */
    private ChatButtonReceiver   mChatButtonReceiver     = new ChatButtonReceiver();

    /**
     * for loading the owned user menu i.e with edit options
     */
    private boolean              mOwnedByUser            = false;

    private ShareActionProvider  mShareActionProvider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                    Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        final View view = inflater
                        .inflate(R.layout.fragment_books_pager, container, false);
        final Bundle extras = getArguments();

        if (extras != null) {
            mBookCounter = extras.getInt(Keys.BOOK_COUNT);
            mBookPosition = extras.getInt(Keys.BOOK_POSITION);

        }

        mBookDetailPager = (ViewPager) view.findViewById(R.id.pager_books);
        mBookDetailPager.setOnPageChangeListener(this);
        mSlidingLayout = (SlidingUpPanelLayout) view
                        .findViewById(R.id.sliding_layout);
        mSlidingLayout.setPanelSlideListener(this);

        if (savedInstanceState == null) {
            final ProfileFragment fragment = new ProfileFragment();

            getChildFragmentManager()
                            .beginTransaction()
                            .replace(R.id.content_user_profile, fragment, FragmentTags.USER_PROFILE)
                            .commit();
        }

        loadBookSearchResults();

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        LocalBroadcastManager
                        .getInstance(activity)
                        .registerReceiver(mChatButtonReceiver, mChatButtonIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity())
                        .unregisterReceiver(mChatButtonReceiver);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        if (mOwnedByUser) {
            inflater.inflate(R.menu.menu_logged_in_book_detail, menu);
        } else {
            inflater.inflate(R.menu.menu_book_detail, menu);
        }

        final MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) menuItem
                        .getActionProvider();

        if (mBookTitleArray != null && mBookTitleArray.size() > 0) {
            updateShareIntent(mBookTitleArray.get(mBookDetailPager
                            .getCurrentItem()));
        } else {
            updateShareIntent(null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home: {
                onUpNavigate();
                return true;
            }

            case R.id.action_edit_book: {

                final int currentItem = mBookDetailPager.getCurrentItem();
                final Bundle args = new Bundle(3);
                args.putString(Keys.ID, mIdArray.get(currentItem));
                args.putBoolean(Keys.EDIT_MODE, true);
                args.putString(Keys.UP_NAVIGATION_TAG, FragmentTags.BS_BOOKS_AROUND_ME);

                loadFragment(R.id.frame_content, (AbstractBarterLiFragment) Fragment
                                .instantiate(getActivity(), AddOrEditBookFragment.class
                                                .getName(), args), FragmentTags.BS_EDIT_BOOK, true, FragmentTags.BS_BOOKS_AROUND_ME);

                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    /**
     * Starts the loader for book search results
     */
    private void loadBookSearchResults() {
        getLoaderManager()
                        .restartLoader(Loaders.SEARCH_BOOKS_ON_PAGER, null, this);
    }

    public class BookPageAdapter extends FragmentStatePagerAdapter {

        /**
         * Maintains a map of the positions to the fragments loaded in that
         * position
         */
        private Map<Integer, BookDetailFragment> mPositionFragmentMap;

        @SuppressLint("UseSparseArrays")
        /*
         * The benefits of SparseArrays are not noticeable unless the data size
         * is huge(~10k) and the API to use them is cumbersome compared to a Map
         */
        public BookPageAdapter(FragmentManager fm) {
            super(fm);
            mPositionFragmentMap = new HashMap<Integer, BookDetailFragment>();
        }

        @Override
        public int getCount() {
            return mBookCounter;
        }

        @Override
        public Fragment getItem(int position) {

            final BookDetailFragment fragment = BookDetailFragment
                            .newInstance(mUserIdArray.get(position), mIdArray
                                            .get(position), true);
            mPositionFragmentMap.put(position, fragment);
            return fragment;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            mPositionFragmentMap.remove(position);
        }

        public BookDetailFragment getFragmentForPosition(final int position) {
            return mPositionFragmentMap.get(position);
        }
    }

    @Override
    protected Object getTaskTag() {
        // TODO Auto-generated method stub

        return hashCode();
    }

    @Override
    public void onSuccess(int requestId, IBlRequestContract request,
                    ResponseInfo response) {

    }

    @Override
    public void onBadRequestError(int requestId, IBlRequestContract request,
                    int errorCode, String errorMessage,
                    Bundle errorResponseBundle) {
        // TODO Auto-generated method stub

    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
        if (loaderId == Loaders.SEARCH_BOOKS_ON_PAGER) {
            return new SQLiteLoader(getActivity(), false, TableSearchBooks.NAME, null, null, null, null, null, null, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == Loaders.SEARCH_BOOKS_ON_PAGER) {

            mBookCounter = cursor.getCount();
            mIdArray.ensureCapacity(mBookCounter);
            mUserIdArray.ensureCapacity(mBookCounter);
            mBookTitleArray.ensureCapacity(mBookCounter);
            while (cursor.moveToNext()) {

                mIdArray.add(cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.ID)));
                mUserIdArray.add(cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.USER_ID)));
                mBookTitleArray.add(cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.TITLE)));

            }

            mAdapter = new BookPageAdapter(getChildFragmentManager());

            mBookDetailPager.setAdapter(mAdapter);
            mBookDetailPager.setCurrentItem(mBookPosition);

            /*
             * Viewpager doesn't call on page selected() on the listener if the
             * set item is 0. This is to workaround that
             */

            if (mBookPosition == 0 && mIdArray.size() > 0) {
                onPageSelected(mBookPosition);
            }

            /*
             * this has moved from oncreateview to here to prevent the crash on
             * oncreateoptionsmenu - so the options menu was created before
             * creating the pager object.
             */

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
                    int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        if (mUserIdArray.size() > 0
                        && mUserIdArray.get(position)
                                        .equals(UserInfo.INSTANCE.getId())) {
            mOwnedByUser = true;
        } else {
            mOwnedByUser = false;
        }
        getActivity().invalidateOptionsMenu();
        updateShareIntent(mBookTitleArray.get(position));
        final ProfileFragment fragment = (ProfileFragment) getChildFragmentManager()
                        .findFragmentByTag(FragmentTags.USER_PROFILE);

        if (fragment != null) {
            fragment.setUserId(mUserIdArray.get(position));
        }
    }

    /**
     * Updates the share intent
     * 
     * @param bookTitle
     */
    private void updateShareIntent(String bookTitle) {

        if(mShareActionProvider == null) {
            return;
        }
        
        if (TextUtils.isEmpty(bookTitle)) {
            mShareActionProvider.setShareIntent(Utils.createAppShareIntent(getActivity()));
            return;
        }

        final String referralId = SharedPreferenceHelper
                        .getString(getActivity(), R.string.pref_share_token);
        String appShareUrl = getString(R.string.book_share_message, bookTitle)
                        .concat(AppConstants.PLAY_STORE_LINK);

        if (!TextUtils.isEmpty(referralId)) {
            appShareUrl = appShareUrl
                            .concat(String.format(Locale.US, AppConstants.REFERRER_FORMAT, referralId));
        }

        final Intent shareIntent = Utils
                        .createShareIntent(getActivity(), appShareUrl);

        mShareActionProvider.setShareIntent(shareIntent);
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
    }

    @Override
    public void onPanelExpanded(View panel) {
        setActionBarTitle(R.string.owner_profile);
        /*
         * TODO If current user is the user whose profile is being displayed,
         * show the edit option
         */
    }

    @Override
    public void onPanelCollapsed(View panel) {
        setActionBarTitle(R.string.Book_Detail_fragment_title);
    }

    @Override
    public void onPanelAnchored(View panel) {

    }

    @Override
    public void onDialogClick(DialogInterface dialog, int which) {
        ((ProfileFragment) getChildFragmentManager()
                        .findFragmentByTag(FragmentTags.USER_PROFILE))
                        .onDialogClick(dialog, which);
    }

    @Override
    public boolean willHandleDialog(DialogInterface dialog) {
        // TODO Auto-generated method stub
        return ((ProfileFragment) getChildFragmentManager()
                        .findFragmentByTag(FragmentTags.USER_PROFILE))
                        .willHandleDialog(dialog);
        //return super.willHandleDialog(dialog);
    }

    /**
     * @param view The drag handle to be set for the Sliding Pane Layout
     */
    public void setDragHandle(View view) {

        Logger.v(TAG, "Setting Drag View %s", view.toString());
        mSlidingLayout.setDragView(view);
        mSlidingLayout.setEnableDragViewTouchEvents(false);
    }

    @Override
    public void onBackPressed() {

        if (mSlidingLayout.isExpanded()) {
            mSlidingLayout.collapsePane();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected String getAnalyticsScreenName() {
        return Screens.BOOKS_PAGER;
    }

    /**
     * Broadcast receiver for receiver chat button clicked events
     * 
     * @author Vinay S Shenoy
     */
    private final class ChatButtonReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (mSlidingLayout.isExpanded()) {
                GoogleAnalyticsManager
                                .getInstance()
                                .sendEvent(new EventBuilder(Categories.USAGE, Actions.CHAT_INITIALIZATION)
                                                .set(ParamKeys.TYPE, ParamValues.PROFILE));
            } else {
                GoogleAnalyticsManager
                                .getInstance()
                                .sendEvent(new EventBuilder(Categories.USAGE, Actions.CHAT_INITIALIZATION)
                                                .set(ParamKeys.TYPE, ParamValues.BOOK));
            }
        }
    }

}
