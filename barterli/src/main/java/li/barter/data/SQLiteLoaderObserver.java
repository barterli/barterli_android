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
 * SQLite Loader entry for keeping a track of loaders and the tables with which
 * they are associated for notification
 */
public class SQLiteLoaderObserver {
    public SQLiteLoader loader;
    public String       table;

    /**
     * @param loader The {@link SQLiteLoader} to add as the entry
     * @param table The Table name
     */
    public SQLiteLoaderObserver(final SQLiteLoader loader, final String table) {
        this.loader = loader;
        this.table = table;
    }

}
