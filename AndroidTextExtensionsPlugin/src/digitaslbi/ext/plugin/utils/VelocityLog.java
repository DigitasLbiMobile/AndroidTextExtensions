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

import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.log.LogChute;

/**
 * @author Evelina Vrabie on 22/05/15.
 */
public class VelocityLog implements LogChute {

    @Override public void init(RuntimeServices runtimeServices) throws Exception {

    }

    @Override public void log(int i, String s) {
        Log.d(getClass(), s);
    }

    @Override public void log(int i, String s, Throwable throwable) {
        Log.e(getClass(), throwable, s);
    }

    @Override public boolean isLevelEnabled(int i) {
        return true;
    }
}
