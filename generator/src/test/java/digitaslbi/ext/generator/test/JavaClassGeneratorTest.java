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

package digitaslbi.ext.generator.test;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import digitaslbi.ext.common.Font;
import digitaslbi.ext.common.FontFamily;
import digitaslbi.ext.generator.FileProcessor;
import digitaslbi.ext.generator.JavaClassGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

import static com.google.common.io.Resources.getResource;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Created by evelina on 13/05/15.
 */
@RunWith(JUnit4.class)
public class JavaClassGeneratorTest {

    FileProcessor processor;
    JavaClassGenerator generator;
    StringBuilder result;

    @Before
    public void setup() {
        processor = mock(FileProcessor.class);
        generator = new JavaClassGenerator("digitaslbi.ext.fonts");
        result = new StringBuilder();
    }


    @Test
    public void testGenerateFontFamilyWithMultipleFonts() throws IOException {
        FontFamily family = new FontFamily("NotoSerif",
                new Font("NotoSerif.Bold", "fonts/Noto_Serif/NotoSerif-Bold.ttf"),
                new Font("NotoSerif.BoldItalic", "fonts/Noto_Serif/NotoSerif-BoldItalic.ttf"),
                new Font("NotoSerif.Italic", "fonts/Noto_Serif/NotoSerif-Italic.ttf"),
                new Font("NotoSerif.Regular", "fonts/Noto_Serif/NotoSerif-Regular.ttf"));

        generator.generate(family, result);

        String expected = Resources.toString(getResource("NotoSerif.java"), Charsets.UTF_8);
        assertEquals(expected, result.toString());
    }

    @Test
    public void testGenerateFontFamilyWithSingleFont() throws Exception {
        FontFamily family = new FontFamily("ComingSoon", new Font("ComingSoon", "fonts/Coming_Soon/ComingSoon.ttf"));

        generator.generate(family, result);

        String expected = Resources.toString(getResource("ComingSoon.java"), Charsets.UTF_8);
        assertEquals(expected, result.toString());
    }


}
