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

package digitaslbi.ext.drawables;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import java.util.ArrayList;

import digitaslbi.ext.BaseViewExtension;
import digitaslbi.ext.Extension;

/**
 * Created by anatriep on 21/05/2015.
 */
public class MultiDrawablesExtension<T extends android.view.View> extends BaseViewExtension<T> {

    public static final Rect EMPTY_RECT = new Rect();
    public static final int Z_ORDER_VIEW_DRAWING = 100;

    private ArrayList<DrawableRepresentation> mDrawables;
    private Rect mTotalDrawableInsets;

    public MultiDrawablesExtension(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mDrawables = new ArrayList<>();
        mTotalDrawableInsets = new Rect();
        //TODO READ DRAWABLES LIST TO ADD HERE FROM ATTRS
    }


    public DrawableRepresentation addDrawable(Drawable drawable, Rect insets, int zOrder) {
        if (insets == null) {
            insets = EMPTY_RECT;
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
            mView.requestLayout();
        }
        drawable.setCallback(mView);
        return representation;
    }

    public void removeDrawable(DrawableRepresentation drawable) {
        mDrawables.remove(drawable);
    }


    @Override
    public Extension getExtensionId() {
        return Extension.DRAWABLE_EXTENSION;
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


    @Override
    public void onPreDraw(Canvas canvas) {
        super.onPreDraw(canvas);
        for (DrawableRepresentation representation : mDrawables) {
            if (representation.getZOrder() < Z_ORDER_VIEW_DRAWING) {
                representation.getDrawable().setBounds(0, 0, mView.getWidth(), mView.getHeight());
                representation.getDrawable().draw(canvas);
            }
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (DrawableRepresentation representation : mDrawables) {
            if (representation.getZOrder() > Z_ORDER_VIEW_DRAWING) {
                representation.getDrawable().setBounds(0, 0, mView.getWidth(), mView.getHeight());
                representation.getDrawable().draw(canvas);
            }
        }
    }

    @Override
    public void onSizeChanged(int width, int height) {
        super.onSizeChanged(width, height);
        for (DrawableRepresentation representation : mDrawables) {
            if (representation.getZOrder() > Z_ORDER_VIEW_DRAWING) {
                representation.getDrawable().setBounds(0, 0, width, height);
            }
        }
    }

    @Override
    public void drawableStateChanged() {
        super.drawableStateChanged();
        for (DrawableRepresentation representation : mDrawables) {
            int[] state=mView.getDrawableState();
            representation.getDrawable().setState(state);
        }
    }

    public boolean verifyDrawable(Drawable who) {
        for (DrawableRepresentation representation : mDrawables) {
            if(representation.getDrawable().equals(who)){
                return true;
            }
        }
        return false;
    }
}
