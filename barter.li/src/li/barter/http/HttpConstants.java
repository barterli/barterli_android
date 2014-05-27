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

package li.barter.http;

/**
 * @author Vinay S Shenoy Interface that holds all constants related to Http
 *         Requests
 */
public class HttpConstants {

    /**
     * The API version in use by the app
     */
    private static final int API_VERSION = 1;

    /**
     * Enum to switch between servers
     */
    private enum Server {

        LOCAL(
                        "http://192.168.1.138:3000/api/v",
                        API_VERSION,
                        "192.168.1.138",
                        5672),

        DEV(
                        "http://162.243.198.171/api/v",
                        API_VERSION,
                        "162.243.198.171",
                        5672),

        PRODUCTION(
                        "http://107.170.10.25/api/v",
                        API_VERSION,
                        "107.170.10.25",
                        5672);

        public final String mUrl;
        public final String mChatUrl;
        public final int    mChatPort;
        public final String mChatLink       = ":3000/api/v1";
        public final String mGoogleBooksApi = "https://www.googleapis.com/books/v1";
        public final String mFoursquareApi  = "https://api.foursquare.com/v2";

        Server(final String url, final int apiVersion, final String chatUrl, final int chatPort) {
            mUrl = url + apiVersion;
            mChatUrl = chatUrl;
            mChatPort = chatPort;
        }
    }

    private static Server SERVER = Server.DEV;

    public static String getApiBaseUrl() {
        return SERVER.mUrl;
    }

    public static String getChatUrl() {
        return SERVER.mChatUrl;
    }

    public static String getGoogleBooksUrl() {
        return SERVER.mGoogleBooksApi;
    }

    public static String getFoursquareUrl() {
        return SERVER.mFoursquareApi;
    }

    public static String getChangedChatUrl() {
        return "http://" + SERVER.mChatUrl + SERVER.mChatLink;
    }

    public static int getChatPort() {
        return SERVER.mChatPort;
    }

    /**
     * Empty interface to remember all API endpoints
     */
    public static interface ApiEndpoints {
        public static final String BOOK_SUGGESTIONS        = "/book_suggestions.json";
        public static final String BOOK_INFO               = "/book_info.json";
        public static final String BOOKS                   = "/books.json";
        public static final String CREATE_USER             = "/create_user.json";
        public static final String USER_PREFERRED_LOCATION = "/user_preferred_location.json";
        public static final String SEARCH                  = "/search.json";
        public static final String AMPQ_EVENT_MACHINE      = "/ampq.json";
        public static final String AMPQ_START_STOP         = "/ampq1.json";
        public static final String USERPROFILE             = "/user_profile.json";
        public static final String UPDATE_USER_INFO        = "/user_update.json";
        public static final String GET_USER_INFO           = "/current_user_profile";
        public static final String REPORT_BUG              = "/feedback";
        public static final String COLLABORATE_REQUEST     = "/register";
        public static final String TRIBUTE                 = "/tribute.json";
        public static final String TEAM                    = "/team.json";
        public static final String VOLUMES                 = "/volumes";
        public static final String FOURSQUARE_VENUES       = "/venues/search";

    }

    /**
     * GOOGLE BOOKS API SPECIAL SEARCH KEYWORDS
     */
    public static interface GoogleBookSearchKey {
        public static final String INTITLE  = "intitle:";
        public static final String INAUTHOR = "inauthor:";
        public static final String ISBN     = "isbn:";
        public static final String ID       = "id:";

    }

    /**
     * Empty interface to store http request identifiers
     */
    public static interface RequestId {

        public static final int GET_BOOK_INFO               = 100;
        public static final int CREATE_BOOK                 = 101;
        public static final int SEARCH_BOOKS                = 102;
        public static final int CREATE_USER                 = 103;
        public static final int FOURSQUARE_VENUES           = 104;
        public static final int SET_USER_PREFERRED_LOCATION = 105;
        public static final int SAVE_USER_PROFILE           = 110;
        public static final int GET_USER_PROFILE            = 111;
        public static final int REPORT_BUG                  = 112;
        public static final int SUGGEST_FEATURE             = 113;
        public static final int COLLABORATE_REQUEST         = 114;
        public static final int AMPQ                        = 115;
        public static final int TRIBUTE                     = 116;
        public static final int TEAM                        = 117;
        public static final int SEARCH_BOOKS_FROM_EDITTEXT  = 118;
        public static final int BOOK_SUGGESTIONS            = 119;
        public static final int UPDATE_BOOK                 = 120;
        public static final int DELETE_BOOK                 = 121;
        public static final int GOOGLEBOOKS_SHOW_BOOK       = 122;

    }

    public static final String ID                          = "id";
    public static final String FORMAT                      = "format";
    public static final String KEY                         = "key";
    public static final String ISBN_10                     = "isbn_10";
    public static final String ISBN_13                     = "isbn_13";
    public static final String ISBN                        = "isbn";

    public static final String ISBN13                      = "isbn13";
    public static final String Q                           = "q";
    public static final String T                           = "t";
    public static final String TITLE                       = "title";
    public static final String DESCRIPTION                 = "description";
    public static final String VALUE                       = "value";
    public static final String AUTHORS                     = "authors";
    public static final String AUTHOR                      = "author";
    public static final String NAME                        = "name";
    public static final String PUBLICATION_YEAR            = "publication_year";
    public static final String PUBLISHED_DATE              = "publishedDate";
    public static final String PUBLICATION_MONTH           = "publication_month";
    public static final String OWNER_NAME                  = "owner_name";
    public static final String OWNER_IMAGE_URL             = "owner_image_url";
    public static final String IMAGE_URL                   = "image_url";
    public static final String EXT_IMAGE_URL               = "ext_image_url";
    public static final String SEARCH                      = "search";
    public static final String LATITUDE                    = "latitude";
    public static final String LONGITUDE                   = "longitude";
    public static final String RADIUS                      = "radius";
    public static final String PROVIDER                    = "provider";
    public static final String ACCESS_TOKEN                = "access_token";
    public static final String EMAIL                       = "email";
    public static final String PASSWORD                    = "password";
    public static final String AUTH_TOKEN                  = "auth_token";
    public static final String STATUS                      = "status";
    public static final String SUCCESS                     = "success";
    public static final String LOCATION                    = "location";
    public static final String ERROR_CODE                  = "error_code";
    public static final String ERROR_MESSAGE               = "error_message";
    public static final String USER                        = "user";
    public static final String USER_PROFILE                = "user_profile";
    public static final String FIRST_NAME                  = "first_name";
    public static final String LAST_NAME                   = "last_name";
    public static final String COUNTRY                     = "country";
    public static final String STATE                       = "state";
    public static final String CITY                        = "city";
    public static final String ADDRESS                     = "address";
    public static final String POSTAL_CODE                 = "postal_code";
    public static final String LOCALITY                    = "locality";
    public static final String METERS                      = "meters";
    public static final String LOCATIONS                   = "locations";
    public static final String TOKEN                       = "token";
    public static final String BOOK                        = "book";
    public static final String TAG_NAMES                   = "tag_names";
    public static final String TAGS                        = "tags";
    public static final String ID_LOCATION                 = "id_location";
    public static final String ID_BOOK                     = "id_book";
    public static final String ID_USER                     = "id_user";
    public static final String HEADER_AUTHORIZATION_FORMAT = "Token token=\"%s\", email=\"%s\"";
    public static final String HEADER_AUTHORIZATION        = "Authorization";
    public static final String MESSAGE                     = "message";
    public static final String TIME                        = "time";
    public static final String SENDER                      = "sender";
    public static final String RECEIVER                    = "receiver";
    public static final String PROFILE_IMAGE               = "profile_image";
    public static final String PROFILE_PIC                 = "profile";
    public static final String BUG_TITLE                   = "title";
    public static final String BUG_BODY                    = "body";
    public static final String BUG_LABEL                   = "label";
    public static final String LABEL_FOR_BUG               = "bug";
    public static final String LABEL_FOR_FEATURE           = "feature";
    public static final String REGISTER_TYPE               = "register_type";
    public static final String BODY                        = "body";
    public static final String SENDER_ID                   = "sender_id";
    public static final String RECEIVER_ID                 = "receiver_id";
    public static final String TRIBUTE                     = "tribute";
    public static final String TRIBUTE_IMAGE_URL           = "image_url";
    public static final String TRIBUTE_TEXT                = "message";
    public static final String TEAM                        = "team";
    public static final String DESC                        = "desc";
    public static final String BOOKS                       = "books";
    public static final String PAGE                        = "page";
    public static final String PERLIMIT                    = "per";
    public static final String RESULTS                     = "results";
    public static final String WORK                        = "work";
    public static final String BEST_BOOK                   = "best_book";
    public static final String SMALL_IMAGE_URL             = "small_image_url";
    public static final String XML                         = "xml";
    public static final String ITEMS                       = "items";
    public static final String VOLUMEINFO                  = "volumeInfo";
    public static final String IMAGELINKS                  = "imageLinks";
    public static final String THUMBNAIL                   = "thumbnail";
    public static final String INDUSTRY_IDENTIFIERS        = "industryIdentifiers";
    public static final String TYPE                        = "type";
    public static final String IDENTIFIER                  = "identifier";
    public static final String LL                          = "ll";
    public static final String CATEGORY_ID                 = "categoryId";
    public static final String INTENT                      = "intent";
    public static final String BROWSE                      = "browse";
    public static final String CLIENT_ID                   = "client_id";
    public static final String CLIENT_SECRET               = "client_secret";
    public static final String V                           = "v";

}
