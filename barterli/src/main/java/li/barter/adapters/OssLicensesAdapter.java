/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
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
 * Adapter for displaying OSS Licenses
 * 
 * @author Vinay S Shenoy
 */
public class OssLicensesAdapter extends BaseAdapter {

    /**
     * A String array containing the Linces to display
     */
    private final String[]       mLicenseStrings;

    /**
     * A reference to the {@link LayoutInflater} to inflating layouts
     */
    private final LayoutInflater mLayoutInflater;

    public OssLicensesAdapter(final Context context, final int licenseStringsResId) {
        mLicenseStrings = context.getResources()
                        .getStringArray(licenseStringsResId);
        mLayoutInflater = LayoutInflater.from(context);
        assert (mLicenseStrings != null);
    }

    @Override
    public int getCount() {
        return mLicenseStrings.length;
    }

    @Override
    public Object getItem(final int position) {
        return mLicenseStrings[position];
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    public View getView(final int position, final View convertView,
                    final ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            view = mLayoutInflater
                            .inflate(R.layout.layout_oss_item, parent, false);
        }

        ((TextView) view).setText(getItem(position).toString());
        return view;
    }

}
