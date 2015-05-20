/*
 * Copyright (c) 2015 DigitasLBi.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package digitaslbi.ext;


import android.util.Log;

/**
 * Logging utility.
 */
public final class L {

    private static final String TAG = "EXT-";

    public static void d(Object caller, String msg, Object... args) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG + caller.getClass().getSimpleName(), String.format(msg, args));
        }
    }

    public static void w(Object caller, String msg, Object... args) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG + caller.getClass().getSimpleName(), String.format(msg, args));
        }
    }

}
