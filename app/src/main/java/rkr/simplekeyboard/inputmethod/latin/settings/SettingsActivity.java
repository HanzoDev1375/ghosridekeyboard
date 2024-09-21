/*
 * Copyright (C) 2012 The Android Open Source Project
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

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import com.google.android.material.color.MaterialColors;
import rkr.simplekeyboard.inputmethod.R;

public class SettingsActivity extends AppCompatActivity
    implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

  private static final String TITLE_TAG = "settingsActivityTitle";
  public static final String DEFAULT_TOOL_TITLE = "Ghost ide Keybord";
  private static final String DEFAULT_FRAGMENT = SettingsFragment.class.getName();
  private static final String TAG = SettingsActivity.class.getSimpleName();

  @Override
  protected void onStart() {
    super.onStart();

    boolean enabled = false;
    try {
      enabled = isInputMethodOfThisImeEnabled();
    } catch (Exception e) {
      Log.e(TAG, "Exception in check if input method is enabled", e);
    }
  }

  /**
   * Check if this IME is enabled in the system.
   *
   * @return whether this IME is enabled in the system.
   */
  private boolean isInputMethodOfThisImeEnabled() {
    final InputMethodManager imm =
        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    final String imePackageName = getPackageName();
    for (final InputMethodInfo imi : imm.getEnabledInputMethodList()) {
      if (imi.getPackageName().equals(imePackageName)) {
        return true;
      }
    }
    return false;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings_activity);

    if (savedInstanceState == null) {
      getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.settings, new SettingsFragment())
          .commit();
    } else {
      setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
    }

    getSupportFragmentManager()
        .addOnBackStackChangedListener(
            () -> {
              if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                setTitle(DEFAULT_TOOL_TITLE);
              }
            });
    setSupportActionBar(findViewById(R.id.toolbar));
    ActionBar actionBar = getSupportActionBar();

    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeButtonEnabled(true);
      actionBar.setSubtitle("Fast Type Keyboard");
    }

    getWindow()
        .setStatusBarColor(
            MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, 0));
    getWindow()
        .setNavigationBarColor(
            MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, 0));
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    // Save current activity title so we can set it again after a configuration change
    outState.putCharSequence(TITLE_TAG, getTitle());
  }

  @Override
  public boolean onSupportNavigateUp() {
    if (getSupportFragmentManager().popBackStackImmediate()) {
      return true;
    }
    return super.onSupportNavigateUp();
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      FragmentManager manager = getSupportFragmentManager();
      if (!manager.popBackStackImmediate()) {
        finish();
      }
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
    // Instantiate the new Fragment
    final Bundle args = pref.getExtras();
    final Fragment fragment =
        getSupportFragmentManager()
            .getFragmentFactory()
            .instantiate(getClassLoader(), pref.getFragment());
    fragment.setArguments(args);
    fragment.setTargetFragment(caller, 0);
    // Replace the existing Fragment with the new Fragment
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.settings, fragment)
        .addToBackStack(null)
        .commit();
    setTitle(pref.getTitle());
    return true;
  }

  public Toolbar getToolbar() {
    return (Toolbar) findViewById(R.id.toolbar);
  }
}
