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

    public static final String BOOK_ID     = "book_id";
    public static final String ISBN_10     = "isbn_10";
    public static final String ISBN_13     = "isbn_13";
    public static final String DESCRIPTION = "description";
    public static final String TITLE       = "title";
    public static final String AUTHOR      = "author";
    public static final String BARTER_TYPE = "barter_type";
    public static final String USER_ID     = "user_id";
}
