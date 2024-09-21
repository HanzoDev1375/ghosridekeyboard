package rkr.simplekeyboard.inputmethod.latin.settings;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.keyboard.KeyboardTheme;

/**
 * @author EUP
 */
public class ThemePreference extends Preference {

  private int selectedThemeId;

  public ThemePreference(Context context) {
    super(context);
  }

  public ThemePreference(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public ThemePreference(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  @Override
  protected void onClick() {
    super.onClick();
    var themeNames = getContext().getResources().getStringArray(R.array.keyboard_theme_names);
    var DEFAULT_THEME = themeNames[KeyboardTheme.DEFAULT_THEME_ID];
    var keyboardTheme = KeyboardTheme.getKeyboardTheme(getContext());

    selectedThemeId = keyboardTheme.mThemeId;

    new MaterialAlertDialogBuilder((AppCompatActivity) getContext())
        .setTitle(R.string.settings_screen_theme)
        .setSingleChoiceItems(
            themeNames,
            selectedThemeId,
            (dialog, which) -> {
              KeyboardTheme.saveKeyboardThemeId(which, getSharedPreferences());
              Settings.removeKeyboardColor(getSharedPreferences());
              notifyChanged();
              dialog.dismiss();
            })
        .setNegativeButton(android.R.string.cancel, (d, w) -> d.dismiss())
        .setCancelable(false)
        .show();
  }

  @Override
  protected String getPersistedString(String fallBackTheme) {
    return getSharedPreferences().getString(KeyboardTheme.KEYBOARD_THEME_KEY, fallBackTheme);
  }

  public static void updateKeyboardThemeSummary(final Preference pref) {
    if (pref == null) return;
    final Context context = pref.getContext();
    final Resources res = context.getResources();
    final KeyboardTheme keyboardTheme = KeyboardTheme.getKeyboardTheme(context);
    final String[] keyboardThemeNames = res.getStringArray(R.array.keyboard_theme_names);

    for (int index = 0; index < keyboardThemeNames.length; index++) {
      if (keyboardTheme.mThemeId == index) {
        pref.setSummary(keyboardThemeNames[index]);
        return;
      }
    }
  }
}
