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

package li.barter.http;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import li.barter.BarterLiApplication;
import li.barter.utils.Utils;
import li.barter.utils.AppConstants.DeviceInfo;

/**
 * Receiver for monitoring network changes
 * 
 * @author Vinay S Shenoy
 */
public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        if ((intent.getAction() != null)
                        && intent.getAction()
                                        .equals(ConnectivityManager.CONNECTIVITY_ACTION)) {

            Utils.setupNetworkInfo(context);
            if (DeviceInfo.INSTANCE.isNetworkConnected()) {
                BarterLiApplication.startChatService();
            }
        }
    }

}
