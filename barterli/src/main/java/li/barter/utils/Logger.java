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

import android.os.SystemClock;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import li.barter.BuildConfig;

/**
 * Logging helper class. Repurposed from the AOSP Volley source
 */
public class Logger {

    public static final String DEFAULT_TAG = "Barter.Li";

    /**
     * Pass object "this" and message to print
     *
     * @param object
     * @param message
     */
    public static void v(Object object, String message) {
        if (BuildConfig.DEBUG_MODE) {
            Log.v(getTag(object), message);
        }
        Crashlytics.log(1, getTag(object), message);
    }

    public static void v(final String tag, final String format,
                         final Object... args) {
        if (BuildConfig.DEBUG_MODE) {
            Log.v(tag, buildMessage(format, args));
        }
    }

    /**
     * Pass object "this" and message to print
     *
     * @param object
     * @param message
     */
    public static void d(Object object, String message) {
        if (BuildConfig.DEBUG_MODE) {
            Log.d(getTag(object), message);
        }
        Crashlytics.log(1, getTag(object), message);
    }

    public static void d(final String tag, final String format,
                         final Object... args) {
        if (BuildConfig.DEBUG_MODE) {
            Log.d(tag, buildMessage(format, args));
        }
    }

    /**
     * Pass object "this" print message and exception
     *
     * @param object
     * @param message
     * @param throwable
     */
    public static void e(Object object, String message, Throwable throwable) {
        if (BuildConfig.DEBUG_MODE) {
            if (message != null) {
                Log.e(getTag(object), message, throwable);
            } else {
                Log.e(getTag(object), null + " ", throwable);
            }
        }
        Crashlytics.logException(throwable);
    }

    public static void e(Object object, Throwable throwable) {
        e(object, null, throwable);
    }

    public static void e(Throwable throwable) {
        e(null, null, throwable);
    }

    public static void e(final String tag, final String format,
                         final Object... args) {
        Log.e(tag, buildMessage(format, args));
    }

    public static void e(final String tag, final Throwable tr,
                         final String format, final Object... args) {
        Log.e(tag, buildMessage(format, args), tr);
    }

    public static void w(final String tag, final String format,
                         final Object... args) {
        Log.w(tag, buildMessage(format, args));
    }

    public static void w(final String tag, final Throwable tr,
                         final String format, final Object... args) {
        Log.w(tag, buildMessage(format, args), tr);
    }

    public static void i(final String tag, final String format,
                         final Object... args) {
        if (BuildConfig.DEBUG_MODE) {
            Log.i(tag, buildMessage(format, args));
        }
    }

    public static String getTag(Object object) {
        if (object == null) {
            return DEFAULT_TAG;
        }
        return object.getClass().getSimpleName();
    }

    /**
     * Formats the caller's provided message and prepends useful info like
     * calling thread ID and method name.
     */
    private static String buildMessage(final String format,
                                       final Object... args) {
        final String msg = (args == null) ? format : String
                .format(Locale.US, format, args);
        final StackTraceElement[] trace = new Throwable().fillInStackTrace()
                .getStackTrace();

        String caller = "<unknown>";
        // Walk up the stack looking for the first caller outside of VolleyLog.
        // It will be at least two frames up, so start there.
        for (int i = 2; i < trace.length; i++) {
            final Class<?> clazz = trace[i].getClass();
            if (!clazz.equals(Logger.class)) {
                String callingClass = trace[i].getClassName();
                callingClass = callingClass.substring(callingClass
                        .lastIndexOf('.') + 1);
                callingClass = callingClass.substring(callingClass
                        .lastIndexOf('$') + 1);

                caller = callingClass + "." + trace[i].getMethodName();
                break;
            }
        }
        return String.format(Locale.US, "[%d] %s: %s", Thread.currentThread()
                .getId(), caller, msg);
    }

    /**
     * A simple event log with records containing a name, thread ID, and
     * timestamp.
     */
    public static class MarkerLog {

        private static final String TAG = "MarkerLog";

        /**
         * Minimum duration from first marker to last in an marker log to
         * warrant logging.
         */
        private static final long MIN_DURATION_FOR_LOGGING_MS = 0;

        private static class Marker {
            public final String name;
            public final long thread;
            public final long time;

            public Marker(final String name, final long thread, final long time) {
                this.name = name;
                this.thread = thread;
                this.time = time;
            }
        }

        private final List<Marker> mMarkers = new ArrayList<Marker>();
        private boolean mFinished = false;

        /**
         * Adds a marker to this log with the specified name.
         */
        public synchronized void add(final String name, final long threadId) {
            if (mFinished) {
                throw new IllegalStateException("Marker added to finished log");
            }

            mMarkers.add(new Marker(name, threadId, SystemClock
                    .elapsedRealtime()));
        }

        /**
         * Closes the log, dumping it to logcat if the time difference between
         * the first and last markers is greater than
         * {@link #MIN_DURATION_FOR_LOGGING_MS}.
         *
         * @param header Header string to print above the marker log.
         */
        public synchronized void finish(final String header) {
            mFinished = true;

            final long duration = getTotalDuration();
            if (duration <= MIN_DURATION_FOR_LOGGING_MS) {
                return;
            }

            long prevTime = mMarkers.get(0).time;
            d(TAG, "(%-4d ms) %s", duration, header);
            for (final Marker marker : mMarkers) {
                final long thisTime = marker.time;
                d(TAG, "(+%-4d) [%2d] %s", (thisTime - prevTime), marker.thread, marker.name);
                prevTime = thisTime;
            }
        }

        @Override
        protected void finalize() throws Throwable {
            // Catch requests that have been collected (and hence end-of-lifed)
            // but had no debugging output printed for them.
            if (!mFinished) {
                finish("Marker on the loose!");
                e(TAG, "Marker log finalized without finish() - uncaught exit point for marker");
            }
        }

        /**
         * Returns the time difference between the first and last events in this
         * log.
         */
        private long getTotalDuration() {
            if (mMarkers.size() == 0) {
                return 0;
            }

            final long first = mMarkers.get(0).time;
            final long last = mMarkers.get(mMarkers.size() - 1).time;
            return last - first;
        }
    }
}
