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

import digitaslbi.ext.drawables.DrawableRepresentation;
import digitaslbi.ext.drawables.MultiDrawablesExtension;

/**
 * Created by anatriep on 21/05/2015.
 */
public class View extends android.view.View {

    protected ExtensionsManager<android.view.View> mExtensions = new ExtensionsManager<>();

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
        mExtensions.init(context, attrs, defStyleAttr, defStyleRes, 0, this);
    }

    public Collection<ViewExtension<android.view.View>> getExtensions() {
        return mExtensions.get();
    }

    public <E extends ViewExtension<android.view.View>> E getExtension(Extension extensionId) {
        return (E) mExtensions.findExtension(extensionId);
    }

    public void addExtension(ViewExtension<android.view.View> extension) {
        mExtensions.add(extension, this);
    }

    public DrawableRepresentation addDrawable(Drawable drawable, Rect insets, int zOrder) {
        MultiDrawablesExtension<android.view.View> drawablesExtension = (MultiDrawablesExtension<android.view.View>) mExtensions.findExtension(Extension.DRAWABLE_EXTENSION);
        if (drawablesExtension == null) {
            drawablesExtension = new MultiDrawablesExtension<>(getContext(), null, 0, 0);
            drawablesExtension.setView(this);
            mExtensions.add(drawablesExtension);
        }
        return drawablesExtension.addDrawable(drawable, insets, zOrder);
    }

    public void removeDrawable(DrawableRepresentation drawable) {
        MultiDrawablesExtension<android.view.View> drawablesExtension = (MultiDrawablesExtension<android.view.View>) mExtensions.findExtension(Extension.DRAWABLE_EXTENSION);
        if (drawablesExtension != null) {
            drawablesExtension.removeDrawable(drawable);
        }
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
        MultiDrawablesExtension<android.view.View> drawablesExtension = (MultiDrawablesExtension<android.view.View>) mExtensions.findExtension(Extension.DRAWABLE_EXTENSION);
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
}
