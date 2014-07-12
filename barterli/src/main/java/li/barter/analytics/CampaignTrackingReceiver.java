/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package li.barter.analytics;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import li.barter.R;
import li.barter.http.HttpConstants;
import li.barter.utils.Logger;
import li.barter.utils.SharedPreferenceHelper;

/**
 * Class that receives campaign tracking intents and broadcasts them
 * 
 * @author Vinay S Shenoy
 */
public class CampaignTrackingReceiver extends BroadcastReceiver {

    private static final String TAG = "CampaignTrackingReceiver";
    
    @Override
    public void onReceive(Context context, Intent intent) {

        final String referrer = intent.getStringExtra(HttpConstants.REFERRER);
        Logger.v(TAG, "Campaign Referrer is %s", referrer);
        
        if(!TextUtils.isEmpty(referrer)) {
            //Store referrer in SharedPreferences for upload later
            SharedPreferenceHelper.set(R.string.pref_referrer, referrer);
        }
    }

}
