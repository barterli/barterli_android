/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package li.barter.widgets.autocomplete;

/**
 * Interface to provide helper methods for connecting to Volley
 * 
 * @author Vinay S Shenoy
 */
public interface INetworkSuggestCallbacks {

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
