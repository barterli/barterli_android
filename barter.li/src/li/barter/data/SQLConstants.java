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
 * Constant interface to hold SQL Strings. Do NOT implement.
 * 
 * @author Vinay S Shenoy
 */
public interface SQLConstants {
    public static final String CREATE_TABLE         = "CREATE TABLE IF NOT EXISTS %s (%s);";
    public static final String CREATE_VIEW          = "CREATE VIEW IF NOT EXISTS %s AS %s;";
    public static final String SELECT_FROM_WHERE    = "SELECT %s FROM %s WHERE %s";
    public static final String TABLE_ALIAS          = "%s %s";
    public static final String ALIAS_COLUMN         = "%s.%s";
    public static final String DROP_TABLE_IF_EXISTS = "DROP TABLE IF EXISTS %s;";
    public static final String DROP_VIEW_IF_EXISTS  = "DROP VIEW IF EXISTS %s;";
    public static final String DATA_TEXT            = "%s TEXT DEFAULT '%s' ";
    public static final String DATA_INTEGER         = "%s INTEGER DEFAULT %d ";
    public static final String DATA_REAL            = "%s REAL DEFAULT %f ";
    public static final String DATA_INTEGER_PK      = "%s INTEGER PRIMARY KEY AUTOINCREMENT ";
    public static final String ASCENDING            = " ASC ";
    public static final String DESCENDING           = " DESC ";
    public static final String LIKE_ARG             = " LIKE ?";
    public static final String AND                  = " AND ";
    public static final String OR                   = " OR ";
    public static final String EQUALS_ARG           = "=?";
    public static final String NOT_EQUALS_ARG       = "!=?";
    public static final String EQUALS_DQUOTE        = "=\"";
    public static final String FROM                 = " FROM ";
    public static final String WHERE                = " WHERE ";
    public static final String IN                   = " IN ";
    public static final String SELECT               = " SELECT ";
    public static final String GROUP_BY             = "GROUP_BY ";
    public static final String HAVING               = "HAVING";
    public static final String LIMIT                = "LIMIT";
    public static final String DISTINCT             = "DISTINCT";
    public static final String PARANTHESIS_OPEN     = "(";
    public static final String PARANTHESES_CLOSE    = ")";
    public static final String LESS_THAN            = "<";
    public static final String GREATER_THAN         = ">";
    public static final String EQUALS               = "=";
    public static final String COMMA                = ",";
    public static final String SLASH                = "/";
    public static final String STAR                 = "*";
    public static final String PERCENT              = "%";
    public static final String DQUOTE               = "\"";
    public static final String QUOTE                = "'";
    public static final String SEMI_COLON           = ";";
    public static final String STRING_SLASH         = "\\";
}
