/*******************************************************************************
 * Copyright 2014, barter.li
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package li.barter.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Shared Preference management
 * 
 * @author Vinay S Shenoy
 */
public class SharedPreferenceHelper {

    /**
     * Checks whether the preferences contains a key or not
     * 
     * @param context
     * @param key The string resource Id of the key
     * @return <code>true</code> if the key exists, <code>false</code> otherwise
     */
    public static boolean contains(final Context context, final int key) {
        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(context);
        return preferences.contains(context.getString(key));
    }

    /**
     * Get String value for a particular key.
     * 
     * @param context
     * @param key The string resource Id of the key
     * @return String value that was stored earlier, or empty string if no
     *         mapping exists
     */
    public static String getString(final Context context, final int key) {

        return getString(context, key, "");
    }

    /**
     * Get String value for a particular key.
     * 
     * @param context
     * @param key The string resource Id of the key
     * @param defValue The default value to return
     * @return String value that was stored earlier, or the supplied default
     *         value if no mapping exists
     */
    public static String getString(final Context context, final int key,
                    final String defValue) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(context);
        return preferences.getString(context.getString(key), defValue);
    }

    /**
     * Get int value for key.
     * 
     * @param context
     * @param key The string resource Id of the key
     * @return value or 0 if no mapping exists
     */
    public static int getInt(final Context context, final int key) {

        return getInt(context, key, 0);
    }

    /**
     * Get int value for key.
     * 
     * @param context
     * @param key The string resource Id of the key
     * @param defValue The default value
     * @return value or defValue if no mapping exists
     */
    public static int getInt(final Context context, final int key,
                    final int defValue) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(context);
        return preferences.getInt(context.getString(key), defValue);
    }

    /**
     * Get float value for a particular key.
     * 
     * @param context
     * @param key The string resource Id of the key
     * @return value or 0.0 if no mapping exists
     */
    public static float getFloat(final Context context, final int key) {

        return getFloat(context, key, 0.0f);

    }

    /**
     * Get float value for a particular key.
     * 
     * @param context
     * @param key The string resource Id of the key
     * @param defValue The default value to return if the requested key is not
     *            present
     * @return value or defValue if no mapping exists
     */
    public static float getFloat(final Context context, final int key,
                    final float defValue) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(context);
        return preferences.getFloat(context.getString(key), defValue);

    }

    /**
     * Get long value for a particular key.
     * 
     * @param context
     * @param key The string resource Id of the key
     * @return value or 0 if no mapping exists
     */
    public static long getLong(final Context context, final int key) {

        return getLong(context, key, 0l);
    }

    /**
     * Get long value for a particular key.
     * 
     * @param context
     * @param key The string resource Id of the key
     * @param defValue The default value to fetch if the requested key doesn't
     *            exist
     * @return value or defValue if no mapping exists
     */
    public static long getLong(final Context context, final int key,
                    final long defValue) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(context);
        return preferences.getLong(context.getString(key), defValue);
    }

    /**
     * Get boolean value for a particular key.
     * 
     * @param context
     * @param key The string resource Id of the key
     * @return value or <code>false</code> if no mapping exists
     */
    public static boolean getBoolean(final Context context, final int key) {

        return getBoolean(context, key, false);
    }

    /**
     * Get boolean value for a particular key.
     * 
     * @param context
     * @param key The string resource Id of the key
     * @param defValue The default value to fetch if the key doesn't exist
     * @return value or defValue if no mapping exists
     */
    public static boolean getBoolean(final Context context, final int key,
                    final boolean defValue) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(context);
        return preferences.getBoolean(context.getString(key), defValue);
    }

    /**
     * Set String value for a particular key.
     * 
     * @param context
     * @param key The string resource Id of the key
     * @param value The value to set for the key
     */
    public static void set(final Context context, final int key,
                    final String value) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(key), value);
        editor.commit();
    }

    /**
     * Set int value for key.
     * 
     * @param context
     * @param key The string resource Id of the key
     * @param value The value to set for the key
     */
    public static void set(final Context context, final int key, final int value) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(context.getString(key), value);
        editor.commit();
    }

    /**
     * Set float value for a key.
     * 
     * @param context
     * @param key The string resource Id of the key
     * @param value The value to set for the key
     */
    public static void set(final Context context, final int key,
                    final float value) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = preferences.edit();

        editor.putFloat(context.getString(key), value);
        editor.commit();
    }

    /**
     * Set long value for key.
     * 
     * @param context
     * @param key The string resource Id of the key
     * @param value The value to set for the key
     */
    public static void set(final Context context, final int key,
                    final long value) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(context.getString(key), value);
        editor.commit();
    }

    /**
     * Set boolean value for key.
     * 
     * @param context
     * @param key The string resource Id of the key
     * @param value The value to set for the key
     */
    public static void set(final Context context, final int key,
                    final boolean value) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(context.getString(key), value);
        editor.commit();
    }

    /**
     * Clear all preferences.
     * 
     * @param context
     */
    public static void clearPreferences(final Context context) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }

}
