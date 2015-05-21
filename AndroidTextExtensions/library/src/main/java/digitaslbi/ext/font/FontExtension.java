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
import digitaslbi.ext.ExtensionsManager;
import digitaslbi.ext.ViewExtension;
import digitaslbi.ext.common.Font;

/**
 * Implements {@link ViewExtension} to provide custom fonts for
 * subclasses of {@link android.widget.TextView} via {@link android.graphics.Typeface}.
 */
public class FontExtension<T extends android.widget.TextView> extends ViewExtension<T> {

    public FontExtension(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public FontExtension(Context context, int defStyleAttr) {
        super(context, null, defStyleAttr, 0);
    }

    @Override
    public void setView(T textView) {
        FontManager.getInstance().applyFont(textView, textView.getContext(), mAttrs, mDefStyleAttr, mDefStyleRes);
    }

    public void applyFont(T textView, Font font) {
        FontManager.getInstance().applyFont(textView, font);
    }

    @Override
    public int getFlag() {
        return ExtensionsManager.FONT_EXTENSION;
    }
}
