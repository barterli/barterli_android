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

import com.android.volley.Request.Method;
import com.squareup.picasso.Picasso;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import li.barter.R;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.Logger;

/**
 * @author Sharath Pandeshwar
 */

@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class TributeFragment extends AbstractBarterLiFragment {

    private static final String TAG          = "TributeFragment";

    private TextView            mTributeTextView;
    private ImageView           mTributeImageView;
    private final String        mDefaultName = "Tribute To Aaron Swartz";

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        setActionBarTitle(R.string.Tribute_fragment_title);
        setActionBarDrawerToggleEnabled(false);
        final View view = inflater.inflate(R.layout.fragment_tribute, null);

        mTributeTextView = (TextView) view.findViewById(R.id.tribute_text);
        mTributeImageView = (ImageView) view.findViewById(R.id.tribute_image);
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
    public void onSuccess(final int requestId,
                    final IBlRequestContract request,
                    final ResponseInfo response) {

        if (requestId == RequestId.TRIBUTE) {
            try {
                final String imageUrl = response.responseBundle
                                .getString(HttpConstants.TRIBUTE_IMAGE_URL);
                final String message = response.responseBundle
                                .getString(HttpConstants.TRIBUTE_TEXT);
                mTributeTextView.setText(message);
                Picasso.with(getActivity()).load(imageUrl).fit()
                                .into(mTributeImageView);
                Logger.v(TAG, imageUrl);

            } catch (final Exception e) {
                // Should never happen
                Logger.e(TAG, e, "Error parsing json response");
            }
        }
    }

    @Override
    public void onBadRequestError(final int requestId,
                    final IBlRequestContract request, final int errorCode,
                    final String errorMessage, final Bundle errorResponseBundle) {
    }

}
