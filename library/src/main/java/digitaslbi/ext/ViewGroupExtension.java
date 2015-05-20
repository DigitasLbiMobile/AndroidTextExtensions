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
public abstract class ViewGroupExtension<V extends android.view.ViewGroup> {

    protected V mViewGroup;
    protected Context mContext;
    protected AttributeSet mAttrs;
    protected int mDefStyleAttr;
    protected int mDefStyleRes;

    public ViewGroupExtension(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;
        mAttrs = attrs;
        mDefStyleAttr = defStyleAttr;
        mDefStyleRes = defStyleRes;
    }

    public void setViewGroup(V viewGroup) {
        mViewGroup = viewGroup;
    }

    public abstract int getFlag();
}
