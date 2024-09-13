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

import android.content.Intent;
import android.content.ComponentName;
import android.os.Bundle;
import android.preference.Preference;
import android.widget.Toast;
import android.preference.PreferenceScreen;

import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.latin.utils.ApplicationUtils;

public final class SettingsFragment extends InputMethodSettingsFragment {
  public static final String main = "Ninja.coder.Ghostemane.code.MainActivity";

  @Override
  public void onCreate(final Bundle icicle) {
    super.onCreate(icicle);
    setHasOptionsMenu(true);
    addPreferencesFromResource(R.xml.prefs);
    final PreferenceScreen preferenceScreen = getPreferenceScreen();
    preferenceScreen.setTitle(
        ApplicationUtils.getActivityTitleResId(getActivity(), SettingsActivity.class));

    Preference openApp = findPreference("open_ghost_ide");
    if (openApp == null) {
      return;
    }
    openApp.setOnPreferenceClickListener(
        it -> {
          Intent intent = new Intent();

          intent.setComponent(new ComponentName("Ninja.coder.Ghostemane.code", main));
          if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            // اگر فعالیت موجود است، آن را شروع کنید
            startActivity(intent);
          } else {
            // اگر فعالیت موجود نیست، می‌توانید یک پیغام خطا را نمایش دهید یا یک فعالیت پیش‌فرض را
            // باز کنید
            Toast.makeText(getActivity(), "فعالیت مورد نظر موجود نیست", Toast.LENGTH_SHORT).show();
          }

          return true;
        });
  }
}
