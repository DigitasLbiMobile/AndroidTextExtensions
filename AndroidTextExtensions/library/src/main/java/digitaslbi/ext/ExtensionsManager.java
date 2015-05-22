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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import digitaslbi.ext.drawables.MultiDrawablesExtension;

/**
 * A collection of {@link Extension}s.
 */
public class ExtensionsManager<T extends android.view.View> {

    protected final Map<Extension, ViewExtension<T>> mViewExtensions = new HashMap<>();

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

        if ((extensionFlags & Extension.DRAWABLE_EXTENSION.getId()) != 0) {
            mViewExtensions.put(Extension.DRAWABLE_EXTENSION, new MultiDrawablesExtension<T>(context, attrs, defStyleAttr, defStyleRes));
        }
        array.recycle();
    }

    public void setView(T view) {
        for (ViewExtension<T> extension : mViewExtensions.values()) {
            extension.setView(view);
        }
    }

    public void add(ViewExtension<T> extension, T view) {
        add(extension);
        extension.setView(view);
    }

    public void add(ViewExtension<T> extension) {
        if (!mViewExtensions.containsKey(extension.getExtensionId())) {
            mViewExtensions.put(extension.getExtensionId(), extension);
        }
    }

    public Collection<ViewExtension<T>> get() {
        return mViewExtensions.values();
    }

    public ViewExtension<T> findExtension(Extension extension) {
        return mViewExtensions.get(extension);
    }

}
