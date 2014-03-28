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

    public static final boolean DEBUG    = true;

    public static final String  FACEBOOK = "facebook";
    public static final String  MANUAL   = "manual";

    /**
     * Singleton to hold frequently accessed info in memory
     * 
     * @author Vinay S Shenoy
     */
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
     * Singleton to hold the current network state. Broadcas receiver for
     * network state will be used to keep this updated
     * 
     * @author Vinay S Shenoy
     */
    public enum NetworkDetails {

        INSTANCE;

        public boolean isNetworkConnected;
        public int     currentNetworkType;

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

        public static final String ISBN               = "isbn";
        public static final String BOOK_TITLE         = "book_title";
        public static final String AUTHOR             = "author";
        public static final String DESCRIPTION        = "description";
        public static final String PUBLICATION_YEAR   = "publication_year";
        public static final String BARTER_TYPES       = "barter_types";
        public static final String SYMBOLOGY          = "symbology";
        public static final String TYPE               = "type";
        public static final String BACKSTACK_TAG      = "backstack_tag";
        public static final String MAP_MOVED_ONCE     = "map_moved_once";
        public static final String DRAWER_OPENED_ONCE = "drawer_opened_once";
        public static final String HAS_FETCHED_INFO   = "has_fetched_info";

    }

    /**
     * Constant interface, DO NOT IMPLEMENT
     * 
     * @author Vinay S Shenoy
     */
    public static interface FragmentTags {
        public static final String BOOKS_AROUND_ME                      = "books_around_me";
        public static final String ADD_OR_EDIT_BOOK                     = "add_or_edit_book";
        public static final String LOGIN_TO_ADD_BOOK                    = "login_to_add_book";
        public static final String LOGIN_FROM_NAV_DRAWER                = "login_from_nav_drawer";
        public static final String SELECT_PREFERRED_LOCATION_FROM_LOGIN = "select_preferred_location_from_login";

        /* Tags for fragment backstack popping and providing up navigation */
        public static final String BS_BOOKS_AROUND_ME                   = "to_books_around_me";
        public static final String BS_ADD_BOOK                          = "to_add_book";
        public static final String BS_PREFERRED_LOCATION                = "to_preferred_location";
    }

    /**
     * Constant interface. DO NOT IMPLEMENT
     * 
     * @author Vinay S Shenoy
     */
    public static interface BarterType {
        public static final String FREE    = "free";
        public static final String PRIVATE = "private";
        public static final String BARTER  = "barter";
        public static final String SALE    = "sale";
        public static final String RENT    = "rent";
        public static final String READ    = "read";
    }

    /**
     * Constant interface. DO NOT IMPLEMENT
     * 
     * @author Vinay S Shenoy
     */
    public static interface Loaders {

        public static final int SEARCH_BOOKS = 201;
    }

}
