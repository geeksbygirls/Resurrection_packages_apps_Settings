
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

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.utils.CMDProcessor;
import com.android.settings.utils.Helpers;
import com.android.settings.Utils;

import cyanogenmod.providers.CMSettings;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.android.internal.logging.MetricsProto.MetricsEvent;

public class BatterySettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {


    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_show_battery_percent";
    private static final String STATUS_BAR_CHARGE_COLOR = "status_bar_charge_color";
    private static final String TEXT_CHARGING_SYMBOL = "text_charging_symbol";
    private static final String LARGE_TEXT = "battery_large_text";
    private static final String COLOR_BATTERY = "colorful_battery";

    private static final int STATUS_BAR_BATTERY_STYLE_HIDDEN = 6;
    private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 8;

    private ListPreference mStatusBarBattery;
    private ListPreference mStatusBarBatteryShowPercent;
    private SwitchPreference mBatteryLargeText;
    private SwitchPreference mColorfulBattery;
    private ColorPickerPreference mChargeColor;
    private ListPreference mTextChargingSymbol;
    private int mTextChargingSymbolValue;

    static final int DEFAULT = 0xffffffff;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.rr_battery);

        final ContentResolver resolver = getContentResolver();
        final PreferenceScreen prefScreen = getPreferenceScreen();
        final Resources res = getResources();
        int intColor;
        String hexColor;

        mTextChargingSymbol = (ListPreference) findPreference(TEXT_CHARGING_SYMBOL);
        mTextChargingSymbolValue = Settings.Secure.getInt(resolver,
                Settings.Secure.TEXT_CHARGING_SYMBOL, 0);
        mTextChargingSymbol.setValue(Integer.toString(mTextChargingSymbolValue));
        mTextChargingSymbol.setSummary(mTextChargingSymbol.getEntry());
        mTextChargingSymbol.setOnPreferenceChangeListener(this);

        mStatusBarBattery = (ListPreference) findPreference(STATUS_BAR_BATTERY_STYLE);
        mStatusBarBatteryShowPercent =
                (ListPreference) findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);
        int batteryStyle = CMSettings.System.getInt(resolver,
                CMSettings.System.STATUS_BAR_BATTERY_STYLE, 0);
        mStatusBarBattery.setValue(String.valueOf(batteryStyle));
        mStatusBarBattery.setSummary(mStatusBarBattery.getEntry());
        mStatusBarBattery.setOnPreferenceChangeListener(this);

        mBatteryLargeText = (SwitchPreference) findPreference(LARGE_TEXT);
        mBatteryLargeText.setChecked(Settings.System.getInt(resolver,
                Settings.System.BATTERY_LARGE_TEXT, 0) == 1);

        mColorfulBattery = (SwitchPreference) findPreference(COLOR_BATTERY);
        mColorfulBattery.setChecked(Settings.System.getInt(resolver,
                Settings.System.COLORFUL_BATTERY, 0) == 1);

        int batteryShowPercent = CMSettings.System.getInt(resolver,
                CMSettings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, 0);
        mStatusBarBatteryShowPercent.setValue(String.valueOf(batteryShowPercent));
        mStatusBarBatteryShowPercent.setSummary(mStatusBarBatteryShowPercent.getEntry());
        enableStatusBarBatteryDependents(batteryStyle);
        mStatusBarBatteryShowPercent.setOnPreferenceChangeListener(this);

        mChargeColor = (ColorPickerPreference) findPreference(STATUS_BAR_CHARGE_COLOR);
        mChargeColor.setOnPreferenceChangeListener(this); 
        intColor = Settings.Secure.getInt(resolver,
                Settings.Secure.STATUS_BAR_CHARGE_COLOR, DEFAULT);
        hexColor = String.format("#%08x", (0xffffffff & intColor));
        mChargeColor.setNewPreviewColor(intColor);
        mChargeColor.setSummary(hexColor);

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();

	if (preference == mStatusBarBattery) {
            int batteryStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarBattery.findIndexOfValue((String) newValue);
            CMSettings.System.putInt(
                    resolver, CMSettings.System.STATUS_BAR_BATTERY_STYLE, batteryStyle);
            mStatusBarBattery.setSummary(mStatusBarBattery.getEntries()[index]);
            enableStatusBarBatteryDependents(batteryStyle);
            return true;
        } else if (preference == mStatusBarBatteryShowPercent) {
            int batteryShowPercent = Integer.valueOf((String) newValue);
            int index = mStatusBarBatteryShowPercent.findIndexOfValue((String) newValue);
            CMSettings.System.putInt(
                    resolver, CMSettings.System.STATUS_BAR_SHOW_BATTERY_PERCENT, batteryShowPercent);
            mStatusBarBatteryShowPercent.setSummary(
                    mStatusBarBatteryShowPercent.getEntries()[index]);
            return true;
        } else if (preference == mChargeColor) {
            String hex = ColorPickerPreference.convertToARGB(Integer
                    .valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.Secure.putInt(resolver,
                    Settings.Secure.STATUS_BAR_CHARGE_COLOR, intHex);
            return true;
        }  else if (preference == mTextChargingSymbol) {
            mTextChargingSymbolValue = Integer.valueOf((String) newValue);
            int index = mTextChargingSymbol.findIndexOfValue((String) newValue);
            mTextChargingSymbol.setSummary(
                    mTextChargingSymbol.getEntries()[index]);
            Settings.Secure.putInt(resolver,
                    Settings.Secure.TEXT_CHARGING_SYMBOL, mTextChargingSymbolValue);
            return true;
         }
	return false;
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        ContentResolver resolver = getActivity().getContentResolver();
        boolean value;

        if (preference == mBatteryLargeText) {
            Settings.System.putInt(resolver,
                    Settings.System.BATTERY_LARGE_TEXT,
                    ((SwitchPreference) preference).isChecked() ? 1 : 0);
            Helpers.showSystemUIrestartDialog(getActivity());
            return true;
        } else if (preference == mColorfulBattery) {
            Settings.System.putInt(resolver,
                    Settings.System.COLORFUL_BATTERY,
                    ((SwitchPreference) preference).isChecked() ? 1 : 0);
            Helpers.showSystemUIrestartDialog(getActivity());
            return true;
        }
        return false;
    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.APPLICATION;
    }

    private void enableStatusBarBatteryDependents(int batteryIconStyle) {
        if (batteryIconStyle == STATUS_BAR_BATTERY_STYLE_HIDDEN ||
                batteryIconStyle == STATUS_BAR_BATTERY_STYLE_TEXT) {
            mStatusBarBatteryShowPercent.setEnabled(false);
            mBatteryLargeText.setEnabled(false);
            mColorfulBattery.setEnabled(false);
        } else {
            mStatusBarBatteryShowPercent.setEnabled(true);
            mBatteryLargeText.setEnabled(true);
            mColorfulBattery.setEnabled(true);
        }
    }

}
