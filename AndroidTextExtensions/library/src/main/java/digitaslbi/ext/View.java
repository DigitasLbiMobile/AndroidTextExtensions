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
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

/**
 * Created by anatriep on 21/05/2015.
 */
public class View extends android.view.View {

    private Rect mIntrinsicPadding;

    public View(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public View(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public View(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public View(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        updateIntrinsicPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

    public void addDrawable(Drawable drawable, int zOrder) {
        //TODO
    }

    public void removeDrawable(Drawable drawable) {

    }


    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        updateIntrinsicPadding(left, top, right, bottom);
        super.setPadding(left, top, right, bottom);
    }

    private void updateIntrinsicPadding(int left, int top, int right, int bottom) {
        if (mIntrinsicPadding == null) {
            mIntrinsicPadding = new Rect();
        }
        mIntrinsicPadding.set(left, top, right, bottom);
    }


}
