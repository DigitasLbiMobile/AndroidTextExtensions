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

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import digitaslbi.ext.common.Font;
import digitaslbi.ext.common.FontFamily;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import java.io.*;

import static com.google.common.collect.Iterables.transform;
import static digitaslbi.ext.generator.Streams.asByteSink;


/**
 * Represents a code {@link CodeGenerator} which generates a Java class for a {@link FontFamily}.
 */
public class FontFamilyClassGenerator implements CodeGenerator {

    protected final String packageName;
    protected final Template template;
    protected final VelocityContext vc;

    public FontFamilyClassGenerator(String packageName, Template template, VelocityContext vc) {
        this.packageName = packageName;
        this.template = template;
        this.vc = vc;
    }

    @Override
    public void generate(FontFamily fontFamily, File outputDir) throws IOException {
        final File dir = new File(outputDir, packageName.replaceAll("\\.", File.separator));
        dir.mkdirs();
        final BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir, fontFamily.getName() + FileType.JAVA.getExtension())));
        bw.write(generate(fontFamily).toString());
        bw.close();
    }

    @Override
    public void generate(FontFamily fontFamily, Appendable appendable) throws IOException {
        appendable.append(generate(fontFamily).toString());
    }

    @Override
    public void generate(FontFamily fontFamily, final OutputStream outputStream) throws Exception {
        final StringBuilder appendable = new StringBuilder();
        generate(fontFamily, appendable);
        asByteSink(outputStream).asCharSink(Charsets.UTF_8).openStream().write(appendable.toString());
    }

    private StringWriter generate(FontFamily fontFamily) {
        vc.internalPut("packageName", packageName);
        vc.internalPut("className", fontFamily.getName());
        vc.internalPut("fontFamily", fontFamily);
        vc.internalPut("params", params(fontFamily));
        final StringWriter writer = new StringWriter();
        template.merge(vc, writer);
        return writer;
    }

    private String params(FontFamily fontFamily) {
        return Joiner.on(",").join(transform(fontFamily.getFonts(), new Function<Font, String>() {
            @Override
            public String apply(Font input) {
                return input.getFieldName();
            }
        }));
    }
}
