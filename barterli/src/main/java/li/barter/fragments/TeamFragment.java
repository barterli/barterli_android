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

import com.android.volley.Request.Method;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import li.barter.R;
import li.barter.adapters.TeamAdapter;
import li.barter.analytics.AnalyticsConstants.Screens;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.models.Team;
import li.barter.utils.Logger;


@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class TeamFragment extends AbstractBarterLiFragment {

    private static final String TAG = "TeamFragment";

    
    private GridView            mGridView;

    private TextView            mAboutBarterli;
    private Team[]              mTeams;

    /**
     * Adapter for displaying Team members
     */
    private TeamAdapter         mTeamAdapter;
    
    private boolean mLoadedIndividually;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        mLoadedIndividually = false;
        final View view = inflater
                .inflate(R.layout.fragment_team, null);
        mGridView = (GridView) view.findViewById(R.id.team_grid);

        
        // Make a call to server
        try {

            final BlRequest request = new BlRequest(Method.GET, HttpConstants.getApiBaseUrl()
                            + ApiEndpoints.TEAM, null, mVolleyCallbacks);
            request.setRequestId(RequestId.TEAM);
            addRequestToQueue(request, true, 0,true);
        } catch (final Exception e) {
            // Should never happen
            Logger.e(TAG, e, "Error building report bug json");
        }
        return view;
    }
    
    @Override
    protected String getAnalyticsScreenName() {
        if(mLoadedIndividually) {
            return Screens.TEAM;
        } else {
            return "";
        }
    }

    @Override
    protected Object getTaskTag() {
        return hashCode();
    }

    @Override
    public void onSuccess(final int requestId,
                    final IBlRequestContract request,
                    final ResponseInfo response) {

        if (requestId == RequestId.TEAM) {
            try {
                Logger.v(TAG, response.responseBundle.toString());
                mTeams = (Team[]) response.responseBundle
                                .getParcelableArray(HttpConstants.TEAM);
                Logger.v(TAG, response.responseBundle
                                .getParcelableArray(HttpConstants.TEAM)
                                .toString());
                mTeamAdapter = new TeamAdapter(getActivity(), mTeams);
                mGridView.setAdapter(mTeamAdapter);

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

	public static TeamFragment newInstance() {
		TeamFragment f = new TeamFragment();
		return f;
	}

}
