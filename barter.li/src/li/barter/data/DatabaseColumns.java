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

package li.barter.data;

/**
 * Constant interface to hold table columns. DO NOT IMPLEMENT.
 * 
 * @author Vinay S Shenoy
 */
public interface DatabaseColumns {

    public static final String ID                = "id";
    public static final String BOOK_ID           = "book_id";
    public static final String ISBN_10           = "isbn_10";
    public static final String ISBN_13           = "isbn_13";
    public static final String DESCRIPTION       = "description";
    public static final String TITLE             = "title";
    public static final String VALUE             = "value";
    public static final String AUTHOR            = "author";
    public static final String BARTER_TYPE       = "barter_type";
    public static final String USER_ID           = "user_id";
    public static final String IMAGE_URL         = "image_url";
    public static final String LOCATION_ID       = "location_id";
    public static final String NAME              = "name";
    public static final String ADDRESS           = "address";
    public static final String LATITUDE          = "latitude";
    public static final String LONGITUDE         = "longitude";
    public static final String PUBLICATION_YEAR  = "publication_year";
    public static final String PUBLICATION_MONTH = "publication_month";
    public static final String SENDER_ID         = "sender_id";
    public static final String RECEIVER_ID       = "receiver_id";
    public static final String FIRST_NAME        = "first_name";
    public static final String LAST_NAME         = "last_name";
    public static final String PROFILE_PICTURE   = "profile_picture";
    public static final String TIMESTAMP         = "timestamp";
    public static final String MESSAGE           = "message";
    public static final String TIMESTAMP_HUMAN   = "timestamp_human";
    public static final String TIMESTAMP_EPOCH   = "timestamp_epoch";
    public static final String CHAT_ID           = "chat_id";
    public static final String CHAT_TYPE         = "chat_type";
    public static final String LAST_MESSAGE_ID   = "last_message_id";
    public static final String UNREAD_COUNT      = "unread_count";
    public static final String OWNER             = "book_owner";

}
