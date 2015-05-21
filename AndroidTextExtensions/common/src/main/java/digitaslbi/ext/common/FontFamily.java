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

import com.google.common.base.Predicate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterators.any;
import static java.util.Arrays.asList;

/**
 * Defines a collection of fonts under the same name.
 */
public class FontFamily {

    protected final String mName;
    protected final List<Font> mFonts = new ArrayList<Font>();

    public FontFamily(String name) {
        mName = name;
    }

    public FontFamily(String name, Font... fonts) {
        this(name);
        checkNotNull(fonts);
        mFonts.addAll(asList(fonts));
    }

    public String getName() {
        return mName;
    }

    public List<Font> getFonts() {
        return mFonts;
    }

    public void setFonts(Set<Font> fonts) {
        checkNotNull(fonts);
        mFonts.clear();
        mFonts.addAll(fonts);
    }

    public void addFont(Font font) {
        checkNotNull(font);
        if (!mFonts.contains(font)) {
            mFonts.add(font);
        }
    }

    public boolean hasFontWithSameName() {
        return any(mFonts.iterator(), new Predicate<Font>() {
            @Override public boolean apply(Font input) {
                return input.getName().equals(mName);
            }
        });
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("name", getName())
                .add("fonts", getFonts())
                .toString();
    }
}
