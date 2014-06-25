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
import android.text.TextUtils;

import li.barter.BarterLiApplication;

/**
 * Shared Preference management
 * 
 * @author Vinay S Shenoy
 */
public class SharedPreferenceHelper {

    private static final String TAG = "SharedPreferenceHelper";

    /**
     * Checks whether the preferences contains a key or not
     * 
     * @param key The string resource Id of the key
     * @return <code>true</code> if the key exists, <code>false</code> otherwise
     */
    public static boolean contains(final int key) {
        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(BarterLiApplication
                                        .getStaticContext());
        return preferences.contains(BarterLiApplication.getStaticContext()
                        .getString(key));
    }

    /**
     * Get String value for a particular key.
     * 
     * @param key The string resource Id of the key
     * @return String value that was stored earlier, or empty string if no
     *         mapping exists
     */
    public static String getString(final int key) {

        return getString(key, "");
    }

    /**
     * Get String value for a particular key.
     * 
     * @param key The string resource Id of the key
     * @param defValue The default value to return
     * @return String value that was stored earlier, or the supplied default
     *         value if no mapping exists
     */
    public static String getString(final int key, final String defValue) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(BarterLiApplication
                                        .getStaticContext());
        return preferences.getString(BarterLiApplication.getStaticContext()
                        .getString(key), defValue);
    }

    /**
     * Get int value for key.
     * 
     * @param key The string resource Id of the key
     * @return value or 0 if no mapping exists
     */
    public static int getInt(final int key) {

        return getInt(key, 0);
    }

    /**
     * Get int value for key.
     * 
     * @param key The string resource Id of the key
     * @param defValue The default value
     * @return value or defValue if no mapping exists
     */
    public static int getInt(final int key, final int defValue) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(BarterLiApplication
                                        .getStaticContext());
        return preferences.getInt(BarterLiApplication.getStaticContext()
                        .getString(key), defValue);
    }

    /**
     * Get float value for a particular key.
     * 
     * @param key The string resource Id of the key
     * @return value or 0.0 if no mapping exists
     */
    public static float getFloat(final int key) {

        return getFloat(key, 0.0f);

    }

    /**
     * Get float value for a particular key.
     * 
     * @param key The string resource Id of the key
     * @param defValue The default value to return if the requested key is not
     *            present
     * @return value or defValue if no mapping exists
     */
    public static float getFloat(final int key, final float defValue) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(BarterLiApplication
                                        .getStaticContext());
        return preferences.getFloat(BarterLiApplication.getStaticContext()
                        .getString(key), defValue);

    }

    /**
     * Get double value for a particular key.
     * 
     * @param key The string resource Id of the key
     * @return value or 0.0 if no mapping exists
     */
    public static double getDouble(final int key) {

        return getDouble(key, 0.0);

    }

    /**
     * Get double value for a particular key.
     * 
     * @param key The string resource Id of the key
     * @param defValue The default value to return if the requested key is not
     *            present
     * @return value or defValue if no mapping exists
     */
    public static double getDouble(final int key, final double defValue) {

        final String stringValue = getString(key);

        if (TextUtils.isEmpty(stringValue)) {
            return defValue;
        } else {

            try {
                return Double.parseDouble(stringValue);
            } catch (final NumberFormatException e) {
                Logger.e(TAG, e, "Incorrect double value");
                return defValue;
            }
        }

    }

    /**
     * Get long value for a particular key.
     * 
     * @param key The string resource Id of the key
     * @return value or 0 if no mapping exists
     */
    public static long getLong(final int key) {

        return getLong(key, 0l);
    }

    /**
     * Get long value for a particular key.
     * 
     * @param key The string resource Id of the key
     * @param defValue The default value to fetch if the requested key doesn't
     *            exist
     * @return value or defValue if no mapping exists
     */
    public static long getLong(final int key, final long defValue) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(BarterLiApplication
                                        .getStaticContext());
        return preferences.getLong(BarterLiApplication.getStaticContext()
                        .getString(key), defValue);
    }

    /**
     * Get boolean value for a particular key.
     * 
     * @param key The string resource Id of the key
     * @return value or <code>false</code> if no mapping exists
     */
    public static boolean getBoolean(final int key) {

        return getBoolean(key, false);
    }

    /**
     * Get boolean value for a particular key.
     * 
     * @param key The string resource Id of the key
     * @param defValue The default value to fetch if the key doesn't exist
     * @return value or defValue if no mapping exists
     */
    public static boolean getBoolean(final int key, final boolean defValue) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(BarterLiApplication
                                        .getStaticContext());
        return preferences.getBoolean(BarterLiApplication.getStaticContext()
                        .getString(key), defValue);
    }

    /**
     * Set String value for a particular key.
     * 
     * @param key The string resource Id of the key
     * @param value The value to set for the key
     */
    public static void set(final int key, final String value) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(BarterLiApplication
                                        .getStaticContext());
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putString(BarterLiApplication.getStaticContext().getString(key), value);
        editor.commit();
    }

    /**
     * Set int value for key.
     * 
     * @param key The string resource Id of the key
     * @param value The value to set for the key
     */
    public static void set(final int key, final int value) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(BarterLiApplication
                                        .getStaticContext());
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(BarterLiApplication.getStaticContext().getString(key), value);
        editor.commit();
    }

    /**
     * Set float value for a key.
     * 
     * @param key The string resource Id of the key
     * @param value The value to set for the key
     */
    public static void set(final int key, final float value) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(BarterLiApplication
                                        .getStaticContext());
        final SharedPreferences.Editor editor = preferences.edit();

        editor.putFloat(BarterLiApplication.getStaticContext().getString(key), value);
        editor.commit();
    }

    /**
     * Set double value for a key.
     * 
     * @param key The string resource Id of the key
     * @param value The value to set for the key
     */
    public static void set(final int key, final double value) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(BarterLiApplication
                                        .getStaticContext());
        final SharedPreferences.Editor editor = preferences.edit();

        editor.putString(BarterLiApplication.getStaticContext().getString(key), String
                        .valueOf(value));
        editor.commit();
    }

    /**
     * Set long value for key.
     * 
     * @param key The string resource Id of the key
     * @param value The value to set for the key
     */
    public static void set(final int key, final long value) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(BarterLiApplication
                                        .getStaticContext());
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putLong(BarterLiApplication.getStaticContext().getString(key), value);
        editor.commit();
    }

    /**
     * Set boolean value for key.
     * 
     * @param key The string resource Id of the key
     * @param value The value to set for the key
     */
    public static void set(final int key, final boolean value) {

        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(BarterLiApplication
                                        .getStaticContext());
        final SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(BarterLiApplication.getStaticContext().getString(key), value);
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

    /**
     * Removes the given keys from the Shared Preferences
     * 
     * @param context
     * @param keys The keys to removed
     */
    public static void removeKeys(final Context context, final int... keys) {

        assert (keys != null);
        assert (keys.length > 0);
        final SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = preferences.edit();

        for (final int aKey : keys) {

            editor.remove(context.getString(aKey));
        }

        editor.commit();
    }

}
