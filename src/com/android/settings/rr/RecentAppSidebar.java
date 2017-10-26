/*
 * Copyright (C) 2015-2017 Android Ice Cold Project
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsProto.MetricsEvent;

public class RecentAppSidebar extends SettingsPreferenceFragment
            implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "RecentAppSidebarSettings";

    // Preferences
    private static final String APP_SIDEBAR_HIDE_LABELS = "recent_app_sidebar_disable_labels";
    private static final String APP_SIDEBAR_LABEL_COLOR = "recent_app_sidebar_label_color";
    private static final String APP_SIDEBAR_BG_COLOR = "recent_app_sidebar_bg_color";
    private static final String APP_SIDEBAR_SCALE = "recent_app_sidebar_scale";

    private SeekBarPreference mAppSidebarScale;
    private ColorPickerPreference mAppSidebarLabelColor;
    private ColorPickerPreference mAppSidebarBgColor;

    private static final int DEFAULT_TEXT = 0x00ffffff;
    private static final int DEFAULT_BG = 0x763367d6;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DIALOG_RESET_CONFIRM = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.recent_app_sidebar_settings);
        initializeAllPreferences();

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    }

    @Override
    public Dialog onCreateDialog(int dialogId) {
        Dialog dialog = null;
        switch (dialogId) {
            case DIALOG_RESET_CONFIRM:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
                alertDialog.setTitle(R.string.recent_reset_title);
                alertDialog.setMessage(R.string.recent_reset_confirm);
                alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        resetSettings();
                    }
                });
                alertDialog.setNegativeButton(R.string.write_settings_off, null);
                dialog = alertDialog.create();
                break;
         }
        return dialog;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(com.android.internal.R.drawable.ic_menu_refresh)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                showDialog(DIALOG_RESET_CONFIRM);
                return true;
             default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetSettings() {
        Settings.System.putInt(getContext().getContentResolver(),
                Settings.System.RECENT_APP_SIDEBAR_TEXT_COLOR,
                DEFAULT_TEXT);
        mAppSidebarLabelColor.setSummary(R.string.default_string);
        mAppSidebarLabelColor.setNewPreviewColor(DEFAULT_TEXT);
        Settings.System.putInt(getContext().getContentResolver(),
                Settings.System.RECENT_APP_SIDEBAR_BG_COLOR,
                DEFAULT_TEXT);
        mAppSidebarBgColor.setSummary(R.string.default_string);
        mAppSidebarBgColor.setNewPreviewColor(DEFAULT_TEXT);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mAppSidebarScale) {
            Settings.System.putInt(getContext().getContentResolver(),
                Settings.System.RECENT_APP_SIDEBAR_SCALE_FACTOR, Integer.valueOf(String.valueOf(newValue)));
            return true;
        } else if (preference == mAppSidebarLabelColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#00ffffff")) {
                preference.setSummary(R.string.default_string);
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContext().getContentResolver(),
                    Settings.System.RECENT_APP_SIDEBAR_TEXT_COLOR,
                    intHex);
            return true;
        } else if (preference == mAppSidebarBgColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            if (hex.equals("#763367d6")) {
                preference.setSummary(R.string.default_string);
            } else {
                preference.setSummary(hex);
            }
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getContext().getContentResolver(),
                    Settings.System.RECENT_APP_SIDEBAR_BG_COLOR,
                    intHex);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void initializeAllPreferences() {
        mAppSidebarScale = (SeekBarPreference) findPreference(APP_SIDEBAR_SCALE);
        mAppSidebarScale.setOnPreferenceChangeListener(this);
        mAppSidebarScale.setValue(Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.RECENT_APP_SIDEBAR_SCALE_FACTOR, 100));


        mAppSidebarLabelColor = (ColorPickerPreference) findPreference(APP_SIDEBAR_LABEL_COLOR);
        mAppSidebarLabelColor.setOnPreferenceChangeListener(this);
        final int intColorSidebarLabel = Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.RECENT_APP_SIDEBAR_TEXT_COLOR, 0x00ffffff);
        String hexColorSidebarLabel = String.format("#%08x", (0x00ffffff & intColorSidebarLabel));
        if (hexColorSidebarLabel.equals("#00ffffff")) {
            mAppSidebarLabelColor.setSummary(R.string.default_string);
        } else {
            mAppSidebarLabelColor.setSummary(hexColorSidebarLabel);
        }
        mAppSidebarLabelColor.setNewPreviewColor(intColorSidebarLabel);

        mAppSidebarBgColor =
                (ColorPickerPreference) findPreference(APP_SIDEBAR_BG_COLOR);
        mAppSidebarBgColor.setOnPreferenceChangeListener(this);
        final int intColorSidebarBg = Settings.System.getInt(getContext().getContentResolver(),
                Settings.System.RECENT_APP_SIDEBAR_BG_COLOR, 0x763367d6);
        String hexColorSidebarBg = String.format("#%08x", (0x00ffffff & intColorSidebarBg));
        if (hexColorSidebarBg.equals("#763367d6")) {
            mAppSidebarBgColor.setSummary(R.string.default_string);
        } else {
            mAppSidebarBgColor.setSummary(hexColorSidebarBg);
        }
        mAppSidebarBgColor.setNewPreviewColor(intColorSidebarBg);
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.RESURRECTED;
    }
}
