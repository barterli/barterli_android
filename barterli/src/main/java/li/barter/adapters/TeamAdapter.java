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

import com.squareup.picasso.Picasso;

import li.barter.R;
import li.barter.models.Team;
import li.barter.utils.AvatarBitmapTransformation;
import li.barter.widgets.RoundedCornerImageView;

/**
 * Adapter for displaying OSS Licenses
 *
 * @author Vinay S Shenoy
 */
public class TeamAdapter extends BaseAdapter {

    private static final String TAG = "TeamAdapter";

    /**
     * A String array containing the Linces to display
     */
    private final Team[] mTeamMembers;

    /**
     * A reference to the {@link LayoutInflater} to inflating layouts
     */
    private final LayoutInflater mLayoutInflater;

    private final Context mContext;

    public TeamAdapter(final Context context, final Team[] mTeams) {

        mTeamMembers = mTeams;
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public int getCount() {
        return mTeamMembers.length;
    }

    @Override
    public Team getItem(final int position) {
        return mTeamMembers[position];
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
            view = mLayoutInflater.inflate(R.layout.layout_team, parent, false);
            view.setTag(R.id.team_desc, view.findViewById(R.id.team_desc));
            view.setTag(R.id.team_name, view.findViewById(R.id.team_name));
            view.setTag(R.id.team_image, view.findViewById(R.id.team_image));
            view.setTag(R.string.tag_avatar_transformation, new AvatarBitmapTransformation(AvatarBitmapTransformation.AvatarSize.SMALL));
        }
        final Team teamMember = getItem(position);
        ((TextView) view.getTag(R.id.team_name)).setText(teamMember.getName());
        ((TextView) view.getTag(R.id.team_desc)).setText(teamMember
                                                                 .getDescription());


        final RoundedCornerImageView roundedCornerImageView = (RoundedCornerImageView) view.getTag(R.id.team_image);
        roundedCornerImageView.setImageResource(0);

        final AvatarBitmapTransformation bitmapTransformation = (AvatarBitmapTransformation) view.getTag(R.string.tag_avatar_transformation);

        Picasso.with(mContext).load(teamMember.getImageUrl())
               .error(R.drawable.pic_avatar)
               .transform(bitmapTransformation)
               .into(roundedCornerImageView.getTarget());

        return view;
    }

}
