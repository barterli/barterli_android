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
 * for the list, so if yu need to capture the scroll events, you can provide
 * your own {@link OnScrollListener} through the relevant methods
 * 
 * @author Vinay S Shenoy
 */
public class LoadMoreHelper implements OnScrollListener {

    private static final String TAG                 = "LoadMoreHelper";

    private static final int    DEFAULT_LOAD_OFFSET = 1;

    /**
     * External {@link OnScrollListener} listener
     */
    private OnScrollListener    mExternalOnScrollListener;

    /**
     * Reference to the {@link AbsListView} which needs the Load More
     * implementation
     */
    private final AbsListView   mAbsListView;

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
     * Constructor which takes a reference to the list to provide load more
     * functionality, as well as an onScrollListener to provide the scroll
     * events to
     * 
     * @param absListView The {@link AbsListView} to provide load more
     *            functionality
     * @param loadMoreCallbacks An implementation of
     *            {@linkplain LoadMoreCallbacks} to trigger the load more events
     * @param externalOnScrollListener The {@link OnScrollListener} to move up
     *            the scroll events
     */
    public LoadMoreHelper(AbsListView absListView, LoadMoreCallbacks loadMoreCallbacks, OnScrollListener externalOnScrollListener) {
        mAbsListView = absListView;
        mLoadMoreCallbacks = loadMoreCallbacks;
        mExternalOnScrollListener = externalOnScrollListener;
        init();
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
    public void setLoadMoreCallbacks(LoadMoreCallbacks callbacks) {
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
                    OnScrollListener externalOnScrollListener) {
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
    public void setLoadMoreEnabled(boolean enabled) {
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
    public void setLoadMoreOffset(int loadMoreOffset) {
        mLoadMoreOffset = loadMoreOffset;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub

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
