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
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

import java.util.Collection;

import digitaslbi.ext.common.Font;
import digitaslbi.ext.drawables.DrawableRepresentation;
import digitaslbi.ext.drawables.MultiDrawablesExtension;
import digitaslbi.ext.font.FontExtension;

/**
 * Extends {@link android.widget.TextView} to delegate functionality to a collection
 * of extensions based on {@link ViewExtension}.
 */
public class TextView extends android.widget.TextView {

    public static final int DEFAULT_STYLE_ATTR = android.R.attr.textViewStyle;

    protected TextViewExtensionsManager<android.widget.TextView> mExtensions = new TextViewExtensionsManager<>();

    private Rect mIntrinsicPadding;

    public TextView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public TextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public TextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mExtensions.init(context, attrs, defStyleAttr, defStyleRes, DEFAULT_STYLE_ATTR, this);
    }

    public Collection<ViewExtension<android.widget.TextView>> getExtensions() {
        return mExtensions.get();
    }

    public <E extends ViewExtension<android.widget.TextView>> E getExtension(Extension extensionId) {
        return (E) mExtensions.findExtension(extensionId);
    }

    public void addExtension(ViewExtension<android.widget.TextView> extension) {
        mExtensions.add(extension, this);
    }

    public DrawableRepresentation addDrawable(Drawable drawable, Rect insets, int zOrder) {
        MultiDrawablesExtension<android.widget.TextView> drawablesExtension = (MultiDrawablesExtension<android.widget.TextView>) mExtensions.findExtension(Extension.DRAWABLE_EXTENSION);
        if (drawablesExtension == null) {
            drawablesExtension = new MultiDrawablesExtension<>(getContext(), null, 0, 0);
            drawablesExtension.setView(this);
            mExtensions.add(drawablesExtension);
        }
        return drawablesExtension.addDrawable(drawable, insets, zOrder);
    }

    public void removeDrawable(DrawableRepresentation drawable) {
        MultiDrawablesExtension<android.widget.TextView> drawablesExtension = (MultiDrawablesExtension<android.widget.TextView>) mExtensions.findExtension(Extension.DRAWABLE_EXTENSION);
        if (drawablesExtension != null) {
            drawablesExtension.removeDrawable(drawable);
        }
    }

    public void setFont(Font font) {
        FontExtension<android.widget.TextView> fontExtension = (FontExtension<android.widget.TextView>) mExtensions.findExtension(Extension.FONT_EXTENSION);
        if (fontExtension == null) {
            fontExtension = new FontExtension<>(getContext(), DEFAULT_STYLE_ATTR);
            mExtensions.add(fontExtension);
        }
        fontExtension.applyFont(this, font);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (!MultiDrawablesExtension.EMPTY_RECT.equals(getExtraPadding())) {
            setTotalPadding();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        updateIntrinsicPadding(left, top, right, bottom);

        if (!MultiDrawablesExtension.EMPTY_RECT.equals(getExtraPadding())) {
            setTotalPadding();
        } else {
            super.setPadding(left, top, right, bottom);
        }
    }

    private Rect getExtraPadding() {
        MultiDrawablesExtension<android.widget.TextView> drawablesExtension = (MultiDrawablesExtension<android.widget.TextView>) mExtensions.findExtension(Extension.DRAWABLE_EXTENSION);
        Rect extraPadding = MultiDrawablesExtension.EMPTY_RECT;
        if (drawablesExtension != null) {
            extraPadding = drawablesExtension.getInsets();
        }
        return extraPadding;
    }

    private void setTotalPadding() {
        Rect extraPadding = getExtraPadding();
        super.setPadding(mIntrinsicPadding.left + extraPadding.left,
                mIntrinsicPadding.top + extraPadding.top,
                mIntrinsicPadding.right + extraPadding.right,
                mIntrinsicPadding.bottom + extraPadding.bottom);
    }

    private void updateIntrinsicPadding(int left, int top, int right, int bottom) {
        if (mIntrinsicPadding == null) {
            mIntrinsicPadding = new Rect();
        }
        mIntrinsicPadding.set(left, top, right, bottom);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        for (ViewExtension<? extends android.view.View> extension : getExtensions()) {
            extension.onPreDraw(canvas);
        }
        super.onDraw(canvas);
        for (ViewExtension<? extends android.view.View> extension : getExtensions()) {
            extension.onDraw(canvas);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        for (ViewExtension<? extends android.view.View> extension : getExtensions()) {
            extension.drawableStateChanged();
        }
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        boolean isDrawableVerifiedForSuperclass = super.verifyDrawable(who);
        return isDrawableVerifiedForSuperclass || verifyDrawableExt(who);
    }

    private boolean verifyDrawableExt(Drawable who) {
        MultiDrawablesExtension<android.widget.TextView> drawablesExtension = (MultiDrawablesExtension<android.widget.TextView>) mExtensions.findExtension(Extension.DRAWABLE_EXTENSION);
        if(drawablesExtension!=null){
            return  drawablesExtension.verifyDrawable(who);
        }
        return false;
    }
}
