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

/**
 * Class that reads an API response and parses it and stores it in the database
 * 
 * @author Vinay S Shenoy
 */
public class HttpResponseParser {

    /**
     * Parses the string response for a particular {@linkplain RequestId} and
     * returns the response data
     * 
     * @param requestId
     * @param response
     * @return
     * @throws JSONException
     */
    public ResponseInfo getSuccessResponse(int requestId, String response)
                    throws JSONException {

        switch (requestId) {

            case RequestId.CREATE_BOOK: {
                return parseCreateBookResponse(response);
            }

            case RequestId.GET_BOOK_INFO: {
                return parseGetBookInfoResponse(response);
            }

            case RequestId.SEARCH_BOOKS: {
                return parseGetSearchBooksResponse(response);
            }

            default: {
                throw new IllegalArgumentException("Unknown request Id:"
                                + requestId);
            }
        }
    }

    /**
     * @param response
     * @return
     * @throws JSONException If the Json resposne is malformed
     */
    private ResponseInfo parseGetSearchBooksResponse(String response)
                    throws JSONException {

        final ResponseInfo responseInfo = new ResponseInfo();

        final JSONObject responseObject = new JSONObject(response);
        final JSONArray booksArray = JsonUtils
                        .getJsonArray(responseObject, HttpConstants.BOOKS);

        JSONObject bookObject = null;
        ContentValues values = new ContentValues();
        final String selection = DatabaseColumns.BOOK_ID
                        + SQLConstants.EQUALS_ARG;
        final String[] args = new String[1];
        for (int i = 0; i < booksArray.length(); i++) {
            bookObject = JsonUtils.getJsonObject(booksArray, i);
            args[0] = readBookDetailsIntoContentValues(bookObject, values, true);

            //First try to update the table if a book already exists
            if (DBUtils.update(TableSearchBooks.NAME, values, selection, args) == 0) {

                // Unable to update, insert the item
                DBUtils.insert(TableSearchBooks.NAME, null, values);
            }
            DBUtils.notifyChange(TableSearchBooks.NAME);
        }
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
}
