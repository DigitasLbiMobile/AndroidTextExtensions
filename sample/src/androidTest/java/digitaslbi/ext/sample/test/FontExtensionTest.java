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

package digitaslbi.ext.sample.test;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import digitaslbi.ext.CheckedTextView;
import digitaslbi.ext.font.FontExtension;
import digitaslbi.ext.sample.MainActivity;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Created by vrabiee on 12/05/15.
 */
@RunWith(AndroidJUnit4.class)
public class FontExtensionTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    public static Matcher<View> hasTypeface() {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("has typeface");
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof CheckedTextView)) return false;
                CheckedTextView tv = ((CheckedTextView) view);
                return tv.getTypeface() != null && tv.getExtension(FontExtension.class) != null;
            }
        };
    }

    @Test
    public void setFontFromCode() {
        onView(withId(digitaslbi.ext.sample.R.id.textView6)).check(matches(hasTypeface()));
    }
}
