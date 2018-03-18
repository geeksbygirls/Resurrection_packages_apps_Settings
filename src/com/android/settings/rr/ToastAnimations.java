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
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.provider.Settings;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import java.util.ArrayList;
import java.util.List;
import android.widget.Toast;

import com.android.internal.logging.MetricsProto.MetricsEvent;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class ToastAnimations extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "ToastAnimations";

    private static final String PREF_TOAST_ANIMATION = "toast_animation";
    private static final String PREF_TEXT_COLOR = "toast_text_color";
    private static final String PREF_ICON_COLOR = "toast_icon_color";

    private static final int DEFAULT_COLOR = 0xffffffff;

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET = 0;

    private ListPreference mToastAnimation;
    private ColorPickerPreference mIconColor;
    private ColorPickerPreference mTextColor;
    protected Context mContext;
    protected ContentResolver mContentRes;

    @Override
    protected int getMetricsCategory() {
        return MetricsEvent.RESURRECTED;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.rr_toast);

        mContext = getActivity().getApplicationContext();
        mContentRes = getActivity().getContentResolver();
        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mTextColor = (ColorPickerPreference) prefSet.findPreference(PREF_TEXT_COLOR);
        mTextColor.setOnPreferenceChangeListener(this);
        int textColor = Settings.System.getInt(resolver,
                Settings.System.TOAST_TEXT_COLOR, DEFAULT_COLOR);
        String textHexColor = String.format("#%08x", (0xffffffff & textColor));
        mTextColor.setSummary(textHexColor);
        mTextColor.setNewPreviewColor(textColor);

        mIconColor = (ColorPickerPreference) prefSet.findPreference(PREF_ICON_COLOR);
        mIconColor.setOnPreferenceChangeListener(this);
        int iconColor = Settings.System.getInt(resolver,
                Settings.System.TOAST_ICON_COLOR, DEFAULT_COLOR);
        String iconHexColor = String.format("#%08x", (0xffffffff & iconColor));
        mIconColor.setSummary(iconHexColor);
        mIconColor.setNewPreviewColor(iconColor);

        mToastAnimation = (ListPreference) findPreference(PREF_TOAST_ANIMATION);
        mToastAnimation.setSummary(mToastAnimation.getEntry());
        int CurrentToastAnimation = Settings.System.getInt(getContentResolver(), Settings.System.TOAST_ANIMATION, 1);
        mToastAnimation.setValueIndex(CurrentToastAnimation); //set to index of default value
        mToastAnimation.setSummary(mToastAnimation.getEntries()[CurrentToastAnimation]);
        mToastAnimation.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue){
	   boolean result = false;
           if (preference == mToastAnimation) {
              int index = mToastAnimation.findIndexOfValue((String) newValue);
              Settings.System.putString(getContentResolver(), Settings.System.TOAST_ANIMATION, (String) newValue);
              mToastAnimation.setSummary(mToastAnimation.getEntries()[index]);
              Toast.makeText(mContext, "Toast Test", Toast.LENGTH_SHORT).show();
              return true;
           } else if (preference == mTextColor) {
              String hex = ColorPickerPreference.convertToARGB(
                   Integer.valueOf(String.valueOf(newValue)));
              preference.setSummary(hex);
              int intHex = ColorPickerPreference.convertToColorInt(hex);
              Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.TOAST_TEXT_COLOR, intHex);
            return true;
           } else if (preference == mIconColor) {
              String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
              preference.setSummary(hex);
              int intHex = ColorPickerPreference.convertToColorInt(hex);
              Settings.System.putInt(getActivity().getApplicationContext().getContentResolver(),
                    Settings.System.TOAST_ICON_COLOR, intHex);
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
        alertDialog.setTitle(R.string.toast_colors_reset_title);
        alertDialog.setMessage(R.string.toast_colors_reset_message);
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
                 Settings.System.TOAST_TEXT_COLOR, DEFAULT_COLOR);
        mTextColor.setNewPreviewColor(DEFAULT_COLOR);
        mTextColor.setSummary(R.string.default_string); 
        Settings.System.putInt(resolver,
                 Settings.System.TOAST_ICON_COLOR, DEFAULT_COLOR);
        mIconColor.setNewPreviewColor(DEFAULT_COLOR);
        mIconColor.setSummary(R.string.default_string);   
    }
}
