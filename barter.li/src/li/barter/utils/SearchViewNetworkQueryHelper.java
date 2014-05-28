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

import android.os.Handler;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

/**
 * Helper class for performing efficient network suggestions with SearchView
 * 
 * @author Vinay S Shenoy
 */
public class SearchViewNetworkQueryHelper implements OnQueryTextListener {

    /**
     * Callbacks to perform network queries
     * 
     * @author Vinay S Shenoy
     */
    public static interface NetworkCallbacks {
        public void performQuery(SearchView searchView, String query);
    }

    /**
     * SearchView to which this helper is set
     */
    private SearchView       mSearchView;

    /**
     * {@link NetworkCallbacks} implementation to perform the actual network
     * query
     */
    private NetworkCallbacks mNetworkCallbacks;

    private Handler          mHandler;

    private Runnable         mPerformSearchRunnable;

    /**
     * Minimum number of characters that need to be entered before the network
     * search is triggered
     */
    private int              mSuggestCountThreshold;

    /**
     * Minimum duration we need to wait after the user has stopped typing to
     * actually perform the search
     */
    private int              mSuggestWaitThreshold;

    /**
     * 
     */
    public SearchViewNetworkQueryHelper(SearchView searchView, NetworkCallbacks networkCallbacks) {
        mSearchView = searchView;
        mNetworkCallbacks = networkCallbacks;
        mHandler = new Handler();
        
        mSearchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {

        removeAnyCallbacks();
        if (newText != null && newText.length() >= mSuggestCountThreshold) {

            mPerformSearchRunnable = makeSearchRunnable(newText);
            mHandler.postDelayed(mPerformSearchRunnable, mSuggestWaitThreshold);
        }
        return true;
    }

    public int getSuggestCountThreshold() {
        return mSuggestCountThreshold;
    }

    public void setSuggestCountThreshold(final int suggestCountThreshold) {
        mSuggestCountThreshold = suggestCountThreshold;
    }

    public int getSuggestWaitThreshold() {
        return mSuggestWaitThreshold;
    }

    public void setSuggestWaitThreshold(final int suggestWaitThreshold) {
        mSuggestWaitThreshold = suggestWaitThreshold;
    }

    /**
     * Removes any pending callbacks(if any) from the handler
     */
    private void removeAnyCallbacks() {
        if (mPerformSearchRunnable != null) {
            mHandler.removeCallbacks(mPerformSearchRunnable);
        }
    }

    /**
     * Creates a runnable for perfoming a search query
     * 
     * @param query The query to search for
     * @return a {@link Runnable} for performing a search request
     */
    private Runnable makeSearchRunnable(final String query) {
        return new Runnable() {

            @Override
            public void run() {
                if (mNetworkCallbacks != null) {
                    mNetworkCallbacks.performQuery(mSearchView, query);
                }
            }
        };
    }

}
