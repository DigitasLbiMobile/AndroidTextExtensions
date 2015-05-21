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
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import java.util.ArrayList;

/**
 * Created by anatriep on 21/05/2015.
 */
public class DrawableManager extends ViewExtension<View> {

    private static final Rect sRectZero = new Rect();

    public DrawableManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mDrawables = new ArrayList<>();
        mTotalDrawableInsets = new Rect();
    }

    private static class DrawableRepresentation implements Comparable<DrawableRepresentation> {

        private final int mZOrder;
        private final Drawable mDrawable;
        private final Rect mInsets;

        @Override
        public int compareTo(DrawableRepresentation drawableRepresentation) {
            return this.mZOrder - drawableRepresentation.mZOrder;
        }

        private DrawableRepresentation(Builder builder) {
            this.mZOrder = builder.zOrder;
            this.mDrawable = builder.drawable;
            this.mInsets = builder.insets;
        }

        public int getZOrder() {
            return mZOrder;
        }

        public Drawable getDrawable() {
            return mDrawable;
        }

        public Rect getInsets() {
            return mInsets;
        }


        public static class Builder {
            private int zOrder;
            private Drawable drawable;
            private Rect insets;

            public Builder withZOrder(int zOrder) {
                this.zOrder = zOrder;
                return this;
            }

            public Builder withDrawable(Drawable drawable) {
                this.drawable = drawable;
                return this;
            }

            public Builder withInsets(Rect insets) {
                this.insets = insets;
                return this;
            }

            public DrawableRepresentation build() {
                DrawableRepresentation representation = new DrawableRepresentation(this);
                return representation;
            }
        }

    }


    private Rect mTotalDrawableInsets;
    private ArrayList<DrawableRepresentation> mDrawables;


    public void addDrawable(Drawable drawable, Rect insets, int zOrder) {
        if (insets == null) {
            insets = sRectZero;
        }
        DrawableRepresentation representation = new DrawableRepresentation.Builder()
                .withDrawable(drawable)
                .withInsets(insets)
                .withZOrder(zOrder)
                .build();
        int idx = 0;
        while (idx < mDrawables.size() && (mDrawables.get(idx).compareTo(representation) <= 0)) {
            idx++;
        }
        mDrawables.add(idx, representation);
        if (computeInsets()) {
            invalidateViewPadding();
        }
    }

    private void invalidateViewPadding() {
        //mView.setPadding(mTotalDrawableInsets.left, mTotalDrawableInsets.top, mTotalDrawableInsets.right, mTotalDrawableInsets.bottom);
    }

    @Override
    public int getFlag() {
        return 0;
    }

    /**
     * @return the total insets that were requested by the drawables
     */
    public Rect getInsets() {
        return mTotalDrawableInsets;
    }


    /**
     * @return true if the insets changed
     */

    private boolean computeInsets() {
        int left = 0;
        int right = 0;
        int top = 0;
        int bottom = 0;
        for (DrawableRepresentation drawableRepresentation : mDrawables) {
            Rect insets = drawableRepresentation.getInsets();
            if (insets.left > left) {
                left = insets.left;
            }
            if (insets.right > right) {
                right = insets.right;
            }
            if (insets.top > top) {
                top = insets.top;
            }
            if (insets.bottom > bottom) {
                bottom = insets.bottom;
            }
        }
        boolean changed = mTotalDrawableInsets.left != left ||
                mTotalDrawableInsets.right != right ||
                mTotalDrawableInsets.bottom != bottom ||
                mTotalDrawableInsets.top != top;

        if (changed) {
            mTotalDrawableInsets.set(left, top, right, bottom);
        }
        return changed;
    }
}
