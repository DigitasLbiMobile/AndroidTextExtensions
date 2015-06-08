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

package digitaslbi.ext.generator;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import digitaslbi.ext.common.FontFamily;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;
import static java.util.Arrays.asList;

/**
 * The **generator** module is used by the **IdeaPlugin** to auto-generate the necessary sources and resources
 * to support the custom fonts extension.
 * It can also be used as a standalone jar.
 * __TODO__ add example
 */
public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final String RES_PATH = "res/values";

    /**
     * Starts the code generation for font family classes and resources.
     *
     * @param args - an input directory containing font files
     *             - the root output directory
     *             - the package name for the generated classes
     */
    public static void main(String[] args) throws Exception {
        if (args == null || args.length < 3) {
            throw new IllegalArgumentException("The input and output directories and the package name are required.");
        }

        final String inputPath = args[0];
        final String outputPath = args[1];
        final String packageName = args[2];

        final File inputDir = new File(inputPath);
        if (!inputDir.exists()) {
            throw new IllegalArgumentException("The input path " + inputPath + " does not exist.");
        }

        LOG.debug("input={}\noutput={}\npackage={}", inputDir, outputPath, packageName);

        processFiles(inputDir, outputPath, packageName);
    }

    private static void processFiles(File inputDir, String outputPath, String packageName) throws Exception {
        final File packageDir = new File(outputPath, packageName.replaceAll("\\.", File.separator));
        packageDir.mkdirs();

        final File resOutputDir = new File(outputPath, RES_PATH);
        resOutputDir.mkdirs();

        final TemplateEngine templateEngine = new TemplateEngine();

        final FontFamilyClassGenerator classGenerator = new FontFamilyClassGenerator.Builder()
                .withPackageName(packageName)
                .withTemplateEngine(templateEngine)
                .build();

        final FontFamilyStyleGenerator styleGenerator = new FontFamilyStyleGenerator.Builder().build();

        final List<FontFamily> fontFamilies = new FileProcessor.Builder().build().process(inputDir);

        for(SimpleImmutableEntry<String, String> entry : classGenerator.generate(fontFamilies)) {
            writeToOutputDir(entry, packageDir);
        }

        for(SimpleImmutableEntry<String, String> entry : styleGenerator.generate(fontFamilies)) {
            writeToOutputDir(entry, resOutputDir);
        }
    }

    private static void writeToOutputDir(SimpleImmutableEntry<String, String> result, File outputDir) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(new File(outputDir, result.getKey())));
            bw.write(result.getValue());
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e1) {
                    // ignore
                }
            }
        }
    }
}
