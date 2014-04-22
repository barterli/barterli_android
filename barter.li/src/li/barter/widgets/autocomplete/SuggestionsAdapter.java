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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.List;

import li.barter.R;

/**
 * Adapter for suggestions
 * 
 * @author Vinay S Shenoy
 */
public class SuggestionsAdapter extends BaseAdapter implements Filterable {

    /**
     * The master list of suggestions from which other suggestions are fetched
     */
    private List<Suggestion> mSuggestionsMaster;

    /**
     * The list of suggestions that are actually displayed
     */
    private List<Suggestion> mVisibleSuggestions;

    /**
     * The filter for this adapter
     */
    private SuggestionFilter mSuggestionsFilter;

    /**
     * Construct a suggestions adapter with an initial data set
     * 
     * @param suggestions The initial master set of {@link Suggestion} objects
     */
    public SuggestionsAdapter(List<Suggestion> suggestions) {

        mSuggestionsMaster = suggestions;
        mSuggestionsFilter = new SuggestionFilter(this);
    }

    @Override
    public int getCount() {
        return mVisibleSuggestions == null ? 0 : mVisibleSuggestions.size();
    }

    @Override
    public Object getItem(int position) {
        return mVisibleSuggestions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {

            view = LayoutInflater
                            .from(parent.getContext())
                            .inflate(R.layout.layout_book_suggestion_item, parent, false);
            view.setTag(R.id.text_book_title, view
                            .findViewById(R.id.text_book_title));
        }

        ((TextView) view.getTag(R.id.text_book_title))
                        .setText(mVisibleSuggestions.get(position).name);
        return view;
    }

    @Override
    public Filter getFilter() {
        return mSuggestionsFilter;
    }

    /**
     * Gets the master list of suggestions contained in this adapter
     */
    public List<Suggestion> getSuggestionsMaster() {
        return mSuggestionsMaster;
    }

    /**
     * Swaps the list of suggestions
     * 
     * @param suggestions The list of suggestions
     */
    public void setSuggestions(List<Suggestion> suggestions) {
        mVisibleSuggestions = suggestions;
        notifyDataSetChanged();
    }

    /**
     * Swaps the list of master suggestions
     * 
     * @param suggestions The list of suggestions
     */
    public void setSuggestionsMaster(List<Suggestion> suggestions) {
        mSuggestionsMaster = suggestions;
        mVisibleSuggestions = null;
        //TODO Force a filter
        notifyDataSetChanged();
    }

}
