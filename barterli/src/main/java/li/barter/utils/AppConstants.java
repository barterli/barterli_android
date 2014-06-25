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
import android.net.ConnectivityManager;
import android.text.TextUtils;

import java.util.Locale;

import li.barter.http.HttpConstants;

/**
 * Class that holds the App Constants
 * 
 * @author Vinay S Shenoy
 */
public class AppConstants {

    public static final boolean DEBUG                      = true;
    /**
     * Set to <code>true</code> for release builds. Will enable Google Analytics
     * reporting. Otherwise, Google Analytics will be set to a dry tun version
     */
    public static final boolean REPORT_GOOGLE_ANALYTICS    = false;

    public static final boolean ENABLE_CRASHLYTICS         = false;

    public static final String  FACEBOOK                   = "facebook";
    public static final String  FBPERMISSIONS[]            = new String[] {
                                                               "email"
                                                           };
    public static final String  GOOGLE                     = "google";
    public static final String  MANUAL                     = "manual";

    public static final String  TIMESTAMP_FORMAT           = "yyyy-MM-dd'T'HH:mm:ssZ";
    public static final String  CHAT_TIME_FORMAT           = "dd MMM, h:mm a";
    public static final String  MESSAGE_TIME_FORMAT        = "h:mm a";
    public static final String  CHAT_ID_FORMAT             = "%s#%s";
    public static final String  BARTER_TYPE_SEPARATOR      = ",";

    public static final String  ACTION_SHOW_ALL_CHATS      = "li.barter.ACTION_SHOW_ALL_CHATS";
    public static final String  ACTION_SHOW_CHAT_DETAIL    = "li.barter.ACTION_SHOW_CHAT_DETAIL";
    public static final String  ACTION_DISCONNECT_CHAT     = "li.barter.ACTION_DISCONNECT_CHAT";
    public static final String  ACTION_CHAT_BUTTON_CLICKED = "li.barter.ACTION_CHAT_BUTTON_CLICKED";

    public static final String  JSON                       = "json";
    public static final String  FALSE                      = "false";

    /*
     * heartbeat interval for rabbitmq chat
     */
    public static final int     HEART_BEAT_INTERVAL        = 20;

    public static final int     DEFAULT_ITEM_COUNT         = 20;

    public static final int     DEFAULT_SEARCH_RADIUS      = 25;

    // Default Book Image url we getting from the server when there is no image
    public static final String  DEFAULT_BOOKIMAGE_URL      = "1_default.png";

    /**
     * Singleton to hold frequently accessed info in memory
     * 
     * @author Vinay S Shenoy
     */
    public enum UserInfo {

        INSTANCE;

        private String mAuthToken;
        private String mEmail;
        private String mId;
        private String mProfilePicture;
        private String mAuthHeader;
        private String mDeviceId;
        private String mFirstName;

        private UserInfo() {
            reset();
        }

        public void reset() {
            mAuthToken = "";
            mAuthHeader = "";
            mEmail = "";
            mId = "";
            mProfilePicture = "";
            mFirstName = "";
        }

        public String getAuthToken() {
            return mAuthToken;
        }

        public void setAuthToken(final String authToken) {
            if (authToken == null) {
                mAuthToken = "";
            } else {
                mAuthToken = authToken;
            }
        }

        public String getEmail() {
            return mEmail;
        }

        public void setEmail(final String email) {
            if (email == null) {
                mEmail = "";
            } else {
                mEmail = email;
            }
        }

        public String getId() {
            return mId;
        }

        public void setId(final String id) {
            if (id == null) {
                mId = "";
            } else {
                mId = id;
            }
        }

        public String getProfilePicture() {
            return mProfilePicture;
        }

        public void setProfilePicture(final String profilePicture) {

            if (profilePicture == null) {
                mProfilePicture = "";
            } else {
                mProfilePicture = profilePicture;
            }

        }

        public void setDeviceId(final String deviceId) {
            mDeviceId = deviceId;
        }

        public String getDeviceId() {
            return mDeviceId;
        }

        public String getAuthHeader() {

            if (TextUtils.isEmpty(mAuthHeader)
                            && !TextUtils.isEmpty(mAuthToken)
                            && !TextUtils.isEmpty(mEmail)) {
                mAuthHeader = String
                                .format(Locale.US, HttpConstants.HEADER_AUTHORIZATION_FORMAT, mAuthToken, mEmail);
            }
            return mAuthHeader;
        }

        public void setFirstName(final String firstName) {
            mFirstName = firstName;
        }

        public String getFirstName() {
            return mFirstName;
        }

    }

    /**
     * Singleton to hold the current network state. Broadcast receiver for
     * network state will be used to keep this updated
     * 
     * @author Vinay S Shenoy
     */
    public enum DeviceInfo {

        INSTANCE;

        private final Location defaultLocation = new Location(LocationManager.PASSIVE_PROVIDER);

        private boolean        mIsNetworkConnected;
        private int            mCurrentNetworkType;
        private Location       mLatestLocation;

        private DeviceInfo() {
            reset();
        }

        public void reset() {

            mIsNetworkConnected = false;
            mCurrentNetworkType = ConnectivityManager.TYPE_DUMMY;
            mLatestLocation = defaultLocation;
        }

        public boolean isNetworkConnected() {
            return mIsNetworkConnected;
        }

        public void setNetworkConnected(final boolean isNetworkConnected) {
            mIsNetworkConnected = isNetworkConnected;
        }

        public int getCurrentNetworkType() {
            return mCurrentNetworkType;
        }

        public void setCurrentNetworkType(final int currentNetworkType) {
            mCurrentNetworkType = currentNetworkType;
        }

        public Location getLatestLocation() {
            return mLatestLocation;
        }

        public void setLatestLocation(final Location latestLocation) {
            if (latestLocation == null) {
                mLatestLocation = defaultLocation;
            }
            mLatestLocation = latestLocation;
        }

    }

    /**
     * All the request codes used in the application will be placed here
     * 
     * @author Vinay S Shenoy
     */
    public static interface RequestCodes {

        public static final int SCAN_ISBN = 100;
        public static final int PLUS_LIKE = 101;
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

        public static final String ISBN                       = "isbn";
        public static final String BOOK_TITLE                 = "book_title";
        public static final String AUTHOR                     = "author";
        public static final String DESCRIPTION                = "description";
        public static final String PUBLICATION_YEAR           = "publication_year";
        public static final String BARTER_TYPES               = "barter_types";
        public static final String SYMBOLOGY                  = "symbology";
        public static final String TYPE                       = "type";
        public static final String UP_NAVIGATION_TAG          = "up_navigation_tag";
        public static final String MAP_MOVED_ONCE             = "map_moved_once";
        public static final String DRAWER_OPENED_ONCE         = "drawer_opened_once";
        public static final String HAS_FETCHED_INFO           = "has_fetched_info";
        public static final String SUBMIT_ON_RESUME           = "submit_on_resumt";
        public static final String LOCATIONS                  = "locations";
        public static final String ID                         = "id";
        public static final String FROM_SEARCH                = "from_search";
        public static final String BOOK_ID                    = "book_id";
        public static final String CHAT_ID                    = "chat_id";
        public static final String USER_ID                    = "user_id";
        public static final String OTHER_PROFILE_FLAG         = "other_profileflag";
        public static final String URL_TO_LOAD                = "url_to_load";
        public static final String LOCATION                   = "location";
        public static final String SEARCH                     = "search";
        public static final String LAST_FETCHED_LOCATION      = "last_fetched_location";
        public static final String LAST_FETCHED_SEARCH_RADIUS = "last_fetched_search_radius";
        public static final String FIRST_MESSAGE              = "first_message";
        public static final String EDIT_MODE                  = "edit_mode";
        public static final String NO_BOOKS_FLAG_KEY          = "no_books_flag_key";
        public static final String CUR_PAGE                   = "cur_page";
        public static final String HAS_LOADED_ALL_ITEMS       = "has_loaded_all_items";
        public static final String RELOAD                     = "reload";
        public static final String BOOK_POSITION              = "book_position";
        public static final String BOOK_COUNT                 = "book_count";
        public static final String EMAIL                      = "email";

        /**
         * The time at which this screen was last seen. Used for google
         * analytics to detect whether to report a screen hit or not on fragment
         * recreation(orientation change/destroyed in background) etc
         */
        public static final String LAST_SCREEN_TIME           = "last_screen_time";
        public static final String OVERLAY_VISIBLE            = "overlay_visible";
    }

    /**
     * Constant interface, DO NOT IMPLEMENT
     * 
     * @author Vinay S Shenoy
     */
    public static interface FragmentTags {
        public static final String BOOKS_AROUND_ME                                 = "books_around_me";
        public static final String ADD_OR_EDIT_BOOK                                = "add_or_edit_book";
        public static final String LOGIN_TO_ADD_BOOK                               = "login_to_add_book";
        public static final String LOGIN_TO_CHAT                                   = "login_to_chat";
        public static final String LOGIN_FROM_NAV_DRAWER                           = "login_from_nav_drawer";
        public static final String SELECT_PREFERRED_LOCATION_FROM_LOGIN            = "select_preferred_location_from_login";
        public static final String SELECT_PREFERRED_LOCATION_FROM_PROFILE          = "select_preferred_location_from_profile";
        public static final String SELECT_PREFERRED_LOCATION_FROM_ADD_OR_EDIT_BOOK = "select_preferred_location_from_add_or_edit_book";
        public static final String OSS_LICENSES                                    = "oss_licenses";
        public static final String SHOW_WEBVIEW                                    = "show_webview";
        public static final String REPORT_BUGS                                     = "report_bugs";
        public static final String SUGGEST_FEATURE                                 = "suggest_feature";
        public static final String COLLABORATE                                     = "collaborate";
        public static final String MY_BOOK_FROM_PROFILE                            = "my_book_from_profile";
        public static final String USER_BOOK_FROM_PROFILE                          = "user_book_from_profile";
        public static final String MY_BOOK_FROM_ADD_OR_EDIT                        = "my_book_from_add_or_edit";
        public static final String BOOK_FROM_BOOKS_AROUND_ME                       = "book_from_books_around_me";
        public static final String PROFILE_FROM_NAV_DRAWER                         = "profile_from_nav_drawer";
        public static final String PROFILE_FROM_CHAT_DETAILS                       = "profile_from_chat_details";
        public static final String PROFILE_FROM_LOGIN                              = "profile_from_login";
        public static final String EDIT_PROFILE                                    = "edit_profile";
        public static final String CHATS                                           = "chats";
        public static final String CHAT_DETAILS                                    = "chat_details";
        public static final String TRIBUTE                                         = "Tribute";
        public static final String TEAM                                            = "Team";
        public static final String ABOUT_ME                                        = "about_me";
        public static final String MY_BOOKS                                        = "my_books";
        public static final String USER_PROFILE                                    = "user_profile";
        public static final String PASSWORD_RESET                                  = "password_reset";

        /* Tags for fragment backstack popping and providing up navigation */
        public static final String BS_BOOKS_AROUND_ME                              = "to_books_around_me";
        public static final String BS_ADD_BOOK                                     = "to_add_book";
        public static final String BS_PREFERRED_LOCATION                           = "to_preferred_location";
        public static final String BS_EDIT_PROFILE                                 = "to_edit_profile";
        public static final String BS_CHATS                                        = "to_chats";
        public static final String BS_EDIT_BOOK                                    = "to_edit_book";
        public static final String BS_PROFILE                                      = "to_profile";
        public static final String BS_BOOK_DETAIL                                  = "to_book_detail";
        public static final String BS_LOGIN_FROM_BOOK_DETAIL                       = "to_login_from_book_detail";

        /* Tags for Dialog fragments */
        public static final String DIALOG_TAKE_PICTURE                             = "dialog_take_picture";
        public static final String DIALOG_ADD_NAME                                 = "dialog_add_name";
        public static final String DIALOG_FORGOT_PASSWORD                          = "dialog_add_name";
        public static final String DIALOG_ADD_BOOK                                 = "dialog_add_book";
        public static final String DIALOG_ENABLE_LOCATION                          = "dialog_enable_location";
        public static final String DIALOG_ABOUT_LOCATION                           = "dialog_about_location";
        public static final String DIALOG_CHAT_LONGCLICK                           = "dialog_chat_longclick";
        public static final String DIALOG_DELETE_BOOK                              = "dialog_delete_book";

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

        public static final int SEARCH_BOOKS              = 201;
        public static final int GET_MY_BOOKS              = 202;
        public static final int ALL_CHATS                 = 203;
        public static final int CHAT_DETAILS              = 204;
        public static final int USER_DETAILS              = 205;
        public static final int SEARCH_BOOKS_ON_PAGER     = 206;
        public static final int USER_DETAILS_ABOUT_ME     = 207;
        public static final int USER_DETAILS_CHAT_DETAILS = 208;
    }

    /**
     * Constant interface. DO NOT IMPLEMENT
     * 
     * @author Vinay S Shenoy
     */
    public static interface QueryTokens {

        // 1-100 for load queries
        public static final int LOAD_LOCATION_FROM_PROFILE_EDIT_PAGE      = 1;
        public static final int LOAD_LOCATION_FROM_PROFILE_SHOW_PAGE      = 2;
        public static final int LOAD_BOOK_DETAIL_CURRENT_USER             = 3;
        public static final int LOAD_BOOK_DETAIL_OTHER_USER               = 4;

        // 101-200 for insert queries

        // 201-300 for update queries
        public static final int UPDATE_MESSAGE_STATUS                     = 201;

        //301-400 for delete queries
        public static final int DELETE_BOOKS_SEARCH_RESULTS               = 301;
        public static final int DELETE_BOOKS_SEARCH_RESULTS_FROM_EDITTEXT = 302;
        public static final int DELETE_CHAT_MESSAGES                      = 303;
        public static final int DELETE_CHATS                              = 304;
        public static final int DELETE_MY_BOOKS                           = 305;
        public static final int DELETE_MY_BOOK                            = 306;
        public static final int DELETE_MY_BOOK_FROM_SEARCH                = 307;
        public static final int DELETE_CHAT_MESSAGE                       = 308;

    }

    /**
     * Constant interface. DO NOT IMPLEMENT
     * 
     * @author Vinay S Shenoy
     */
    public static interface ChatType {

        public static final String PERSONAL = "personal";
        public static final String GROUP    = "group";
    }

    /**
     * Constant interface. DO NOT IMPLEMENT.
     * 
     * @author Vinay S Shenoy
     */
    public static interface ChatStatus {
        //Different types of chat status. Linked to the chat_sending_status of database 
        public static final int SENDING  = 0;
        public static final int SENT     = 1;
        public static final int FAILED   = -1;
        public static final int RECEIVED = 2;
    }

    public static final String PLAY_STORE_LINK        = "https://play.google.com/store/apps/details?id=li.barter";
    public static final String PLAY_STORE_MARKET_LINK = "market://details?id=li.barter";

    public static final String REFERRER_FORMAT        = "&referrer=%s";
}
