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

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.telephony.SubscriptionManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import java.util.ArrayList;
import java.util.List;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import com.android.internal.logging.MetricsProto.MetricsEvent;

public class StatusBarIcons extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "StatusBarIcons";
    private static final String FOUR_G = "show_fourg";
    private static final String THREE_G = "show_threeg";
    private static final String ROAMING_PREF = "roaming_indicator_icon";
    private static final String DATA_DISABLED_PREF = "data_disabled_icon";
    private static final String SIM_EMPTY_SWITCH = "no_sim_cluster_switch";
    private static final String PREF_TEXT_COLOR = "status_bar_notif_count_text_color";
    private static final String PREF_ICON_COLOR = "status_bar_notif_count_icon_color";

    private static final int WHITE = 0xffffffff;
    private static final int BLACK = 0xff000000;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET = 0;

    private ColorPickerPreference mTextColor;
    private ColorPickerPreference mIconColor;
    private SubscriptionManager mSm;

    private SwitchPreference mNoSims;
    private SwitchPreference m4g;
    private SwitchPreference m3g;
    private SwitchPreference mRoaming;
    private SwitchPreference mNoData;

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.RESURRECTED;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.rr_sb_icons);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mTextColor = (ColorPickerPreference) prefSet.findPreference(PREF_TEXT_COLOR);
        mTextColor.setOnPreferenceChangeListener(this);
        int textColor = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_NOTIF_COUNT_TEXT_COLOR, BLACK);
        String textHexColor = String.format("#%08x", (0xffb0b0b0 & textColor));
        mTextColor.setSummary(textHexColor);
        mTextColor.setNewPreviewColor(textColor);

        mIconColor = (ColorPickerPreference) prefSet.findPreference(PREF_ICON_COLOR);
        mIconColor.setOnPreferenceChangeListener(this);
        int iconColor = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_NOTIF_COUNT_ICON_COLOR, WHITE);
        String iconHexColor = String.format("#%08x", (0xffb0b0b0 & iconColor));
        mIconColor.setSummary(iconHexColor);
        mIconColor.setNewPreviewColor(iconColor);

        mNoSims = (SwitchPreference) findPreference(SIM_EMPTY_SWITCH);
        m4g = (SwitchPreference) findPreference(FOUR_G);
        m3g = (SwitchPreference) findPreference(THREE_G);
        mRoaming = (SwitchPreference) findPreference(ROAMING_PREF);
        mNoData = (SwitchPreference) findPreference(DATA_DISABLED_PREF);
        mSm = (SubscriptionManager) getSystemService(getContext().TELEPHONY_SUBSCRIPTION_SERVICE);

        if (mNoSims != null) { 
            if (!TelephonyManager.getDefault().isMultiSimEnabled() || mSm.getActiveSubscriptionInfoCount() <= 0){
                getPreferenceScreen().removePreference(mNoSims);
            }
        }

        if (mSm.getActiveSubscriptionInfoCount() <= 0) {
                getPreferenceScreen().removePreference(m4g);
                getPreferenceScreen().removePreference(m3g);
                getPreferenceScreen().removePreference(mRoaming);
                getPreferenceScreen().removePreference(mNoData);
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mTextColor) {
            String hex = ColorPickerPreference.convertToARGB(
                   Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_NOTIF_COUNT_TEXT_COLOR, intHex);
            return true;
        } else if (preference == mIconColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_NOTIF_COUNT_ICON_COLOR, intHex);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_action_reset_alpha)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefault();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(R.string.count_colors_reset_title);
        alertDialog.setMessage(R.string.lockscreen_colors_reset_message);
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetValues();
            }
        });
        alertDialog.setNegativeButton(R.string.cancel, null);
        alertDialog.create().show();
    }

    private void resetValues() {
	ContentResolver resolver = getActivity().getContentResolver();
	Settings.System.putInt(resolver,
                 Settings.System.STATUS_BAR_NOTIF_COUNT_TEXT_COLOR, BLACK);
        mTextColor.setNewPreviewColor(BLACK);
        mTextColor.setSummary(R.string.default_string); 
        Settings.System.putInt(resolver,
                 Settings.System.STATUS_BAR_NOTIF_COUNT_ICON_COLOR, WHITE);
        mIconColor.setNewPreviewColor(WHITE);
        mIconColor.setSummary(R.string.default_string);   
    }
}
