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

package digitaslbi.ext.common;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

/**
 * Collection of utility methods for {@link Font} and {@link FontFamily}.
 */
public final class FontUtils {

    public static final String ASSET_SEPARATORS = "-_ ";
    public static final String NAME_SEPARATOR = "_";
    public static final String STYLE_SEPARATOR = ".";
    public static final int NAME_PARTS = 2;

    private FontUtils() {
    }

    public static String cleanupFontName(String name) {
        return Joiner.on(NAME_SEPARATOR).join(Splitter.on(CharMatcher.anyOf(ASSET_SEPARATORS))
                .trimResults()
                .omitEmptyStrings()
                .split(name));
    }

    public static String capitalize(String string) {
        if (string == null || string.length() == 0) {
            return string;
        }
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    public static String nameToStyleName(String fontName) {
        return fontName.replace(NAME_SEPARATOR, STYLE_SEPARATOR);
    }

    public static String styleNameToName(String styleName) {
        return styleName.replace(STYLE_SEPARATOR, NAME_SEPARATOR);
    }
}
