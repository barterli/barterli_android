/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package li.barter.data;

import android.database.sqlite.SQLiteDatabase;

import java.util.Locale;

/**
 * View representation for my books, combined with their locations
 * 
 * @deprecated This class has been deprecated since the table is not in use
 *             anymore. All data is transfered to
 *             {@link ViewUserBooksWithLocations} as of DB version 2
 * @author Vinay S Shenoy
 */
@Deprecated
public class ViewMyBooksWithLocations {

    private static final String TAG             = "ViewMyBooksWithLocations";

    /* No longer public because this table is deprecared and should not be accessible outside*/
    static final String         NAME            = "MY_BOOKS_WITH_LOCATIONS";

    public static void upgrade(final SQLiteDatabase db, final int oldVersion,
                    final int newVersion) {

        /* Drop the view if upgrading from DB version 1(alpha version) */
        if (oldVersion == 1) {
            db.execSQL(String
                            .format(Locale.US, SQLConstants.DROP_VIEW_IF_EXISTS, NAME));
        }
    }
}
