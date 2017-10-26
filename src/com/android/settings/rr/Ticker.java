/*
 * Copyright (C) 2014 The Dirty Unicorns Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import java.util.ArrayList;
import java.util.List;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import net.margaritov.preference.colorpicker.ColorPickerPreference;
import com.android.internal.logging.MetricsProto.MetricsEvent;

public class Ticker extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String TAG = "Ticker";

    private static final String PREF_SHOW_TICKER = "status_bar_show_ticker";
    private static final String PREF_TEXT_COLOR = "status_bar_ticker_text_color";
    private static final String PREF_ICON_COLOR = "status_bar_ticker_icon_color";

    private static final int DEFAULT_COLOR = 0xffb0b0b0;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET = 0;

    private SwitchPreference mShowTicker;
    private ColorPickerPreference mTextColor;
    private ColorPickerPreference mIconColor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.ticker);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mShowTicker = (SwitchPreference) prefSet.findPreference(PREF_SHOW_TICKER);
        mShowTicker.setChecked(Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_SHOW_TICKER, 0) != 0);
        mShowTicker.setOnPreferenceChangeListener(this);

        mTextColor = (ColorPickerPreference) prefSet.findPreference(PREF_TEXT_COLOR);
        mTextColor.setOnPreferenceChangeListener(this);
        int textColor = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_TICKER_TEXT_COLOR, DEFAULT_COLOR);
        String textHexColor = String.format("#%08x", (0xffb0b0b0 & textColor));
        mTextColor.setSummary(textHexColor);
        mTextColor.setNewPreviewColor(textColor);

        mIconColor = (ColorPickerPreference) prefSet.findPreference(PREF_ICON_COLOR);
        mIconColor.setOnPreferenceChangeListener(this);
        int iconColor = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_TICKER_ICON_COLOR, DEFAULT_COLOR);
        String iconHexColor = String.format("#%08x", (0xffb0b0b0 & iconColor));
        mIconColor.setSummary(iconHexColor);
        mIconColor.setNewPreviewColor(iconColor);

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mShowTicker) {
            int enabled = ((Boolean) newValue) ? 1 : 0;
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_SHOW_TICKER, enabled);
            return true;
        } else if (preference == mTextColor) {
            String hex = ColorPickerPreference.convertToARGB(
                   Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_TICKER_TEXT_COLOR, intHex);
            return true;
        } else if (preference == mIconColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.STATUS_BAR_TICKER_ICON_COLOR, intHex);
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
        alertDialog.setTitle(R.string.ticker_colors_reset_title);
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
                 Settings.System.STATUS_BAR_TICKER_TEXT_COLOR, DEFAULT_COLOR);
        mTextColor.setNewPreviewColor(DEFAULT_COLOR);
        mTextColor.setSummary(R.string.default_string); 
        Settings.System.putInt(resolver,
                 Settings.System.STATUS_BAR_TICKER_ICON_COLOR, DEFAULT_COLOR);
        mIconColor.setNewPreviewColor(DEFAULT_COLOR);
        mIconColor.setSummary(R.string.default_string);   
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.RESURRECTED;
    }
}
