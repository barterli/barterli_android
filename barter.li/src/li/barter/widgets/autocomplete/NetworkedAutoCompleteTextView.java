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
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import li.barter.widgets.TypefacedAutoCompleteTextView;

/**
 * @author Vinay S Shenoy Custom AutoCompleteTextView to provide suggestions
 *         when typing from internet
 */
public class NetworkedAutoCompleteTextView extends
                TypefacedAutoCompleteTextView implements OnItemClickListener {

    private static final String       TAG = "NetworkedAutoCompleteTextView";

    /**
     * The threshold of the search query length at which the network search
     * should be performed
     */
    private int                       mSuggestCountThreshold;

    /**
     * The amount of time(in milliseconds) to wait after the user has typed to
     * actually trigger the network search
     */
    private int                       mSuggestWaitThreshold;

    /**
     * The current suggestions used for displaying the dropdowns
     */
    private List<Suggestion>          mSuggestions;

    /**
     * Holds a reference to the last search sequence. Used for optimizing
     * network calls
     */
    private String                    mLastSearchSequence;

    /**
     * Handler for posting callbacks for perfoming the search request
     */
    private Handler                   mHandler;

    /**
     * Runnable for perfoming search requests
     */
    private Runnable                  mPerformSearchRunnable;

    /**
     * Callbacks for perfomiong search requests
     */
    private INetworkSuggestCallbacks  mNetworkSuggestCallbacks;

    /**
     * TextWatcher reference for performing network requests
     */
    private SuggestNetworkTextWatcher mSuggestNetworkTextWatcher;

    /**
     * Whether the network suggestions are enabled or not
     */
    private boolean                   mSuggestionsEnabled;

    /**
     * Suggestions adapter for providing suggestions
     */
    private SuggestionsAdapter        mSuggestionsAdapter;

    /**
     * @param context
     */
    public NetworkedAutoCompleteTextView(final Context context) {
        super(context);
        init();
    }

    /**
     * @param context
     * @param attrs
     */
    public NetworkedAutoCompleteTextView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mSuggestNetworkTextWatcher = new SuggestNetworkTextWatcher();
        addTextChangedListener(mSuggestNetworkTextWatcher);
        setOnItemClickListener(this);
        mHandler = new Handler();
        mSuggestionsAdapter = new SuggestionsAdapter(null);
        setAdapter(mSuggestionsAdapter);
        mSuggestionsEnabled = true;
    }

    /**
     * Enable/Disable the network suggestions
     * 
     * @param enabled <code>true</code> to enable network suggestions,
     *            <code>false</code> to disable them
     */
    public void setNetworkSuggestionsEnabled(final boolean enabled) {

        mSuggestionsEnabled = enabled;
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

    public INetworkSuggestCallbacks getNetworkSuggestCallbacks() {
        return mNetworkSuggestCallbacks;
    }

    public void setNetworkSuggestCallbacks(
                    final INetworkSuggestCallbacks callbacks) {
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
    public void onSuggestionsFetched(final String query,
                    final Suggestion[] suggestions, final boolean replace) {

        mLastSearchSequence = query;
        if (mSuggestions == null) {
            mSuggestions = new ArrayList<Suggestion>();
        }

        if (replace) {
            mSuggestions.clear();
        }

        mSuggestions.addAll(Arrays.asList(suggestions));
        mSuggestionsAdapter.setSuggestionsMaster(mSuggestions);
        performFiltering(query, 0);
    }

    /**
     * {@link TextWatcher} implementation for perfoming Network calls
     * 
     * @author Vinay S Shenoy
     */
    private class SuggestNetworkTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(final CharSequence s, final int start,
                        final int count, final int after) {
        }

        @Override
        public void onTextChanged(final CharSequence s, final int start,
                        final int before, final int count) {

            if (mSuggestionsEnabled) {

                final String newSearchSequence = s.toString();
                /*
                 * if (!TextUtils.isEmpty(mLastSearchSequence) &&
                 * newSearchSequence .startsWith(mLastSearchSequence)) { //Don't
                 * fetch new search results if the new search sequence starts
                 * with the older search sequence return; }
                 */
                removeAnyCallbacks();
                if (newSearchSequence.length() >= mSuggestCountThreshold) {

                    mPerformSearchRunnable = makeSearchRunnable(newSearchSequence);
                    mHandler.postDelayed(mPerformSearchRunnable, mSuggestWaitThreshold);
                }

            }

        }

        @Override
        public void afterTextChanged(final Editable s) {

        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeAnyCallbacks();
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
                if (mNetworkSuggestCallbacks != null) {
                    mNetworkSuggestCallbacks
                                    .performNetworkQuery(NetworkedAutoCompleteTextView.this, query);
                }
            }
        };
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {
        final Suggestion suggestion = (Suggestion) mSuggestionsAdapter
                        .getItem(position);

        if (mNetworkSuggestCallbacks != null) {
            mNetworkSuggestCallbacks.onSuggestionClicked(this, suggestion);
        }

    }
}
