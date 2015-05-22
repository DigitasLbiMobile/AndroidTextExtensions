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

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

public class DrawableRepresentation implements Comparable<DrawableRepresentation> {
    private final int mZOrder;
    private final Drawable mDrawable;
    private final Rect mInsets;

    @Override
    public int compareTo(@NonNull DrawableRepresentation drawableRepresentation) {
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
            return new DrawableRepresentation(this);
        }
    }

}