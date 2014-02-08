package com.koramangala.barterli;

import org.json.JSONArray;

public class JSONHelper {
	
	public String[] JsonStringofArraysToArray(String jsonString){
		JSONArray array;
		final String[] emptyArray = new String[0];
		try {
			array = new JSONArray(jsonString);
			final String[] normalArray = new String[array.length()];
			for (int i = 0; i < array.length(); i++){	normalArray[i] = array.getString(i);	} 
			return normalArray;
		}catch(Exception e){
			e.printStackTrace();
		}
		return emptyArray;	
	}
}
