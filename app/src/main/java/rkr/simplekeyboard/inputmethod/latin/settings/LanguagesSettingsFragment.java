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

import static rkr.simplekeyboard.inputmethod.latin.settings.SingleLanguageSettingsFragment.LOCALE_BUNDLE_KEY;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.view.MenuProvider;
import androidx.lifecycle.Lifecycle;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.Comparator;
import java.util.Locale;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.latin.RichInputMethodManager;
import rkr.simplekeyboard.inputmethod.latin.Subtype;
import rkr.simplekeyboard.inputmethod.latin.common.LocaleUtils;
import rkr.simplekeyboard.inputmethod.latin.utils.LocaleResourceUtils;
import rkr.simplekeyboard.inputmethod.latin.utils.SubtypeLocaleUtils;

/** "Languages" settings sub screen. */
public final class LanguagesSettingsFragment extends PreferenceFragmentCompat
    implements MenuProvider {

  private static final String TAG = LanguagesSettingsFragment.class.getSimpleName();

  private static final boolean DEBUG_SUBTYPE_ID = false;
  private static final int MENU_ICON_MARGIN = 8;

  private RichInputMethodManager mRichImm;
  private CharSequence[] mUsedLocaleNames;
  private String[] mUsedLocaleValues;
  private CharSequence[] mUnusedLocaleNames;
  private String[] mUnusedLocaleValues;
  private AlertDialog mAlertDialog;
  private View mView;

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    RichInputMethodManager.init(getActivity());
    mRichImm = RichInputMethodManager.getInstance();
    setPreferencesFromResource(R.xml.empty_settings, rootKey);
  }

  @Override
  public View onCreateView(
      final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
    mView = super.onCreateView(inflater, container, savedInstanceState);
    var toolbar = ((SettingsActivity) requireActivity()).getToolbar();
    ((SettingsActivity) requireActivity()).setSupportActionBar(toolbar);
    return mView;
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    requireActivity().addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
  }

  @Override
  public void onStart() {
    super.onStart();
    buildContent();
  }

  @Override
  public void onPrepareMenu(@NonNull Menu menu) {
    if (mUsedLocaleNames != null) {
      menu.findItem(R.id.action_remove_language).setVisible(mUsedLocaleNames.length > 1);
    }
  }

  @Override
  public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    inflater.inflate(R.menu.remove_language, menu);
    inflater.inflate(R.menu.add_language, menu);
    showMenuIcons(menu);
  }

  @Override
  public boolean onMenuItemSelected(@NonNull MenuItem item) {
    final int itemId = item.getItemId();
    if (itemId == R.id.action_add_language) {
      showAddLanguagePopup();
    } else if (itemId == R.id.action_remove_language) {
      showRemoveLanguagePopup();
    }
    invalidateMenu();
    return true;
  }

  void showMenuIcons(Menu menu) {
    if (menu instanceof MenuBuilder) {
      MenuBuilder menuBuilder = (MenuBuilder) menu;
      menuBuilder.setOptionalIconsVisible(true);
      for (MenuItem item : menuBuilder.getVisibleItems()) {
        int iconMarginPx =
            (int)
                TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    MENU_ICON_MARGIN,
                    getResources().getDisplayMetrics());

        if (item.getIcon() != null) {
          if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            item.setIcon(new InsetDrawable(item.getIcon(), iconMarginPx, 0, iconMarginPx, 0));
          } else {
            item.setIcon(
                new InsetDrawable(item.getIcon(), iconMarginPx, 0, iconMarginPx, 0) {
                  @Override
                  public int getIntrinsicWidth() {
                    return getIntrinsicHeight() + iconMarginPx + iconMarginPx;
                  }
                });
          }
        }
      }
    }
  }

  /**
   * Invalidates the {@link android.view.Menu} to ensure that what is displayed matches the current
   * internal state of the menu.
   *
   * <p>This should be called whenever the state of the menu is changed, such as items being removed
   * or disabled based on some user event.
   *
   * @see {@link MenuHost}
   */
  public void invalidateMenu() {
    ((SettingsActivity) getActivity()).invalidateMenu();
  }

  /** Build the preferences and them to this settings screen. */
  private void buildContent() {
    final Context context = getActivity();
    final PreferenceGroup group = getPreferenceScreen();
    group.removeAll();

    final PreferenceCategory languageCategory = new PreferenceCategory(context);
    languageCategory.setTitle(R.string.user_languages);
    languageCategory.setIconSpaceReserved(false);
    group.addPreference(languageCategory);
    group.setIconSpaceReserved(false);
    final Comparator<Locale> comparator = new LocaleUtils.LocaleComparator();
    final Set<Subtype> enabledSubtypes = mRichImm.getEnabledSubtypes(false);
    final SortedSet<Locale> usedLocales = getUsedLocales(enabledSubtypes, comparator);
    final SortedSet<Locale> unusedLocales = getUnusedLocales(usedLocales, comparator);

    buildLanguagePreferences(usedLocales, group, context);
    setLocaleEntries(usedLocales, unusedLocales);
  }

  /**
   * Get all of the unique languages from the subtypes that have been enabled.
   *
   * @param subtypes the list of subtypes for this IME that have been enabled.
   * @param comparator the comparator to sort the languages.
   * @return a set of locales for the used languages sorted using the specified comparator.
   */
  private SortedSet<Locale> getUsedLocales(
      final Set<Subtype> subtypes, final Comparator<Locale> comparator) {
    final SortedSet<Locale> locales = new TreeSet<>(comparator);

    for (final Subtype subtype : subtypes) {
      if (DEBUG_SUBTYPE_ID) {
        var msg = "Enabled subtype: %-6s 0x%08x %11d %s";
        Log.d(
            TAG,
            String.format(
                msg,
                subtype.getLocale(),
                subtype.hashCode(),
                subtype.hashCode(),
                subtype.getName()));
      }
      locales.add(subtype.getLocaleObject());
    }

    return locales;
  }

  /**
   * Get the list of languages supported by this IME that aren't included in {@link
   * #getUsedLocales}.
   *
   * @param usedLocales the used locales.
   * @param comparator the comparator to sort the languages.
   * @return a set of locales for the unused languages sorted using the specified comparator.
   */
  private SortedSet<Locale> getUnusedLocales(
      final Set<Locale> usedLocales, final Comparator<Locale> comparator) {
    final SortedSet<Locale> locales = new TreeSet<>(comparator);
    for (String localeString : SubtypeLocaleUtils.getSupportedLocales()) {
      final Locale locale = LocaleUtils.constructLocaleFromString(localeString);
      if (usedLocales.contains(locale)) {
        continue;
      }
      locales.add(locale);
    }
    return locales;
  }

  /**
   * Create a language preference for each of the specified locales in the preference group. These
   * preferences will be added to the group in the order of the locales that are passed in.
   *
   * @param locales the locales to add preferences for.
   * @param group the preference group to add preferences to.
   * @param context the context for this application.
   */
  private void buildLanguagePreferences(
      final SortedSet<Locale> locales, final PreferenceGroup group, final Context context) {
    for (final Locale locale : locales) {
      final String localeString = LocaleUtils.getLocaleString(locale);
      final SingleLanguagePreference pref = new SingleLanguagePreference(context, localeString);
      group.addPreference(pref);
    }
  }

  /**
   * Set the lists of used languages that can be removed and unused languages that can be added.
   *
   * @param usedLocales the enabled locales for this IME.
   * @param unusedLocales the unused locales that are supported in this IME.
   */
  private void setLocaleEntries(
      final SortedSet<Locale> usedLocales, final SortedSet<Locale> unusedLocales) {
    mUsedLocaleNames = new CharSequence[usedLocales.size()];
    mUsedLocaleValues = new String[usedLocales.size()];
    int i = 0;
    for (Locale locale : usedLocales) {
      final String localeString = LocaleUtils.getLocaleString(locale);
      mUsedLocaleValues[i] = localeString;
      mUsedLocaleNames[i] = LocaleResourceUtils.getLocaleDisplayNameInSystemLocale(localeString);
      i++;
    }

    mUnusedLocaleNames = new CharSequence[unusedLocales.size()];
    mUnusedLocaleValues = new String[unusedLocales.size()];
    i = 0;
    for (Locale locale : unusedLocales) {
      final String localeString = LocaleUtils.getLocaleString(locale);
      mUnusedLocaleValues[i] = localeString;
      mUnusedLocaleNames[i] = LocaleResourceUtils.getLocaleDisplayNameInSystemLocale(localeString);
      i++;
    }
  }

  /** Show the popup to add a new language. */
  private void showAddLanguagePopup() {
    showMultiChoiceDialog(
        mUnusedLocaleNames,
        R.string.add_language,
        R.string.add,
        true,
        new OnMultiChoiceDialogAcceptListener() {
          @Override
          public void onClick(boolean[] checkedItems) {
            // enable the default layout for all of the checked languages
            for (int i = 0; i < checkedItems.length; i++) {
              if (!checkedItems[i]) {
                continue;
              }
              final Subtype subtype =
                  SubtypeLocaleUtils.getDefaultSubtype(
                      mUnusedLocaleValues[i], LanguagesSettingsFragment.this.getResources());
              mRichImm.addSubtype(subtype);
            }

            // refresh the list of enabled languages
            getActivity().invalidateOptionsMenu();
            invalidateMenu();
            buildContent();
          }
        });
  }

  /** Show the popup to remove an existing language. */
  private void showRemoveLanguagePopup() {
    showMultiChoiceDialog(
        mUsedLocaleNames,
        R.string.remove_language,
        R.string.remove,
        false,
        new OnMultiChoiceDialogAcceptListener() {
          @Override
          public void onClick(boolean[] checkedItems) {
            // disable the layouts for all of the checked languages
            for (int i = 0; i < checkedItems.length; i++) {
              if (!checkedItems[i]) {
                continue;
              }
              final Set<Subtype> subtypes =
                  mRichImm.getEnabledSubtypesForLocale(mUsedLocaleValues[i]);
              for (final Subtype subtype : subtypes) {
                mRichImm.removeSubtype(subtype);
              }
            }

            // refresh the list of enabled languages
            getActivity().invalidateOptionsMenu();
            invalidateMenu();
            buildContent();
          }
        });
  }

  /**
   * Show a multi-select popup.
   *
   * @param names the list of the choice display names.
   * @param titleRes the title of the dialog.
   * @param positiveButtonRes the text for the positive button.
   * @param allowAllChecked whether the positive button should be enabled when all items are
   *     checked.
   * @param listener the listener for when the user clicks the positive button.
   */
  private void showMultiChoiceDialog(
      final CharSequence[] names,
      final int titleRes,
      final int positiveButtonRes,
      final boolean allowAllChecked,
      final OnMultiChoiceDialogAcceptListener listener) {

    final boolean[] checkedItems = new boolean[names.length];

    mAlertDialog =
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(titleRes)
            .setMultiChoiceItems(
                names,
                checkedItems,
                (dialogInterface, which, isChecked) -> {
                  // make sure the positive button is only enabled when at least one
                  // item is checked and when not all of the items are checked (unless
                  // allowAllChecked is true)
                  boolean hasCheckedItem = false;
                  boolean hasUncheckedItem = false;
                  for (boolean itemChecked : checkedItems) {
                    if (itemChecked) {
                      hasCheckedItem = true;
                      if (allowAllChecked) {
                        break;
                      }
                    } else {
                      hasUncheckedItem = true;
                    }
                    if (hasCheckedItem && hasUncheckedItem) {
                      break;
                    }
                  }
                  mAlertDialog
                      .getButton(DialogInterface.BUTTON_POSITIVE)
                      .setEnabled(hasCheckedItem && (hasUncheckedItem || allowAllChecked));
                })
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(positiveButtonRes, (d, w) -> listener.onClick(checkedItems))
            .create();
    mAlertDialog.show();
    // disable the positive button since nothing is checked by default
    mAlertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
  }

  /**
   * Interface used to add some code to run when the positive button on a multi-select dialog is
   * clicked.
   */
  private interface OnMultiChoiceDialogAcceptListener {
    /**
     * Handler triggered when the positive button of the dialog is clicked.
     *
     * @param checkedItems a list of whether each item in the dialog was checked.
     */
    void onClick(final boolean[] checkedItems);
  }

  /** Preference to link to a language specific settings screen. */
  private static class SingleLanguagePreference extends Preference {
    private final String mLocale;
    private Bundle mExtras;

    /**
     * Create a new preference for a language.
     *
     * @param context the context for this application.
     * @param localeString a string specification of a locale, in a format of "ll_cc_variant", where
     *     "ll" is a language code, "cc" is a country code.
     */
    public SingleLanguagePreference(final Context context, final String localeString) {
      super(context);
      mLocale = localeString;

      setTitle(LocaleResourceUtils.getLocaleDisplayNameInSystemLocale(localeString));
      setFragment(SingleLanguageSettingsFragment.class.getName());
      setIconSpaceReserved(false);
    }

    @Override
    public Bundle getExtras() {
      if (mExtras == null) {
        mExtras = new Bundle();
        mExtras.putString(LOCALE_BUNDLE_KEY, mLocale);
      }
      return mExtras;
    }

    @Override
    public Bundle peekExtras() {
      return mExtras;
    }
  }
}
