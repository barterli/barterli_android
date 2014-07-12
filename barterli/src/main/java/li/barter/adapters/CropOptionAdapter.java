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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import li.barter.R;
import li.barter.models.CropOption;

/**
 * Adapter for crop option list.
 * 
 * @author Sharath Pandeshwar
 */
public class CropOptionAdapter extends ArrayAdapter<CropOption> {
    private final ArrayList<CropOption> mOptions;
    private final LayoutInflater        mInflater;

    public CropOptionAdapter(final Context context, final ArrayList<CropOption> options) {
        super(context, R.layout.crop_selector, options);

        mOptions = options;

        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(final int position, View convertView,
                    final ViewGroup group) {
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.crop_selector, null);
        }

        final CropOption item = mOptions.get(position);

        if (item != null) {
            ((ImageView) convertView.findViewById(R.id.iv_icon))
                            .setImageDrawable(item.icon);
            ((TextView) convertView.findViewById(R.id.tv_name))
                            .setText(item.title);

            return convertView;
        }

        return null;
    }
}
