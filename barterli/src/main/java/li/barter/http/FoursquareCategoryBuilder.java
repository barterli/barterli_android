/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package li.barter.http;

/**
 * @author Vinay S Shenoy Class to build the string for filtering Foursquare
 *         categories
 */
public class FoursquareCategoryBuilder {

    /*
     * Foursquare Category IDs. The IDs are defined at
     * https://developer.foursquare.com/categorytree
     */
    public static final String ARTS_AND_ENTERTAINMENT = "4d4b7104d754a06370d81259";
    public static final String COLLEGE_AND_UNIVERSITY = "4d4b7105d754a06372d81259";
    public static final String FOOD = "4d4b7105d754a06374d81259";
    public static final String NIGHTLIFE_SPOT = "4d4b7105d754a06376d81259";
    public static final String OUTDOORS_AND_RECREATION = "4d4b7105d754a06377d81259";
    public static final String PROFESSIONAL_AND_OTHER_PLACES = "4d4b7105d754a06375d81259";
    public static final String SHOP_AND_SERVICE = "4d4b7105d754a06378d81259";
    public static final String TRAVEL_AND_TRANSPORT = "4d4b7105d754a06379d81259";
    
    private StringBuilder mCategoryStringBuilder;

    private FoursquareCategoryBuilder() {
        mCategoryStringBuilder = new StringBuilder();
    }
    
    public static FoursquareCategoryBuilder init() {
        return new FoursquareCategoryBuilder();
    }
    
    /**
     * Add a Foursquare Category Id to the list of categories
     * @param categoryId One of the Foursquare Category Ids
     */
    public FoursquareCategoryBuilder with(String categoryId) {
        mCategoryStringBuilder.append(categoryId).append(',');
        return this;
    }
    
    /**
     * @return A Comma separated values of Foursquare category Ids
     * @throws IllegalStateException If no categories were added
     */
    public String build() throws IllegalStateException {
        
        if(mCategoryStringBuilder.length() == 0) {
            throw new IllegalStateException("No Categories Added!");
        }
        
        //Delete the trailing comma
        mCategoryStringBuilder.deleteCharAt(mCategoryStringBuilder.length() - 1);
        return mCategoryStringBuilder.toString();
    }

}
