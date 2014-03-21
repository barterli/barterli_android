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
package li.barter.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.Locale;

import li.barter.R;

public class BooksAroundMeAdapter extends BaseAdapter {

    private static final int    COUNT  = 20;

    private static final String FORMAT = "Book #%d";

    @Override
    public int getCount() {
        return COUNT;
    }

    @Override
    public Object getItem(final int position) {
        return String.format(Locale.US, FORMAT, position + 1);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView,
                    final ViewGroup parent) {

        View view = convertView;

        if (convertView == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(
                            R.layout.layout_item_book, null);
            view.setTag(R.id.text_book_name,
                            view.findViewById(R.id.text_book_name));
        }

        ((TextView) view.getTag(R.id.text_book_name))
                        .setText((String) getItem(position));
        return view;
    }

}
