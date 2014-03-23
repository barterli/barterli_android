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

package li.barter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONHelper {

    public String[] JsonStringofArraysToArray(final String jsonString) {
        JSONArray array;
        final String[] emptyArray = new String[0];
        try {
            array = new JSONArray(jsonString);
            final String[] normalArray = new String[array.length()];
            for (int i = 0; i < array.length(); i++) {
                normalArray[i] = array.getString(i);
            }
            return normalArray;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return emptyArray;
    }

    public String[] getBookNamesFromUserProfile(final String UserJSONString) {

        JSONObject userObject;
        try {
            userObject = new JSONObject(UserJSONString);
            if (!userObject.has("books")
                            || (userObject.getJSONArray("books").length() == 0)) {
                return new String[0];
            }
            final JSONArray booksArray = userObject.getJSONArray("books");
            final String[] bookTitleArray = new String[booksArray.length()];
            for (int i = 0; i < booksArray.length(); i++) {
                bookTitleArray[i] = booksArray.getJSONObject(i)
                                .getString("title");
            }
            return bookTitleArray;
        } catch (final JSONException e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    public String[] getBookIDsFromUserProfile(final String UserJSONString) {

        JSONObject userObject;
        try {
            userObject = new JSONObject(UserJSONString);
            if (!userObject.has("books")
                            || (userObject.getJSONArray("books").length() == 0)) {
                return new String[0];
            }
            final JSONArray booksArray = userObject.getJSONArray("books");
            final String[] bookIDsArray = new String[booksArray.length()];
            for (int i = 0; i < booksArray.length(); i++) {
                bookIDsArray[i] = booksArray.getJSONObject(i).getString("id");
            }
            return bookIDsArray;
        } catch (final JSONException e) {
            e.printStackTrace();
            return new String[0];
        }
    }

    public JSONArray getBookObjectsFromUserProfile(final String UserJSONString) {

        JSONObject userObject;
        try {
            userObject = new JSONObject(UserJSONString);
            if (!userObject.has("books")
                            || (userObject.getJSONArray("books").length() == 0)) {
                return new JSONArray();
            }
            final JSONArray booksArray = userObject.getJSONArray("books");
            final JSONArray BooksJSONArray = new JSONArray();
            for (int i = 0; i < booksArray.length(); i++) {
                final JSONObject _obj = new JSONObject();
                _obj.put("id", booksArray.getJSONObject(i).opt("id"));
                _obj.put("title", booksArray.getJSONObject(i).opt("title"));
                _obj.put("author", booksArray.getJSONObject(i).opt("author"));
                _obj.put("description", booksArray.getJSONObject(i)
                                .opt("description"));
                _obj.put("publication_year", booksArray.getJSONObject(i)
                                .opt("publication_year"));
                _obj.put("barter_type", booksArray.getJSONObject(i)
                                .opt("barter_type"));
                BooksJSONArray.put(_obj);
                // bookTitleArray[i] =
                // booksArray.getJSONObject(i).getString("title");
            }
            return BooksJSONArray;
        } catch (final JSONException e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

}
