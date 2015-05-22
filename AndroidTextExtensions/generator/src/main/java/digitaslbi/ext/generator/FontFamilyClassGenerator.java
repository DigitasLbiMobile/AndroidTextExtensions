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
import com.squareup.javapoet.*;
import digitaslbi.ext.common.Font;
import digitaslbi.ext.common.FontFamily;

import javax.lang.model.element.Modifier;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import static com.google.common.collect.Iterables.transform;
import static digitaslbi.ext.common.Font.nameToStyleName;
import static digitaslbi.ext.common.Font.styleNameToName;
import static digitaslbi.ext.generator.Streams.asByteSink;


/**
 * Represents a code {@link CodeGenerator} which generates a Java class for a {@link FontFamily}.
 */
public class FontFamilyClassGenerator implements CodeGenerator {

    protected final String packageName;

    public FontFamilyClassGenerator(String packageName) {
        this.packageName = packageName;
    }

    @Override
    public void generate(FontFamily fontFamily, File outputDir) throws IOException {
        generate(fontFamily).writeTo(outputDir);
    }

    @Override
    public void generate(FontFamily fontFamily, Appendable appendable) throws IOException {
        generate(fontFamily).writeTo(appendable);
    }

    @Override
    public void generate(FontFamily fontFamily, final OutputStream outputStream) throws Exception {
        final StringBuilder appendable = new StringBuilder();
        generate(fontFamily, appendable);
        asByteSink(outputStream).asCharSink(Charsets.UTF_8).openStream().write(appendable.toString());
    }

    private JavaFile generate(FontFamily fontFamily) {
        final TypeSpec.Builder fontFamilyClass = TypeSpec.classBuilder(fontFamily.getName())
                .superclass(ClassName.get(FontFamily.class))
                .addModifiers(Modifier.PUBLIC)
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("super($S, $L)", fontFamily.getName(), params(fontFamily))
                        .build())
                .addJavadoc("Generated by Android Text Extensions plugin.\n" +
                        "@see <a href=\"https://github.com/DigitasLbiMobile/AndroidTextExtensions\">Android Text Extensions</a>\n");

        for (FieldSpec fieldSpec : transform(fontFamily.getFonts(), new Function<Font, FieldSpec>() {
            @Override
            public FieldSpec apply(Font input) {
                return fieldSpec(input);
            }
        })) {
            fontFamilyClass.addField(fieldSpec);
        }

        return JavaFile.builder(packageName, fontFamilyClass.build()).build();
    }

    private FieldSpec fieldSpec(Font font) {
        return FieldSpec.builder(Font.class, styleNameToName(font.getName()))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("new Font($S, $S)", nameToStyleName(font.getName()), font.getAssetName())
                .build();
    }

    private String params(FontFamily fontFamily) {
        return Joiner.on(",").join(transform(fontFamily.getFonts(), new Function<Font, String>() {
            @Override
            public String apply(Font input) {
                return styleNameToName(input.getName());
            }
        }));
    }
}