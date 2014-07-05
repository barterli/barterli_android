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
public class NavDrawerAdapter extends BaseAdapter {

    private static final String TAG = "NavDrawerAdapter";

    /** View type for primary options */
    private static final int VIEW_PRIMARY = 0;

    /** View type for secondary options */
    private static final int VIEW_SECONDARY = 1;

    /**
     * Navigation Drawer primary options
     */
    private final String[] mNavDrawerPrimaryOptions;

    /**
     * Navigation Drawer secondary options
     */
    private final String[] mNavDrawerSecondaryOptions;

    /**
     * Layout Inflater for inflating layouts
     */
    private final LayoutInflater mLayoutInflater;


    /**
     * COnstruct the adapter for the Navigation drawer
     *
     * @param context                         {@link Context} reference
     * @param drawerItemPrimaryOptionsResId   The resource id of an aray that contains the strings
     *                                        of the titles in the nav drawer
     * @param drawerItemSecondaryOptionsResId The resource id of an array that contains the strings
     *                                        of the descriptions of the items in the navigation
     *                                        drawer
     */
    public NavDrawerAdapter(final Context context, final int drawerItemPrimaryOptionsResId, final int drawerItemSecondaryOptionsResId) {
        mLayoutInflater = LayoutInflater.from(context);
        mNavDrawerPrimaryOptions = context.getResources()
                                          .getStringArray(drawerItemPrimaryOptionsResId);
        mNavDrawerSecondaryOptions = context.getResources()
                                            .getStringArray(drawerItemSecondaryOptionsResId);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return mNavDrawerPrimaryOptions.length + mNavDrawerSecondaryOptions.length;
    }

    @Override
    public int getItemViewType(final int position) {

        if (position < mNavDrawerPrimaryOptions.length) {
            return VIEW_PRIMARY;
        } else if (position < (mNavDrawerPrimaryOptions.length + mNavDrawerSecondaryOptions.length)) {
            return VIEW_SECONDARY;
        } else {
            throw new IllegalStateException("Invalid position " + position);
        }
    }

    @Override
    public Object getItem(final int position) {
        final int viewType = getItemViewType(position);

        if (viewType == VIEW_PRIMARY) {
            return mNavDrawerPrimaryOptions[position];
        } else {
            return mNavDrawerSecondaryOptions[position - mNavDrawerPrimaryOptions.length];
        }
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView,
                        final ViewGroup parent) {

        final int viewType = getItemViewType(position);
        final String title = (String) getItem(position);

        View view = convertView;

        if (viewType == VIEW_PRIMARY) {

            if (view == null) {
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.layout_nav_drawer_item_primary, parent,
                                              false);
                view.setTag(R.id.text_nav_item_title, view
                        .findViewById(R.id.text_nav_item_title));
            }

            ((TextView) view.getTag(R.id.text_nav_item_title))
                    .setText(title);
        } else if (viewType == VIEW_SECONDARY) {
            if (view == null) {
                view = LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.layout_nav_drawer_item_secondary, parent,
                                              false);
                view.setTag(R.id.text_nav_item_title, view
                        .findViewById(R.id.text_nav_item_title));
            }

            ((TextView) view.getTag(R.id.text_nav_item_title))
                    .setText(title);
        }

        return view;
    }


}
