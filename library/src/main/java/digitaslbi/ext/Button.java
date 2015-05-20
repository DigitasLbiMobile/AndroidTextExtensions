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

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import digitaslbi.ext.common.Font;

import java.util.Collection;

/**
 * Extends {@link android.widget.Button} to delegate functionality to a collection
 * of extensions based on {@link TextExtension}.
 */
public class Button extends android.widget.Button {

    public static final int DEFAULT_STYLE_ATTR = android.R.attr.buttonStyle;
    protected TextViewExtensions<android.widget.Button> mExtensions = new TextViewExtensions<>();

    public Button(Context context) {
        super(context);
        mExtensions.init(context, null, 0, 0, DEFAULT_STYLE_ATTR, this);
    }

    public Button(Context context, AttributeSet attrs) {
        super(context, attrs);
        mExtensions.init(context, attrs, 0, 0, DEFAULT_STYLE_ATTR, this);
    }

    public Button(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mExtensions.init(context, attrs, defStyleAttr, 0, DEFAULT_STYLE_ATTR, this);
    }

    @TargetApi(VERSION_CODES.LOLLIPOP)
    public Button(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mExtensions.init(context, attrs, defStyleAttr, defStyleRes, DEFAULT_STYLE_ATTR, this);
    }

    public Collection<TextExtension<android.widget.Button>> getExtensions() {
        return mExtensions.get();
    }

    @SuppressWarnings("unchecked")
    public <E extends TextExtension<android.widget.Button>> E getExtension(Class<E> clazz) {
        for (TextExtension<android.widget.Button> extension : getExtensions()) {
            if (extension.getClass().equals(clazz)) {
                return (E) extension;
            }
        }
        return null;
    }

    public void addExtension(TextExtension<android.widget.Button> extension) {
        mExtensions.add(extension, this);
    }

    public void setFont(Font font) {
        mExtensions.setFont(font, this, DEFAULT_STYLE_ATTR);
    }

}
