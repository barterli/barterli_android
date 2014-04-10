/*******************************************************************************
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

package li.barter.fragments;

import java.io.IOException;
import java.util.ArrayList;

import li.barter.R;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.ImageByte;
import li.barter.utils.Logger;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request.Method;

/**
 * @author Sharath Pandeshwar
 */

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class TributeFragment  extends AbstractBarterLiFragment {

    private static final String           TAG          = "TributeFragment";

    private TextView                      mTributeTextView;
    private ImageView                     mTributeImageView;
    private String                        mDefaultName = "Tribute To Aaron Swartz";

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container);
        setHasOptionsMenu(true);
        final View view = inflater
                        .inflate(R.layout.fragment_tribute, null);

        mTributeTextView = (TextView) view
                        .findViewById(R.id.tribute_text);
        mTributeImageView = (ImageView) view
                        .findViewById(R.id.tribute_image);
        mTributeTextView.setText("");
      
        // Make a call to server
        try {
            
            final BlRequest request = new BlRequest(Method.GET, HttpConstants.getApiBaseUrl()
                            + ApiEndpoints.TRIBUTE, null, mVolleyCallbacks);
            request.setRequestId(RequestId.TRIBUTE);
            addRequestToQueue(request, true, 0);
        } catch (final Exception e) {
            // Should never happen
            Logger.e(TAG, e, "Error building report bug json");
        }
  
        return view;
    }

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    @Override
    public void onSuccess(int requestId, IBlRequestContract request,
                    ResponseInfo response) {

        if (requestId == RequestId.TRIBUTE) {
            	try {
                   String image_url = response.responseBundle.getString(HttpConstants.TRIBUTE_IMAGE_URL);
                   String message = response.responseBundle.getString(HttpConstants.TRIBUTE_TEXT);
                   mTributeTextView.setText(message);
                   new FetchImageTask().execute(image_url);
                   Logger.e(TAG, image_url);
                  // mTributeTextView.setText(message);
                   
                     
            	}
            	catch (final Exception e) {
                    // Should never happen
                    Logger.e(TAG, e, "Error parsing json response");
                }
            }
     }

    @Override
    public void onBadRequestError(int requestId, IBlRequestContract request,
                    int errorCode, String errorMessage,
                    Bundle errorResponseBundle) {
    }
    
    
    private class FetchImageTask extends AsyncTask<String,Void,Bitmap> {
        @Override
        public Bitmap doInBackground(String... params) {
        	Bitmap bitmap = null;
        	for (String image_url : params) {
        	  try {
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

        @Override
        protected void onPostExecute(Bitmap bitmap) {
        	
        	mTributeImageView.setImageBitmap(bitmap);  
       
        }	
        
    }
    

    

}
