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

import li.barter.R;
import li.barter.models.Team;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter for displaying OSS Licenses
 * 
 * @author Vinay S Shenoy
 */
public class TeamAdapter extends BaseAdapter {

    /**
     * A String array containing the Linces to display
     */
    private final Team[]         mTeamMembers;

    /**
     * A reference to the {@link LayoutInflater} to inflating layouts
     */
    private final LayoutInflater mLayoutInflater;

    public TeamAdapter(Context context, Team[] mTeams) {
        
    	mTeamMembers = mTeams;
        mLayoutInflater = LayoutInflater.from(context);
        
    }

    @Override
    public int getCount() {
        return mTeamMembers.length;
    }

    @Override
    public Team getItem(int position) {
        return mTeamMembers[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null) {
            view = mLayoutInflater
                            .inflate(R.layout.layout_team, parent, false);
        }
        
        Team c = getItem(position);

        TextView mTeamEmailView = (TextView) view
                .findViewById(R.id.team_email);
        mTeamEmailView.setText(c.getEmail());
        TextView mTeamDescView = (TextView) view.findViewById(R.id.team_desc);
        mTeamDescView.setText(c.getDesc());
        ImageView mTeamImageView = (ImageView) view.findViewById(R.id.team_image);
        return view;
    }

}
