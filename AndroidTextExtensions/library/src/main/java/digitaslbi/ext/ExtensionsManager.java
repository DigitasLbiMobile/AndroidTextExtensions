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

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import digitaslbi.ext.common.Font;
import digitaslbi.ext.font.FontExtension;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A collection of
 */
public class ExtensionsManager <T extends android.view.View>{

    public static final int FONT_EXTENSION = 0x01;
    public static final int BORDER_EXTENSION = 0x02;
    public static final int CLEARABLE_EXTENSION = 0x04;
    public static final int DRAWABLE_EXTENSION = 0x08;

    protected final Map<Integer, ViewExtension<T>> mViewExtensions = new HashMap<>();



    public void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes, int fallbackStyleAttr, T textView) {
        collect(context, attrs, defStyleAttr, defStyleRes);
        if (get().isEmpty()) {
            collect(context, attrs, fallbackStyleAttr, 0);
        }
        setView(textView);
    }

    public void collect(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        if (attrs == null) {
            L.w(this, "No extensions to collect, the AttributeSet is null.");
            return;
        }
        TypedArray array = context.obtainStyledAttributes(attrs, new int[]{R.attr.extensions}, defStyleAttr, defStyleRes);
        int extensionFlags = array.getInt(0, 0);
        //TODO MANAGE OTHER EXTENSIONS HERE
        array.recycle();
    }

    public void setView(T view) {
        L.d(this, "Initializing extensions for %s: %s", view, mViewExtensions);
        for (ViewExtension<T> extension : mViewExtensions.values()) {
            extension.setView(view);
        }
    }

    public void add(ViewExtension<T> extension, T view) {
        add(extension);
        extension.setView(view);
    }

    public void add(ViewExtension<T> extension) {
        if (!mViewExtensions.containsKey(extension.getFlag())) {
            mViewExtensions.put(extension.getFlag(), extension);
        }
    }

    public Collection<ViewExtension<T>> get() {
        return mViewExtensions.values();
    }

}
