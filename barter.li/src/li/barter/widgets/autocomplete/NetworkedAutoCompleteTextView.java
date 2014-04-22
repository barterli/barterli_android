/*******************************************************************************
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
 ******************************************************************************/

package li.barter.widgets.autocomplete;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import li.barter.widgets.TypefacedAutoCompleteTextView;

/**
 * @author Vinay S Shenoy Custom AutoCompleteTextView to provide suggestions
 *         when typing from internet
 */
public class NetworkedAutoCompleteTextView extends
                TypefacedAutoCompleteTextView {

    private static final String     TAG = "NetworkedAutoCompleteTextView";

    /**
     * The threshold of the search query length at which the network search
     * should be performed
     */
    private int                     mSuggestCountThreshold;

    /**
     * The amount of time(in milliseconds) to wait after the user has typed to
     * actually trigger the network search
     */
    private int                     mSuggestWaitThreshold;

    /**
     * The current suggestions used for displaying the dropdowns
     */
    private List<Suggestion>        mSuggestions;

    /**
     * Holds a reference to the last search sequence. Used for optimizing
     * network calls
     */
    private String                  mLastSearchSequence;

    /**
     * Handler for posting callbacks for perfoming the search request
     */
    private Handler                 mHandler;

    /**
     * Runnable for perfoming search requests
     */
    private Runnable                mPerformSearchRunnable;

    /**
     * Callbacks for perfomiong search requests
     */
    private NetworkSuggestCallbacks mNetworkSuggestCallbacks;

    /**
     * @param context
     */
    public NetworkedAutoCompleteTextView(Context context) {
        super(context);
        init();
    }

    /**
     * @param context
     * @param attrs
     */
    public NetworkedAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        addTextChangedListener(new SuggestNetworkTextWatcher());
        mHandler = new Handler();
    }

    public int getSuggestCountThreshold() {
        return mSuggestCountThreshold;
    }

    public void setSuggestCountThreshold(int suggestCountThreshold) {
        mSuggestCountThreshold = suggestCountThreshold;
    }

    public int getSuggestWaitThreshold() {
        return mSuggestWaitThreshold;
    }

    public void setSuggestWaitThreshold(int suggestWaitThreshold) {
        mSuggestWaitThreshold = suggestWaitThreshold;
    }

    public NetworkSuggestCallbacks getNetworkSuggestCallbacks() {
        return mNetworkSuggestCallbacks;
    }

    public void setNetworkSuggestCallbacks(NetworkSuggestCallbacks callbacks) {
        mNetworkSuggestCallbacks = callbacks;
    }

    /**
     * Add a new set of suggestions to this TextView
     * 
     * @param query The query for which the siggestions are fetched
     * @param suggestions The list of suggestions to use
     * @param append <code>false</code> to add the new suggestions to the
     *            TextView, <code>true</code> to replace the suggestion
     */
    public void onSuggestionsFetched(String query, Suggestion[] suggestions,
                    boolean replace) {

        mLastSearchSequence = query;
        if (mSuggestions == null) {
            mSuggestions = new ArrayList<Suggestion>();
        }

        if (replace) {
            mSuggestions.clear();
        }

        mSuggestions.addAll(Arrays.asList(suggestions));
    }

    /**
     * Interface to provide helper methods for connecting to Volley
     * 
     * @author Vinay S Shenoy
     */
    public static interface NetworkSuggestCallbacks {

        /**
         * Perform a network query. Once the query returns, use
         * {@link NetworkedAutoCompleteTextView#onSuggestionsFetched(String, Suggestion[], boolean)}
         * to update the suggestions
         * 
         * @param textView The TextView that performed the search query
         * @param query The query text
         */
        public void performNetworkQuery(NetworkedAutoCompleteTextView textView,
                        String query);

        /**
         * The callback method when a suggestion is tapped
         * 
         * @param textView The TextView that performed the search query
         * @param suggestion The {@link Suggestion} that was tapped
         */
        public void onSuggestionClicked(NetworkedAutoCompleteTextView textView,
                        Suggestion suggestion);
    }

    /**
     * Class representing a suggestion
     * 
     * @author Vinay S Shenoy
     */
    public static class Suggestion {

        /**
         * The suggestion id. Used for selecting the suggestion when an item
         * from the drop down is tapped
         */
        public final String id;

        /**
         * The name of the suggestion. Used for displaying the title label
         */
        public final String name;

        /**
         * @param id The suggestion id
         * @param name The suggestion name
         */
        public Suggestion(String id, String name) {
            this.id = id;
            this.name = name;
        }

    }

    /**
     * {@link TextWatcher} implementation for perfoming Network calls
     * 
     * @author Vinay S Shenoy
     */
    private class SuggestNetworkTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                        int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                        int count) {
            
            final String newSearchSequence = s.toString();
            if (!TextUtils.isEmpty(mLastSearchSequence)
                            && newSearchSequence
                                            .startsWith(mLastSearchSequence)) {

                //Don't fetch new search results if the new search sequence starts with the older search sequence
                return;
            }

            if (newSearchSequence.length() >= mSuggestCountThreshold) {
                if (mPerformSearchRunnable != null) {
                    mHandler.removeCallbacks(mPerformSearchRunnable);
                }

                mPerformSearchRunnable = makeSearchRunnable(newSearchSequence);
                mHandler.postDelayed(mPerformSearchRunnable, mSuggestWaitThreshold);
            }

        }

        @Override
        public void afterTextChanged(Editable s) {

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
                if (mNetworkSuggestCallbacks != null) {
                    mNetworkSuggestCallbacks
                                    .performNetworkQuery(NetworkedAutoCompleteTextView.this, query);
                }
            }
        };
    }
}
