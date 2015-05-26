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

package digitaslbi.ext.drawables.ripple;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Arrays;

/**
 * Drawable that shows a ripple effect in response to state changes. The
 * anchoring position of the ripple for a given state may be specified by
 * calling {@link #setHotspot(float, float)} with the corresponding state
 * attribute identifier.
 * <p/>
 * A touch feedback drawable may contain multiple child layers, including a
 * special mask layer that is not drawn to the screen. A single layer may be set
 * as the mask by specifying its android:id value as {@link android.R.id#mask}.
 * <pre>
 * <code>&lt!-- A red ripple masked against an opaque rectangle. --/>
 * &ltripple android:color="#ffff0000">
 *   &ltitem android:id="@android:id/mask"
 *         android:drawable="@android:color/white" />
 * &ltripple /></code>
 * </pre>
 * <p/>
 * If a mask layer is set, the ripple effect will be masked against that layer
 * before it is drawn over the composite of the remaining child layers.
 * <p/>
 * If no mask layer is set, the ripple effect is masked against the composite
 * of the child layers.
 * <pre>
 * <code>&lt!-- A blue ripple drawn atop a black rectangle. --/>
 * &ltripple android:color="#ff00ff00">
 *   &ltitem android:drawable="@android:color/black" />
 * &ltripple />
 *
 * &lt!-- A red ripple drawn atop a drawable resource. --/>
 * &ltripple android:color="#ff00ff00">
 *   &ltitem android:drawable="@drawable/my_drawable" />
 * &ltripple /></code>
 * </pre>
 * <p/>
 * If no child layers or mask is specified and the ripple is set as a View
 * background, the ripple will be drawn atop the first available parent
 * background within the View's hierarchy. In this case, the drawing region
 * may extend outside of the Drawable bounds.
 * <pre>
 * <code>&lt!-- An unbounded green ripple. --/>
 * &ltripple android:color="#ff0000ff" /></code>
 * </pre>
 *
 * @attr ref android.R.styleable#RippleDrawable_color
 */
public class RippleDrawable extends Drawable {
    private static final PorterDuffXfermode DST_IN = new PorterDuffXfermode(Mode.DST_IN);
    private static final PorterDuffXfermode SRC_ATOP = new PorterDuffXfermode(Mode.SRC_ATOP);
    private static final PorterDuffXfermode SRC_OVER = new PorterDuffXfermode(Mode.SRC_OVER);

    /**
     * Constant for automatically determining the maximum ripple radius.
     *
     * @hide
     * @see #setMaxRadius(int)
     */
    public static final int RADIUS_AUTO = -1;

    /**
     * The maximum number of ripples supported.
     */
    private static final int MAX_RIPPLES = 10;

    private final Rect mTempRect = new Rect();

    /**
     * Current ripple effect bounds, used to constrain ripple effects.
     */
    private final Rect mHotspotBounds = new Rect();

    /**
     * Current drawing bounds, used to compute dirty region.
     */
    private final Rect mDrawingBounds = new Rect();

    /**
     * Current dirty bounds, union of current and previous drawing bounds.
     */
    private final Rect mDirtyBounds = new Rect();

    /**
     * Mirrors mLayerState with some extra information.
     */
    private RippleState mState;

    /**
     * The current ripple. May be actively animating or pending entry.
     */
    private Ripple mRipple;

    /**
     * Whether we expect to draw a ripple when visible.
     */
    private boolean mRippleActive;

    // Hotspot coordinates that are awaiting activation.
    private float mPendingX;
    private float mPendingY;
    private boolean mHasPending;

    /**
     * Lazily-created array of actively animating ripples. Inactive ripples are
     * pruned during draw(). The locations of these will not change.
     */
    private Ripple[] mExitingRipples;
    private int mExitingRipplesCount = 0;

    /**
     * Paint used to control appearance of ripples.
     */
    private Paint mRipplePaint;

    /**
     * Paint used to control reveal layer masking.
     */
    private Paint mMaskingPaint;

    /**
     * Target density of the display into which ripples are drawn.
     */
    private float mDensity = 1.0f;

    /**
     * Whether bounds are being overridden.
     */
    private boolean mOverrideBounds;

    /**
     * Whether the next draw MUST draw something to canvas. Used to work around
     * a bug in hardware invalidation following a render thread-accelerated
     * animation.
     */
    private boolean mNeedsDraw;
    private RippleState mLayerState;

    /**
     * Creates a new ripple drawable with the specified ripple color and
     * optional content and mask drawables.
     *
     * @param color The ripple color
     */
    public RippleDrawable(ColorStateList color) {
        super();
        if (color == null) {
            throw new IllegalArgumentException("RippleDrawable requires a non-null color");
        }
        mState=new RippleState(null);
        setColor(color);
    }

    private RippleDrawable(RippleState state) {
        super();
        mState=state;
    }

    @Override
    public void jumpToCurrentState() {
        super.jumpToCurrentState();

        boolean needsDraw = false;

        if (mRipple != null) {
            needsDraw |= mRipple.isHardwareAnimating();
            mRipple.jump();
        }

        needsDraw |= cancelExitingRipples();

        mNeedsDraw = needsDraw;
        invalidateSelf();
    }

    private boolean cancelExitingRipples() {
        boolean needsDraw = false;

        final int count = mExitingRipplesCount;
        final Ripple[] ripples = mExitingRipples;
        for (int i = 0; i < count; i++) {
            needsDraw |= ripples[i].isHardwareAnimating();
            ripples[i].cancel();
        }

        if (ripples != null) {
            Arrays.fill(ripples, 0, count, null);
        }
        mExitingRipplesCount = 0;

        return needsDraw;
    }

    @Override
    public int getOpacity() {
        // Worst-case scenario.
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    protected boolean onStateChange(int[] stateSet) {
        final boolean changed = super.onStateChange(stateSet);

        boolean enabled = false;
        boolean pressed = false;
        for (int state : stateSet) {
            if (state == android.R.attr.state_enabled) {
                enabled = true;
            }

            if (state == android.R.attr.state_pressed) {
                pressed = true;
            }
        }

        setRippleActive(enabled && pressed);
        return changed;
    }

    private void setRippleActive(boolean active) {
        if (mRippleActive != active) {
            mRippleActive = active;
            if (active) {
                tryRippleEnter();
            } else {
                tryRippleExit();
            }
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        if (!mOverrideBounds) {
            mHotspotBounds.set(bounds);
            onHotspotBoundsChanged();
        }

        invalidateSelf();
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        final boolean changed = super.setVisible(visible, restart);

        if (!visible) {
            clearHotspots();
        } else if (changed) {
            // If we just became visible, ensure the background and ripple
            // visibilities are consistent with their internal states.
            if (mRippleActive) {
                tryRippleEnter();
            }
        }

        return changed;
    }

    @Override
    public boolean isStateful() {
        return true;
    }

    public void setColor(ColorStateList color) {
        mState.mColor = color;
        invalidateSelf();
    }

//    @Override
//    public void inflate(Resources r, XmlPullParser parser, AttributeSet attrs, Theme theme)
//            throws XmlPullParserException, IOException {
//        final TypedArray a = obtainAttributes(r, theme, attrs, R.styleable.RippleDrawable);
//        updateStateFromTypedArray(a);
//        a.recycle();
//
//        // Force padding default to STACK before inflating.
//        setPaddingMode(PADDING_MODE_STACK);
//
//        super.inflate(r, parser, attrs, theme);
//
//        setTargetDensity(r.getDisplayMetrics());
//        initializeFromState();
//    }

    /**
     * Set the density at which this drawable will be rendered.
     *
     * @param metrics The display metrics for this drawable.
     */
    private void setTargetDensity(DisplayMetrics metrics) {
        if (mDensity != metrics.density) {
            mDensity = metrics.density;
            invalidateSelf();
        }
    }


    @Override
    public boolean canApplyTheme() {
        return false;
    }

    public void setHotspotExt(float x, float y) {
        if (mRipple == null) {
            mPendingX = x;
            mPendingY = y;
            mHasPending = true;
        }

        if (mRipple != null) {
            mRipple.move(x, y);
        }
    }

    /**
     * Attempts to start an enter animation for the active hotspot. Fails if
     * there are too many animating ripples.
     */
    private void tryRippleEnter() {
        if (mExitingRipplesCount >= MAX_RIPPLES) {
            // This should never happen unless the user is tapping like a maniac
            // or there is a bug that's preventing ripples from being removed.
            return;
        }

        if (mRipple == null) {
            final float x;
            final float y;
            if (mHasPending) {
                mHasPending = false;
                x = mPendingX;
                y = mPendingY;
            } else {
                x = mHotspotBounds.exactCenterX();
                y = mHotspotBounds.exactCenterY();
            }
            mRipple = new Ripple(this, mHotspotBounds, x, y);
        }

        final int color = mState.mColor.getColorForState(getState(), Color.TRANSPARENT);
        mRipple.setup(mState.mMaxRadius, color, mDensity);
        mRipple.enter();
    }

    /**
     * Attempts to start an exit animation for the active hotspot. Fails if
     * there is no active hotspot.
     */
    private void tryRippleExit() {
        if (mRipple != null) {
            if (mExitingRipples == null) {
                mExitingRipples = new Ripple[MAX_RIPPLES];
            }
            mExitingRipples[mExitingRipplesCount++] = mRipple;
            mRipple.exit();
            mRipple = null;
        }
    }

    /**
     * Cancels and removes the active ripple, all exiting ripples, and the
     * background. Nothing will be drawn after this method is called.
     */
    private void clearHotspots() {
        boolean needsDraw = false;

        if (mRipple != null) {
            needsDraw |= mRipple.isHardwareAnimating();
            mRipple.cancel();
            mRipple = null;
        }
        needsDraw |= cancelExitingRipples();

        mNeedsDraw = needsDraw;
        invalidateSelf();
    }

    public void setHotspotBounds(int left, int top, int right, int bottom) {
        mOverrideBounds = true;
        mHotspotBounds.set(left, top, right, bottom);

        onHotspotBoundsChanged();
    }

    /**
     * Notifies all the animating ripples that the hotspot bounds have changed.
     */
    private void onHotspotBoundsChanged() {
        final int count = mExitingRipplesCount;
        final Ripple[] ripples = mExitingRipples;
        for (int i = 0; i < count; i++) {
            ripples[i].onHotspotBoundsChanged();
        }

        if (mRipple != null) {
            mRipple.onHotspotBoundsChanged();
        }

    }


    @Override
    public void draw(Canvas canvas) {
        //TODO ONLY DRAW DIRTY BOUNDS
        final int saveCount = canvas.save(Canvas.CLIP_SAVE_FLAG);

        // If we have content, draw it into a layer first.


        // Next, try to draw the ripples (into a layer if necessary). If we need
        // to mask against the underlying content, set the xfermode to SRC_ATOP.
        final PorterDuffXfermode xfermode = SRC_OVER;

        // If we have ripples and a non-opaque mask, draw the masking layer.
        drawRippleLayer(canvas, getBounds(), xfermode);
        mNeedsDraw = false;

        canvas.restoreToCount(saveCount);
    }

    /**
     * Removes a ripple from the exiting ripple list.
     *
     * @param ripple the ripple to remove
     */
    void removeRipple(Ripple ripple) {
        // Ripple ripple ripple ripple. Ripple ripple.
        final Ripple[] ripples = mExitingRipples;
        final int count = mExitingRipplesCount;
        final int index = getRippleIndex(ripple);
        if (index >= 0) {
            System.arraycopy(ripples, index + 1, ripples, index, count - (index + 1));
            ripples[count - 1] = null;
            mExitingRipplesCount--;

            invalidateSelf();
        }
    }

    private int getRippleIndex(Ripple ripple) {
        final Ripple[] ripples = mExitingRipples;
        final int count = mExitingRipplesCount;
        for (int i = 0; i < count; i++) {
            if (ripples[i] == ripple) {
                return i;
            }
        }
        return -1;
    }

    private void drawRippleLayer(Canvas canvas, Rect bounds, PorterDuffXfermode mode) {
        boolean drewRipples = false;

        // Draw ripples and update the animating ripples array.
        final int count = mExitingRipplesCount;
        final Ripple[] ripples = mExitingRipples;
        for (int i = 0; i <= count; i++) {
            final Ripple ripple;
            if (i < count) {
                ripple = ripples[i];
            } else if (mRipple != null) {
                ripple = mRipple;
            } else {
                continue;
            }

            drewRipples |= ripple.draw(canvas, getRipplePaint());
        }


    }

    private Paint getRipplePaint() {
        if (mRipplePaint == null) {
            mRipplePaint = new Paint();
            mRipplePaint.setAntiAlias(true);
        }
        return mRipplePaint;
    }

    private Paint getMaskingPaint(PorterDuffXfermode xfermode) {
        if (mMaskingPaint == null) {
            mMaskingPaint = new Paint();
        }
        mMaskingPaint.setXfermode(xfermode);
        mMaskingPaint.setAlpha(0xFF);
        return mMaskingPaint;
    }

    @Override
    public void setAlpha(int i) {
        //NOT SUPPORTED
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        //NOT SUPPORTED
    }

    @Override
    public ConstantState getConstantState() {
        return mState;
    }

    @Override
    public Drawable mutate() {
        super.mutate();

        // LayerDrawable creates a new state using createConstantState, so
        // this should always be a safe cast.
        mState = (RippleState) mLayerState;
        return this;
    }

    static class RippleState extends ConstantState {
        ColorStateList mColor = ColorStateList.valueOf(Color.MAGENTA);
        int mMaxRadius = RADIUS_AUTO;

        public RippleState(RippleState orig) {
            super();

            if (orig != null && orig instanceof RippleState) {
                final RippleState origs = (RippleState) orig;
                mColor = origs.mColor;
                mMaxRadius = origs.mMaxRadius;
            }
        }

        @Override
        public boolean canApplyTheme() {
            return false;
        }

        @Override
        public Drawable newDrawable() {
            return new RippleDrawable(this);
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }
    }

    /**
     * Sets the maximum ripple radius in pixels. The default value of
     * {@link #RADIUS_AUTO} defines the radius as the distance from the center
     * of the drawable bounds (or hotspot bounds, if specified) to a corner.
     *
     * @param maxRadius the maximum ripple radius in pixels or
     *                  {@link #RADIUS_AUTO} to automatically determine the maximum
     *                  radius based on the bounds
     * @hide
     * @see #getMaxRadius()
     * @see #setHotspotBounds(int, int, int, int)
     */
    public void setMaxRadius(int maxRadius) {
        if (maxRadius != RADIUS_AUTO && maxRadius < 0) {
            throw new IllegalArgumentException("maxRadius must be RADIUS_AUTO or >= 0");
        }

        mState.mMaxRadius = maxRadius;
    }

    /**
     * @return the maximum ripple radius in pixels, or {@link #RADIUS_AUTO} if
     * the radius is determined automatically
     * @hide
     * @see #setMaxRadius(int)
     */
    public int getMaxRadius() {
        return mState.mMaxRadius;
    }

}
