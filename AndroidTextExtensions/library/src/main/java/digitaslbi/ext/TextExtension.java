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
import android.util.AttributeSet;

/**
 * Created by vrabiee on 12/05/15.
 */
public abstract class TextExtension<T extends android.widget.TextView> {

    protected T mTextView;
    protected Context mContext;
    protected AttributeSet mAttrs;
    protected int mDefStyleAttr;
    protected int mDefStyleRes;

    public TextExtension(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;
        mAttrs = attrs;
        mDefStyleAttr = defStyleAttr;
        mDefStyleRes = defStyleRes;
    }

    /**
     * @param textView the view on which to apply the extension
     */
    public void setTextView(T textView) {
        mTextView = textView;
    }

    /**
     * Each extension is uniquely identified by a bit flag.
     * A view can specify multiple extensions by turning on multiple flags in XML.
     * For example: <code><item name="extensions">font|border</item></code> will turn on the
     * {@link digitaslbi.ext.font.FontExtension} and the {@link BorderExtension}.
     *
     * @return the unique bit flag associated with the extension
     */
    public abstract int getFlag();
}
