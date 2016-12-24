/* 
 * Copyright (C) 2014 DarkKat
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
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.internal.logging.MetricsProto.MetricsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.android.settings.utils.CMDProcessor;
import com.android.settings.utils.Helpers;
import com.android.settings.Utils;

import com.android.internal.util.rr.PackageUtils;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class LockScreenWeatherSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String PREF_CAT_NOTIFICATIONS =
            "weather_cat_notifications";
    private static final String PREF_SHOW_WEATHER =
            "lock_screen_show_weather";
    private static final String PREF_SHOW_LOCATION =
            "lock_screen_show_weather_location";
    private static final String PREF_SHOW_CON =
            "lock_screen_show_weather_condition";
    private static final String PREF_HIDE_WEATHER =
            "weather_hide_panel";
    private static final String PREF_NUMBER_OF_NOTIFICATIONS =
            "weather_number_of_notifications";
    private static final String PREF_CON_COLOR = 
	        "lock_screen_weather_con_color";
    private static final String PREF_ICON_COLOR = 
	        "weather_icon_color";
    private static final String PREF_TEMP_COLOR = 
	        "lock_screen_weather_temp_color";
    private static final String PREF_CITY_COLOR = 
	        "lock_screen_weather_city_color";
    private static final String PREF_SHOW_AMBIENT = 
	        "ambient_display_show_weather";
    private static final String CATEGORY_WEATHER = "weather_category";
    private static final String WEATHER_ICON_PACK = "weather_icon_pack";
    private static final String DEFAULT_WEATHER_ICON_PACKAGE = "org.omnirom.omnijaws";
    private static final String WEATHER_SERVICE_PACKAGE = "org.omnirom.omnijaws";
    private static final String CHRONUS_ICON_PACK_INTENT = "com.dvtonder.chronus.ICON_PACK";

    private static final int DEFAULT_COLOR = 0xFFFFFFFF;
    private static final int TRANSPARENT = 0x99FFFFFF;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET = 0;

    private SwitchPreference mShowWeather;
    private SwitchPreference mShowAmbient;
    private SwitchPreference mShowLocation;
    private SwitchPreference mShowCon;
    private ColorPickerPreference mConColor;
    private ColorPickerPreference mIconColor;
    private ColorPickerPreference mTempColor;
    private ColorPickerPreference mCityColor;
    private ListPreference mHideWeather;
    private ListPreference mNumberOfNotifications;
    private PreferenceCategory mWeatherCategory;
    private ListPreference mWeatherIconPack;

    private ContentResolver mResolver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshSettings();
    }

    public void refreshSettings() {
        PreferenceScreen prefs = getPreferenceScreen();
        if (prefs != null) {
            prefs.removeAll();
        }

        addPreferencesFromResource(R.xml.lock_screen_weather_settings);
        mResolver = getActivity().getContentResolver();

        boolean showWeather = Settings.System.getInt(mResolver,
                Settings.System.LOCK_SCREEN_SHOW_WEATHER, 0) == 1;

        int intColor;
        String hexColor;

        mShowWeather =
                (SwitchPreference) findPreference(PREF_SHOW_WEATHER);
        mShowWeather.setChecked(showWeather);
        mShowWeather.setOnPreferenceChangeListener(this);

        PreferenceCategory catNotifications =
                (PreferenceCategory) findPreference(PREF_CAT_NOTIFICATIONS);

        mHideWeather =
                (ListPreference) findPreference(PREF_HIDE_WEATHER);
        mNumberOfNotifications =
                (ListPreference) findPreference(PREF_NUMBER_OF_NOTIFICATIONS);

        mConColor = (ColorPickerPreference) findPreference(PREF_CON_COLOR);
        mIconColor = (ColorPickerPreference) findPreference(PREF_ICON_COLOR);
        mTempColor = (ColorPickerPreference) findPreference(PREF_TEMP_COLOR);
        mCityColor = (ColorPickerPreference) findPreference(PREF_CITY_COLOR);
        initweathercat();

        if (showWeather) {
            mShowLocation =
                    (SwitchPreference) findPreference(PREF_SHOW_LOCATION);
            mShowLocation.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.LOCK_SCREEN_SHOW_WEATHER_LOCATION, 1) == 1);
            mShowLocation.setOnPreferenceChangeListener(this);

            mShowCon =
                    (SwitchPreference) findPreference(PREF_SHOW_CON);
            mShowCon.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.LOCK_SCREEN_SHOW_WEATHER_CONDITION, 1) == 1);
            mShowCon.setOnPreferenceChangeListener(this);

            mShowAmbient =
                    (SwitchPreference) findPreference(PREF_SHOW_AMBIENT);
            mShowAmbient.setChecked(Settings.System.getInt(mResolver,
                    Settings.System.AMBIENT_DISPLAY_SHOW_WEATHER, 0) == 1);
            mShowAmbient.setOnPreferenceChangeListener(this);

            intColor = Settings.System.getInt(mResolver,
                Settings.System.LOCK_SCREEN_WEATHER_CON_COLOR, -2);
            if (intColor == -2) {
                intColor = 0xFFFFFFFF;
                mConColor.setSummary(getResources().getString(R.string.default_string));
            } else {
                hexColor = String.format("#%08x", (0xFFFFFFFF & intColor));
                mConColor.setSummary(hexColor);
            }
            mConColor.setNewPreviewColor(intColor);
            mConColor.setOnPreferenceChangeListener(this);

            intColor = Settings.System.getInt(mResolver,
                Settings.System.LOCK_SCREEN_WEATHER_ICON_COLOR, -2);
            if (intColor == -2) {
                intColor = 0xFFFFFFFF;
                mIconColor.setSummary(getResources().getString(R.string.default_string));
            } else {
                hexColor = String.format("#%08x", (0xFFFFFFFF & intColor));
                mIconColor.setSummary(hexColor);
            }
            mIconColor.setNewPreviewColor(intColor);
            mIconColor.setOnPreferenceChangeListener(this);

            intColor = Settings.System.getInt(mResolver,
                Settings.System.LOCK_SCREEN_WEATHER_TEMP_COLOR, -2);
            if (intColor == -2) {
                intColor = 0xFFFFFFFF;
                mTempColor.setSummary(getResources().getString(R.string.default_string));
            } else {
                hexColor = String.format("#%08x", (0xFFFFFFFF & intColor));
                mTempColor.setSummary(hexColor);
            }
            mTempColor.setNewPreviewColor(intColor);
            mTempColor.setOnPreferenceChangeListener(this);

            intColor = Settings.System.getInt(mResolver,
                Settings.System.LOCK_SCREEN_WEATHER_CITY_COLOR, -2);
            if (intColor == -2) {
                intColor = 0xFFFFFFFF;
                mCityColor.setSummary(getResources().getString(R.string.default_string));
            } else {
                hexColor = String.format("#%08x", (0xFFFFFFFF & intColor));
                mCityColor.setSummary(hexColor);
            }
            mCityColor.setNewPreviewColor(intColor);
            mCityColor.setOnPreferenceChangeListener(this);

            int  hideWeather = Settings.System.getInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_HIDE_PANEL, 0);
            mHideWeather.setValue(String.valueOf(hideWeather));
            mHideWeather.setOnPreferenceChangeListener(this);

            if (hideWeather == 0) {
                mHideWeather.setSummary(R.string.weather_hide_panel_auto_summary);
                mNumberOfNotifications.setEnabled(false);
                catNotifications.removePreference(mNumberOfNotifications);
            } else if (hideWeather == 1) {
                int numberOfNotifications = Settings.System.getInt(mResolver,
                       Settings.System.LOCK_SCREEN_WEATHER_NUMBER_OF_NOTIFICATIONS, 3);
                mNumberOfNotifications.setValue(String.valueOf(numberOfNotifications));
                mNumberOfNotifications.setSummary(mNumberOfNotifications.getEntry());
                mNumberOfNotifications.setOnPreferenceChangeListener(this);

                mHideWeather.setSummary(getString(R.string.weather_hide_panel_custom_summary,
                        mNumberOfNotifications.getEntry()));
                mNumberOfNotifications.setEnabled(true);
            } else {
                mHideWeather.setSummary(R.string.weather_hide_panel_never_summary);
                mNumberOfNotifications.setEnabled(false);
                catNotifications.removePreference(mNumberOfNotifications);
            }

        } else {
            removePreference(PREF_SHOW_LOCATION);
            removePreference(PREF_SHOW_CON);
            removePreference(PREF_SHOW_AMBIENT);
            catNotifications.removePreference(mHideWeather);
            catNotifications.removePreference(mNumberOfNotifications);
            removePreference(PREF_CAT_NOTIFICATIONS);
            removePreference(PREF_CON_COLOR);
            removePreference(PREF_ICON_COLOR);
            removePreference(PREF_CITY_COLOR);
            removePreference(PREF_TEMP_COLOR);
        }

        setHasOptionsMenu(true);
    }


    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int intValue;
        int index;
        int intHex;

        if (preference == mShowWeather) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.LOCK_SCREEN_SHOW_WEATHER,
                    value ? 1 : 0);
            refreshSettings();
            return true;
        } else if (preference == mShowLocation) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.LOCK_SCREEN_SHOW_WEATHER_LOCATION,
                    value ? 1 : 0);
            return true;
        } else if (preference == mShowCon) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.LOCK_SCREEN_SHOW_WEATHER_CONDITION,
                    value ? 1 : 0);
            return true;
        } else if (preference == mShowAmbient) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(mResolver,
                    Settings.System.AMBIENT_DISPLAY_SHOW_WEATHER,
                    value ? 1 : 0);
            return true;
        } else if (preference == mHideWeather) {
            intValue = Integer.valueOf((String) newValue);
            Settings.System.putInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_HIDE_PANEL, intValue);
            refreshSettings();
            return true;
        } else if (preference == mNumberOfNotifications) {
            intValue = Integer.valueOf((String) newValue);
            Settings.System.putInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_NUMBER_OF_NOTIFICATIONS, intValue);
            refreshSettings();
            return true;
        } else if (preference == mConColor) {
            String hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_CON_COLOR, intHex);
            preference.setSummary(hex);
            return true;
        } else if (preference == mIconColor) {
            String hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(mResolver,
                    Settings.System.LOCK_SCREEN_WEATHER_ICON_COLOR, intHex);
            preference.setSummary(hex);
            Helpers.showSystemUIrestartDialog(getActivity());
            return true;
	} else if (preference == mTempColor) {
            String hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCK_SCREEN_WEATHER_TEMP_COLOR, intHex);
            return true;
	} else if (preference == mCityColor) {
            String hex = ColorPickerPreference.convertToARGB(
                Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.LOCK_SCREEN_WEATHER_CITY_COLOR, intHex);
            return true;
        } else if (preference == mWeatherIconPack) {
                String value = (String) newValue;
                Settings.System.putString(getContentResolver(),
                    Settings.System.OMNIJAWS_WEATHER_ICON_PACK, value);
                int valueIndex = mWeatherIconPack.findIndexOfValue(value);
                mWeatherIconPack.setSummary(mWeatherIconPack.getEntries()[valueIndex]);
            refreshSettings();
            return true;
        }
        return false;
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
        alertDialog.setTitle(R.string.lockscreen_weather_colors_reset_title);
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
                 Settings.System.LOCK_SCREEN_WEATHER_ICON_COLOR, -2);
          mIconColor.setNewPreviewColor(-2);
          mIconColor.setSummary(R.string.default_string); 
          Settings.System.putInt(resolver,
                   Settings.System.LOCK_SCREEN_WEATHER_TEMP_COLOR, -2);
          mTempColor.setNewPreviewColor(-2);
          mTempColor.setSummary(R.string.default_string);   
          Settings.System.putInt(resolver,
                   Settings.System.LOCK_SCREEN_WEATHER_CON_COLOR, -2);
          mConColor.setNewPreviewColor(-2);
          mConColor.setSummary(R.string.default_string);  
          Settings.System.putInt(resolver,
                   Settings.System.LOCK_SCREEN_WEATHER_CITY_COLOR, -2);
          mCityColor.setNewPreviewColor(-2);
          mCityColor.setSummary(R.string.default_string);   

    }

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.RESURRECTED;
    }

   public void initweathercat() {
        mWeatherCategory = (PreferenceCategory) getPreferenceScreen().findPreference(CATEGORY_WEATHER);
             if (mWeatherCategory != null && !isOmniJawsServiceInstalled()) {
             getPreferenceScreen().removePreference(mWeatherCategory);
             } else {
             String settingsJaws = Settings.System.getString(getContentResolver(),
                     Settings.System.OMNIJAWS_WEATHER_ICON_PACK);
             if (settingsJaws == null) {
                 settingsJaws = DEFAULT_WEATHER_ICON_PACKAGE;
             }
             mWeatherIconPack = (ListPreference) findPreference(WEATHER_ICON_PACK);
 
             List<String> entriesJaws = new ArrayList<String>();
             List<String> valuesJaws = new ArrayList<String>();
             getAvailableWeatherIconPacks(entriesJaws, valuesJaws);
             mWeatherIconPack.setEntries(entriesJaws.toArray(new String[entriesJaws.size()]));
             mWeatherIconPack.setEntryValues(valuesJaws.toArray(new String[valuesJaws.size()]));
 
             int valueJawsIndex = mWeatherIconPack.findIndexOfValue(settingsJaws);
             if (valueJawsIndex == -1) {
                 // no longer found
                 settingsJaws = DEFAULT_WEATHER_ICON_PACKAGE;
                 Settings.System.putString(getContentResolver(),
                         Settings.System.OMNIJAWS_WEATHER_ICON_PACK, settingsJaws);
                 valueJawsIndex = mWeatherIconPack.findIndexOfValue(settingsJaws);
             }
             mWeatherIconPack.setValueIndex(valueJawsIndex >= 0 ? valueJawsIndex : 0);
             mWeatherIconPack.setSummary(mWeatherIconPack.getEntry());
             mWeatherIconPack.setOnPreferenceChangeListener(this);
          }
   }

    private boolean isOmniJawsServiceInstalled() {
         return PackageUtils.isAvailableApp(WEATHER_SERVICE_PACKAGE, getActivity());
     }
 
     private void getAvailableWeatherIconPacks(List<String> entries, List<String> values) {
         Intent i = new Intent();
         PackageManager packageManager = getPackageManager();
         i.setAction("org.omnirom.WeatherIconPack");
         for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
             String packageName = r.activityInfo.packageName;
             Log.d("maxwen", packageName);
             if (packageName.equals(DEFAULT_WEATHER_ICON_PACKAGE)) {
                 values.add(0, r.activityInfo.name);
             } else {
                 values.add(r.activityInfo.name);
             }
             String label = r.activityInfo.loadLabel(getPackageManager()).toString();
             if (label == null) {
                 label = r.activityInfo.packageName;
             }
             if (packageName.equals(DEFAULT_WEATHER_ICON_PACKAGE)) {
                 entries.add(0, label);
             } else {
                 entries.add(label);
             }
         }
         i = new Intent(Intent.ACTION_MAIN);
         i.addCategory(CHRONUS_ICON_PACK_INTENT);
         for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
             String packageName = r.activityInfo.packageName;
             values.add(packageName + ".weather");
             String label = r.activityInfo.loadLabel(getPackageManager()).toString();
             if (label == null) {
                 label = r.activityInfo.packageName;
             }
             entries.add(label);
         }
     }
 
     private boolean isOmniJawsEnabled() {
         final Uri SETTINGS_URI
             = Uri.parse("content://org.omnirom.omnijaws.provider/settings");
 
         final String[] SETTINGS_PROJECTION = new String[] {
             "enabled"
         };
 
         final Cursor c = getContentResolver().query(SETTINGS_URI, SETTINGS_PROJECTION,
                 null, null, null);
         if (c != null) {
             int count = c.getCount();
             if (count == 1) {
                 c.moveToPosition(0);
                 boolean enabled = c.getInt(0) == 1;
                 return enabled;
             }
         }
         return true;
     }
}
