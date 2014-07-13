/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package li.barter.fragments;

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
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.widget.ShareActionProvider;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.analytics.HitBuilders.EventBuilder;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;

import java.util.Locale;

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
import li.barter.utils.Logger;
import li.barter.utils.SharedPreferenceHelper;
import li.barter.utils.Utils;

/**
 * @author Anshul Kamboj Fragment for Paging Books Around Me. Also contains a Profile that the user
 *         can chat directly with the owner
 */

public class BooksPagerFragment extends AbstractBarterLiFragment implements
        LoaderCallbacks<Cursor>, OnPageChangeListener,
        PanelSlideListener {

    private static final String TAG = "BookDetailPagerFragment";

    /**
     * {@link BookPageAdapter} holds the {@link BookDetailFragment} as viewpager
     */
    private BookPageAdapter mAdapter;

    /**
     * ViewPager which holds the fragment
     */
    private ViewPager mBookDetailPager;

    /**
     * It holds the Book which is clicked
     */
    private int mBookPosition;

    /**
     * Used to provide a slide up UI companent to place the user's profile fragment
     */
    private SlidingUpPanelLayout mSlidingLayout;

    /**
     * Intent filter for chat button click events
     */
    private final IntentFilter mChatButtonIntentFilter = new IntentFilter(
            AppConstants.ACTION_CHAT_BUTTON_CLICKED);

    /**
     * Receiver for chat button click events
     */
    private ChatButtonReceiver mChatButtonReceiver = new ChatButtonReceiver();

    private ShareActionProvider mShareActionProvider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        final View view = inflater
                .inflate(R.layout.fragment_books_pager, container, false);
        final Bundle extras = getArguments();

        if (extras != null) {
            mBookPosition = extras.getInt(Keys.BOOK_POSITION);
        }

        mBookDetailPager = (ViewPager) view.findViewById(R.id.pager_books);
        mBookDetailPager.setOnPageChangeListener(this);
        mSlidingLayout = (SlidingUpPanelLayout) view
                .findViewById(R.id.sliding_layout);
        mSlidingLayout.setPanelSlideListener(this);

        mAdapter = new BookPageAdapter(getChildFragmentManager(), null);
        mBookDetailPager.setAdapter(mAdapter);

        if (savedInstanceState == null) {
            final ProfileFragment fragment = new ProfileFragment();

            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_user_profile, fragment, FragmentTags.USER_PROFILE)
                    .commit();
        } else {
            mBookPosition = savedInstanceState.getInt(Keys.BOOK_POSITION);
        }

        loadBookSearchResults();

        return view;
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(Keys.BOOK_POSITION, mBookDetailPager.getCurrentItem());
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

        inflater.inflate(R.menu.menu_book_detail, menu);

        final MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

        if (mAdapter.getCount() > 0) {
            final int position = mBookDetailPager.getCurrentItem();
            updateShareIntent(mAdapter.getBookTitleForPosition(position));
            updateUserProfile(position);
        } else {
            updateShareIntent(null);
        }
    }

    /**
     * Starts the loader for book search results
     */
    private void loadBookSearchResults() {
        getLoaderManager()
                .restartLoader(Loaders.SEARCH_BOOKS_ON_PAGER, null, this);
    }

    /** Pager Adapter to page between books for Search */
    public class BookPageAdapter extends FragmentPagerAdapter {

        private Cursor mCursor;

        public BookPageAdapter(FragmentManager fm, Cursor cursor) {
            super(fm);
            mCursor = cursor;
            notifyDataSetChanged();
        }

        /**
         * Swaps the backing Cursor with a new one
         */
        public Cursor swapCursor(final Cursor newCursor) {
            final Cursor oldCursor = mCursor;
            mCursor = newCursor;
            notifyDataSetChanged();
            return oldCursor;
        }

        @Override
        public int getCount() {
            return mCursor != null ? mCursor.getCount() : 0;
        }

        @Override
        public Fragment getItem(int position) {

            mCursor.moveToPosition(position);
            final BookDetailFragment fragment = BookDetailFragment
                    .newInstance(Utils.cursorToBundle(mCursor));
            return fragment;

        }

        public String getBookTitleForPosition(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getString(mCursor
                                             .getColumnIndex(DatabaseColumns.TITLE));
        }

        public String getUserIdForPosition(int position) {
            mCursor.moveToPosition(position);
            return mCursor.getString(mCursor.getColumnIndex(DatabaseColumns.USER_ID));
        }

    }

    @Override
    protected Object getTaskTag() {
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

    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
        if (loaderId == Loaders.SEARCH_BOOKS_ON_PAGER) {
            return new SQLiteLoader(getActivity(), false, TableSearchBooks.NAME, null, null, null,
                                    null, null, null, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == Loaders.SEARCH_BOOKS_ON_PAGER) {

            mAdapter.swapCursor(cursor);

            if (cursor.getCount() > 0) {
                mBookDetailPager.setCurrentItem(mBookPosition);

            /*
             * Viewpager doesn't call on page selected() on the listener if the
             * set item is 0. This is to workaround that
             */

                if (mBookPosition == 0 && cursor.getCount() > 0) {
                    onPageSelected(mBookPosition);
                }
            }

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

        updateShareIntent(mAdapter.getBookTitleForPosition(position));
        updateUserProfile(position);

    }

    /**
     * Updates the book owner profile for a particular selection
     *
     * @param position The book page selected
     */
    private void updateUserProfile(int position) {
        final ProfileFragment fragment = (ProfileFragment) getChildFragmentManager()
                .findFragmentByTag(FragmentTags.USER_PROFILE);

        if (fragment != null) {
            fragment.setUserId(mAdapter.getUserIdForPosition(position));
        }
    }


    /**
     * Updates the share intent
     *
     * @param bookTitle
     */
    private void updateShareIntent(String bookTitle) {

        if (mShareActionProvider == null) {
            return;
        }

        if (TextUtils.isEmpty(bookTitle)) {
            mShareActionProvider.setShareIntent(Utils
                                                        .createAppShareIntent(getActivity()));
            return;
        }

        final String referralId = SharedPreferenceHelper
                .getString(R.string.pref_share_token);
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

        return ((ProfileFragment) getChildFragmentManager()
                .findFragmentByTag(FragmentTags.USER_PROFILE))
                .willHandleDialog(dialog);
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
    public boolean onBackPressed() {

        if (mSlidingLayout.isExpanded()) {
            mSlidingLayout.collapsePane();
            return true;
        } else {
            return super.onBackPressed();
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
