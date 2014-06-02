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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import li.barter.R;
import li.barter.adapters.OssLicensesAdapter;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;

/**
 * @author Vinay S Shenoy Fragment to display OSS Software used in the
 *         application
 */
@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class OssLicenseFragment extends AbstractBarterLiFragment {

    private static final String TAG = "OssLicenseFragment";

    /**
     * List that displays the Oss Licenses
     */
    private ListView            mListView;

    /**
     * Adapter for displaying Oss Licenses
     */
    private OssLicensesAdapter  mLicensesAdapter;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        mListView = (ListView) inflater
                        .inflate(R.layout.fragment_oss_licenses, container, false);
        mLicensesAdapter = new OssLicensesAdapter(getActivity(), R.array.oss_titles);
        mListView.setAdapter(mLicensesAdapter);
        setActionBarDrawerToggleEnabled(false);
        return mListView;
    }

    @Override
    protected Object getVolleyTag() {
        return hashCode();
    }

    @Override
    public void onSuccess(final int requestId,
                    final IBlRequestContract request,
                    final ResponseInfo response) {

    }

    @Override
    public void onBadRequestError(final int requestId,
                    final IBlRequestContract request, final int errorCode,
                    final String errorMessage, final Bundle errorResponseBundle) {

    }

	public static OssLicenseFragment newInstance() {
		OssLicenseFragment f = new OssLicenseFragment();
		return f;
	}

}
