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

package digitaslbi.ext.font;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import digitaslbi.ext.L;
import digitaslbi.ext.R;
import digitaslbi.ext.common.Font;
import digitaslbi.ext.common.FontFamily;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static android.text.TextUtils.isEmpty;
import static com.google.common.collect.FluentIterable.from;
import static digitaslbi.ext.common.Constants.BOOTSTRAP_CLASS_NAME;
import static digitaslbi.ext.common.Constants.BOOTSTRAP_PACKAGE_NAME;


/**
 * Handles the creation, caching and application of {@link Typeface} for the custom fonts
 * specified through the {@link FontExtension}.
 */
public class FontManager {

    private static final FontManager INSTANCE = new FontManager();

    static {
        bootstrapFontFamilies();
    }

    public static FontManager getInstance() {
        return INSTANCE;
    }

    /**
     * Register the fonts for which the manager will create and handle {@link Typeface}s.
     *
     * @param fontFamilies the font families to register
     */
    public static void registerFontFamily(FontFamily... fontFamilies) {
        if (fontFamilies == null) {
            throw new IllegalArgumentException("Must provide at least one non-null FontFamily.");
        }
        Collections.addAll(INSTANCE.mFontFamilies, fontFamilies);
    }

    private static void bootstrapFontFamilies() {
        try {
            Class<?> bootstrapClass = Class.forName(BOOTSTRAP_PACKAGE_NAME + "." + BOOTSTRAP_CLASS_NAME);
            Method init = bootstrapClass.getDeclaredMethod("init");
            init.invoke(bootstrapClass);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    private final Set<FontFamily> mFontFamilies = new HashSet<>();
    private final Map<Font, Typeface> mTypefaceCache = new HashMap<>();

    private FontManager() {
    }

    /**
     * Applies a custom font to a {@link android.widget.TextView} on creation.
     *
     * @param textView     the view to apply the font
     * @param context      the {@link Context} associated with the view
     * @param attrs        the {@link AttributeSet} of the view
     * @param defStyleAttr the default style attribute or android.R.attr.textViewStyle if none is provided
     * @param defStyleRes  the default style resource or 0 if none is provided
     */
    public void applyFont(android.widget.TextView textView, Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        Optional<Font> font = obtainFontFromStyle(context, attrs);
        if (!font.isPresent()) {
            font = obtainFontFromTextAppearance(context, attrs, defStyleAttr, defStyleRes);
        }
        if (font.isPresent()) {
            applyFont(textView, font.get());
        }
    }

    /**
     * Applies a custom font to a {@link android.widget.TextView}
     *
     * @param textView the view to apply the font
     * @param font     the {@link Font}
     */
    public void applyFont(android.widget.TextView textView, Font font) {
        final Typeface typeface = getOrCreateTypeface(textView.getContext(), font);
        textView.setPaintFlags(textView.getPaintFlags() | Paint.SUBPIXEL_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        textView.setTypeface(typeface);
    }

    /**
     * Creates or retrieves a cached {@link Typeface} for a {@link Font}.
     *
     * @param context a {@link Context}
     * @param font    the {@link Font}
     * @return the {@link Typeface}; if the Typeface can't be created a {@link RuntimeException} will be thrown.
     */
    public Typeface getOrCreateTypeface(Context context, Font font) throws RuntimeException {
        if (font == null) {
            throw new IllegalArgumentException("Provide a non-null Font object.");
        }
        if (mTypefaceCache.containsKey(font)) {
            return mTypefaceCache.get(font);
        }
        final Typeface typeface = Typeface.createFromAsset(context.getAssets(), font.getAssetName());
        if (typeface == null) {
            throw new RuntimeException("Can't create Typeface for font '" + font.getAssetName() + "'");
        }
        mTypefaceCache.put(font, typeface);
        return typeface;
    }


    /**
     * Creates a {@link Font} object by reading the <code>android:fontFamily</code> attribute
     * directly from the default style, android.R.attr.textViewStyle.
     *
     * @param context the {@link Context}
     * @param attrs   the {@link AttributeSet} of the view
     * @return the {@link Font} or null if the <code>android:fontFamily</code> is not found
     */
    private Optional<Font> obtainFontFromStyle(Context context, AttributeSet attrs) {
        final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.FontExtension, android.R.attr.textViewStyle, 0);
        final String fontName = array.getString(R.styleable.FontExtension_android_fontFamily);
        array.recycle();
        if (isEmpty(fontName)) {
            L.w(this, "Can't find 'android:fontFamily' attribute in the current style.");
            return Optional.absent();
        }
        return findFont(fontName);
    }

    /**
     * Creates a {@link Font} object by reading the <code>android:textAppearance</code> attribute from the current
     * style/theme.
     *
     * @param context      the {@link Context}
     * @param attrs        the {@link AttributeSet} of the view
     * @param defStyleAttr the default style attribute or <code>android.R.attr.textViewStyle</code> if none is provided
     * @param defStyleRes  the default style resource or 0 if none is provided
     * @return the {@link Font} or null if the <code>android:fontFamily</code> is not found
     */
    private Optional<Font> obtainFontFromTextAppearance(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final Optional<String> fontName = obtainFontFamilyFromTextAppearance(context, attrs, defStyleAttr, defStyleRes);
        if (!fontName.isPresent() || isEmpty(fontName.get())) {
            L.w(this, "Can't find 'android:fontFamily' attribute in the current style.");
            return Optional.absent();
        }
        return findFont(fontName.get());
    }

    /**
     * Reads the <code>android:fontFamily</code> from the current <code>android:textAppearance</code> style.
     *
     * @param context      the {@link Context}
     * @param attrs        the {@link AttributeSet} of the view
     * @param defStyleAttr the default style attribute or <code>android.R.attr.textViewStyle</code> if none is provided
     * @param defStyleRes  the default style resource or 0 if none is provided
     * @return the font name, e.g. MULI_REGULAR
     */
    private Optional<String> obtainFontFamilyFromTextAppearance(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        String fontFamily = null;

        final Optional<int[]> textViewAppearance = obtainTextViewAppearance();
        if (textViewAppearance.isPresent()) {

            final int textViewAppearanceTextAppearance = obtainTextViewAppearanceTextAppearance();
            if (textViewAppearanceTextAppearance != -1) {

                final TypedArray array = context.getTheme().obtainStyledAttributes(attrs, textViewAppearance.get(), defStyleAttr, defStyleRes);
                final int ap = array.getResourceId(textViewAppearanceTextAppearance, -1);

                final Optional<int[]> textAppearance = obtainTextAppearance();
                if (ap != -1 && textAppearance.isPresent()) {

                    final int attrFontFamily = obtainTextAppearanceFontFamily();
                    if (attrFontFamily != -1) {

                        TypedArray appearanceArray = context.getTheme().obtainStyledAttributes(ap, textAppearance.get());
                        int indexCount = appearanceArray.getIndexCount();

                        for (int i = 0; i < indexCount; i++) {
                            int attr = appearanceArray.getIndex(i);
                            if (attr == attrFontFamily) {
                                fontFamily = appearanceArray.getString(attr);
                            }
                        }

                        appearanceArray.recycle();
                    }
                }
                array.recycle();
            }
        }

        return Optional.fromNullable(fontFamily);
    }

    /**
     * @return com.android.internal.R$styleable.TextViewAppearance
     */
    private Optional<int[]> obtainTextViewAppearance() {
        try {
            Class clazz = Class.forName("com.android.internal.R$styleable");
            return Optional.of((int[]) clazz.getField("TextViewAppearance").get(clazz));
        } catch (Exception e) {
            return Optional.absent();
        }
    }

    /**
     * @return com.android.internal.R$styleable.TextViewAppearance_textAppearance
     */
    private int obtainTextViewAppearanceTextAppearance() {
        try {
            Class clazz = Class.forName("com.android.internal.R$styleable");
            return clazz.getField("TextViewAppearance_textAppearance").getInt(clazz);
        } catch (Exception e) {
            return -1;
        }
    }

    /**
     * @return com.android.internal.R$styleable.TextAppearance
     */
    private Optional<int[]> obtainTextAppearance() {
        try {
            Class clazz = Class.forName("com.android.internal.R$styleable");
            return Optional.of((int[]) clazz.getField("TextAppearance").get(clazz));
        } catch (Exception e) {
            return Optional.absent();
        }
    }

    /**
     * @return com.android.internal.R$styleable.TextAppearance_fontFamily
     */
    private int obtainTextAppearanceFontFamily() {
        try {
            Class clazz = Class.forName("com.android.internal.R$styleable");
            return clazz.getField("TextAppearance_fontFamily").getInt(clazz);
        } catch (Exception e) {
            return -1;
        }
    }

    private Optional<Font> findFont(final String fontFamily) {
        return from(mFontFamilies)
                .transformAndConcat(new Function<FontFamily, Iterable<Font>>() {
                    @Override
                    public Iterable<Font> apply(FontFamily input) {
                        return input.getFonts();
                    }
                })
                .filter(new Predicate<Font>() {
                    @Override public boolean apply(Font input) {
                        return input.getStyleName().equals(fontFamily);
                    }
                }).first();
    }
}
