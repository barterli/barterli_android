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
	 * @param key
	 * @return <code>true</code> if the key exists, <code>false</code> otherwise
	 */
	public static boolean contains(final Context context, final String key) {
		final SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		return preferences.contains(key);
	}

	/**
	 * Get String value for a particular key.
	 * 
	 * @param context
	 * @param key
	 * @return String value that was stored earlier, or empty string if no
	 *         mapping exists
	 */
	public static String getString(final Context context, final String key) {

		return getString(context, key, "");
	}

	/**
	 * Get String value for a particular key.
	 * 
	 * @param context
	 * @param key
	 * @param defValue
	 *            The default value to return
	 * @return String value that was stored earlier, or the supplied default
	 *         value if no mapping exists
	 */
	public static String getString(final Context context, final String key,
			final String defValue) {

		final SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		return preferences.getString(key, defValue);
	}

	/**
	 * Get int value for key.
	 * 
	 * @param context
	 * @param key
	 * @return value or 0 if no mapping exists
	 */
	public static int getInt(final Context context, final String key) {

		return getInt(context, key, 0);
	}

	/**
	 * Get int value for key.
	 * 
	 * @param context
	 * @param key
	 * @param defValue
	 *            The default value
	 * @return value or defValue if no mapping exists
	 */
	public static int getInt(final Context context, final String key,
			final int defValue) {

		final SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		return preferences.getInt(key, defValue);
	}

	/**
	 * Get float value for a particular key.
	 * 
	 * @param context
	 * @param key
	 * @return value or 0.0 if no mapping exists
	 */
	public static float getFloat(final Context context, final String key) {

		return getFloat(context, key, 0.0f);

	}

	/**
	 * Get float value for a particular key.
	 * 
	 * @param context
	 * @param key
	 * @param defValue
	 * @return value or defValue if no mapping exists
	 */
	public static float getFloat(final Context context, final String key,
			final float defValue) {

		final SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		return preferences.getFloat(key, defValue);

	}

	/**
	 * Get long value for a particular key.
	 * 
	 * @param context
	 * @param key
	 * @return value or 0 if no mapping exists
	 */
	public static long getLong(final Context context, final String key) {

		return getLong(context, key, 0l);
	}

	/**
	 * Get long value for a particular key.
	 * 
	 * @param context
	 * @param key
	 * @param defValue
	 * @return value or defValue if no mapping exists
	 */
	public static long getLong(final Context context, final String key,
			final long defValue) {

		final SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		return preferences.getLong(key, defValue);
	}

	/**
	 * Get boolean value for a particular key.
	 * 
	 * @param context
	 * @param key
	 * @return value or false if no mapping exists
	 */
	public static boolean getBoolean(final Context context, final String key) {

		return getBoolean(context, key, false);
	}

	/**
	 * Get boolean value for a particular key.
	 * 
	 * @param context
	 * @param key
	 * @param defValue
	 * @return value or defValue if no mapping exists
	 */
	public static boolean getBoolean(final Context context, final String key,
			final boolean defValue) {

		final SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		return preferences.getBoolean(key, defValue);
	}

	/**
	 * Set String value for a particular key. Convert non-Strings to appropriate
	 * Strings before storing.
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void set(final Context context, final String key,
			final String value) {

		final SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putString(key, value);
		editor.commit();
	}

	/**
	 * Set int value for key.
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void set(final Context context, final String key,
			final int value) {

		final SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	/**
	 * Set float value for a key.
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void set(final Context context, final String key,
			final float value) {

		final SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = preferences.edit();

		editor.putFloat(key, value);
		editor.commit();
	}

	/**
	 * Set long value for key.
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void set(final Context context, final String key,
			final long value) {

		final SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putLong(key, value);
		editor.commit();
	}

	/**
	 * Set boolean value for key.
	 * 
	 * @param context
	 * @param key
	 * @param value
	 */
	public static void set(final Context context, final String key,
			final boolean value) {

		final SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		final SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(key, value);
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
