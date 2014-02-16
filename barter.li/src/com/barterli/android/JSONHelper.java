package com.barterli.android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONHelper {

	public String[] JsonStringofArraysToArray(String jsonString) {
		JSONArray array;
		final String[] emptyArray = new String[0];
		try {
			array = new JSONArray(jsonString);
			final String[] normalArray = new String[array.length()];
			for (int i = 0; i < array.length(); i++) {
				normalArray[i] = array.getString(i);
			}
			return normalArray;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return emptyArray;
	}

	public String[] getBookNamesFromUserProfile(String UserJSONString) {

		JSONObject userObject;
		try {
			userObject = new JSONObject(UserJSONString);
			if (!userObject.has("books")
					|| userObject.getJSONArray("books").length() == 0) {
				return new String[0];
			}
			JSONArray booksArray = userObject.getJSONArray("books");
			final String[] bookTitleArray = new String[booksArray.length()];
			for (int i = 0; i < booksArray.length(); i++) {
				bookTitleArray[i] = booksArray.getJSONObject(i).getString(
						"title");
			}
			return bookTitleArray;
		} catch (JSONException e) {
			e.printStackTrace();
			return new String[0];
		}
	}

	public String[] getBookIDsFromUserProfile(String UserJSONString) {

		JSONObject userObject;
		try {
			userObject = new JSONObject(UserJSONString);
			if (!userObject.has("books")
					|| userObject.getJSONArray("books").length() == 0) {
				return new String[0];
			}
			JSONArray booksArray = userObject.getJSONArray("books");
			final String[] bookIDsArray = new String[booksArray.length()];
			for (int i = 0; i < booksArray.length(); i++) {
				bookIDsArray[i] = booksArray.getJSONObject(i).getString("id");
			}
			return bookIDsArray;
		} catch (JSONException e) {
			e.printStackTrace();
			return new String[0];
		}
	}

	public JSONArray getBookObjectsFromUserProfile(String UserJSONString) {

		JSONObject userObject;
		try {
			userObject = new JSONObject(UserJSONString);
			if (!userObject.has("books")
					|| userObject.getJSONArray("books").length() == 0) {
				return new JSONArray();
			}
			JSONArray booksArray = userObject.getJSONArray("books");
			final JSONArray BooksJSONArray = new JSONArray();
			for (int i = 0; i < booksArray.length(); i++) {
				JSONObject _obj = new JSONObject();
				_obj.put("id", booksArray.getJSONObject(i).opt("id"));
				_obj.put("title", booksArray.getJSONObject(i).opt("title"));
				_obj.put("author", booksArray.getJSONObject(i).opt("author"));
				_obj.put("description",
						booksArray.getJSONObject(i).opt("description"));
				_obj.put("publication_year",
						booksArray.getJSONObject(i).opt("publication_year"));
				_obj.put("barter_type",
						booksArray.getJSONObject(i).opt("barter_type"));
				BooksJSONArray.put(_obj);
				// bookTitleArray[i] =
				// booksArray.getJSONObject(i).getString("title");
			}
			return BooksJSONArray;
		} catch (JSONException e) {
			e.printStackTrace();
			return new JSONArray();
		}
	}

}
