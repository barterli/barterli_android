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

package li.barter.fragments;

import li.barter.R;
import li.barter.adapters.TeamAdapter;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.models.Team;
import li.barter.utils.Logger;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.volley.Request.Method;

/**
 * @author Vinay S Shenoy Fragment to display OSS Software used in the
 *         application
 */
@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class TeamFragment extends AbstractBarterLiFragment {

    private static final String TAG = "TeamFragment";

    /**
     * List that displays the Oss Licenses
     */
    private ListView            mListView;
    private Team []       mTeams;

    /**
     * Adapter for displaying Oss Licenses
     */
    private TeamAdapter  mTeamAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                    Bundle savedInstanceState) {
        init(container);
        mListView = (ListView) inflater
                        .inflate(R.layout.fragment_team, container, false);
        setActionBarDrawerToggleEnabled(false);
        // Make a call to server
        try {
            
            final BlRequest request = new BlRequest(Method.GET, HttpConstants.getApiBaseUrl()
                            + ApiEndpoints.TEAM, null, mVolleyCallbacks);
            request.setRequestId(RequestId.TEAM);
            addRequestToQueue(request, true, 0);
        } catch (final Exception e) {
            // Should never happen
            Logger.e(TAG, e, "Error building report bug json");
        }
        return mListView;
    }

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    @Override
    public void onSuccess(int requestId, IBlRequestContract request,
                    ResponseInfo response) {

        if (requestId == RequestId.TEAM) {
        	try {
        		Logger.e(TAG, response.responseBundle.toString());
        		mTeams = (Team [])response.responseBundle.getParcelableArray(HttpConstants.TEAM);
        		Logger.e(TAG, response.responseBundle.getParcelableArray(HttpConstants.TEAM).toString());
        		mTeamAdapter = new TeamAdapter(getActivity(), mTeams);
        	    mListView.setAdapter(mTeamAdapter) ;              
                 
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

}
