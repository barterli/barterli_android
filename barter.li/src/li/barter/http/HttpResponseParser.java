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
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentValues;
import android.os.Bundle;
import android.text.TextUtils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import li.barter.data.DBInterface;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLConstants;
import li.barter.data.TableLocations;
import li.barter.data.TableMyBooks;
import li.barter.data.TableSearchBooks;
import li.barter.data.TableUserBooks;
import li.barter.data.TableUsers;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.JsonUtils.FieldType;
import li.barter.models.Hangout;
import li.barter.models.Team;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.Logger;
import li.barter.widgets.autocomplete.Suggestion;

/**
 * Class that reads an API response and parses it and stores it in the database
 * 
 * @author Vinay S Shenoy
 */
public class HttpResponseParser {



	private static final String         TAG  = "HttpResponseParser";
	private static final Object         LOCK = new Object();

	private static XmlPullParserFactory sPullParserFactory;

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
			return parseGetBookInfoResponse(response);
		}

		case RequestId.SEARCH_BOOKS: {
			return parseSearchBooksResponse(response);
		}

		case RequestId.CREATE_USER: {
			return parseCreateUserResponse(response);
		}

		case RequestId.SAVE_USER_PROFILE: {
			return parseCreateUserResponse(response);
		}

		case RequestId.HANGOUTS: {
			return parseHangoutsResponse(response);
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
			return parseBookSuggestionsResponse(response);
		}

		case RequestId.UPDATE_BOOK: {
			return parseUpdateBookResponse(response);
		}

		case RequestId.DELETE_BOOK: {
			return parseDeleteBookResponse(response);
		}

		case RequestId.GOODREADS_SHOW_BOOK: {
			return parseGoodreadsBookResponse(response);
		}

		default: {
			throw new IllegalArgumentException("Unknown request Id:"
					+ requestId);
		}
		}
	}

	/**
	 * Parses the good reads API response for fetching a book
	 * 
	 * @param response
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private ResponseInfo parseGoodreadsBookResponse(String response)
			throws XmlPullParserException, IOException {

		final ResponseInfo responseInfo = new ResponseInfo();
		final XmlPullParser xmlParser = getPullParserInstance(false, false);
		xmlParser.setInput(new StringReader(response));

		for (int eventType = xmlParser.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = xmlParser
				.next()) {

			String name = null;

			if (eventType == XmlPullParser.START_TAG) {
				name = xmlParser.getName();

				if ((name != null) && name.equals(HttpConstants.BOOK)) {

					responseInfo.responseBundle = parseGoodreadsBookInfo(xmlParser);
					break;

				}
			}
		}
		return responseInfo;
	}

	/**
	 * Parse the response for goodreads book info
	 * 
	 * @param xmlParser An {@link XmlPullParser} instance, forwarded to an event
	 *            before the book item begins
	 * @return A {@link Bundle} containing the parsed info
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private Bundle parseGoodreadsBookInfo(XmlPullParser xmlParser)
			throws XmlPullParserException, IOException {

		final Bundle bundle = new Bundle(7);
		String name;
		int authorEventType;
		final ArrayList<String> authorsList = new ArrayList<String>();
		for (int eventType = xmlParser.next(); eventType != XmlPullParser.END_DOCUMENT; eventType = xmlParser
				.next()) {

			if (eventType == XmlPullParser.START_TAG) {
				name = xmlParser.getName();

				if (name.equals(HttpConstants.TITLE)) {
					bundle.putString(HttpConstants.TITLE, xmlParser.nextText());
				} else if (name.equals(HttpConstants.ISBN)) {
					bundle.putString(HttpConstants.ISBN_10, xmlParser
							.nextText());
				} else if (name.equals(HttpConstants.ISBN13)) {
					bundle.putString(HttpConstants.ISBN_13, xmlParser
							.nextText());
				} else if (name.equals(HttpConstants.DESCRIPTION)) {
					bundle.putString(HttpConstants.DESCRIPTION, xmlParser
							.nextText());
				} else if (name.equals(HttpConstants.PUBLICATION_YEAR)) {
					bundle.putString(HttpConstants.PUBLICATION_YEAR, xmlParser
							.nextText());
				} else if (name.equals(HttpConstants.AUTHORS)) {
					//We need to handle multiple authors here
					while (true) {
						authorEventType = xmlParser.next();

						if (authorEventType == XmlPullParser.END_DOCUMENT) {
							break;
						} else if (authorEventType == XmlPullParser.END_TAG) {
							name = xmlParser.getName();

							if (name.equals(HttpConstants.AUTHORS)) {
								//We have parsed out all authors

								if (authorsList.size() == 1) {
									//There is only 1 author
									bundle.putString(HttpConstants.AUTHOR, authorsList
											.get(0));
								} else if (authorsList.size() > 1) {
									//Multiple authors
									bundle.putString(HttpConstants.AUTHOR, TextUtils
											.join(", ", authorsList));
								}

								authorsList.clear();
								break;
							}

						} else if (authorEventType == XmlPullParser.START_TAG) {
							name = xmlParser.getName();

							if (name.equals(HttpConstants.NAME)) {
								authorsList.add(xmlParser.nextText());
							}
						}

					}

					break;
				}
			}
		}
		return bundle;
	}

	/**
	 * Parse the response for book suggestions
	 * 
	 * @param response
	 * @return
	 * @throws XmlPullParserException
	 * @throws IOException
	 */

	private ResponseInfo parseBookSuggestionsResponse(final String response)
			throws XmlPullParserException, IOException {

		Logger.d(TAG, response);

		final ResponseInfo responseInfo = new ResponseInfo();
		final Bundle responseBundle = new Bundle(1);
		final XmlPullParser xmlParser = getPullParserInstance(false, false);
		xmlParser.setInput(new StringReader(response));

		for (int eventType = xmlParser.getEventType(); eventType != XmlPullParser.END_DOCUMENT; eventType = xmlParser
				.next()) {

			String name = null;

			if (eventType == XmlPullParser.START_TAG) {
				name = xmlParser.getName();

				if ((name != null) && name.equals(HttpConstants.RESULTS)) {

					final ArrayList<Suggestion> results = parseBookSuggestionItems(xmlParser);
					responseBundle.putParcelableArray(HttpConstants.RESULTS, results
							.toArray(new Suggestion[results.size()]));

				}
			}
		}
		responseInfo.responseBundle = responseBundle;
		return responseInfo;
	}

	/**
	 * Parses the book suggestion items from the book suggestions
	 * 
	 * @param xmlParser An {@link XmlPullParser} instance, forwarded to an event
	 *            before the book items begin
	 * @return An array of {@link Suggestion} items
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	private ArrayList<Suggestion> parseBookSuggestionItems(
			final XmlPullParser xmlParser)
					throws XmlPullParserException, IOException {

		final ArrayList<Suggestion> suggestions = new ArrayList<Suggestion>();
		String name;
		int bookEventType;
		String bookEventName;
		Suggestion suggestion;
		for (int eventType = xmlParser.next(); eventType != XmlPullParser.END_DOCUMENT; eventType = xmlParser
				.next()) {

			if (eventType == XmlPullParser.END_TAG) {
				name = xmlParser.getName();

				if (name.equals(HttpConstants.RESULTS)) {
					//We have finished parsing sugesstions
					break;
				}
			}

			else if (eventType == XmlPullParser.START_TAG) {

				name = xmlParser.getName();

				if (name.equals(HttpConstants.BEST_BOOK)) {

					suggestion = new Suggestion();
					while (true) {
						bookEventType = xmlParser.next();

						if (bookEventType == XmlPullParser.END_TAG) {
							bookEventName = xmlParser.getName();

							if (bookEventName.equals(HttpConstants.BEST_BOOK)) {

								if (!TextUtils.isEmpty(suggestion.id)
										&& !TextUtils.isEmpty(suggestion.name)) {
									suggestions.add(suggestion);
								}
								//We have parsed out one book suggestion
								break;
							}
						} else if (bookEventType == XmlPullParser.START_TAG) {
							bookEventName = xmlParser.getName();

							if (bookEventName.equals(HttpConstants.AUTHOR)) {

								while (true) {
									//Skip parsing the author ID since it has the same key as the book id
									bookEventType = xmlParser.next();

									if (bookEventType == XmlPullParser.END_TAG
											&& xmlParser.getName()
											.equals(HttpConstants.AUTHOR)) {
										break;
									}
								}
							} else if (bookEventName.equals(HttpConstants.ID)) {
								suggestion.id = xmlParser.nextText();
							} else if (bookEventName
									.equals(HttpConstants.TITLE)) {
								suggestion.name = xmlParser.nextText();
							} else if (bookEventName
									.equals(HttpConstants.SMALL_IMAGE_URL)) {
								suggestion.imageUrl = xmlParser.nextText();
							}

						}
					}
				}
			}

		}
		return suggestions;
	}

	/**
	 * @return an instance of an {@link XmlPullParser}4
	 * @param namespaceAware Whether the parser is namespace aware
	 * @param validating Whether the parser is validating
	 * @throws XmlPullParserException
	 */
	private static XmlPullParser getPullParserInstance(
			final boolean namespaceAware, final boolean validating)
					throws XmlPullParserException {

		synchronized (LOCK) {

			if (sPullParserFactory == null) {
				synchronized (LOCK) {
					sPullParserFactory = XmlPullParserFactory.newInstance();
				}
			}

			sPullParserFactory.setNamespaceAware(namespaceAware);
			sPullParserFactory.setValidating(validating);
			return sPullParserFactory.newPullParser();
		}

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
		//return new ResponseInfo();

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

		final JSONArray booksArray = JsonUtils
				.readJSONArray(userObject, HttpConstants.BOOKS, true, true);

		JSONObject bookObject = null;
		final ContentValues values = new ContentValues();
		final String selection = DatabaseColumns.BOOK_ID
				+ SQLConstants.EQUALS_ARG;
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
		responseBundle.putString(HttpConstants.ID_USER, JsonUtils
				.readString(userObject, HttpConstants.ID_USER, true, true));
		responseBundle.putString(HttpConstants.AUTH_TOKEN, JsonUtils
				.readString(userObject, HttpConstants.AUTH_TOKEN, true, true));
		responseBundle.putString(HttpConstants.EMAIL, JsonUtils
				.readString(userObject, HttpConstants.EMAIL, true, true));
		responseBundle.putString(HttpConstants.DESCRIPTION, JsonUtils
				.readString(userObject, HttpConstants.DESCRIPTION, false, false));
		responseBundle.putString(HttpConstants.FIRST_NAME, JsonUtils
				.readString(userObject, HttpConstants.FIRST_NAME, false, false));
		responseBundle.putString(HttpConstants.LAST_NAME, JsonUtils
				.readString(userObject, HttpConstants.LAST_NAME, false, false));
		responseBundle.putString(HttpConstants.IMAGE_URL, JsonUtils
				.readString(userObject, HttpConstants.IMAGE_URL, false, false));

		final JSONArray booksArray = JsonUtils
				.readJSONArray(userObject, HttpConstants.BOOKS, true, true);

		JSONObject bookObject = null;
		final ContentValues values = new ContentValues();
		final String selection = DatabaseColumns.BOOK_ID
				+ SQLConstants.EQUALS_ARG;
		final String[] args = new String[1];
		for (int i = 0; i < booksArray.length(); i++) {
			bookObject = JsonUtils.readJSONObject(booksArray, i, true, true);
			args[0] = readBookDetailsIntoContentValues(bookObject, values, true, false);

			//First try to update the table if a book already exists
			if (DBInterface.update(TableMyBooks.NAME, values, selection, args, false) == 0) {

				// Unable to update, insert the item
				DBInterface.insert(TableMyBooks.NAME, null, values, false);
			}
		}
		DBInterface.notifyChange(TableMyBooks.NAME);

		final JSONObject locationObject = JsonUtils
				.readJSONObject(userObject, HttpConstants.LOCATION, false, false);

		String locationId = null;
		if (locationObject != null) {
			locationId = parseAndStoreLocation(locationObject, false);
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
	private ResponseInfo parseSearchBooksResponse(final String response)
			throws JSONException {

		final ResponseInfo responseInfo = new ResponseInfo();

		final JSONObject responseObject = new JSONObject(response);

		final JSONArray searchResults = JsonUtils
				.readJSONArray(responseObject, HttpConstants.SEARCH, true, true);

		JSONObject bookObject = null;
		final ContentValues values = new ContentValues();
		final String selection = DatabaseColumns.BOOK_ID
				+ SQLConstants.EQUALS_ARG;
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
	 * Method for parsing the hangouts response
	 * 
	 * @param response The Json string response
	 * @return
	 * @throws JSONException if the Json string is invalid
	 */
	private ResponseInfo parseHangoutsResponse(final String response)
			throws JSONException {

		final ResponseInfo responseInfo = new ResponseInfo();

		final JSONObject responseObject = new JSONObject(response);
		final JSONArray hangoutsArray = JsonUtils
				.readJSONArray(responseObject, HttpConstants.LOCATIONS, true, true);

		final Hangout[] hangouts = new Hangout[hangoutsArray.length()];
		JSONObject hangoutObject = null;
		for (int i = 0; i < hangouts.length; i++) {
			hangoutObject = JsonUtils
					.readJSONObject(hangoutsArray, i, true, true);
			hangouts[i] = new Hangout();
			readHangoutObjectIntoHangout(hangoutObject, hangouts[i]);
		}

		final Bundle responseBundle = new Bundle(1);
		responseBundle.putParcelableArray(HttpConstants.LOCATIONS, hangouts);
		responseInfo.responseBundle = responseBundle;
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
	 * Reads a Hangout {@link JSONObject} into a {@link Hangout} model
	 * 
	 * @param hangoutObject The Json response representing a Hangout
	 * @param hangout The {@link Hangout} model to write into
	 * @throws JSONException If the Json is invalid
	 */
	private void readHangoutObjectIntoHangout(final JSONObject hangoutObject,
			final Hangout hangout) throws JSONException {

		hangout.name = JsonUtils
				.readString(hangoutObject, HttpConstants.NAME, true, true);
		hangout.address = JsonUtils
				.readString(hangoutObject, HttpConstants.ADDRESS, true, true);
		hangout.latitude = JsonUtils
				.readDouble(hangoutObject, HttpConstants.LATITUDE, true, true);
		hangout.longitude = JsonUtils
				.readDouble(hangoutObject, HttpConstants.LONGITUDE, true, true);

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
				.readString(bookObject, HttpConstants.ID_BOOK, true, true);

		final int id = JsonUtils
				.readInt(bookObject, HttpConstants.ID, true, true);
		Logger.d(TAG, "ID : " + id);
		values.put(DatabaseColumns.BOOK_ID, bookId);
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
		values.put(DatabaseColumns.IMAGE_URL, JsonUtils
				.readString(bookObject, HttpConstants.IMAGE_URL, false, false));
		values.put(DatabaseColumns.PUBLICATION_YEAR, JsonUtils
				.readString(bookObject, HttpConstants.PUBLICATION_YEAR, false, false));
		values.put(DatabaseColumns.PUBLICATION_MONTH, JsonUtils
				.readString(bookObject, HttpConstants.PUBLICATION_MONTH, false, false));
		values.put(DatabaseColumns.VALUE, JsonUtils
				.readString(bookObject, HttpConstants.VALUE, false, false));
		values.put(DatabaseColumns.OWNER, JsonUtils
				.readString(bookObject, HttpConstants.OWNER_NAME, false, false));

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
		return bookId;
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
		return locationId;
	}

	/**
	 * @param response
	 * @return
	 */
	private ResponseInfo parseGetBookInfoResponse(final String response)
			throws JSONException {

		Logger.d(TAG, "Request Id Test \nResponse %s", response);
		final ResponseInfo responseInfo = new ResponseInfo();

		final JSONObject bookInfoObject = new JSONObject(response);

		final Bundle responseBundle = new Bundle(6);
		responseBundle.putString(HttpConstants.TITLE, JsonUtils
				.readString(bookInfoObject, HttpConstants.TITLE, false, false));
		responseBundle.putString(HttpConstants.DESCRIPTION, JsonUtils
				.readString(bookInfoObject, HttpConstants.DESCRIPTION, false, false));

		responseBundle.putString(HttpConstants.PUBLICATION_YEAR, JsonUtils
				.readString(bookInfoObject, HttpConstants.PUBLICATION_YEAR, false, false));

		responseBundle.putString(HttpConstants.IMAGE_URL, JsonUtils
				.readString(bookInfoObject, HttpConstants.IMAGE_URL, false, false));

		responseBundle.putString(HttpConstants.ISBN_13, JsonUtils
				.readString(bookInfoObject, HttpConstants.ISBN_13, false, false));

		responseBundle.putString(HttpConstants.VALUE, JsonUtils
				.readString(bookInfoObject, HttpConstants.VALUE, false, false));

		final JSONObject authorsObject = JsonUtils
				.readJSONObject(bookInfoObject, HttpConstants.AUTHORS, false, false);

		if (authorsObject != null) {
			final FieldType type = JsonUtils
					.getTypeForKey(authorsObject, HttpConstants.AUTHOR);

			if (type == FieldType.OBJECT) {
				final JSONObject authorObject = JsonUtils
						.readJSONObject(authorsObject, HttpConstants.AUTHOR, true, true);
				responseBundle.putString(HttpConstants.AUTHOR, JsonUtils
						.readString(authorObject, HttpConstants.NAME, true, true));
			} else if (type == FieldType.ARRAY) {
				final JSONArray authorsArray = JsonUtils
						.readJSONArray(authorsObject, HttpConstants.AUTHOR, true, true);
				final String[] authorNames = new String[authorsArray.length()];

				for (int i = 0; i < authorsArray.length(); i++) {
					authorNames[i] = JsonUtils
							.readString(JsonUtils
									.readJSONObject(authorsArray, i, true, true), HttpConstants.NAME, true, true);
				}

				if (authorNames.length > 0) {
					responseBundle.putString(HttpConstants.AUTHOR, TextUtils
							.join(", ", authorNames));
				}
			}
		}

		responseInfo.responseBundle = responseBundle;
		return responseInfo;
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
		final String bookId = readBookDetailsIntoContentValues(bookObject, values, true, false);

		// Unable to update, insert the item
		if (DBInterface.insert(TableMyBooks.NAME, null, values, true) >= 0) {

			final Bundle responseBundle = new Bundle(1);
			responseBundle.putString(HttpConstants.ID_BOOK, bookId);
			responseInfo.responseBundle = responseBundle;
		} else {
			responseInfo.success = false;
		}

		return responseInfo;
	}

	private ResponseInfo parseUpdateBookResponse(final String response)
			throws JSONException {
		final ResponseInfo responseInfo = new ResponseInfo();
		return responseInfo;
	}

	private ResponseInfo parseDeleteBookResponse(final String response)
			throws JSONException {
		final ResponseInfo responseInfo = new ResponseInfo();
		return responseInfo;
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
