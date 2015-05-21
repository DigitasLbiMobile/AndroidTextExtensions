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
import com.google.common.base.Joiner;
import digitaslbi.ext.common.FontFamily;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import java.io.*;
import java.util.List;

import static com.google.common.collect.Collections2.transform;
import static digitaslbi.ext.common.Constants.BOOTSTRAP_CLASS_NAME;

/**
 * @author Evelina Vrabie on 20/05/2015.
 */
public class BootstrapClassGenerator extends FontFamilyClassGenerator {

    public BootstrapClassGenerator(String packageName, Template template, VelocityContext vc) {
        super(packageName, template, vc);
    }

    public void generate(List<FontFamily> fontFamilies, File outputDir) throws IOException {
        final File dir = new File(outputDir, packageName.replaceAll("\\.", File.separator));
        dir.mkdirs();
        final BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir, BOOTSTRAP_CLASS_NAME + FileType.JAVA.getExtension())));
        bw.write(generate(fontFamilies).toString());
        bw.close();
    }

    public void generate(List<FontFamily> fontFamilies, Appendable appendable) throws IOException {
        appendable.append(generate(fontFamilies).toString());
    }

    private StringWriter generate(List<FontFamily> fontFamilies) {

        final String params = Joiner.on(",").join(transform(fontFamilies, new Function<FontFamily, String>() {
            @Override public String apply(FontFamily input) {
                return "new " + input.getName() + "()";
            }
        }));

        vc.internalPut("packageName", packageName);
        vc.internalPut("className", BOOTSTRAP_CLASS_NAME);
        vc.internalPut("fontFamilies", params);
        final StringWriter writer = new StringWriter();
        template.merge(vc, writer);
        return writer;
    }
}
