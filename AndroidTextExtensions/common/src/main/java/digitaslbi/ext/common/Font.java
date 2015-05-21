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


import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import static com.google.common.base.CharMatcher.anyOf;

/**
 * Defines a custom font.
 */
public class Font {

    public static final String ASSET_FILE_SEPARATORS = "-_ ";
    public static final String CLASS_NAME_SEPARATOR = "_";
    public static final String STYLE_NAME_SEPARATOR = ".";
    public static final int NAME_PARTS_COUNT = 2;

    protected final String mName;
    protected final String mAssetName;

    public Font(String name, String assetName) {
        mName = name;
        mAssetName = assetName;
    }

    public String getAssetName() {
        return mAssetName;
    }

    public String getName() {
        return mName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Font font = (Font) o;

        return !(mName != null ? !mName.equals(font.mName) : font.mName != null);

    }

    @Override
    public int hashCode() {
        return mName != null ? mName.hashCode() : 0;
    }

    @Override
    public String toString() {
        return mName;
    }

    public static String cleanupName(String name) {
        return Joiner.on(CLASS_NAME_SEPARATOR)
                .join(Splitter.on(anyOf(ASSET_FILE_SEPARATORS))
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
        return fontName.replace(CLASS_NAME_SEPARATOR, STYLE_NAME_SEPARATOR);
    }

    public static String styleNameToName(String styleName) {
        return styleName.replace(STYLE_NAME_SEPARATOR, CLASS_NAME_SEPARATOR);
    }
}
