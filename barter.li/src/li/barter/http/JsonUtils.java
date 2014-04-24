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

import java.util.Locale;

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
     * Enum to indicate the type of field
     * 
     * @author Vinay S Shenoy
     */
    public static enum FieldType {
        INT,
        STRING,
        DOUBLE,
        LONG,
        OBJECT,
        ARRAY,
        BOOL
    }

    /**
     * String format for formatting exception of null value in Json Objects
     */
    private static final String NULL_VALUE_FORMAT_OBJECT = "Value is null for key %s when key is specified as not null";

    /**
     * String format for formatting exception of null value in Json Arrays
     */
    private static final String NULL_VALUE_FORMAT_ARRAY  = "Value is null for index %d when index is specified as not null";

    /**
     * String format for formatting exceptions for get type
     */
    private static final String UNKNOWN_TYPE             = "Unknown type %s";

    /**
     * Gets the field type for a particular key
     * 
     * @param jsonObject The {@link JSONObject} to check the key
     * @param key The key to check
     * @return what kind of type the field is, represented as a
     *         {@link FieldType} object
     * @throws JSONException If the key is not present, or the value is
     *             <code>null</code>
     */
    public static FieldType getTypeForKey(JSONObject jsonObject, String key)
                    throws JSONException {

        if (jsonObject.isNull(key)) {
            throw new JSONException(String.format(Locale.US, NULL_VALUE_FORMAT_OBJECT, key));
        }

        final Object object = jsonObject.get(key);

        if (object instanceof Long) {
            return FieldType.LONG;
        } else if (object instanceof Integer) {
            return FieldType.INT;
        } else if (object instanceof Boolean) {
            return FieldType.BOOL;
        } else if (object instanceof String) {
            return FieldType.STRING;
        } else if (object instanceof Double) {
            return FieldType.DOUBLE;
        } else if (object instanceof JSONObject) {
            return FieldType.OBJECT;
        } else if (object instanceof JSONArray) {
            return FieldType.ARRAY;
        }

        throw new JSONException(String.format(Locale.US, UNKNOWN_TYPE, object
                        .getClass().getName()));
    }

    /**
     * Reads the string value from the Json Object for specified tag.
     * 
     * @param jsonObject The {@link JSONObject} to read the key from
     * @param key The key to read
     * @param required Whether the key is required. If <code>true</code>,
     *            attempting to read it when it is <code>null</code> will throw
     *            a {@link JSONException}
     * @param notNull Whether the value is allowed to be <code>null</code>. If
     *            <code>true</code>, will throw a {@link JSONException} if the
     *            value is null
     * @return The read value
     * @throws JSONException If the string was unable to be read, or if the
     *             required/notNull flags were violated
     */
    public static String readString(final JSONObject jsonObject,
                    final String key, final boolean required,
                    final boolean notNull) throws JSONException {

        if (required) {
            //Will throw JsonException if mapping doesn't exist
            return jsonObject.getString(key);
        }

        if (notNull && jsonObject.isNull(key)) {
            //throw JsonException because key is null
            throw new JSONException(String.format(Locale.US, NULL_VALUE_FORMAT_OBJECT, key));
        }
        String value = null;
        if (!jsonObject.isNull(key)) {
            value = jsonObject.getString(key);
        }
        return value;
    }

    /**
     * Reads the int value from the Json Object for specified tag.
     * 
     * @param jsonObject The {@link JSONObject} to read the key from
     * @param key The key to read
     * @param required Whether the key is required. If <code>true</code>,
     *            attempting to read it when it is <code>null</code> will throw
     *            a {@link JSONException}
     * @param notNull Whether the value is allowed to be <code>null</code>. If
     *            <code>true</code>, will throw a {@link JSONException} if the
     *            value is null
     * @return The read value
     * @throws JSONException If the int was unable to be read, or if the
     *             required/notNull flags were violated
     */
    public static int readInt(final JSONObject jsonObject, final String key,
                    final boolean required, final boolean notNull)
                    throws JSONException {

        if (required) {
            //Will throw JsonException if mapping doesn't exist
            return jsonObject.getInt(key);
        }

        if (notNull && jsonObject.isNull(key)) {
            //throw JsonException because key is null
            throw new JSONException(String.format(Locale.US, NULL_VALUE_FORMAT_OBJECT, key));
        }
        int value = 0;
        if (!jsonObject.isNull(key)) {
            value = jsonObject.getInt(key);
        }
        return value;
    }

    /**
     * Reads the boolean value from the Json Object for specified tag.
     * 
     * @param jsonObject The {@link JSONObject} to read the key from
     * @param key The key to read
     * @param required Whether the key is required. If <code>true</code>,
     *            attempting to read it when it is <code>null</code> will throw
     *            a {@link JSONException}
     * @param notNull Whether the value is allowed to be <code>null</code>. If
     *            <code>true</code>, will throw a {@link JSONException} if the
     *            value is null
     * @return The read value
     * @throws JSONException If the boolean was unable to be read, or if the
     *             required/notNull flags were violated
     */
    public static boolean readBoolean(final JSONObject jsonObject,
                    final String key, final boolean required,
                    final boolean notNull) throws JSONException {

        if (required) {
            //Will throw JsonException if mapping doesn't exist
            return jsonObject.getBoolean(key);
        }

        if (notNull && jsonObject.isNull(key)) {
            //throw JsonException because key is null
            throw new JSONException(String.format(Locale.US, NULL_VALUE_FORMAT_OBJECT, key));
        }
        boolean value = false;
        if (!jsonObject.isNull(key)) {
            value = jsonObject.getBoolean(key);
        }
        return value;
    }

    /**
     * Reads the float value from the Json Object for specified tag.
     * 
     * @param jsonObject The {@link JSONObject} to read the key from
     * @param key The key to read
     * @param required Whether the key is required. If <code>true</code>,
     *            attempting to read it when it is <code>null</code> will throw
     *            a {@link JSONException}
     * @param notNull Whether the value is allowed to be <code>null</code>. If
     *            <code>true</code>, will throw a {@link JSONException} if the
     *            value is null
     * @return The read value
     * @throws JSONException If the float was unable to be read, or if the
     *             required/notNull flags were violated
     */
    public static float readFloat(final JSONObject jsonObject,
                    final String key, final boolean required,
                    final boolean notNull) throws JSONException {

        if (required) {
            //Will throw JsonException if mapping doesn't exist
            return (float) jsonObject.getDouble(key);
        }

        if (notNull && jsonObject.isNull(key)) {
            //throw JsonException because key is null
            throw new JSONException(String.format(Locale.US, NULL_VALUE_FORMAT_OBJECT, key));
        }
        float value = 0.0f;
        if (!jsonObject.isNull(key)) {
            value = (float) jsonObject.getDouble(key);
        }
        return value;
    }

    /**
     * Reads the double value from the Json Object for specified tag.
     * 
     * @param jsonObject The {@link JSONObject} to read the key from
     * @param key The key to read
     * @param required Whether the key is required. If <code>true</code>,
     *            attempting to read it when it is <code>null</code> will throw
     *            a {@link JSONException}
     * @param notNull Whether the value is allowed to be <code>null</code>. If
     *            <code>true</code>, will throw a {@link JSONException} if the
     *            value is null
     * @return The read value
     * @throws JSONException If the double was unable to be read, or if the
     *             required/notNull flags were violated
     */
    public static double readDouble(final JSONObject jsonObject,
                    final String key, final boolean required,
                    final boolean notNull) throws JSONException {

        if (required) {
            //Will throw JsonException if mapping doesn't exist
            return jsonObject.getDouble(key);
        }

        if (notNull && jsonObject.isNull(key)) {
            //throw JsonException because key is null
            throw new JSONException(String.format(Locale.US, NULL_VALUE_FORMAT_OBJECT, key));
        }
        double value = 0.0;
        if (!jsonObject.isNull(key)) {
            value = jsonObject.getDouble(key);
        }
        return value;
    }

    /**
     * Reads the Long value from the Json Object for specified tag.
     * 
     * @param jsonObject The {@link JSONObject} to read the key from
     * @param key The key to read
     * @param required Whether the key is required. If <code>true</code>,
     *            attempting to read it when it is <code>null</code> will throw
     *            a {@link JSONException}
     * @param notNull Whether the value is allowed to be <code>null</code>. If
     *            <code>true</code>, will throw a {@link JSONException} if the
     *            value is null
     * @return The read value
     * @throws JSONException If the long was unable to be read, or if the
     *             required/notNull flags were violated
     */
    public static long readLong(final JSONObject jsonObject, final String key,
                    final boolean required, final boolean notNull)
                    throws JSONException {

        if (required) {
            //Will throw JsonException if mapping doesn't exist
            return jsonObject.getLong(key);
        }

        if (notNull && jsonObject.isNull(key)) {
            //throw JsonException because key is null
            throw new JSONException(String.format(Locale.US, NULL_VALUE_FORMAT_OBJECT, key));
        }
        long value = 0l;
        if (!jsonObject.isNull(key)) {
            value = jsonObject.getLong(key);
        }
        return value;
    }

    /**
     * Reads the json value from the Json Object for specified tag.
     * 
     * @param jsonObject The {@link JSONObject} to read the key from
     * @param key The key to read
     * @param required Whether the key is required. If <code>true</code>,
     *            attempting to read it when it is <code>null</code> will throw
     *            a {@link JSONException}
     * @param notNull Whether the value is allowed to be <code>null</code>. If
     *            <code>true</code>, will throw a {@link JSONException} if the
     *            value is null
     * @return The read value
     * @throws JSONException If the {@link JSONObject} was unable to be read, or
     *             if the required/notNull flags were violated
     */
    public static JSONObject readJSONObject(final JSONObject jsonObject,
                    final String key, final boolean required,
                    final boolean notNull) throws JSONException {

        if (required) {
            //Will throw JsonException if mapping doesn't exist
            return jsonObject.getJSONObject(key);
        }

        if (notNull && jsonObject.isNull(key)) {
            //throw JsonException because key is null
            throw new JSONException(String.format(Locale.US, NULL_VALUE_FORMAT_OBJECT, key));
        }
        JSONObject value = null;
        if (!jsonObject.isNull(key)) {
            value = jsonObject.getJSONObject(key);
        }
        return value;
    }

    /**
     * Reads the json array value from the Json Object for specified tag.
     * 
     * @param jsonObject The {@link JSONObject} to read the key from
     * @param key The key to read
     * @param required Whether the key is required. If <code>true</code>,
     *            attempting to read it when it is <code>null</code> will throw
     *            a {@link JSONException}
     * @param notNull Whether the value is allowed to be <code>null</code>. If
     *            <code>true</code>, will throw a {@link JSONException} if the
     *            value is null
     * @return The read value
     * @throws JSONException If the {@link JSONArray} was unable to be read, or
     *             if the required/notNull flags were violated
     */
    public static JSONArray readJSONArray(final JSONObject jsonObject,
                    final String key, final boolean required,
                    final boolean notNull) throws JSONException {

        if (required) {
            //Will throw JsonException if mapping doesn't exist
            return jsonObject.getJSONArray(key);
        }

        if (notNull && jsonObject.isNull(key)) {
            //throw JsonException because key is null
            throw new JSONException(String.format(Locale.US, NULL_VALUE_FORMAT_OBJECT, key));
        }
        JSONArray value = null;
        if (!jsonObject.isNull(key)) {
            value = jsonObject.getJSONArray(key);
        }
        return value;
    }

    /**
     * Reads the json object value from the Json Array for specified index.
     * 
     * @param jsonArray The {@link JSONArray} to read the index from
     * @param index The key to read
     * @param required Whether the index is required. If <code>true</code>,
     *            attempting to read it when it is <code>null</code> will throw
     *            a {@link JSONException}
     * @param notNull Whether the value is allowed to be <code>null</code>. If
     *            <code>true</code>, will throw a {@link JSONException} if the
     *            value is null
     * @return The read value
     * @throws JSONException If the {@link JSONObject} was unable to be read, or
     *             if the required/notNull flags were violated
     */
    public static JSONObject readJSONObject(final JSONArray jsonArray,
                    final int index, final boolean required,
                    final boolean notNull) throws JSONException {

        if (required) {
            //Will throw JsonException if mapping doesn't exist
            return jsonArray.getJSONObject(index);
        }

        if (notNull && jsonArray.isNull(index)) {
            //throw JsonException because key is null
            throw new JSONException(String.format(Locale.US, NULL_VALUE_FORMAT_ARRAY, index));
        }
        JSONObject value = null;
        if (!jsonArray.isNull(index)) {
            value = jsonArray.getJSONObject(index);
        }
        return value;
    }

    /**
     * Reads the json array value from the Json Array for specified index
     * 
     * @param jsonArray The {@link JSONArray} to read the index from
     * @param index The key to read
     * @param required Whether the index is required. If <code>true</code>,
     *            attempting to read it when it is <code>null</code> will throw
     *            a {@link JSONException}
     * @param notNull Whether the value is allowed to be <code>null</code>. If
     *            <code>true</code>, will throw a {@link JSONException} if the
     *            value is null
     * @return The read value
     * @throws JSONException If the {@link JSONArray} was unable to be read, or
     *             if the required/notNull flags were violated
     */
    public static JSONArray readJSONArray(final JSONArray jsonArray,
                    final int index, final boolean required,
                    final boolean notNull) throws JSONException {

        if (required) {
            //Will throw JsonException if mapping doesn't exist
            return jsonArray.getJSONArray(index);
        }

        if (notNull && jsonArray.isNull(index)) {
            //throw JsonException because key is null
            throw new JSONException(String.format(Locale.US, NULL_VALUE_FORMAT_ARRAY, index));
        }
        JSONArray value = null;
        if (!jsonArray.isNull(index)) {
            value = jsonArray.getJSONArray(index);
        }
        return value;
    }

    /**
     * Reads the string value from the Json Array for specified index
     * 
     * @param jsonArray The {@link JSONArray} to read the index from
     * @param index The key to read
     * @param required Whether the index is required. If <code>true</code>,
     *            attempting to read it when it is <code>null</code> will throw
     *            a {@link JSONException}
     * @param notNull Whether the value is allowed to be <code>null</code>. If
     *            <code>true</code>, will throw a {@link JSONException} if the
     *            value is null
     * @return The read value
     * @throws JSONException If the String was unable to be read, or if the
     *             required/notNull flags were violated
     */
    public static String readString(final JSONArray jsonArray, final int index,
                    final boolean required, final boolean notNull)
                    throws JSONException {

        if (required) {
            //Will throw JsonException if mapping doesn't exist
            return jsonArray.getString(index);
        }

        if (notNull && jsonArray.isNull(index)) {
            //throw JsonException because key is null
            throw new JSONException(String.format(Locale.US, NULL_VALUE_FORMAT_ARRAY, index));
        }
        String value = null;
        if (!jsonArray.isNull(index)) {
            value = jsonArray.getString(index);
        }
        return value;
    }

    /**
     * Reads the int value from the Json Array for specified index
     * 
     * @param jsonArray The {@link JSONArray} to read the index from
     * @param index The key to read
     * @param required Whether the index is required. If <code>true</code>,
     *            attempting to read it when it is <code>null</code> will throw
     *            a {@link JSONException}
     * @param notNull Whether the value is allowed to be <code>null</code>. If
     *            <code>true</code>, will throw a {@link JSONException} if the
     *            value is null
     * @return The read value
     * @throws JSONException If the int was unable to be read, or if the
     *             required/notNull flags were violated
     */
    public static int readInt(final JSONArray jsonArray, final int index,
                    final boolean required, final boolean notNull)
                    throws JSONException {

        if (required) {
            //Will throw JsonException if mapping doesn't exist
            return jsonArray.getInt(index);
        }

        if (notNull && jsonArray.isNull(index)) {
            //throw JsonException because key is null
            throw new JSONException(String.format(Locale.US, NULL_VALUE_FORMAT_ARRAY, index));
        }
        int value = 0;
        if (!jsonArray.isNull(index)) {
            value = jsonArray.getInt(index);
        }
        return value;
    }

    /**
     * Reads the boolean value from the Json Array for specified index
     * 
     * @param jsonArray The {@link JSONArray} to read the index from
     * @param index The key to read
     * @param required Whether the index is required. If <code>true</code>,
     *            attempting to read it when it is <code>null</code> will throw
     *            a {@link JSONException}
     * @param notNull Whether the value is allowed to be <code>null</code>. If
     *            <code>true</code>, will throw a {@link JSONException} if the
     *            value is null
     * @return The read value
     * @throws JSONException If the boolean was unable to be read, or if the
     *             required/notNull flags were violated
     */
    public static boolean readBoolean(final JSONArray jsonArray,
                    final int index, final boolean required,
                    final boolean notNull) throws JSONException {

        if (required) {
            //Will throw JsonException if mapping doesn't exist
            return jsonArray.getBoolean(index);
        }

        if (notNull && jsonArray.isNull(index)) {
            //throw JsonException because key is null
            throw new JSONException(String.format(Locale.US, NULL_VALUE_FORMAT_ARRAY, index));
        }
        boolean value = false;
        if (!jsonArray.isNull(index)) {
            value = jsonArray.getBoolean(index);
        }
        return value;
    }

    /**
     * Reads the double value from the Json Array for specified index
     * 
     * @param jsonArray The {@link JSONArray} to read the index from
     * @param index The key to read
     * @param required Whether the index is required. If <code>true</code>,
     *            attempting to read it when it is <code>null</code> will throw
     *            a {@link JSONException}
     * @param notNull Whether the value is allowed to be <code>null</code>. If
     *            <code>true</code>, will throw a {@link JSONException} if the
     *            value is null
     * @return The read value
     * @throws JSONException If the double was unable to be read, or if the
     *             required/notNull flags were violated
     */
    public static double readDouble(final JSONArray jsonArray, final int index,
                    final boolean required, final boolean notNull)
                    throws JSONException {

        if (required) {
            //Will throw JsonException if mapping doesn't exist
            return jsonArray.getDouble(index);
        }

        if (notNull && jsonArray.isNull(index)) {
            //throw JsonException because key is null
            throw new JSONException(String.format(Locale.US, NULL_VALUE_FORMAT_ARRAY, index));
        }
        double value = 0.0;
        if (!jsonArray.isNull(index)) {
            value = jsonArray.getDouble(index);
        }
        return value;
    }

    /**
     * Reads the float value from the Json Array for specified index
     * 
     * @param jsonArray The {@link JSONArray} to read the index from
     * @param index The key to read
     * @param required Whether the index is required. If <code>true</code>,
     *            attempting to read it when it is <code>null</code> will throw
     *            a {@link JSONException}
     * @param notNull Whether the value is allowed to be <code>null</code>. If
     *            <code>true</code>, will throw a {@link JSONException} if the
     *            value is null
     * @return The read value
     * @throws JSONException If the float was unable to be read, or if the
     *             required/notNull flags were violated
     */
    public static float readFloat(final JSONArray jsonArray, final int index,
                    final boolean required, final boolean notNull)
                    throws JSONException {

        if (required) {
            //Will throw JsonException if mapping doesn't exist
            return (float) jsonArray.getDouble(index);
        }

        if (notNull && jsonArray.isNull(index)) {
            //throw JsonException because key is null
            throw new JSONException(String.format(Locale.US, NULL_VALUE_FORMAT_ARRAY, index));
        }
        float value = 0.0f;
        if (!jsonArray.isNull(index)) {
            value = (float) jsonArray.getDouble(index);
        }
        return value;
    }

    /**
     * Reads the long value from the Json Array for specified index
     * 
     * @param jsonArray The {@link JSONArray} to read the index from
     * @param index The key to read
     * @param required Whether the index is required. If <code>true</code>,
     *            attempting to read it when it is <code>null</code> will throw
     *            a {@link JSONException}
     * @param notNull Whether the value is allowed to be <code>null</code>. If
     *            <code>true</code>, will throw a {@link JSONException} if the
     *            value is null
     * @return The read value
     * @throws JSONException If the long was unable to be read, or if the
     *             required/notNull flags were violated
     */
    public static long readLong(final JSONArray jsonArray, final int index,
                    final boolean required, final boolean notNull)
                    throws JSONException {

        if (required) {
            //Will throw JsonException if mapping doesn't exist
            return jsonArray.getLong(index);
        }

        if (notNull && jsonArray.isNull(index)) {
            //throw JsonException because key is null
            throw new JSONException(String.format(Locale.US, NULL_VALUE_FORMAT_ARRAY, index));
        }
        long value = 0l;
        if (!jsonArray.isNull(index)) {
            value = jsonArray.getLong(index);
        }
        return value;
    }

}
