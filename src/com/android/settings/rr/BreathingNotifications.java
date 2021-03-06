/*Copyright (C) 2015 The ResurrectionRemix Project
     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
*/
package com.android.settings.rr;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.provider.Settings;

import com.android.internal.logging.MetricsProto.MetricsEvent;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class BreathingNotifications extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String MISSED_CALL_BREATH = "missed_call_breath";
    private static final String VOICEMAIL_BREATH = "voicemail_breath";
    private static final String SMS_BREATH = "sms_breath";

    private SwitchPreference mMissedCallBreath;
    private SwitchPreference mVoicemailBreath;
    private SwitchPreference mSmsBreath;

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.RESURRECTED;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);

     addPreferencesFromResource(R.xml.rr_breathing_notif);

     final PreferenceScreen prefSet = getPreferenceScreen();
     final ContentResolver resolver = getActivity().getContentResolver();

     mMissedCallBreath = (SwitchPreference) findPreference(MISSED_CALL_BREATH);
     mVoicemailBreath = (SwitchPreference) findPreference(VOICEMAIL_BREATH);
     mSmsBreath = (SwitchPreference) findPreference(SMS_BREATH);

     Context context = getActivity();
     ConnectivityManager cm = (ConnectivityManager)
     context.getSystemService(Context.CONNECTIVITY_SERVICE);

     if(cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE)) {
        mMissedCallBreath.setChecked(Settings.System.getInt(resolver,
               Settings.System.KEY_MISSED_CALL_BREATH, 0) == 1);
        mMissedCallBreath.setOnPreferenceChangeListener(this);
        mVoicemailBreath.setChecked(Settings.System.getInt(resolver,
               Settings.System.KEY_VOICEMAIL_BREATH, 0) == 1);
        mVoicemailBreath.setOnPreferenceChangeListener(this);
        mSmsBreath.setChecked(Settings.Global.getInt(resolver,
               Settings.Global.KEY_SMS_BREATH, 0) == 1);
        mSmsBreath.setOnPreferenceChangeListener(this);
     } else {
        prefSet.removePreference(mMissedCallBreath);
        prefSet.removePreference(mVoicemailBreath);
        prefSet.removePreference(mSmsBreath);
    }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
     if (preference == mMissedCallBreath) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(), MISSED_CALL_BREATH,
                    value ? 1 : 0);
            return true;
        } else if (preference == mVoicemailBreath) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(), VOICEMAIL_BREATH,
                    value ? 1 : 0);
            return true;
        } else if (preference == mSmsBreath) {
            boolean value = (Boolean) newValue;
            Settings.Global.putInt(getContentResolver(), SMS_BREATH,
                    value ? 1 : 0);
            return true;
        }
        return false;
    } 
}
