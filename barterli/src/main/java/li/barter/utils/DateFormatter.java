/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package li.barter.utils;

import android.text.TextUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Class to simplify data formatting. This class accepts date strings in a given
 * format(See {@link SimpleDateFormat} for examples for valid symbols) and
 * outputs them to a requested output pattern or an epoch timestamp
 * 
 * @author Vinay S Shenoy
 */
public class DateFormatter {

    private static final String    TAG = "DateFormatter";

    private final SimpleDateFormat mInputTimestampParser;

    private SimpleDateFormat       mOutputTimestampParser;

    /**
     * Constructs a {@link DateFormatter} with an input pattern
     * 
     * @param inPattern A pattern representing the input timestamps. See
     *            {@link SimpleDateFormat} for an example for applicable
     *            symbols. Cannot be <code>null</code> or empty.
     * @param outPattern A pattern representing the output timestamps. See
     *            {@link SimpleDateFormat} for an example for applicable symbols
     * @throws IllegalArgumentException if the pattern is invalid
     */
    public DateFormatter(final String inPattern, final String outPattern) {

        assert ((inPattern != null) && !inPattern.equals(""));
        mInputTimestampParser = new SimpleDateFormat(inPattern, Locale.getDefault());

        if (!TextUtils.isEmpty(outPattern)) {
            mOutputTimestampParser = new SimpleDateFormat(outPattern, Locale.getDefault());
        }
    }

    /**
     * Gets the epoch(UNIX) timestamp representation for the particular
     * timestamp
     * 
     * @param timestamp The input timestamp. Must match the pattern provided in
     *            the constructor of the class
     * @return The UNIX timestamp representation
     * @throws ParseException If the timestamp cannot be parsed
     */
    public long getEpoch(final String timestamp) throws ParseException {

        return mInputTimestampParser.parse(timestamp).getTime() / 1000;
    }

    /**
     * Getsthe output timestamp representation for the given timestamp
     * 
     * @param timestamp The input timestamp. Must match the pattern provided in
     *            the constructor of the class
     * @return The output timestamp. In order to use this method, the output
     *         timestamp must have been set via the constructor
     * @throws ParseException If the timestamp cannot be parsed
     */
    public String getOutputTimestamp(final String timestamp)
                    throws ParseException {
        return mOutputTimestampParser.format(mInputTimestampParser
                        .parse(timestamp));
    }

}
