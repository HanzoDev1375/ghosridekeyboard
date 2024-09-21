/*
 * Copyright (C) 2020 Raimondas Rimkus
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

package rkr.simplekeyboard.inputmethod.compat;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

public class PreferenceManagerCompat {

  /**
   * Returns a new Context object for the current Context but whose storage APIs are backed by
   * device-protected storage. Otherwise prior to API 24 the current Context is returned.
   *
   * <p>Each call to this method returns a new instance of a Context object; Context objects are not
   * shared, however common state (ClassLoader, other Resources for the same configuration) may be
   * so the Context itself can be fairly lightweight.
   *
   * <p>Prior to API 24 this method returns the current context, since device-protected storage is
   * not available.
   *
   * @see Context#createDeviceProtectedStorageContext()
   */
  public static Context getDeviceContext(Context context) {
    var dpc = ContextCompat.createDeviceProtectedStorageContext(context);
    return (dpc == null) ? context : dpc;
  }

  /**
   * Gets a {@link SharedPreferences} instance that points to the default file that is used by the
   * preference framework in the given context.
   *
   * @param context The context of the preferences whose values are wanted
   * @return A {@link SharedPreferences} instance that can be used to retrieve and listen to values
   *     of the preferences
   */
  public static SharedPreferences getDeviceSharedPreferences(Context context) {
    return PreferenceManager.getDefaultSharedPreferences(getDeviceContext(context));
  }
}
