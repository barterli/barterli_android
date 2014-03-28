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

package li.barter.http;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import li.barter.utils.Logger;

/**
 * Utility class for reading items from {@link JSONObject}s or {@link JSONArray}
 * s
 * 
 * @author Vinay S Shenoy
 */
public class JsonUtils {

    /** Tag used to print logs for debugging. */
    private static final String TAG = "JsonUtils";

    /**
     * Reads the string value from the Json Object for specified tag.
     * 
     * @param jsonObject
     * @param tag
     * @return
     */
    public static String readString(final JSONObject jsonObject,
                    final String tag) {

        String value = null;
        try {
            if (!jsonObject.isNull(tag)) {
                value = jsonObject.getString(tag);
            }
        } catch (final JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
        return value;
    }

    /**
     * Reads the int value from the Json Object for specified tag.
     * 
     * @param jsonObject
     * @param tag
     * @return
     */
    public static int readInt(final JSONObject jsonObject, final String tag) {

        int value = -1;
        try {
            if (!jsonObject.isNull(tag)) {
                value = jsonObject.getInt(tag);
            }
        } catch (final JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
        return value;
    }

    /**
     * Reads the boolean value from the Json Object for specified tag.
     * 
     * @param jsonObject
     * @param tag
     * @return
     */
    public static boolean readBoolean(final JSONObject jsonObject,
                    final String tag) {

        boolean value = false;
        try {
            if (!jsonObject.isNull(tag)) {
                value = jsonObject.getBoolean(tag);
            }
        } catch (final JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
        return value;
    }

    /**
     * Reads the float value from the Json Object for specified tag.
     * 
     * @param jsonObject
     * @param tag
     * @return
     */
    public static float readFloat(final JSONObject jsonObject, final String tag) {

        float value = 0.0f;
        try {
            if (!jsonObject.isNull(tag)) {
                value = (float) jsonObject.getDouble(tag);
            }
        } catch (final JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
        return value;
    }

    /**
     * Reads the double value from the Json Object for specified tag.
     * 
     * @param jsonObject
     * @param tag
     * @return
     */
    public static double readDouble(final JSONObject jsonObject,
                    final String tag) {

        double value = 0.0;
        try {
            if (!jsonObject.isNull(tag)) {
                value = jsonObject.getDouble(tag);
            }
        } catch (final JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
        return value;
    }

    /**
     * Reads the Long value from the Json Object for specified tag.
     * 
     * @param jsonObject
     * @param tag
     * @return
     */
    public static long readLong(final JSONObject jsonObject, final String tag) {

        long value = -1l;
        try {
            if (!jsonObject.isNull(tag)) {
                value = jsonObject.getLong(tag);
            }
        } catch (final JSONException e) {
            Logger.e(TAG, e.getMessage());

        }
        return value;
    }

    /**
     * Reads the json value from the Json Object for specified tag.
     * 
     * @param jsonObject
     * @param tag
     * @return
     */
    public static JSONObject readJSONObject(final JSONObject jsonObject,
                    final String tag) {

        JSONObject json = null;
        try {
            if (!jsonObject.isNull(tag)) {
                json = jsonObject.getJSONObject(tag);
            }
        } catch (final JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
        return json;
    }

    /**
     * Reads the json array value from the Json Object for specified tag.
     * 
     * @param jsonObject
     * @param tag
     * @return
     */
    public static JSONArray readJSONArray(final JSONObject jsonObject,
                    final String tag) {

        JSONArray jsonArray = null;
        try {
            if (!jsonObject.isNull(tag)) {
                jsonArray = jsonObject.getJSONArray(tag);
            }
        } catch (final JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
        return jsonArray;
    }

    /**
     * Reads the json object value from the Json Array for specified index.
     * 
     * @param jsonArray
     * @param index
     * @return
     */
    public static JSONObject readJSONObject(final JSONArray jsonArray,
                    final int index) {

        JSONObject json = null;
        try {
            if (!jsonArray.isNull(index)) {
                json = jsonArray.getJSONObject(index);
            }
        } catch (final JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
        return json;
    }

    /**
     * Reads the json array value from the Json Array for specified index
     * 
     * @param jsonObject
     * @param tag
     * @return
     */
    public static JSONArray readJSONArray(final JSONArray jsonArray,
                    final int index) {

        JSONArray jArray = null;
        try {
            if (!jsonArray.isNull(index)) {
                jArray = jsonArray.getJSONArray(index);
            }
        } catch (final JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
        return jArray;
    }

    /**
     * Reads the string value from the Json Array for specified index
     * 
     * @param jsonArray
     * @param index
     * @return
     */
    public static String readString(final JSONArray jsonArray, final int index) {

        String value = null;
        try {
            if (!jsonArray.isNull(index)) {
                value = jsonArray.getString(index);
            }
        } catch (final JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
        return value;
    }

    /**
     * Reads the int value from the Json Array for specified index
     * 
     * @param jsonArray
     * @param index
     * @return
     */
    public static int readInt(final JSONArray jsonArray, final int index) {

        int value = -1;
        try {
            if (!jsonArray.isNull(index)) {
                value = jsonArray.getInt(index);
            }
        } catch (final JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
        return value;
    }

    /**
     * Reads the boolean value from the Json Array for specified index
     * 
     * @param jsonArray
     * @param index
     * @return
     */
    public static boolean readBoolean(final JSONArray jsonArray, final int index) {

        boolean value = false;
        try {
            if (!jsonArray.isNull(index)) {
                value = jsonArray.getBoolean(index);
            }
        } catch (final JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
        return value;
    }

    /**
     * Reads the double value from the Json Array for specified index
     * 
     * @param jsonArray
     * @param index
     * @return
     */
    public static double readDouble(final JSONArray jsonArray, final int index) {

        double value = 0.0;
        try {
            if (!jsonArray.isNull(index)) {
                value = jsonArray.getDouble(index);
            }
        } catch (final JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
        return value;
    }

    /**
     * Reads the float value from the Json Array for specified index
     * 
     * @param jsonArray
     * @param index
     * @return
     */
    public static float readFloat(final JSONArray jsonArray, final int index) {

        float value = 0.0f;
        try {
            if (!jsonArray.isNull(index)) {
                value = (float) jsonArray.getDouble(index);
            }
        } catch (final JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
        return value;
    }

    /**
     * Reads the long value from the Json Array for specified index
     * 
     * @param jsonArray
     * @param index
     * @return
     */
    public static long readLong(final JSONArray jsonArray, final int index) {

        long value = -1L;
        try {
            if (!jsonArray.isNull(index)) {
                value = jsonArray.getLong(index);
            }
        } catch (final JSONException e) {
            Logger.e(TAG, e.getMessage());
        }
        return value;
    }

}
