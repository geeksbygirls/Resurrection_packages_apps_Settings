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
import android.content.Context;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import java.util.ArrayList;
import java.util.List;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.util.CMDProcessor;
import com.android.settings.util.Helpers;
import com.android.settings.Utils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.android.internal.logging.MetricsProto.MetricsEvent;

public class BatteryBarSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {


    private static final String PREF_BATT_BAR = "battery_bar_list";
    private static final String PREF_BATT_BAR_STYLE = "battery_bar_style";
    private static final String PREF_BATT_BAR_WIDTH = "battery_bar_thickness";
    private static final String PREF_BATT_ANIMATE = "battery_bar_animate";
    private static final String PREF_AMBIENT = "show_batterybar_ambient";
    private static final String PREF_BATT_BAR_COLOR = "battery_bar_color";
    private static final String PREF_BATT_BAR_CHARGING_COLOR = "battery_bar_charging_color";
    private static final String STATUS_BAR_BATTERY_LOW_COLOR_WARNING = "battery_bar_battery_low_color_warning";
    private static final String STATUS_BAR_USE_GRADIENT_COLOR = "statusbar_battery_bar_use_gradient_color";
    private static final String STATUS_BAR_BAR_LOW_COLOR = "statusbar_battery_bar_low_color";
    private static final String STATUS_BAR_BAR_HIGH_COLOR = "statusbar_battery_bar_high_color";

    private ListPreference mBatteryBar;
    private ListPreference mBatteryBarStyle;
    private SeekBarPreference mBatteryBarThickness;
    private SwitchPreference mBatteryBarChargingAnimation;
    private SwitchPreference mBatteryBarUseGradient;
    private SwitchPreference mAmbient;
    private ColorPickerPreference mBatteryBarColor;
    private ColorPickerPreference mBatteryBarChargingColor;
    private ColorPickerPreference mBatteryBarBatteryLowColor;
    private ColorPickerPreference mBatteryBarBatteryLowColorWarn;
    private ColorPickerPreference mBatteryBarBatteryHighColor;

    static final int DEFAULT = 0xffffffff;
    static final int highColor = 0xff99CC00;
    static final int lowColor = 0xffff4444;

    private static final int MENU_RESET = Menu.FIRST;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.rr_battery_bar);

        final ContentResolver resolver = getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources res = getResources();
        int intColor;
        String hexColor;

        mBatteryBar = (ListPreference) findPreference(PREF_BATT_BAR);
        mBatteryBar.setOnPreferenceChangeListener(this);
        mBatteryBar.setValue((Settings.System.getInt(resolver,
                        Settings.System.STATUSBAR_BATTERY_BAR, 0)) + "");
        mBatteryBar.setSummary(mBatteryBar.getEntry());

        mBatteryBarStyle = (ListPreference) findPreference(PREF_BATT_BAR_STYLE);
        mBatteryBarStyle.setOnPreferenceChangeListener(this);
        mBatteryBarStyle.setValue((Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_BATTERY_BAR_STYLE, 0)) + "");
        mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntry());
        
        mBatteryBarThickness = (SeekBarPreference) findPreference(PREF_BATT_BAR_WIDTH);
        int thick = (Settings.System.getInt(resolver,
		Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS, 1));
        mBatteryBarThickness.setValue(thick/1);
        mBatteryBarThickness.setOnPreferenceChangeListener(this);

        mBatteryBarChargingAnimation = (SwitchPreference) findPreference(PREF_BATT_ANIMATE);
        mBatteryBarChargingAnimation.setChecked(Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE, 0) == 1);
        mBatteryBarChargingAnimation.setOnPreferenceChangeListener(this);
                
        mBatteryBarUseGradient = (SwitchPreference) findPreference(STATUS_BAR_USE_GRADIENT_COLOR);
        mBatteryBarUseGradient.setChecked(Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_BATTERY_BAR_USE_GRADIENT_COLOR, 0) == 1);
        mBatteryBarUseGradient.setOnPreferenceChangeListener(this);     

        mAmbient = (SwitchPreference) findPreference(PREF_AMBIENT);
        mAmbient.setChecked(Settings.System.getInt(resolver,
                Settings.System.SHOW_BATTERYBAR_AMBIENT, 0) == 1);
        mAmbient.setOnPreferenceChangeListener(this);

        mBatteryBarColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_COLOR);
        mBatteryBarColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_BATTERY_BAR_COLOR, DEFAULT);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mBatteryBarColor.setNewPreviewColor(intColor);
        mBatteryBarColor.setSummary(hexColor);
        
        mBatteryBarChargingColor = (ColorPickerPreference) findPreference(PREF_BATT_BAR_CHARGING_COLOR);
        mBatteryBarChargingColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_BATTERY_BAR_CHARGING_COLOR, DEFAULT);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mBatteryBarChargingColor.setNewPreviewColor(intColor);
        mBatteryBarChargingColor.setSummary(hexColor);
        
        mBatteryBarBatteryLowColorWarn =
                (ColorPickerPreference) findPreference(STATUS_BAR_BATTERY_LOW_COLOR_WARNING);
        mBatteryBarBatteryLowColorWarn.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_BATTERY_BAR_BATTERY_LOW_COLOR_WARNING, DEFAULT);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mBatteryBarBatteryLowColorWarn.setSummary(hexColor);
        
        mBatteryBarBatteryLowColor =
                (ColorPickerPreference) findPreference(STATUS_BAR_BAR_LOW_COLOR);
        mBatteryBarBatteryLowColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_BATTERY_BAR_LOW_COLOR, lowColor);
        hexColor = String.format("#%08x", (0xffff4444 & intColor));
        mBatteryBarBatteryLowColor.setNewPreviewColor(intColor);
        mBatteryBarBatteryLowColor.setSummary(hexColor);
        
        mBatteryBarBatteryHighColor = (ColorPickerPreference) findPreference(STATUS_BAR_BAR_HIGH_COLOR);
        mBatteryBarBatteryHighColor.setOnPreferenceChangeListener(this);
        intColor = Settings.System.getInt(resolver,
                Settings.System.STATUSBAR_BATTERY_BAR_HIGH_COLOR, highColor);
        hexColor = String.format("#%08x", (0xff99CC00 & intColor));
        mBatteryBarBatteryHighColor.setSummary(hexColor); 

        updateBatteryBarOptions();

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        int intValue;
        int index;
        int intHex;

        if (preference == mBatteryBar) {
            intValue = Integer.valueOf((String) newValue);
            index = mBatteryBar.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.STATUSBAR_BATTERY_BAR, intValue);
            mBatteryBar.setSummary(mBatteryBar.getEntries()[index]);
            updateBatteryBarOptions();
            return true;
        } else if (preference == mBatteryBarStyle) {
            intValue = Integer.valueOf((String) newValue);
            index = mBatteryBarStyle.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver,
                    Settings.System.STATUSBAR_BATTERY_BAR_STYLE, intValue);
            mBatteryBarStyle.setSummary(mBatteryBarStyle.getEntries()[index]);
            return true;
        } else if (preference == mBatteryBarThickness) {
            int val = (Integer) newValue;
            return Settings.System.putInt(resolver,
                    Settings.System.STATUSBAR_BATTERY_BAR_THICKNESS, val);
        } else if (preference == mBatteryBarColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(resolver,
                    Settings.System.STATUSBAR_BATTERY_BAR_COLOR, intHex);
           return true;
        }  else if (preference == mBatteryBarChargingColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_BAR_CHARGING_COLOR, intHex);
            return true;
        } else if (preference == mBatteryBarBatteryLowColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_BAR_LOW_COLOR, intHex);
            return true;
        } else if (preference == mBatteryBarBatteryLowColorWarn) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_BAR_BATTERY_LOW_COLOR_WARNING, intHex);
            return true;
        } else if (preference == mBatteryBarBatteryHighColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_BATTERY_BAR_HIGH_COLOR, intHex);
            return true;
        } else if (preference == mBatteryBarUseGradient) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.STATUSBAR_BATTERY_BAR_USE_GRADIENT_COLOR,
                    value ? 1 : 0);
            return true;
        } else if (preference == mBatteryBarChargingAnimation) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.STATUSBAR_BATTERY_BAR_ANIMATE,
                    value ? 1 : 0);
            return true;
        } else if (preference == mAmbient) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver,
                    Settings.System.SHOW_BATTERYBAR_AMBIENT,
                    value ? 1 : 0);
            Helpers.showSystemUIrestartDialog(getActivity());
            return true;
        }
        return false;
    }

    private void updateBatteryBarOptions() {
        if (Settings.System.getInt(getActivity().getContentResolver(),
                 Settings.System.STATUSBAR_BATTERY_BAR, 0) == 0) {
            mBatteryBarStyle.setEnabled(false);
            mBatteryBarThickness.setEnabled(false);
            mBatteryBarChargingAnimation.setEnabled(false);
            mBatteryBarUseGradient.setEnabled(false);
            mAmbient.setEnabled(false);
            mBatteryBarColor.setEnabled(false);
            mBatteryBarChargingColor.setEnabled(false);
            mBatteryBarBatteryLowColor.setEnabled(false);
            mBatteryBarBatteryLowColorWarn.setEnabled(false);
            mBatteryBarBatteryHighColor.setEnabled(false);

        } else {
            mBatteryBarStyle.setEnabled(true);
            mBatteryBarThickness.setEnabled(true);
            mBatteryBarChargingAnimation.setEnabled(true);
            mBatteryBarUseGradient.setEnabled(true);
            mAmbient.setEnabled(true);
            mBatteryBarColor.setEnabled(true);
            mBatteryBarChargingColor.setEnabled(true);
            mBatteryBarBatteryLowColor.setEnabled(true);
            mBatteryBarBatteryLowColorWarn.setEnabled(true);
            mBatteryBarBatteryHighColor.setEnabled(true);
        }
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
        alertDialog.setTitle(R.string.battery_colors_reset_title);
        alertDialog.setMessage(R.string.battery_colors_reset_message);
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
        Settings.System.putInt(getContentResolver(),
                Settings.System.STATUSBAR_BATTERY_BAR_COLOR, DEFAULT);
        mBatteryBarColor.setNewPreviewColor(DEFAULT);
        mBatteryBarColor.setSummary(R.string.default_string);
        Settings.System.putInt(getContentResolver(),
                Settings.System.STATUSBAR_BATTERY_BAR_CHARGING_COLOR, DEFAULT);
        mBatteryBarChargingColor.setNewPreviewColor(DEFAULT);
        mBatteryBarChargingColor.setSummary(R.string.default_string);
        Settings.System.putInt(getContentResolver(),
                Settings.System.STATUSBAR_BATTERY_BAR_LOW_COLOR, lowColor);
        mBatteryBarBatteryLowColor.setNewPreviewColor(lowColor);
        mBatteryBarBatteryLowColor.setSummary(R.string.default_string);
        Settings.System.putInt(getContentResolver(),
                Settings.System.STATUSBAR_BATTERY_BAR_BATTERY_LOW_COLOR_WARNING, DEFAULT);    
        mBatteryBarBatteryLowColorWarn.setNewPreviewColor(DEFAULT);
        mBatteryBarBatteryLowColorWarn.setSummary(R.string.default_string);
        Settings.System.putInt(getContentResolver(),
                Settings.System.STATUSBAR_BATTERY_BAR_HIGH_COLOR, highColor);   
        mBatteryBarBatteryHighColor.setNewPreviewColor(highColor);
        mBatteryBarBatteryHighColor.setSummary(R.string.default_string);   
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.APPLICATION;
    }
}
