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
import android.widget.*;

import digitaslbi.ext.common.Font;
import digitaslbi.ext.font.FontExtension;

/**
 * Created by anatriep on 21/05/2015.
 */
public class TextViewExtensionsManager<T extends android.widget.TextView> extends ExtensionsManager<T>{

    @Override
    public void collect(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super.collect(context,attrs,defStyleAttr,defStyleRes);

        TypedArray array = context.obtainStyledAttributes(attrs, new int[]{R.attr.extensions}, defStyleAttr, defStyleRes);
        int extensionFlags = array.getInt(0, 0);
        if ((extensionFlags & FONT_EXTENSION) != 0) {
            mViewExtensions.put(FONT_EXTENSION, new FontExtension<T>(context, attrs, defStyleAttr, defStyleRes));
        }
        array.recycle();
    }


    public void setFont(Font font, T textView, int defaultStyleAttr) {
        boolean found = false;
        for (ViewExtension<T> extension : get()) {
            if (extension instanceof FontExtension) {
                found = true;
                ((FontExtension<T>) extension).applyFont(textView, font);
            }
        }
        if (!found) {
            FontExtension<T> fontExtension = new FontExtension<>(textView.getContext(), defaultStyleAttr);
            fontExtension.applyFont(textView, font);
            add(fontExtension);
        }
    }

}
