
package li.barter.fragments;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout.PanelSlideListener;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import li.barter.R;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLiteLoader;
import li.barter.data.TableSearchBooks;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.Loaders;
import li.barter.utils.Logger;

/**
 * @author Anshul Kamboj Fragment for Paging Books Around Me. Also contains a
 *         Profile that the user can chat directly with the owner
 */

public class BooksPagerFragment extends AbstractBarterLiFragment implements
                LoaderCallbacks<Cursor>, OnPageChangeListener,
                PanelSlideListener {

    private static final String  TAG          = "BookDetailPagerFragment";

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
     * These arrays holds bookids and userids for all the books in the pager to
     * map them
     */
    private ArrayList<String>    mBookIdArray = new ArrayList<String>();
    private ArrayList<String>    mUserIdArray = new ArrayList<String>();

    /**
     * Used to provide a slide up UI companent to place the user's profile
     * fragment
     */
    private SlidingUpPanelLayout mLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                    Bundle savedInstanceState) {
        init(container, savedInstanceState);

        final View view = inflater
                        .inflate(R.layout.fragment_books_pager, container, false);
        final Bundle extras = getArguments();

        if (extras != null) {
            mBookCounter = extras.getInt(Keys.BOOK_COUNT);
            mBookPosition = extras.getInt(Keys.BOOK_POSITION);

        }

        mBookDetailPager = (ViewPager) view.findViewById(R.id.pager_books);
        mBookDetailPager.setOnPageChangeListener(this);
        mLayout = (SlidingUpPanelLayout) view.findViewById(R.id.sliding_layout);
        mLayout.setPanelSlideListener(this);

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

    /**
     * Starts the loader for book search results
     */
    private void loadBookSearchResults() {
        getLoaderManager().restartLoader(Loaders.SEARCH_BOOKS, null, this);
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
                            .newInstance(mUserIdArray.get(position), mBookIdArray
                                            .get(position));
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
    protected Object getVolleyTag() {
        // TODO Auto-generated method stub

        return hashCode();
    }

    @Override
    public void onSuccess(int requestId, IBlRequestContract request,
                    ResponseInfo response) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBadRequestError(int requestId, IBlRequestContract request,
                    int errorCode, String errorMessage,
                    Bundle errorResponseBundle) {
        // TODO Auto-generated method stub

    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
        if (loaderId == Loaders.SEARCH_BOOKS) {
            return new SQLiteLoader(getActivity(), false, TableSearchBooks.NAME, null, null, null, null, null, null, null);
        } else {
            return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (loader.getId() == Loaders.SEARCH_BOOKS) {

            mBookCounter = cursor.getCount();
            mBookIdArray.ensureCapacity(mBookCounter);
            mUserIdArray.ensureCapacity(mBookCounter);
            while (cursor.moveToNext()) {
                mBookIdArray.add(cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.BOOK_ID)));
                mUserIdArray.add(cursor.getString(cursor
                                .getColumnIndex(DatabaseColumns.USER_ID)));
            }

            mAdapter = new BookPageAdapter(getChildFragmentManager());

            mBookDetailPager.setAdapter(mAdapter);
            mBookDetailPager.setCurrentItem(mBookPosition);

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

        final ProfileFragment fragment = (ProfileFragment) getChildFragmentManager()
                        .findFragmentByTag(FragmentTags.USER_PROFILE);

        if (fragment != null) {
            fragment.setUserId(mUserIdArray.get(position));
        }
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

    /**
     * @param view The drag handle to be set for the Sliding Pane Layout
     */
    public void setDragHandle(View view) {

        Logger.v(TAG, "Setting Drag View %s", view.toString());
        mLayout.setDragView(view);
        mLayout.setEnableDragViewTouchEvents(false);
    }

    @Override
    public void onBackPressed() {

        if (mLayout.isExpanded()) {
            mLayout.collapsePane();
        } else {
            super.onBackPressed();
        }
    }

}
