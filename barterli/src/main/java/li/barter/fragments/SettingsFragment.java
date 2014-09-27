/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package li.barter.fragments;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.RingtonePreference;
import android.text.TextUtils;

import li.barter.R;
import li.barter.utils.SharedPreferenceHelper;

/**
 * Fragment for displaying App Settings Created by vinay.shenoy on 05/07/14.
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    /** Reference to ringtone preference to set selected notification ringtone when changed */
    private RingtonePreference mChatRingtonePreference;

    /** Chat ringtone preference key */
    private String mChatRingtoneKey;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        mChatRingtoneKey = getString(R.string.pref_chat_ringtone);
        mChatRingtonePreference = (RingtonePreference) findPreference(mChatRingtoneKey);
        mChatRingtonePreference.setOnPreferenceChangeListener(this);
        updateRingtonePreferenceSummary(SharedPreferenceHelper
                                                .getString(R.string.pref_chat_ringtone));
    }

    /**
     * Sets the summary of the Ringtone Preference to the human readable name of the selected
     * ringtone
     *
     * @param selectedRingtoneUriString The String version of the selected ringtone Uri
     */
    private void updateRingtonePreferenceSummary(final String selectedRingtoneUriString) {

        if (!TextUtils.isEmpty(selectedRingtoneUriString)) {
            final Ringtone selectedRingtone = RingtoneManager
                    .getRingtone(getActivity(), Uri.parse(selectedRingtoneUriString));

            if (selectedRingtone == null) {
                mChatRingtonePreference.setSummary(null);
            } else {
                mChatRingtonePreference.setSummary(selectedRingtone.getTitle(getActivity()));
            }
        } else {
            mChatRingtonePreference.setSummary(null);
        }

    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object o) {

        if (preference.getKey().equals(mChatRingtoneKey)) {
            updateRingtonePreferenceSummary((String) o);
        }

        return true;
    }
}
