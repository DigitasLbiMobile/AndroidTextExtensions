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

package digitaslbi.ext.font;

import android.content.Context;
import android.util.AttributeSet;
import digitaslbi.ext.DefaultTextExtension;
import digitaslbi.ext.TextExtension;
import digitaslbi.ext.TextViewExtensions;
import digitaslbi.ext.common.Font;

/**
 * Implements {@link TextExtension} to provide custom fonts for
 * subclasses of {@link android.widget.TextView} via {@link android.graphics.Typeface}.
 */
public class FontExtension<T extends android.widget.TextView> extends DefaultTextExtension<T> {

    public FontExtension(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FontExtension(Context context, int defStyleAttr) {
        super(context, null, defStyleAttr, 0);
    }

    @Override
    public void setTextView(T textView) {
        FontManager.getInstance().applyFont(textView, textView.getContext(), mAttrs, mDefStyleAttr, mDefStyleRes);
    }

    public void applyFont(T textView, Font font) {
        FontManager.getInstance().applyFont(textView, font);
    }

    @Override
    public int getFlag() {
        return TextViewExtensions.FONT_EXTENSION;
    }
}
