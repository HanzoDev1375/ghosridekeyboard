/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.os.Build;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.SwitchPreferenceCompat;
import java.util.ArrayList;

public class TwoStatePreferenceHelper {

  private static final String EMPTY_TEXT = "";

  private TwoStatePreferenceHelper() {
    // This utility class is not publicly instantiable.
  }

  public static void replaceCheckBoxPreferencesByMaterialSwitchPreferences(
      final PreferenceGroup group) {
    // The keyboard settings keeps using a CheckBoxPreference on KitKat or previous.
    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
      return;
    }
    // The keyboard settings starts using a SwitchPreference without switch on/off text on
    // API versions newer than KitKat.
    replaceAllCheckBoxPreferencesByMaterialSwitchPreferences(group);
  }

  private static void replaceAllCheckBoxPreferencesByMaterialSwitchPreferences(
      final PreferenceGroup group) {

    final var preferences = new ArrayList<Preference>();
    final int count = group.getPreferenceCount();

    for (int index = 0; index < count; index++) {
      preferences.add(group.getPreference(index));
    }

    group.removeAll();

    for (int index = 0; index < count; index++) {
      final Preference preference = preferences.get(index);
      if (preference instanceof CheckBoxPreference) {
        addMaterialSwitchPreferenceBasedOnCheckBoxPreference(
            (CheckBoxPreference) preference, group);
      } else {
        group.addPreference(preference);
        if (preference instanceof PreferenceGroup) {
          replaceAllCheckBoxPreferencesByMaterialSwitchPreferences((PreferenceGroup) preference);
        }
      }
    }
  }

  static void addMaterialSwitchPreferenceBasedOnCheckBoxPreference(
      final CheckBoxPreference checkBox, final PreferenceGroup group) {
    final var switchPreferenceCompat = new SwitchPreferenceCompat(checkBox.getContext());

    switchPreferenceCompat.setTitle(checkBox.getTitle());
    switchPreferenceCompat.setKey(checkBox.getKey());
    switchPreferenceCompat.setOrder(checkBox.getOrder());
    switchPreferenceCompat.setPersistent(checkBox.isPersistent());
    switchPreferenceCompat.setEnabled(checkBox.isEnabled());
    switchPreferenceCompat.setChecked(checkBox.isChecked());
    switchPreferenceCompat.setIconSpaceReserved(checkBox.isIconSpaceReserved());
    switchPreferenceCompat.setSummary(checkBox.getSummary());
    switchPreferenceCompat.setSummaryOn(checkBox.getSummaryOn());
    switchPreferenceCompat.setSummaryOff(checkBox.getSummaryOff());
    switchPreferenceCompat.setSwitchTextOn(EMPTY_TEXT);
    switchPreferenceCompat.setSwitchTextOff(EMPTY_TEXT);
    group.addPreference(switchPreferenceCompat);
    switchPreferenceCompat.setDependency(checkBox.getDependency());
  }
}
