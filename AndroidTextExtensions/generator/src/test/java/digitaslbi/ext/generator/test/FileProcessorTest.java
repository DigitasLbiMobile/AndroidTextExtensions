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

import digitaslbi.ext.common.Font;
import digitaslbi.ext.common.FontFamily;
import digitaslbi.ext.generator.CodeGenerator;
import digitaslbi.ext.generator.FileProcessor;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by evelina on 14/05/15.
 */
@RunWith(JUnit4.class)
public class FileProcessorTest {

    @Rule
    public TemporaryFolder assets = new TemporaryFolder();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    File fonts;
    FileProcessor processor;
    CodeGenerator generator;
    StringBuilder result;

    private void newFile(String fileName) throws IOException {
        File file = new File(fonts, fileName);
        file.createNewFile();
    }

    @Before
    public void setUp() throws IOException {
        generator = mock(CodeGenerator.class);
        processor = new FileProcessor(generator);
        result = new StringBuilder();
        fonts = assets.newFolder("fonts1");
    }

    @After
    public void tearDown() {
        fonts.delete();
        assets.delete();
    }


    @Test
    public void testProcessFontFamilyWithMultipleFonts() throws Exception {
        newFile("Muli light italic.ttf");
        newFile("Muli Regular.ttf");
        newFile("Muli-Light.ttf");
        newFile("Some.txt");

        List<FontFamily> families = processor.process(assets.getRoot());
        assertEquals(1, families.size());

        FontFamily family = families.get(0);
        assertEquals("Muli", family.getName());
        assertEquals(3, family.getFonts().size());
    }


    @Test
    public void testProcessFontFamilyWithWeirdName() throws Exception {
        newFile("Muli light     italic     __.ttf");

        List<FontFamily> families = processor.process(assets.getRoot());
        assertEquals(1, families.size());

        FontFamily family = families.get(0);
        assertEquals("Muli", family.getName());
        assertEquals(1, family.getFonts().size());

        Font font3 = family.getFonts().get(0);
        assertEquals("Muli_light_italic", font3.getName());
        assertEquals("fonts1/Muli light     italic     __.ttf", font3.getAssetName());
    }


    @Test
    public void testProcessWhenNoFonts() throws Exception {
        List<FontFamily> families = processor.process(assets.getRoot());
        assertEquals(0, families.size());

        verify(generator, never()).generate(any(FontFamily.class), any(Appendable.class));
    }

    @Test
    public void testProcessWhenGeneratorThrowsException() throws Exception {
        newFile("Muli.ttf");

        doThrow(new IOException("BOOM")).when(generator).generate(any(FontFamily.class), any(Appendable.class));

        exception.expect(RuntimeException.class);

        processor.generate(assets.getRoot(), result);

        assertNotEquals("", result.toString());
    }
}
