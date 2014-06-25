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

package li.barter.utils;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;

/**
 * Helper class to detect whenever an {@link AbsListView} has to given a Load
 * More implementation. This class will be set as the {@link OnScrollListener}
 * for the list, so if you need to capture the scroll events, you can provide
 * your own {@link OnScrollListener} through the relevant methods
 * 
 * @author Vinay S Shenoy
 */
public class LoadMoreHelper implements OnScrollListener {

    private enum ScrollDirection {
        UP,
        DOWN,
        SAME
    }

    private static final String TAG                 = "LoadMoreHelper";

    private static final int    DEFAULT_LOAD_OFFSET = 45;

    /**
     * External {@link OnScrollListener} listener
     */
    private OnScrollListener    mExternalOnScrollListener;

    /**
     * Reference to the {@link AbsListView} which needs the Load More
     * implementation
     */
    private AbsListView         mAbsListView;

    /**
     * Whether load more is enabled
     */
    private boolean             mIsLoadMoreEnabled;

    /**
     * The position offset from the bottom of the list at which the load event
     * must be triggered
     */
    private int                 mLoadMoreOffset;

    /**
     * Callbacks for trigereing the load events
     */
    private LoadMoreCallbacks   mLoadMoreCallbacks;

    /**
     * Current scrolling direction
     */
    private ScrollDirection     mCurScrollingDirection;

    /**
     * Holds the last first visible item. Used to calculate the scroll direction
     */
    private int                 mPrevFirstVisibleItem;

    /**
     * Construct a {@link LoadMoreHelper} class with a {@link LoadMoreCallbacks}
     * implementation
     * 
     * @param callbacks An implementation of {@linkplain LoadMoreCallbacks} to
     *            trigger the load more events
     */
    private LoadMoreHelper(final LoadMoreCallbacks callbacks) {
        mLoadMoreCallbacks = callbacks;
    }

    /**
     * Initialize the {@link LoadMoreHelper} with the callbacks
     * 
     * @param loadMoreCallbacks An implementation of
     *            {@linkplain LoadMoreCallbacks} to trigger the load more events
     */
    public static LoadMoreHelper init(LoadMoreCallbacks loadMoreCallbacks) {
        return new LoadMoreHelper(loadMoreCallbacks);
    }

    /**
     * Set an external {@link OnScrollListener} to receive the OnScroll events
     * 
     * @param onScrollListener The {@link OnScrollListener} to move up the
     *            scroll events
     */
    public LoadMoreHelper withExternalOnScrollListener(
                    OnScrollListener onScrollListener) {

        if (mAbsListView != null) {
            throw new IllegalArgumentException("Should set external on scroll listener before setting AbsListView");
        }
        mExternalOnScrollListener = onScrollListener;
        return this;
    }

    /**
     * Set the {@link AbsListView} reference to provide the load more callbacks
     * on
     * 
     * @param absListView The {@link AbsListView} to provide load more
     *            functionality
     */
    public LoadMoreHelper on(AbsListView absListView) {
        mAbsListView = absListView;
        init();
        return this;
    }

    /**
     * Initialize the class parameters
     */
    private void init() {

        mAbsListView.setOnScrollListener(this);
        mIsLoadMoreEnabled = true;
        mLoadMoreOffset = DEFAULT_LOAD_OFFSET;
    }

    /**
     * Gets the {@link LoadMoreCallbacks} reference currently set
     */
    public LoadMoreCallbacks getLoadMoreCallbacks() {
        return mLoadMoreCallbacks;
    }

    /**
     * Sets the {@link LoadMoreCallbacks} to receive the load more events
     */
    public void setLoadMoreCallbacks(final LoadMoreCallbacks callbacks) {
        mLoadMoreCallbacks = callbacks;
    }

    /**
     * Gets the external {@link OnScrollListener} added
     */
    public OnScrollListener getExternalScrollListener() {
        return mExternalOnScrollListener;
    }

    /**
     * Sets an external {@link OnScrollListener} to receive the scroll events
     */
    public void setExternalOnScrollListener(
                    final OnScrollListener externalOnScrollListener) {
        mExternalOnScrollListener = externalOnScrollListener;
    }

    /**
     * Whether the load more is enabled
     */
    public boolean isLoadMoreEnabled() {
        return mIsLoadMoreEnabled;
    }

    /**
     * Used to enable or disable the load more functionality
     */
    public void setLoadMoreEnabled(final boolean enabled) {
        mIsLoadMoreEnabled = enabled;
    }

    /**
     * Gets the load more offset(default 2). This is the position from the
     * bottom at which the load more event is triggered
     */
    public int getLoadMoreOffset() {
        return mLoadMoreOffset;
    }

    /**
     * Sets the load more offset. This is the position from the bottom at which
     * the load more event is triggered
     */
    public void setLoadMoreOffset(final int loadMoreOffset) {
        mLoadMoreOffset = loadMoreOffset;
    }

    @Override
    public void onScrollStateChanged(final AbsListView view,
                    final int scrollState) {

        mCurScrollingDirection = null;

        if (mExternalOnScrollListener != null) {
            mExternalOnScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    @Override
    public void onScroll(final AbsListView view, final int firstVisibleItem,
                    final int visibleItemCount, final int totalItemCount) {

        if (mCurScrollingDirection == null) { //User has just started a scrolling motion
            /*
             * Doesn't matter what we set as, the actual setting will happen in
             * the next call to this method
             */
            mCurScrollingDirection = ScrollDirection.SAME;
            mPrevFirstVisibleItem = firstVisibleItem;
        } else {
            if (firstVisibleItem > mPrevFirstVisibleItem) {
                //User is scrolling up
                mCurScrollingDirection = ScrollDirection.UP;
            } else if (firstVisibleItem < mPrevFirstVisibleItem) {
                //User is scrolling down
                mCurScrollingDirection = ScrollDirection.DOWN;
            } else {
                mCurScrollingDirection = ScrollDirection.SAME;
            }
            mPrevFirstVisibleItem = firstVisibleItem;
        }

        if (mIsLoadMoreEnabled
                        && (mCurScrollingDirection == ScrollDirection.UP)) {
            //We only need to paginate if user scrolling near the end of the list

            if (!mLoadMoreCallbacks.isLoading()
                            && !mLoadMoreCallbacks.hasLoadedAllItems()) {
                //Only trigger a load more if a load operation is NOT happening AND all the items have not been loaded
                final int lastAdapterPosition = totalItemCount - 1;
                final int lastVisiblePosition = (firstVisibleItem + visibleItemCount) - 1;
                if (lastVisiblePosition >= (lastAdapterPosition - mLoadMoreOffset)) {
                    mLoadMoreCallbacks.onLoadMore();
                }

            }
        }
        if (mExternalOnScrollListener != null) {
            mExternalOnScrollListener
                            .onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    /**
     * Callbacks for load more methods
     * 
     * @author Vinay S Shenoy
     */
    public static interface LoadMoreCallbacks {

        /**
         * Callback for when the next set of items should be loaded
         */
        public void onLoadMore();

        /**
         * Callback for whether a load operation is currently ongoing
         * 
         * @return <code>true</code> if a load operation is happening,
         *         <code>false</code> otherwise. If <code>true</code>, load more
         *         event won't be triggered
         */
        public boolean isLoading();

        /**
         * Callback for whether all items have been loaded
         * 
         * @return <code>true</code> if all items have been loaded,
         *         <code>false</code> otherwise. If <code>true</code>, load more
         *         event won't be triggered
         */
        public boolean hasLoadedAllItems();
    }

}
