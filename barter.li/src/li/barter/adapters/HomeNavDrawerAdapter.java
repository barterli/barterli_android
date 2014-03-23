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

package li.barter.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import li.barter.R;

/**
 * Adapter for adding items in the Navigration in the home screen
 * 
 * @author Vinay S Shenoy
 */
public class HomeNavDrawerAdapter extends BaseAdapter {

    /**
     * Navigation Drawer titles
     */
    private String[]       mNavDrawerTitles;

    /**
     * Navigation Drawer descriptions
     */
    private String[]       mNavDrawerDescriptions;

    /**
     * Layout Inflater for inflating layouts
     */
    private LayoutInflater mLayoutInflater;

    /**
     * COnstruct the adapter for the Navigation drawer
     * 
     * @param context {@link Context} reference
     * @param drawerItemTitlesResId The resource id of an aray that contains the
     *            strings of the titles in the nav drawer
     * @param drawerItemDescriptionResId The resource id of an array that
     *            contains the strings of the descriptions of the items in the
     *            navigation drawer
     */
    public HomeNavDrawerAdapter(Context context, int drawerItemTitlesResId, int drawerItemDescriptionResId) throws IllegalArgumentException {
        mLayoutInflater = LayoutInflater.from(context);
        mNavDrawerTitles = context.getResources().getStringArray(
                        drawerItemTitlesResId);
        mNavDrawerDescriptions = context.getResources().getStringArray(
                        drawerItemDescriptionResId);

        if (mNavDrawerTitles.length != mNavDrawerDescriptions.length) {
            throw new IllegalArgumentException(
                            "The passed arrays do not have an equal number of items. There should be one description matching to each item. Add an empty item if you don't want any description to be displayed");
        }
    }

    @Override
    public int getCount() {
        return mNavDrawerTitles.length;
    }

    @Override
    public Object getItem(int position) {
        return mNavDrawerTitles[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            view = mLayoutInflater.inflate(R.layout.layout_nav_drawer_item,
                            parent, false);

            view.setTag(R.id.text_nav_item_title,
                            view.findViewById(R.id.text_nav_item_title));
            view.setTag(R.id.text_nav_desc,
                            view.findViewById(R.id.text_nav_desc));
        }

        ((TextView) view.getTag(R.id.text_nav_item_title))
                        .setText(mNavDrawerTitles[position]);
        ((TextView) view.getTag(R.id.text_nav_desc))
                        .setText(mNavDrawerDescriptions[position]);
        return view;
    }
    
    

}
