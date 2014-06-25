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

package li.barter.widgets.autocomplete;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Vinay S Shenoy
 */
public class SuggestionFilter extends Filter {

    private final SuggestionsAdapter mSuggestionsAdapter;

    /**
     * @param suggestionsAdapter A reference to the {@link SuggestionsAdapter}
     *            this filter will be filtering
     */
    public SuggestionFilter(final SuggestionsAdapter suggestionsAdapter) {
        mSuggestionsAdapter = suggestionsAdapter;
    }

    @Override
    protected FilterResults performFiltering(final CharSequence constraint) {

        final FilterResults results = new FilterResults();
        final List<Suggestion> suggestions = mSuggestionsAdapter
                        .getSuggestionsMaster();

        results.count = (suggestions == null ? 0 : suggestions.size());

        if (results.count > 0) {

            if (constraint == null) {
                results.values = suggestions;
            } else {
                final ArrayList<Suggestion> filtered = new ArrayList<Suggestion>(results.count);

                for (final Suggestion eachSuggestion : suggestions) {

                    /*
                     * if (eachSuggestion.name.contains(constraint)) {
                     * filtered.add(eachSuggestion); }
                     */

                    if (eachSuggestion.name.toLowerCase().contains(constraint
                                    .toString().toLowerCase())) {
                        filtered.add(eachSuggestion);
                    }
                }

                filtered.trimToSize();
                results.values = filtered;
            }

        }
        return results;
    }

    @Override
    protected void publishResults(final CharSequence constraint,
                    final FilterResults results) {

        if ((results != null) && (results.count > 0)) {
            mSuggestionsAdapter
                            .setSuggestions((List<Suggestion>) results.values);
        } else {
            mSuggestionsAdapter.notifyDataSetInvalidated();
        }
    }
    
    @Override
    public CharSequence convertResultToString(Object resultValue) {
        return ((Suggestion) resultValue).name;
    }

}
