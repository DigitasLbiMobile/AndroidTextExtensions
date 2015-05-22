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
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by vrabiee on 12/05/15.
 */
public abstract class ViewExtension<V extends android.view.View> {

    protected final Context mContext;
    protected final AttributeSet mAttrs;
    protected final int mDefStyleAttr;
    protected final int mDefStyleRes;
    protected V mView;

    public ViewExtension(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;
        mAttrs = attrs;
        mDefStyleAttr = defStyleAttr;
        mDefStyleRes = defStyleRes;
    }

    public void setView(V view) {
        mView = view;
    }

    public abstract Extension getExtensionId();

    public abstract void onPreDraw(Canvas canvas);

    public abstract void onDraw(Canvas canvas);

    public abstract void onTouchEvent(MotionEvent event);

    public abstract void onAttachedToWindow();

    public abstract void onDetachedFromWindow();

    public abstract void onFinishInflate();

    public abstract void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect);

    public abstract void drawableStateChanged();

    public abstract void drawableHotspotChanged(float x, float y);

    public abstract void onSizeChanged(int width, int height);

}
