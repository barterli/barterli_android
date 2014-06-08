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
    }

    public static interface ParamKeys {
        public static final String TYPE      = "type";
        public static final String LOGGED_IN = "logged_in";
    }

    public static interface ParamValues {
        public static final String FACEBOOK = "facebook";
        public static final String GOOGLE   = "google";
        public static final String EMAIL    = "email";
        public static final String PROFILE  = "profile";
        public static final String BOOK     = "book";
        public static final String MANUAL   = "manual";
        public static final String SCAN     = "scan";
        public static final String YES      = "yes";
        public static final String NO       = "no";
    }
}
