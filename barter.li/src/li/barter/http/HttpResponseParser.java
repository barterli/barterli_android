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
import android.text.TextUtils;

import li.barter.data.DBUtils;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
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
                if(DBUtils.delete(TableSearchBooks.NAME, null, null) > 0) {
                    DBUtils.notifyChange(TableSearchBooks.NAME);
                }
                return parseSearchBooksResponse(response);
            }

            case RequestId.CREATE_USER: {
                return parseCreateUserResponse(response);
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

        final String authToken = JsonUtils
                        .getStringValue(responseObject, HttpConstants.AUTH_TOKEN);
        Logger.d(TAG, "On Login: %s", authToken);
        return responseInfo;
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
                        .getJsonArray(responseObject, HttpConstants.SEARCH);

        JSONObject bookObject = null;
        ContentValues values = new ContentValues();
        final String selection = DatabaseColumns.BOOK_ID
                        + SQLConstants.EQUALS_ARG;
        final String[] args = new String[1];
        for (int i = 0; i < searchResults.length(); i++) {
            bookObject = JsonUtils.getJsonObject(searchResults, i);
            args[0] = readBookDetailsIntoContentValues(bookObject, values, true);

            //First try to update the table if a book already exists
            if (DBUtils.update(TableSearchBooks.NAME, values, selection, args) == 0) {

                // Unable to update, insert the item
                DBUtils.insert(TableSearchBooks.NAME, null, values);
            }
        }
        DBUtils.notifyChange(TableSearchBooks.NAME);
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
                        .getStringValue(bookObject, HttpConstants.ID);

        if (TextUtils.isEmpty(bookId)) {
            throw new IllegalArgumentException("Not a valid book json:"
                            + bookObject.toString());
        }
        values.put(DatabaseColumns.BOOK_ID, bookId);
        values.put(DatabaseColumns.ISBN_10, JsonUtils
                        .getStringValue(bookObject, HttpConstants.ISBN_10));
        values.put(DatabaseColumns.ISBN_13, JsonUtils
                        .getStringValue(bookObject, HttpConstants.ISBN_13));
        values.put(DatabaseColumns.AUTHOR, JsonUtils
                        .getStringValue(bookObject, HttpConstants.AUTHOR));
        values.put(DatabaseColumns.BARTER_TYPE, JsonUtils
                        .getStringValue(bookObject, HttpConstants.BARTER_TYPE));
        values.put(DatabaseColumns.USER_ID, JsonUtils
                        .getStringValue(bookObject, HttpConstants.USER_ID));
        values.put(DatabaseColumns.TITLE, JsonUtils
                        .getStringValue(bookObject, HttpConstants.TITLE));
        values.put(DatabaseColumns.DESCRIPTION, JsonUtils
                        .getStringValue(bookObject, HttpConstants.DESCRIPTION));
        values.put(DatabaseColumns.IMAGE_URL, JsonUtils
                        .getStringValue(bookObject, HttpConstants.IMAGE_URL));
        return bookId;
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
                        .getIntValue(errorObject, HttpConstants.ERROR_CODE);
        responseInfo.errorCode = errorCode;
        //Parse error response specific to any request here
        return responseInfo;

    }

}
