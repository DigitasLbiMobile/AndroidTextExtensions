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
 * Created by anatriep on 21/05/2015.
 */
public class BaseViewExtension<T extends android.view.View> extends ViewExtension<T> {

    public BaseViewExtension(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public Extension getExtensionId() {
        return null;
    }

    @Override
    public void onPreDraw(Canvas canvas) {

    }

    @Override
    public void onDraw(Canvas canvas) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }

    @Override
    public void onAttachedToWindow() {

    }

    @Override
    public void onDetachedFromWindow() {

    }

    @Override
    public void onFinishInflate() {

    }

    @Override
    public void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {

    }

    @Override
    public void drawableStateChanged() {

    }

    @Override
    public void drawableHotspotChanged(float x, float y) {

    }

    @Override
    public void onSizeChanged(int width, int height) {

    }
}
