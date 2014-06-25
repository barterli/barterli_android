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
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentValues;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;

import li.barter.data.DBInterface;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.TableLocations;
import li.barter.data.TableSearchBooks;
import li.barter.data.TableUserBooks;
import li.barter.data.TableUsers;
import li.barter.http.HttpConstants.GoogleBookSearchKey;
import li.barter.http.HttpConstants.RequestId;
import li.barter.models.Team;
import li.barter.models.Venue;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.DeviceInfo;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.UserInfo;
import li.barter.utils.Logger;
import li.barter.widgets.autocomplete.Suggestion;

/**
 * Class that reads an API response and parses it and stores it in the database
 * 
 * @author Vinay S Shenoy
 */
public class HttpResponseParser {

    /**
     * It stores the latitude of the book response which we use for adding the
     * book by calculating the distance between current location and the
     * preferred location
     */
    private double              mEndLatitude;

    /**
     * It stores the longitude of the book response which we use for adding the
     * book by calculating the distance between current location and the
     * preferred location
     */
    private double              mEndLongitude;

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
     * @throws XmlPullParserException
     * @throws IOException
     */
    public ResponseInfo getSuccessResponse(final int requestId,
                    final String response) throws JSONException,
                    XmlPullParserException, IOException {

        Logger.d(TAG, "Request Id %d\nResponse %s", requestId, response);
        switch (requestId) {

            case RequestId.CREATE_BOOK: {
                return parseCreateBookResponse(response);
            }

            case RequestId.GET_BOOK_INFO: {
                return parseGoogleBooksBookInfo(response);
            }

            case RequestId.SEARCH_BOOKS: {
                return parseSearchBooksResponse(response);
            }

            case RequestId.BLOCK_CHATS: {
                return parseBlockUserResponse(response);
            }

            case RequestId.PASSWORD_RESET: {
                return parsePasswordResetResponse(response);
            }

            case RequestId.CREATE_USER: {
                return parseCreateUserResponse(response);
            }

            case RequestId.SAVE_USER_PROFILE: {
                return parseCreateUserResponse(response);
            }

            case RequestId.FOURSQUARE_VENUES: {
                return parseVenuesResponse(response);
            }

            case RequestId.FOURSQUARE_VENUES_WITHOUT_CATEGORIES: {
                return parseVenuesResponse(response);
            }

            case RequestId.REPORT_BUG: {
                return parseReportBugOrSuggestFeatureResponse(response);
            }

            case RequestId.SUGGEST_FEATURE: {
                return parseReportBugOrSuggestFeatureResponse(response);
            }

            case RequestId.COLLABORATE_REQUEST: {
                return parseReportBugOrSuggestFeatureResponse(response);
            }

            case RequestId.SET_USER_PREFERRED_LOCATION: {
                return parseSetUserPreferredLocationResponse(response);
            }

            case RequestId.AMPQ: {
                return parseAmpqResponse(response);
            }

            case RequestId.TRIBUTE: {
                return parseTributeResponse(response);
            }

            case RequestId.TEAM: {
                return parseTeamResponse(response);
            }

            case RequestId.GET_USER_PROFILE: {
                return parseUserProfileResponse(response);
            }

            case RequestId.BOOK_SUGGESTIONS: {
                return parseGoogleBookSuggestionsResponse(response);
            }

            case RequestId.UPDATE_BOOK: {
                return parseUpdateBookResponse(response);
            }

            case RequestId.DELETE_BOOK: {
                return parseDeleteBookResponse(response);
            }

            case RequestId.GOOGLEBOOKS_SHOW_BOOK: {
                return parseGoogleBooksBookInfo(response);
            }

            case RequestId.REFERRAL: {
                return parseReferralResponse(response);
            }

            default: {
                throw new IllegalArgumentException("Unknown request Id:"
                                + requestId);
            }
        }
    }

    /**
     * Parse the response for referral inform API
     * 
     * @param response
     * @return
     */
    private ResponseInfo parseReferralResponse(String response) {
        return new ResponseInfo();
    }

    /**
     * @param parsing google book api response
     * @return
     */
    private ResponseInfo parseGoogleBookSuggestionsResponse(
                    final String response) throws JSONException {

        final ResponseInfo responseInfo = new ResponseInfo();
        final Bundle responseBundle = new Bundle(1);

        final JSONObject bookInfoObject = new JSONObject(response);
        final JSONArray searchResults = JsonUtils
                        .readJSONArray(bookInfoObject, HttpConstants.ITEMS, false, false);
        Suggestion[] suggestion = new Suggestion[searchResults.length()];
        for (int i = 0; i < searchResults.length(); i++) {
            String id, name, imageUrl;

            JSONObject bookInfo = JsonUtils
                            .readJSONObject(searchResults, i, false, false);

            id = JsonUtils.readString(bookInfo, HttpConstants.ID, false, false);

            JSONObject volumeInfo = JsonUtils
                            .readJSONObject(bookInfo, HttpConstants.VOLUMEINFO, false, false);
            name = JsonUtils.readString(volumeInfo, HttpConstants.TITLE, false, false);

            try {
                JSONObject imageLinks = JsonUtils
                                .readJSONObject(volumeInfo, HttpConstants.IMAGELINKS, false, false);
                imageUrl = JsonUtils
                                .readString(imageLinks, HttpConstants.THUMBNAIL, false, false);
            } catch (Exception e) {
                imageUrl = "";
            }

            suggestion[i] = new Suggestion();
            suggestion[i].id = id;
            suggestion[i].name = name;
            suggestion[i].imageUrl = imageUrl;

        }
        responseBundle.putParcelableArray(HttpConstants.RESULTS, suggestion);
        responseInfo.responseBundle = responseBundle;
        return responseInfo;
    }

    /**
     * Parses the good reads API response for fetching a book
     * 
     * @param response
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private ResponseInfo parseGoogleBooksBookInfo(String response)
                    throws JSONException {

        final ResponseInfo responseInfo = new ResponseInfo();
        Logger.d(TAG, "Request Id Test \nResponse %s", response);

        final Bundle responseBundle = new Bundle(1);

        final JSONObject bookInfoObject = new JSONObject(response);
        final JSONArray searchResults = JsonUtils
                        .readJSONArray(bookInfoObject, HttpConstants.ITEMS, true, true);
        for (int i = 0; i < searchResults.length(); i++) {

            JSONObject bookInfo = JsonUtils
                            .readJSONObject(searchResults, i, false, false);

            JSONObject volumeInfo = JsonUtils
                            .readJSONObject(bookInfo, HttpConstants.VOLUMEINFO, false, false);
            responseBundle.putString(HttpConstants.TITLE, JsonUtils
                            .readString(volumeInfo, HttpConstants.TITLE, false, false));
            JSONArray authors = JsonUtils
                            .readJSONArray(volumeInfo, HttpConstants.AUTHORS, false, false);
            ArrayList<String> authorName = new ArrayList<String>();
            if (authors.length() == 1) {
                authorName.add(authors.get(0).toString());

            } else {
                for (int k = 0; k < authors.length(); k++) {

                    authorName.add(authors.get(k).toString());

                }
            }
            String author = TextUtils.join(", ", authorName);
            responseBundle.putString(HttpConstants.AUTHOR, author);

            responseBundle.putString(HttpConstants.PUBLICATION_YEAR, JsonUtils
                            .readString(volumeInfo, HttpConstants.PUBLISHED_DATE, false, false));

            responseBundle.putString(HttpConstants.DESCRIPTION, JsonUtils
                            .readString(volumeInfo, HttpConstants.DESCRIPTION, false, false));

            JSONArray identifiers = JsonUtils
                            .readJSONArray(volumeInfo, HttpConstants.INDUSTRY_IDENTIFIERS, false, false);
            JSONObject identifierObject = null;
            //TODO might change the true flag
            if (identifiers != null) {
                for (int j = 0; j < identifiers.length(); j++) {
                    identifierObject = JsonUtils
                                    .readJSONObject(identifiers, j, true, true);
                    final String type = JsonUtils
                                    .readString(identifierObject, HttpConstants.TYPE, true, true);

                    if (type.equals(GoogleBookSearchKey.ISBN_13)) {
                        responseBundle.putString(HttpConstants.ISBN_13, JsonUtils
                                        .readString(identifierObject, HttpConstants.IDENTIFIER, true, true));
                    } else if (type.equals(GoogleBookSearchKey.ISBN_10)) {
                        responseBundle.putString(HttpConstants.ISBN_10, JsonUtils
                                        .readString(identifierObject, HttpConstants.IDENTIFIER, true, true));
                    }
                }
            }

            JSONObject imageLinks = null;

            try {
                imageLinks = JsonUtils
                                .readJSONObject(volumeInfo, HttpConstants.IMAGELINKS, false, false);
                responseBundle.putString(HttpConstants.IMAGE_URL, JsonUtils
                                .readString(imageLinks, HttpConstants.THUMBNAIL, false, false));
            } catch (Exception e) {
                responseBundle.putString(HttpConstants.IMAGE_URL, "");
            }

        }
        responseInfo.responseBundle = responseBundle;
        return responseInfo;
    }

    /**
     * Parse the response for Ampq
     * 
     * @param response The response from server
     * @return
     */
    private ResponseInfo parseAmpqResponse(final String response) {
        return new ResponseInfo();
    }

    /**
     * Parse the response for Tribute
     * 
     * @param response The response from server
     * @return
     */
    private ResponseInfo parseTributeResponse(final String response)
                    throws JSONException {
        final ResponseInfo responseInfo = new ResponseInfo();

        final JSONObject responseObject = new JSONObject(response);
        final JSONObject tributeObject = JsonUtils
                        .readJSONObject(responseObject, HttpConstants.TRIBUTE, true, true);
        final Bundle responseBundle = new Bundle();
        responseBundle.putString(HttpConstants.TRIBUTE_IMAGE_URL, JsonUtils
                        .readString(tributeObject, HttpConstants.TRIBUTE_IMAGE_URL, false, false));
        responseBundle.putString(HttpConstants.TRIBUTE_TEXT, JsonUtils
                        .readString(tributeObject, HttpConstants.TRIBUTE_TEXT, false, false));
        responseInfo.responseBundle = responseBundle;
        return responseInfo;
    }

    /**
     * Parse the response for Tribute
     * 
     * @param response The response from server
     * @return
     */
    private ResponseInfo parseUserProfileResponse(final String response)
                    throws JSONException {

        Logger.d(TAG, "Request \nResponse %s", response);
        final ResponseInfo responseInfo = new ResponseInfo();

        final JSONObject responseObject = new JSONObject(response);

        final JSONObject userObject = JsonUtils
                        .readJSONObject(responseObject, HttpConstants.USER_PROFILE, true, true);

        final Bundle responseBundle = new Bundle();
        responseBundle.putString(HttpConstants.ID_USER, JsonUtils
                        .readString(userObject, HttpConstants.ID_USER, true, true));
        responseBundle.putString(HttpConstants.DESCRIPTION, JsonUtils
                        .readString(userObject, HttpConstants.DESCRIPTION, false, false));
        responseBundle.putString(HttpConstants.FIRST_NAME, JsonUtils
                        .readString(userObject, HttpConstants.FIRST_NAME, false, false));
        responseBundle.putString(HttpConstants.LAST_NAME, JsonUtils
                        .readString(userObject, HttpConstants.LAST_NAME, false, false));
        responseBundle.putString(HttpConstants.IMAGE_URL, JsonUtils
                        .readString(userObject, HttpConstants.IMAGE_URL, false, false));
        responseBundle.putString(HttpConstants.REFERRAL_COUNT, JsonUtils
                        .readString(userObject, HttpConstants.REFERRAL_COUNT, false, false));

        final JSONArray booksArray = JsonUtils
                        .readJSONArray(userObject, HttpConstants.BOOKS, true, true);

        JSONObject bookObject = null;
        final ContentValues values = new ContentValues();
        final String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
        final String[] args = new String[1];
        // DBInterface.delete(TableUserBooks.NAME, null, null, true);
        for (int i = 0; i < booksArray.length(); i++) {
            bookObject = JsonUtils.readJSONObject(booksArray, i, true, true);
            args[0] = readBookDetailsIntoContentValues(bookObject, values, true, false);

            //First try to update the table if a book already exists
            if (DBInterface.update(TableUserBooks.NAME, values, selection, args, false) == 0) {

                // Unable to update, insert the item
                DBInterface.insert(TableUserBooks.NAME, null, values, false);
            }
        }
        final ContentValues userValues = new ContentValues();
        final String selectionUser = DatabaseColumns.USER_ID
                        + SQLConstants.EQUALS_ARG;
        final String[] argsUser = new String[1];
        // DBInterface.delete(TableUserBooks.NAME, null, null, true);

        argsUser[0] = readUserDetailsIntoContentValues(userObject, userValues, true, false);

        //First try to update the table if a book already exists
        if (DBInterface.update(TableUsers.NAME, userValues, selectionUser, argsUser, false) == 0) {

            // Unable to update, insert the item
            DBInterface.insert(TableUsers.NAME, null, userValues, false);

        }

        DBInterface.notifyChange(TableUserBooks.NAME);
        DBInterface.notifyChange(TableUsers.NAME);

        final JSONObject locationObject = JsonUtils
                        .readJSONObject(userObject, HttpConstants.LOCATION, false, false);

        responseBundle.putString(HttpConstants.ADDRESS, JsonUtils
                        .readString(locationObject, HttpConstants.ADDRESS, false, false));
        responseInfo.responseBundle = responseBundle;
        return responseInfo;
    }

    /**
     * Parse the response for Team
     * 
     * @param response The response from server
     * @return
     */
    private ResponseInfo parseTeamResponse(final String response)
                    throws JSONException {
        final ResponseInfo responseInfo = new ResponseInfo();
        final JSONObject responseObject = new JSONObject(response);
        final JSONArray teamResults = JsonUtils
                        .readJSONArray(responseObject, HttpConstants.TEAM, true, true);
        final Team[] teamArray = new Team[teamResults.length()];
        JSONObject teamObject = null;
        for (int i = 0; i < teamArray.length; i++) {
            teamObject = JsonUtils.readJSONObject(teamResults, i, true, true);
            Logger.e(TAG, teamObject.toString());
            teamArray[i] = new Team();
            readTeamObjectIntoTeam(teamObject, teamArray[i]);
        }
        final Bundle responseBundle = new Bundle(1);
        responseBundle.putParcelableArray(HttpConstants.TEAM, teamArray);
        responseInfo.responseBundle = responseBundle;
        return responseInfo;
    }

    /**
     * Method for parsing the create user/login response
     * 
     * @param response
     * @return
     * @throws JSONException If the Json response is malformed
     */
    private ResponseInfo parseCreateUserResponse(final String response)
                    throws JSONException {

        final ResponseInfo responseInfo = new ResponseInfo();

        final JSONObject responseObject = new JSONObject(response);

        final JSONObject userObject = JsonUtils
                        .readJSONObject(responseObject, HttpConstants.USER, true, true);

        final Bundle responseBundle = new Bundle();

        final String userId = JsonUtils
                        .readString(userObject, HttpConstants.ID_USER, true, true);
        final String firstName = JsonUtils
                        .readString(userObject, HttpConstants.FIRST_NAME, false, false);
        final String lastName = JsonUtils
                        .readString(userObject, HttpConstants.LAST_NAME, false, false);
        final String imageUrl = JsonUtils
                        .readString(userObject, HttpConstants.IMAGE_URL, false, false);
        final String description = JsonUtils
                        .readString(userObject, HttpConstants.DESCRIPTION, false, false);

        UserInfo.INSTANCE.setFirstName(firstName);
        responseBundle.putString(HttpConstants.ID_USER, userId);
        responseBundle.putString(HttpConstants.AUTH_TOKEN, JsonUtils
                        .readString(userObject, HttpConstants.AUTH_TOKEN, true, true));
        responseBundle.putString(HttpConstants.EMAIL, JsonUtils
                        .readString(userObject, HttpConstants.EMAIL, true, true));
        responseBundle.putString(HttpConstants.DESCRIPTION, description);
        responseBundle.putString(HttpConstants.FIRST_NAME, firstName);
        responseBundle.putString(HttpConstants.LAST_NAME, lastName);
        responseBundle.putString(HttpConstants.IMAGE_URL, imageUrl);
        responseBundle.putString(HttpConstants.SHARE_TOKEN, JsonUtils
                        .readString(userObject, HttpConstants.SHARE_TOKEN, true, true));
        responseBundle.putString(HttpConstants.REFERRAL_COUNT, JsonUtils
                        .readString(userObject, HttpConstants.REFERRAL_COUNT, false, true));

        final JSONArray booksArray = JsonUtils
                        .readJSONArray(userObject, HttpConstants.BOOKS, true, true);

        JSONObject bookObject = null;
        final ContentValues values = new ContentValues();
        final String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
        final String[] args = new String[1];
        for (int i = 0; i < booksArray.length(); i++) {
            bookObject = JsonUtils.readJSONObject(booksArray, i, true, true);
            args[0] = readBookDetailsIntoContentValues(bookObject, values, true, false);

            //First try to update the table if a book already exists
            if (DBInterface.update(TableUserBooks.NAME, values, selection, args, false) == 0) {

                // Unable to update, insert the item
                DBInterface.insert(TableUserBooks.NAME, null, values, false);
            }
        }
        DBInterface.notifyChange(TableUserBooks.NAME);

        final JSONObject locationObject = JsonUtils
                        .readJSONObject(userObject, HttpConstants.LOCATION, false, false);

        String locationId = null;
        if (locationObject != null) {
            locationId = parseAndStoreLocation(locationObject, false);
        }

        responseBundle.putString(HttpConstants.LOCATION, locationId);
        responseInfo.responseBundle = responseBundle;

        //Save created user into Users table
        final ContentValues userValues = new ContentValues();
        userValues.put(DatabaseColumns.USER_ID, userId);
        userValues.put(DatabaseColumns.FIRST_NAME, firstName);
        userValues.put(DatabaseColumns.LAST_NAME, lastName);
        userValues.put(DatabaseColumns.DESCRIPTION, description);
        userValues.put(DatabaseColumns.PROFILE_PICTURE, imageUrl);
        userValues.put(DatabaseColumns.LOCATION_ID, locationId);

        if (DBInterface.update(TableUsers.NAME, userValues, DatabaseColumns.USER_ID
                        + SQLConstants.EQUALS_ARG, new String[] {
            userId
        }, true) == 0) {
            DBInterface.insert(TableUsers.NAME, null, userValues, true);
        }
        return responseInfo;
    }

    /**
     * Reads out a Location object from Json, stores it the DB and returns the
     * location id
     * 
     * @param locationObject The Location object
     * @param autoNotify <code>true</code> to automatically notify any connected
     *            loaders
     * @return The id of the parsed location
     * @throws JSONException If the Json is invalid
     */
    private String parseAndStoreLocation(final JSONObject locationObject,
                    final boolean autoNotify) throws JSONException {

        final ContentValues values = new ContentValues();
        final String locationId = readLocationDetailsIntoContentValues(locationObject, values, true);
        final String selection = DatabaseColumns.LOCATION_ID
                        + SQLConstants.EQUALS_ARG;
        final String[] args = new String[] {
            locationId
        };

        //Update the locations table if the location already exists
        if (DBInterface.update(TableLocations.NAME, values, selection, args, autoNotify) == 0) {

            //Location was not present, insert into locations table
            DBInterface.insert(TableLocations.NAME, null, values, autoNotify);
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
    private ResponseInfo parseSearchBooksResponse(final String response)
                    throws JSONException {

        final ResponseInfo responseInfo = new ResponseInfo();

        final JSONObject responseObject = new JSONObject(response);

        final JSONArray searchResults = JsonUtils
                        .readJSONArray(responseObject, HttpConstants.SEARCH, true, true);

        JSONObject bookObject = null;
        final ContentValues values = new ContentValues();
        final String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;
        final String[] args = new String[1];

        for (int i = 0; i < searchResults.length(); i++) {
            bookObject = JsonUtils
                            .readJSONObject(searchResults, i, false, false);
            args[0] = readBookDetailsIntoContentValues(bookObject, values, true, false);

            //First try to update the table if a book already exists
            if (DBInterface.update(TableSearchBooks.NAME, values, selection, args, false) == 0) {

                // Unable to update, insert the item
                DBInterface.insert(TableSearchBooks.NAME, null, values, false);
            }
        }
        DBInterface.notifyChange(TableSearchBooks.NAME);
        final Bundle responseBundle = new Bundle();
        if (searchResults.isNull(0)) {
            responseBundle.putBoolean(Keys.NO_BOOKS_FLAG_KEY, true);
        } else {
            responseBundle.putBoolean(Keys.NO_BOOKS_FLAG_KEY, false);
        }
        responseInfo.responseBundle = responseBundle;
        return responseInfo;
    }

    /**
     * Method for parsing the foursquare response
     * 
     * @param response The Json string response
     * @return
     * @throws JSONException if the Json string is invalid
     */
    private ResponseInfo parseVenuesResponse(final String response)
                    throws JSONException {

        final ResponseInfo responseInfo = new ResponseInfo();

        final JSONObject responseObject = JsonUtils
                        .readJSONObject(new JSONObject(response), HttpConstants.RESPONSE, true, true);
        final JSONArray venuesArray = JsonUtils
                        .readJSONArray(responseObject, HttpConstants.VENUES, true, true);

        final Venue[] venues = new Venue[venuesArray.length()];
        JSONObject venueObject = null;
        for (int i = 0; i < venues.length; i++) {
            venueObject = JsonUtils.readJSONObject(venuesArray, i, true, true);
            venues[i] = new Venue();
            readVenueObjectIntoVenue(venueObject, venues[i]);
        }

        final Bundle responseBundle = new Bundle(1);
        responseBundle.putParcelableArray(HttpConstants.LOCATIONS, venues);
        responseInfo.responseBundle = responseBundle;
        return responseInfo;
    }

    private ResponseInfo parseBlockUserResponse(final String response)
                    throws JSONException {

        final ResponseInfo responseInfo = new ResponseInfo();

        return responseInfo;
    }

    private ResponseInfo parsePasswordResetResponse(final String response)
                    throws JSONException {

        final ResponseInfo responseInfo = new ResponseInfo();

        return responseInfo;
    }

    /**
     * Parse the set user preferred location response
     * 
     * @param response The Json response representing the set location
     * @return
     * @throws JSONException If response is invalid json
     */
    private ResponseInfo parseSetUserPreferredLocationResponse(
                    final String response) throws JSONException {
        final ResponseInfo responseInfo = new ResponseInfo();

        final JSONObject responseObject = new JSONObject(response);
        final JSONObject locationObject = JsonUtils
                        .readJSONObject(responseObject, HttpConstants.LOCATION, true, true);

        final String locationId = parseAndStoreLocation(locationObject, true);
        final Bundle responseBundle = new Bundle(1);
        responseBundle.putString(HttpConstants.ID_LOCATION, locationId);
        responseInfo.responseBundle = responseBundle;
        return responseInfo;
    }

    /**
     * Reads a Venue {@link JSONObject} into a {@link Venue} model
     * 
     * @param venueObject The Json response representing a Foursquare Venue
     * @param venue The {@link Venue} model to write into
     * @throws JSONException If the Json is invalid
     */
    private void readVenueObjectIntoVenue(final JSONObject venueObject,
                    final Venue venue) throws JSONException {

        venue.foursquareId = JsonUtils
                        .readString(venueObject, HttpConstants.ID, true, true);
        venue.name = JsonUtils
                        .readString(venueObject, HttpConstants.NAME, true, true);
        final JSONObject locationObject = JsonUtils
                        .readJSONObject(venueObject, HttpConstants.LOCATION, true, true);
        venue.address = JsonUtils
                        .readString(locationObject, HttpConstants.ADDRESS, false, false);
        venue.latitude = JsonUtils
                        .readDouble(locationObject, HttpConstants.LAT, true, true);
        venue.longitude = JsonUtils
                        .readDouble(locationObject, HttpConstants.LNG, true, true);
        venue.distance = JsonUtils
                        .readInt(locationObject, HttpConstants.DISTANCE, false, false);
        venue.city = JsonUtils
                        .readString(locationObject, HttpConstants.CITY, false, false);
        venue.state = JsonUtils
                        .readString(locationObject, HttpConstants.STATE, false, false);
        venue.country = JsonUtils
                        .readString(locationObject, HttpConstants.COUNTRY, false, false);

    }

    /**
     * Reads a TEAM {@link JSONObject} into a {@link Team} model
     * 
     * @param teamObject The Json response representing a Team
     * @param team The {@link Team} model to write into
     * @throws JSONException If the Json is invalid
     */
    private void readTeamObjectIntoTeam(final JSONObject teamObject,
                    final Team team) throws JSONException {
        final String email = (JsonUtils
                        .readString(teamObject, HttpConstants.EMAIL, false, false));
        final String name = JsonUtils
                        .readString(teamObject, HttpConstants.NAME, true, true);
        final String description = JsonUtils
                        .readString(teamObject, HttpConstants.DESCRIPTION, false, false);
        final String imageUrl = JsonUtils
                        .readString(teamObject, HttpConstants.IMAGE_URL, false, false);
        team.setName(name);
        team.setEmail(email);
        team.setDescription(description);
        team.setImageUrl(imageUrl);
    }

    /**
     * Reads the book details from the Book response json into a content values
     * object
     * 
     * @param bookObject The Json representation of a book search result
     * @param values The values instance to read into
     * @param clearBeforeAdd Whether the values should be emptied before adding
     * @param autoNotify <code>true</code> to automatically notify any connected
     *            loaders
     * @return The book Id that was parsed
     * @throws JSONException If the Json is invalid
     */
    private String readBookDetailsIntoContentValues(
                    final JSONObject bookObject, final ContentValues values,
                    final boolean clearBeforeAdd, final boolean autoNotify)
                    throws JSONException {

        if (clearBeforeAdd) {
            values.clear();
        }

        final String bookId = JsonUtils
                        .readString(bookObject, HttpConstants.ID_BOOK, false, false);

        final int id = JsonUtils
                        .readInt(bookObject, HttpConstants.ID, true, true);
        Logger.d(TAG, "ID : " + id);
        values.put(DatabaseColumns.ID, id + "");
        values.put(DatabaseColumns.ISBN_10, JsonUtils
                        .readString(bookObject, HttpConstants.ISBN_10, false, false));
        values.put(DatabaseColumns.ISBN_13, JsonUtils
                        .readString(bookObject, HttpConstants.ISBN_13, false, false));
        values.put(DatabaseColumns.AUTHOR, JsonUtils
                        .readString(bookObject, HttpConstants.AUTHOR, false, false));
        values.put(DatabaseColumns.USER_ID, JsonUtils
                        .readString(bookObject, HttpConstants.ID_USER, false, false));
        values.put(DatabaseColumns.TITLE, JsonUtils
                        .readString(bookObject, HttpConstants.TITLE, false, false));
        values.put(DatabaseColumns.DESCRIPTION, JsonUtils
                        .readString(bookObject, HttpConstants.DESCRIPTION, false, false));

        final String imagePresent = JsonUtils
                        .readString(bookObject, HttpConstants.IMAGE_PRESENT, false, false);
        if (imagePresent != null && imagePresent.equals("false")) {
            values.put(DatabaseColumns.IMAGE_URL, JsonUtils
                            .readString(bookObject, HttpConstants.IMAGE_PRESENT, false, false));
        } else {
            values.put(DatabaseColumns.IMAGE_URL, JsonUtils
                            .readString(bookObject, HttpConstants.IMAGE_URL, false, false));
        }
        values.put(DatabaseColumns.PUBLICATION_YEAR, JsonUtils
                        .readString(bookObject, HttpConstants.PUBLICATION_YEAR, false, false));
        values.put(DatabaseColumns.PUBLICATION_MONTH, JsonUtils
                        .readString(bookObject, HttpConstants.PUBLICATION_MONTH, false, false));
        values.put(DatabaseColumns.VALUE, JsonUtils
                        .readString(bookObject, HttpConstants.VALUE, false, false));
        values.put(DatabaseColumns.BOOK_OWNER, JsonUtils
                        .readString(bookObject, HttpConstants.OWNER_NAME, false, false));
        values.put(DatabaseColumns.BOOK_OWNER_IMAGE_URL, JsonUtils
                        .readString(bookObject, HttpConstants.OWNER_IMAGE_URL, false, false));

        final JSONObject locationObject = JsonUtils
                        .readJSONObject(bookObject, HttpConstants.LOCATION, false, false);

        if (locationObject != null) {
            values.put(DatabaseColumns.LOCATION_ID, parseAndStoreLocation(locationObject, autoNotify));
        }

        final JSONArray tagsArray = JsonUtils
                        .readJSONArray(bookObject, HttpConstants.TAGS, true, true);

        if (tagsArray.length() > 0) {
            final String[] tags = new String[tagsArray.length()];

            for (int i = 0; i < tagsArray.length(); i++) {
                tags[i] = JsonUtils.readString(tagsArray, i, true, true);
            }

            values.put(DatabaseColumns.BARTER_TYPE, TextUtils
                            .join(AppConstants.BARTER_TYPE_SEPARATOR, tags));
        }
        return id + "";
    }

    /**
     * Reads the book details from the Book response json into a content values
     * object
     * 
     * @param bookObject The Json representation of a book search result
     * @param values The values instance to read into
     * @param clearBeforeAdd Whether the values should be emptied before adding
     * @param autoNotify <code>true</code> to automatically notify any connected
     *            loaders
     * @return The book Id that was parsed
     * @throws JSONException If the Json is invalid
     */
    private String readUserDetailsIntoContentValues(
                    final JSONObject bookObject, final ContentValues values,
                    final boolean clearBeforeAdd, final boolean autoNotify)
                    throws JSONException {

        if (clearBeforeAdd) {
            values.clear();
        }

        final String userId = JsonUtils
                        .readString(bookObject, HttpConstants.ID_USER, true, true);

        values.put(DatabaseColumns.USER_ID, JsonUtils
                        .readString(bookObject, HttpConstants.ID_USER, false, false));
        values.put(DatabaseColumns.FIRST_NAME, JsonUtils
                        .readString(bookObject, HttpConstants.FIRST_NAME, false, false));
        values.put(DatabaseColumns.LAST_NAME, JsonUtils
                        .readString(bookObject, HttpConstants.LAST_NAME, false, false));
        values.put(DatabaseColumns.PROFILE_PICTURE, JsonUtils
                        .readString(bookObject, HttpConstants.IMAGE_URL, false, false));

        values.put(DatabaseColumns.DESCRIPTION, JsonUtils
                        .readString(bookObject, HttpConstants.DESCRIPTION, false, false));

        final JSONObject locationObject = JsonUtils
                        .readJSONObject(bookObject, HttpConstants.LOCATION, false, false);

        if (locationObject != null) {
            values.put(DatabaseColumns.LOCATION_ID, parseAndStoreLocation(locationObject, autoNotify));
        }

        return userId;
    }

    /**
     * Reads the location details from the Location response json into a content
     * values object
     * 
     * @param locationObject The Json representation of a location
     * @param values The values instance to read into
     * @param clearBeforeAdd Whether the values should be emptied before adding
     * @return The location Id that was parsed
     * @throws JSONException If the Json is invalid
     */
    private String readLocationDetailsIntoContentValues(
                    final JSONObject locationObject,
                    final ContentValues values, final boolean clearBeforeAdd)
                    throws JSONException {

        if (clearBeforeAdd) {
            values.clear();
        }

        final String locationId = JsonUtils
                        .readString(locationObject, HttpConstants.ID_LOCATION, true, true);
        values.put(DatabaseColumns.LOCATION_ID, locationId);
        values.put(DatabaseColumns.NAME, JsonUtils
                        .readString(locationObject, HttpConstants.NAME, true, true));
        values.put(DatabaseColumns.ADDRESS, JsonUtils
                        .readString(locationObject, HttpConstants.ADDRESS, true, true));
        values.put(DatabaseColumns.LATITUDE, JsonUtils
                        .readDouble(locationObject, HttpConstants.LATITUDE, true, true));
        values.put(DatabaseColumns.LONGITUDE, JsonUtils
                        .readDouble(locationObject, HttpConstants.LONGITUDE, true, true));

        mEndLatitude = JsonUtils
                        .readDouble(locationObject, HttpConstants.LATITUDE, true, true);

        mEndLongitude = JsonUtils
                        .readDouble(locationObject, HttpConstants.LONGITUDE, true, true);

        return locationId;
    }

    /**
     * @param response
     * @return
     * @throws JSONException
     */
    private ResponseInfo parseCreateBookResponse(final String response)
                    throws JSONException {
        final ResponseInfo responseInfo = new ResponseInfo();

        final JSONObject responseObject = new JSONObject(response);
        final JSONObject bookObject = JsonUtils
                        .readJSONObject(responseObject, HttpConstants.BOOK, true, true);

        final ContentValues values = new ContentValues();
        final String[] args = new String[1];

        args[0] = readBookDetailsIntoContentValues(bookObject, values, true, false);
        final String mId = JsonUtils
                        .readInt(bookObject, HttpConstants.ID, true, true) + "";

        final String selection = DatabaseColumns.ID + SQLConstants.EQUALS_ARG;

        final Location latestLocation = DeviceInfo.INSTANCE.getLatestLocation();

        float distanceBetween = getDistanceBetweenInKms(latestLocation.getLatitude(), latestLocation
                        .getLongitude(), mEndLatitude, mEndLongitude);
        Logger.d(TAG, distanceBetween + "");
        if (distanceBetween <= AppConstants.DEFAULT_SEARCH_RADIUS) {
            if (DBInterface.update(TableSearchBooks.NAME, values, selection, args, false) == 0) {

                // Unable to update, insert the item
                DBInterface.insert(TableSearchBooks.NAME, null, values, false);
            }
        }
        if (DBInterface.insert(TableUserBooks.NAME, null, values, true) >= 0) {
            final Bundle responseBundle = new Bundle(1);
            responseBundle.putString(HttpConstants.ID, mId);
            responseInfo.responseBundle = responseBundle;
        } else {
            responseInfo.success = false;
        }

        return responseInfo;
    }

    private ResponseInfo parseUpdateBookResponse(final String response)
                    throws JSONException {
        //TODO Parse update book response
        final ResponseInfo responseInfo = new ResponseInfo();
        return responseInfo;
    }

    private ResponseInfo parseDeleteBookResponse(final String response)
                    throws JSONException {
        final ResponseInfo responseInfo = new ResponseInfo();
        return responseInfo;
    }

    /**
     * Method for calculating the distance between two location points
     * 
     * @param startLatitude
     * @param startLongitude
     * @param endLatitude
     * @param endLongitude
     * @return distance in kms
     */
    private float getDistanceBetweenInKms(double startLatitude,
                    double startLongitude, double endLatitude,
                    double endLongitude) {
        float[] distanceBetween = new float[1];
        if ((startLatitude != 0.0) && (startLongitude != 0.0)) {
            Location.distanceBetween(startLatitude, startLongitude, mEndLatitude, mEndLongitude, distanceBetween);
            // to convert it into kms
            distanceBetween[0] = distanceBetween[0] / 1000;
        } else {
            distanceBetween[0] = 0;
        }
        return distanceBetween[0];

    }

    /**
     * Method for parsing report bug response
     * 
     * @param response
     * @return
     * @throws JSONException
     */

    private ResponseInfo parseReportBugOrSuggestFeatureResponse(
                    final String response) throws JSONException {
        final ResponseInfo responseInfo = new ResponseInfo();
        final JSONObject responseObject = new JSONObject(response);
        final String mStatus = JsonUtils
                        .readString(responseObject, HttpConstants.STATUS, true, true);

        final Bundle responseBundle = new Bundle(1);
        responseBundle.putString(HttpConstants.STATUS, mStatus);
        responseInfo.responseBundle = responseBundle;
        return responseInfo;
    }

    /**
     * Parses the string response(when the request was unsuccessful -
     * {@linkplain HttpStatus#SC_BAD_REQUEST} was returned) for a particular
     * {@linkplain RequestId} and returns the response data
     * 
     * @param requestId The {@linkplain RequestId} for the request
     * @param response The response from the server
     * @return a {@linkplain BlBadRequestError} object representing the response
     * @throws JSONException If the response was an invalid json
     */
    public BlBadRequestError getErrorResponse(final int requestId,
                    final String response) throws JSONException {

        Logger.d(TAG, "Request Id %d\nResponse %s", requestId, response);
        return parseErrorResponse(requestId, response);
    }

    /**
     * Do the actual parsing of error response here
     * 
     * @param requestId The {@linkplain RequestId} for the request
     * @param response The Json response from server
     * @return a {@link BlBadRequestError} object representing the error
     * @throws JSONException If the response was invalid json
     */
    private BlBadRequestError parseErrorResponse(final int requestId,
                    final String response) throws JSONException {

        final JSONObject errorObject = new JSONObject(response);

        final int errorCode = JsonUtils
                        .readInt(errorObject, HttpConstants.ERROR_CODE, true, true);
        final String errorMessage = JsonUtils
                        .readString(errorObject, HttpConstants.ERROR_MESSAGE, true, true);
        //Parse error response specific to any request here

        final BlBadRequestError error = new BlBadRequestError(errorCode, errorMessage);
        return error;

    }

}
