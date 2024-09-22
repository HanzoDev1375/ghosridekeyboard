/*
 * Copyright (C) 2008 The Android Open Source Project
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

package rkr.simplekeyboard.inputmethod.latin.settings;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import androidx.preference.Preference;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.latin.utils.ApplicationUtils;

public final class SettingsFragment extends InputMethodSettingsFragment {

  private static final String GHOST_APP_PACKAGE = "Ninja.coder.Ghostemane.code";
  private static final String GHOST_TARGET_ACTIVITY_CLASS =
      "Ninja.coder.Ghostemane.code.MainActivity";

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    super.onCreatePreferences(savedInstanceState, rootKey);
    addPreferencesFromResource(R.xml.prefs);

    getPreferenceScreen()
        .setTitle(ApplicationUtils.getActivityTitleResId(getActivity(), SettingsActivity.class));

    var openApp = findPreference("open_ghost_ide");
    var keyboardinstall = findPreference("keyboardset");

    if (keyboardinstall == null || openApp == null) return;

    keyboardinstall.setOnPreferenceClickListener(this::configInputMethod);

    openApp.setOnPreferenceClickListener(
        // assuming activity is exported
        it -> {
          try {
            var intent = new Intent();
            intent.setComponent(new ComponentName(GHOST_APP_PACKAGE, GHOST_TARGET_ACTIVITY_CLASS));
            startActivity(intent);
          } catch (ActivityNotFoundException e) {
            Toast.makeText(
                    getContext(),
                    "Ghost IDE is not installed! " + "\n" + e.getLocalizedMessage(),
                    Toast.LENGTH_SHORT)
                .show();
          }
          return true;
        });
  }

  boolean configInputMethod(Preference preference) {
    new MaterialAlertDialogBuilder(preference.getContext())
        .setTitle("Install Keyboard")
        .setMessage("You can install the keyboard")
        .setPositiveButton(
            "Install settings",
            (d, w) -> {
              startActivity(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS));
            })
        .setNegativeButton(
            "Change input method",
            (d, w) -> {
              InputMethodManager i =
                  (InputMethodManager) getActivity().getSystemService("input_method");
              i.showInputMethodPicker();
            })
        .setNeutralButton("Cancel", null)
        .setCancelable(false)
        .show();
    return true; // to handle preference click
  }
}
