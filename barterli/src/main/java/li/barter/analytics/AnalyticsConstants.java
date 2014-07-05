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

package li.barter.analytics;

/**
 * @author Vinay S Shenoy
 */
public class AnalyticsConstants {

    public static interface Screens {

        public static final String SCAN_ISBN                 = "Scan ISBN";
        public static final String ABOUT_CURRENT_USER        = "About Current User";
        public static final String ABOUT_OTHER_USER          = "About Other User";
        public static final String ABOUT_US_PAGER            = "About Us Pager";
        public static final String EDIT_BOOK                 = "Edit Book";
        public static final String ADD_BOOK                  = "Add Book";
        public static final String BARTERLI_DESCRIPTION      = "barter.li Description";
        public static final String BOOK_DETAIL               = "Book Detail";
        public static final String BOOKS_AROUND_ME           = "Books Around Me";
        public static final String BOOKS_PAGER               = "Books Pager";
        public static final String CHAT_DETAILS              = "Chat Details";
        public static final String CHATS                     = "Chats";
        public static final String COLLABORATE               = "Collaborate";
        public static final String EDIT_PROFILE              = "Edit Profile";
        public static final String LOGIN                     = "Login";
        public static final String CURRENT_USER_BOOKS        = "Current User Books";
        public static final String OTHER_USER_BOOKS          = "Other User Books";
        public static final String OPEN_SOURCE               = "Open Source";
        public static final String CURRENT_USER_PROFILE      = "Current User Profile";
        public static final String REPORT_BUG                = "Report Bug";
        public static final String SELECT_PREFERRED_LOCATION = "Select Preferred Location";
        public static final String TEAM                      = "Team";
        public static final String TRIBUTE                   = "Tribute";
        public static final String PASSWORD_RESET            = "Password Reset";
        public static final String SETTINGS                  = "Settings";

    }

    public static interface Categories {
        public static final String CONVERSION = "CONVERSION";
        public static final String USAGE      = "USAGE";
    }

    public static interface Actions {
        public static final String SIGN_IN_ATTEMPT     = "SIGN_IN_ATTEMPT";
        public static final String CHAT_INITIALIZATION = "CHAT_INITIALIZATION";
        public static final String ADD_BOOK            = "ADD_BOOK";
        public static final String BOOK_PROFILE_CLICK  = "BOOK_PROFILE_CLICK";
        public static final String NAVIGATION_OPTION   = "NAVIGATION_OPTION";
    }

    public static interface ParamKeys {
        public static final String TYPE      = "type";
        public static final String LOGGED_IN = "logged_in";
    }

    public static interface ParamValues {
        public static final String FACEBOOK   = "facebook";
        public static final String RESET      = "reset";
        public static final String GOOGLE     = "google";
        public static final String EMAIL      = "email";
        public static final String PROFILE    = "profile";
        public static final String BOOK       = "book";
        public static final String MANUAL     = "manual";
        public static final String SCAN       = "scan";
        public static final String YES        = "yes";
        public static final String NO         = "no";
        public static final String CHATS      = "chats";
        public static final String REPORT_BUG = "report";
        public static final String SHARE      = "share";
        public static final String ABOUT_US   = "about_us";
        public static final String RATE_US    = "rate_us";
    }
}
