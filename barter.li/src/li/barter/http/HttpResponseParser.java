/**
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
 */

package li.barter.http;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.os.Bundle;
import android.text.TextUtils;

import li.barter.data.DBInterface;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.TableLocations;
import li.barter.data.TableSearchBooks;
import li.barter.http.HttpConstants.RequestId;
import li.barter.utils.Logger;

/**
 * Class that reads an API response and parses it and stores it in the database
 * 
 * @author Vinay S Shenoy
 */
public class HttpResponseParser {

    private static final String TAG = "HttpResponseParser";

    /**
     * Parses the string response(when the request was successful -
     * {@linkplain HttpStatus#SC_OK} was returned) for a particular
     * {@linkplain RequestId} and returns the response data
     * 
     * @param requestId
     * @param response
     * @return
     * @throws JSONException
     */
    public ResponseInfo getSuccessResponse(int requestId, String response)
                    throws JSONException {

        Logger.d(TAG, "Request Id %d\nResponse %s", requestId, response);
        switch (requestId) {

            case RequestId.CREATE_BOOK: {
                return parseCreateBookResponse(response);
            }

            case RequestId.GET_BOOK_INFO: {
                return parseGetBookInfoResponse(response);
            }

            case RequestId.SEARCH_BOOKS: {
                //Delete the current search results before parsing the old ones
                DBInterface.delete(TableSearchBooks.NAME, null, null, true);
                return parseSearchBooksResponse(response);
            }

            case RequestId.CREATE_USER: {
                return parseCreateUserResponse(response);
            }

            case RequestId.HANGOUTS: {
                return parseHangoutsResponse(response);
            }

            default: {
                throw new IllegalArgumentException("Unknown request Id:"
                                + requestId);
            }
        }
    }

    /**
     * Method for parsing the create user/login response
     * 
     * @param response
     * @return
     * @throws JSONException If the Json response is malformed
     */
    private ResponseInfo parseCreateUserResponse(String response)
                    throws JSONException {

        final ResponseInfo responseInfo = new ResponseInfo();

        final JSONObject responseObject = new JSONObject(response);

        final JSONObject userObject = JsonUtils
                        .readJSONObject(responseObject, HttpConstants.USER);

        final Bundle responseBundle = new Bundle();
        responseBundle.putString(HttpConstants.ID, JsonUtils
                        .readString(userObject, HttpConstants.ID));
        responseBundle.putString(HttpConstants.AUTH_TOKEN, JsonUtils
                        .readString(userObject, HttpConstants.AUTH_TOKEN));
        responseBundle.putString(HttpConstants.EMAIL, JsonUtils
                        .readString(userObject, HttpConstants.EMAIL));
        responseBundle.putString(HttpConstants.DESCRIPTION, JsonUtils
                        .readString(userObject, HttpConstants.DESCRIPTION));
        responseBundle.putString(HttpConstants.FIRST_NAME, JsonUtils
                        .readString(userObject, HttpConstants.FIRST_NAME));
        responseBundle.putString(HttpConstants.LAST_NAME, JsonUtils
                        .readString(userObject, HttpConstants.LAST_NAME));

        final JSONObject locationObject = JsonUtils
                        .readJSONObject(userObject, HttpConstants.LOCATION);

        String locationId = null;
        if (locationObject != null) {
            locationId = parseAndStoreLocation(locationObject);
        }

        responseBundle.putString(HttpConstants.LOCATION, locationId); //Would like to use location id, but server just sends id for location
        responseInfo.responseBundle = responseBundle;
        return responseInfo;
    }

    /**
     * Reads out a Location object from Json, stores it the DB and returns the
     * location id
     * 
     * @param locationObject The Location object
     * @return The id of the parsed location
     */
    private String parseAndStoreLocation(JSONObject locationObject) {

        final ContentValues values = new ContentValues();
        final String locationId = readLocationDetailsIntoContentValues(locationObject, values, true);
        final String selection = DatabaseColumns.LOCATION_ID
                        + SQLConstants.EQUALS_ARG;
        final String[] args = new String[] {
            locationId
        };

        //Update the locations table if the location already exists
        if (DBInterface.update(TableLocations.NAME, values, selection, args, true) == 0) {

            //Location was not present, insert into locations table
            DBInterface.insert(TableLocations.NAME, null, values, true);
        }

        return locationId;
    }

    /**
     * Method for parsing the search results
     * 
     * @param response
     * @return
     * @throws JSONException If the Json resposne is malformed
     */
    private ResponseInfo parseSearchBooksResponse(String response)
                    throws JSONException {

        final ResponseInfo responseInfo = new ResponseInfo();

        final JSONObject responseObject = new JSONObject(response);
        final JSONArray searchResults = JsonUtils
                        .readJSONArray(responseObject, HttpConstants.SEARCH);

        JSONObject bookObject = null;
        ContentValues values = new ContentValues();
        final String selection = DatabaseColumns.BOOK_ID
                        + SQLConstants.EQUALS_ARG;
        final String[] args = new String[1];
        for (int i = 0; i < searchResults.length(); i++) {
            bookObject = JsonUtils.readJSONObject(searchResults, i);
            args[0] = readBookDetailsIntoContentValues(bookObject, values, true);

            //First try to update the table if a book already exists
            if (DBInterface.update(TableSearchBooks.NAME, values, selection, args, true) == 0) {

                // Unable to update, insert the item
                DBInterface.insert(TableSearchBooks.NAME, null, values, true);
            }
        }
        return responseInfo;
    }

    /**
     * Method for parsing the hangouts response
     * 
     * @param response The Json string response
     * @return
     * @throws JSONException if the Json string is invalid
     */
    private ResponseInfo parseHangoutsResponse(String response)
                    throws JSONException {

        final ResponseInfo responseInfo = new ResponseInfo();
        
        final JSONArray hangoutsArray = new JSONArray(response);
        return responseInfo;
    }

    /**
     * Reads the book details from the Book response json into a content values
     * object
     * 
     * @param bookObject The Json representation of a book search result
     * @param values The values instance to read into
     * @param clearBeforeAdd Whether the values should be emptied before adding
     * @return The book Id that was parsed
     */
    private String readBookDetailsIntoContentValues(JSONObject bookObject,
                    ContentValues values, boolean clearBeforeAdd) {

        if (clearBeforeAdd) {
            values.clear();
        }

        final String bookId = JsonUtils
                        .readString(bookObject, HttpConstants.ID);

        if (TextUtils.isEmpty(bookId)) {
            throw new IllegalArgumentException("Not a valid book json:"
                            + bookObject.toString());
        }
        values.put(DatabaseColumns.BOOK_ID, bookId);
        values.put(DatabaseColumns.ISBN_10, JsonUtils
                        .readString(bookObject, HttpConstants.ISBN_10));
        values.put(DatabaseColumns.ISBN_13, JsonUtils
                        .readString(bookObject, HttpConstants.ISBN_13));
        values.put(DatabaseColumns.AUTHOR, JsonUtils
                        .readString(bookObject, HttpConstants.AUTHOR));
        values.put(DatabaseColumns.BARTER_TYPE, JsonUtils
                        .readString(bookObject, HttpConstants.BARTER_TYPE));
        values.put(DatabaseColumns.USER_ID, JsonUtils
                        .readString(bookObject, HttpConstants.USER_ID));
        values.put(DatabaseColumns.TITLE, JsonUtils
                        .readString(bookObject, HttpConstants.TITLE));
        values.put(DatabaseColumns.DESCRIPTION, JsonUtils
                        .readString(bookObject, HttpConstants.DESCRIPTION));
        values.put(DatabaseColumns.IMAGE_URL, JsonUtils
                        .readString(bookObject, HttpConstants.IMAGE_URL));

        final JSONObject locationObject = JsonUtils
                        .readJSONObject(bookObject, HttpConstants.LOCATION);

        if (locationObject != null) {
            values.put(DatabaseColumns.LOCATION_ID, parseAndStoreLocation(locationObject));
        }
        return bookId;
    }

    /**
     * Reads the location details from the Location response json into a content
     * values object
     * 
     * @param locationObject The Json representation of a location
     * @param values The values instance to read into
     * @param clearBeforeAdd Whether the values should be emptied before adding
     * @return The location Id that was parsed
     */
    private String readLocationDetailsIntoContentValues(
                    JSONObject locationObject, ContentValues values,
                    boolean clearBeforeAdd) {

        if (clearBeforeAdd) {
            values.clear();
        }

        final String locationId = JsonUtils
                        .readString(locationObject, HttpConstants.ID);
        if (TextUtils.isEmpty(locationId)) {
            throw new IllegalArgumentException("Not a valid location json:"
                            + locationObject.toString());
        }
        values.put(DatabaseColumns.LOCATION_ID, locationId);
        values.put(DatabaseColumns.NAME, JsonUtils
                        .readString(locationObject, HttpConstants.NAME));
        values.put(DatabaseColumns.ADDRESS, JsonUtils
                        .readString(locationObject, HttpConstants.ADDRESS));
        values.put(DatabaseColumns.POSTAL_CODE, JsonUtils
                        .readString(locationObject, HttpConstants.POSTAL_CODE));
        values.put(DatabaseColumns.LOCALITY, JsonUtils
                        .readString(locationObject, HttpConstants.LOCALITY));
        values.put(DatabaseColumns.CITY, JsonUtils
                        .readString(locationObject, HttpConstants.CITY));
        values.put(DatabaseColumns.STATE, JsonUtils
                        .readString(locationObject, HttpConstants.STATE));
        values.put(DatabaseColumns.COUNTRY, JsonUtils
                        .readString(locationObject, HttpConstants.COUNTRY));
        values.put(DatabaseColumns.LATITUDE, JsonUtils
                        .readDouble(locationObject, HttpConstants.LATITUDE));
        values.put(DatabaseColumns.LONGITUDE, JsonUtils
                        .readDouble(locationObject, HttpConstants.LONGITUDE));
        return locationId;
    }

    /**
     * @param response
     * @return
     */
    private ResponseInfo parseGetBookInfoResponse(String response) {
        // TODO Parse get book info response
        return new ResponseInfo();
    }

    /**
     * @param response
     * @return
     */
    private ResponseInfo parseCreateBookResponse(String response) {
        // TODO Parse get create book response
        return new ResponseInfo();
    }

    /**
     * Parses the string response(when the request was unsuccessful -
     * {@linkplain HttpStatus#SC_BAD_REQUEST} was returned) for a particular
     * {@linkplain RequestId} and returns the response data
     * 
     * @param requestId The {@linkplain RequestId} for the request
     * @param response The response from the server
     * @return a {@linkplain ResponseInfo} object representing the response
     * @throws JSONException If the response was an invalid json
     */
    public ResponseInfo getErrorResponse(int requestId, String response)
                    throws JSONException {

        Logger.d(TAG, "Request Id %d\nResponse %s", requestId, response);
        final ResponseInfo responseInfo = parseErrorResponse(requestId, response);
        return responseInfo;
    }

    /**
     * Do the actual parsing of error response here
     * 
     * @param requestId The {@linkplain RequestId} for the request
     * @param response The Json response from server
     * @return a {@linkplain ResponseInfo} object representing the response
     * @throws JSONException If the response was invalid json
     */
    private ResponseInfo parseErrorResponse(final int requestId, String response)
                    throws JSONException {

        ResponseInfo responseInfo = new ResponseInfo(false);
        JSONObject errorObject = new JSONObject(response);

        final int errorCode = JsonUtils
                        .readInt(errorObject, HttpConstants.ERROR_CODE);
        responseInfo.errorCode = errorCode;
        //Parse error response specific to any request here
        return responseInfo;

    }

}
