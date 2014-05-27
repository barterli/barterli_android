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
import li.barter.models.Venue;

/**
 * Adapter for displaying a list of places to select a list of places from
 * 
 * @author Vinay S Shenoy
 */
public class SelectLocationAdapter extends BaseAdapter {

    /**
     * Array of {@link Venue} objects, used as the data source for the adapter
     */
    private Venue[] mVenues;

    /**
     * A String which describes a formatted string with an integer argument for
     * formating the distance of the place from the user's location
     */
    private String  mDistanceFormat;

    /**
     * @param context
     * @param venues An Array of {@link Venue} objects to serve as the data
     *            source
     */
    public SelectLocationAdapter(Context context, Venue[] venues) {
        mVenues = venues;
        mDistanceFormat = context.getString(R.string.meters_away);
    }

    @Override
    public int getCount() {
        return mVenues == null ? 0 : mVenues.length;
    }

    @Override
    public Object getItem(int position) {
        return mVenues[position];
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
                            .inflate(R.layout.layout_select_place_item, parent, false);
            view.setTag(R.id.text_place_name, view
                            .findViewById(R.id.text_place_name));
            view.setTag(R.id.text_place_address, view
                            .findViewById(R.id.text_place_address));
            view.setTag(R.id.text_distance, view
                            .findViewById(R.id.text_distance));
        }

        final Venue venue = mVenues[position];

        ((TextView) view.getTag(R.id.text_place_name)).setText(venue.name);
        ((TextView) view.getTag(R.id.text_place_address))
                        .setText(venue.address);
        ((TextView) view.getTag(R.id.text_distance)).setText(String
                        .format(mDistanceFormat, venue.distance));

        return view;
    }

    public void setVenues(Venue[] venues) {
        mVenues = venues;
        notifyDataSetChanged();
    }
}
