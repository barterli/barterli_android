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

package li.barter.utils;

import android.location.Location;
import android.location.LocationManager;

/**
 * Class that holds the App Constants
 * 
 * @author Vinay S Shenoy
 */
public class AppConstants {

    public static final boolean DEBUG = true;

    public enum UserInfo {

        INSTANCE;

        private final Location defaultLocation = new Location(LocationManager.PASSIVE_PROVIDER);

        public String          authToken;
        public Location        latestLocation;

        private UserInfo() {
            clear();
        }

        public void clear() {
            authToken = "";
            latestLocation = defaultLocation;
            latestLocation.setLatitude(0.0);
            latestLocation.setLongitude(0.0);
        }
    }

    /**
     * All the request codes used in the application will be placed here
     * 
     * @author Vinay S Shenoy
     */
    public static interface RequestCodes {

        public static final int SCAN_ISBN = 100;
    }

    /**
     * The result codes used in the application will be placed here
     * 
     * @author Vinay S Shenoy
     */
    public static interface ResultCodes {

        public static final int FAILURE = -1;
        public static final int CANCEL  = 0;
        public static final int SUCCESS = 1;
    }

    /**
     * Constant Interface, DO NOT IMPLEMENT
     * 
     * @author vinaysshenoy
     */
    public static interface Keys {

        public static final String ISBN             = "isbn";
        public static final String BOOL_1           = "bool_1";
        public static final String BOOK_TITLE       = "book_title";
        public static final String AUTHOR           = "author";
        public static final String DESCRIPTION      = "description";
        public static final String PUBLICATION_YEAR = "publication_year";
        public static final String BARTER_TYPES     = "barter_types";
        public static final String SYMBOLOGY        = "symbology";
        public static final String TYPE             = "type";

    }

    /**
     * Constant interface, DO NOT IMPLEMENT
     * 
     * @author Vinay S Shenoy
     */
    public static interface FragmentTags {
        public static final String BOOKS_AROUND_ME  = "books_around_me";
        public static final String ADD_OR_EDIT_BOOK = "add_or_edit_book";
        public static final String LOGIN            = "login";
    }

}
