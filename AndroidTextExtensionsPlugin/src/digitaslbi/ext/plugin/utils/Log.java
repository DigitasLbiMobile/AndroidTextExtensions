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

package digitaslbi.ext.plugin.utils;

import com.intellij.openapi.diagnostic.Logger;

/**
 * Created by evelina on 14/05/15.
 */
public final class Log {

    private static final boolean DEBUG = true;

    public static void d(Class<?> caller, String msg, Object... args) {
        if (DEBUG) {
            String formattedMsg = String.format(msg, args);
            System.out.println(tag(caller) + ": " + formattedMsg);
            Logger.getInstance(caller).debug(formattedMsg);
        }
    }

    public static void e(Class<?> caller, Throwable e, String msg, Object... args) {
        if (DEBUG) {
            String formattedMsg = String.format(msg, args);
            System.out.println(tag(caller) + ": " + formattedMsg + "\n" + e.getMessage());
            Logger.getInstance(caller).error(formattedMsg, e);
        }
    }

    public static void e(Class<?> caller, String msg, Object... args) {
        if (DEBUG) {
            String formattedMsg = String.format(msg, args);
            System.out.println(tag(caller) + ": " + formattedMsg);
            Logger.getInstance(caller).error(formattedMsg);
        }
    }

    private static String tag(Class<?> caller) {
        return caller.isAnonymousClass() ? caller.getName() : caller.getCanonicalName();
    }
}
