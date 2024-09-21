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

import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import com.google.android.material.transition.MaterialSharedAxis;
import rkr.simplekeyboard.inputmethod.compat.PreferenceManagerCompat;

/**
 * A base abstract class for a {@link PreferenceFragmentCompat} that implements a nested {@link
 * PreferenceScreen} of the main preference screen.
 */
public abstract class SubScreenFragment extends PreferenceFragmentCompat
    implements OnSharedPreferenceChangeListener {

  private OnSharedPreferenceChangeListener mSharedPreferenceChangeListener;
  public static boolean isOnCreateCalled = false;

  @Override
  public void onCreate(Bundle arg0) {
    super.onCreate(arg0);
    // TODO: Implement this method
    setEnterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
    setReturnTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false));
    setExitTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, true));
    setReenterTransition(new MaterialSharedAxis(MaterialSharedAxis.Z, false));
  }

  static void setPreferenceEnabled(
      final String prefKey, final boolean enabled, final PreferenceScreen screen) {
    final Preference preference = screen.findPreference(prefKey);
    if (preference != null) {
      preference.setEnabled(enabled);
    }
  }

  static void removePreference(final String prefKey, final PreferenceScreen screen) {
    final Preference preference = screen.findPreference(prefKey);
    if (preference != null) {
      screen.removePreference(preference);
    }
  }

  final void setPreferenceEnabled(final String prefKey, final boolean enabled) {
    setPreferenceEnabled(prefKey, enabled, getPreferenceScreen());
  }

  final void removePreference(final String prefKey) {
    removePreference(prefKey, getPreferenceScreen());
  }

  final SharedPreferences getSharedPreferences() {
    return PreferenceManagerCompat.getDeviceSharedPreferences(getActivity());
  }

  @Override
  public void addPreferencesFromResource(final int preferencesResId) {
    super.addPreferencesFromResource(preferencesResId);
    TwoStatePreferenceHelper.replaceCheckBoxPreferencesByMaterialSwitchPreferences(
        getPreferenceScreen());
  }

  @Override
  public void setPreferencesFromResource(int res, String rootKey) {
    super.setPreferencesFromResource(res, rootKey);
    TwoStatePreferenceHelper.replaceCheckBoxPreferencesByMaterialSwitchPreferences(
        getPreferenceScreen());
  }

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    isOnCreateCalled = true;

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
      super.getPreferenceManager().setStorageDeviceProtected();
    }
    mSharedPreferenceChangeListener =
        new OnSharedPreferenceChangeListener() {
          @Override
          public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {
            final SubScreenFragment fragment = SubScreenFragment.this;
            final Context context = fragment.getActivity();
            if (context == null || fragment.getPreferenceScreen() == null) {
              final String tag = fragment.getClass().getSimpleName();
              // TODO: Introduce a static function to register this class and ensure that
              // onCreate must be called before "onSharedPreferenceChanged" is called.
              Log.w(tag, "onSharedPreferenceChanged called before activity starts.");
              return;
            }
            new BackupManager(context).dataChanged();
            fragment.onSharedPreferenceChanged(prefs, key);
          }
        };
    getSharedPreferences()
        .registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
  }

  @Override
  public void onDestroy() {
    getSharedPreferences()
        .unregisterOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);
    super.onDestroy();
  }

  @Override
  public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {}
}
