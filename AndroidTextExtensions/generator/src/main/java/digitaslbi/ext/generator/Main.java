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

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Properties;

/**
 * The **generator** module is used by the **IdeaPlugin** to auto-generate the necessary sources and resources
 * to support the custom fonts extension.
 * <p/>
 * It can also be used as a standalone jar.
 * <p/>
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
        final File classOutputDir = new File(outputPath);
        classOutputDir.mkdirs();

        final File resOutputDir = new File(outputPath, RES_PATH);
        resOutputDir.mkdirs();

        final VelocityEngine ve = new VelocityEngine();
        final Properties properties = new Properties();
        properties.setProperty("resource.loader", "file");
        properties.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        ve.init(properties);

        final VelocityContext vc = new VelocityContext();
        final Template classTemplate = ve.getTemplate("FontFamily.java.vm");
        final Template bootstrapTemplate = ve.getTemplate("FontFamilies.java.vm");

        final FontFamilyClassGenerator classGenerator = new FontFamilyClassGenerator(packageName, classTemplate, vc);
        final BootstrapClassGenerator bootstrapClassGenerator = new BootstrapClassGenerator(packageName, bootstrapTemplate, vc);

        new FileProcessor(classGenerator, bootstrapClassGenerator).generate(inputDir, classOutputDir);
        new FileProcessor(new FontFamilyStyleGenerator()).generate(inputDir, resOutputDir);
    }
}
