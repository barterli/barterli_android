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

import java.io.IOException;
import java.lang.ref.WeakReference;

import li.barter.R;
import li.barter.models.Team;
import li.barter.utils.ImageByte;
import li.barter.utils.Logger;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
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
	
	 private static final String TAG       = "TeamAdapter";

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
        //TextView mTeamEmailView = (TextView) view
        //       .findViewById(R.id.team_email);
        //mTeamEmailView.setText(c.getEmail());
        TextView mTeamDescView = (TextView) view.findViewById(R.id.team_desc);
        TextView mTeamNameView = (TextView) view.findViewById(R.id.team_name);
        mTeamNameView.setText(c.getName());
        mTeamDescView.setText(c.getDescription());
        ImageView mTeamImageView = (ImageView) view.findViewById(R.id.team_image);
        mTeamImageView.setImageResource(R.drawable.download_image);
        new FetchImageTask(mTeamImageView).execute(c.getImageUrl());
        return view;
    }
    
    
    private class FetchImageTask extends AsyncTask<String,Void,Bitmap> {
	    private  WeakReference<ImageView> imageViewReference;
        @Override
        public Bitmap doInBackground(String... params) {
        	Bitmap bitmap = null;
        	for (String image_url : params) {
        	  try {
        		if(image_url == null)
        			return null;
        		byte[] bitmapBytes = new ImageByte().getUrlBytes(image_url);
                bitmap = BitmapFactory
                .decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
        	  }
        	  catch (IOException e1) {
                  e1.printStackTrace();
              }
                
        	}
        	return bitmap;	
       }
        
        public FetchImageTask(ImageView imageView){
        	imageViewReference = new WeakReference<ImageView>(imageView);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
        	if(bitmap == null)
        	  return;
    	    ImageView imageView = imageViewReference.get();
    	    imageView.setImageBitmap(bitmap);  
       
        }	
        
    }

}
