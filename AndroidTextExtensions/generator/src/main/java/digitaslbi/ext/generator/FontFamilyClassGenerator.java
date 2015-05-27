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
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import digitaslbi.ext.common.Font;
import digitaslbi.ext.common.FontFamily;
import org.apache.velocity.VelocityContext;

import java.io.StringWriter;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;

import static com.google.common.collect.Iterables.transform;
import static digitaslbi.ext.common.Constants.BOOTSTRAP_CLASS_NAME;


/**
 * Represents a code {@link CodeGenerator} which generates a Java class for a {@link FontFamily}.
 */
public class FontFamilyClassGenerator implements CodeGenerator {

    protected final String packageName;
    protected final TemplateEngine templateEngine;

    private FontFamilyClassGenerator(Builder builder) {
        this.packageName = builder.packageName;
        this.templateEngine = builder.templateEngine;
    }

    public static class Builder extends AbstractBuilder<FontFamilyClassGenerator, Builder> {
        @Override
        public Builder self() {
            return this;
        }

        @Override
        public FontFamilyClassGenerator build() {
            return new FontFamilyClassGenerator(this);
        }
    }

    @Override
    public List<SimpleImmutableEntry<String, String>> generate(List<FontFamily> fontFamilies) {
        return FluentIterable.from(fontFamilies).transform(new Function<FontFamily, SimpleImmutableEntry<String, String>>() {
            @Override public SimpleImmutableEntry<String, String> apply(FontFamily input) {
                return generate(input);
            }
        }).filter(new Predicate<SimpleImmutableEntry<String, String>>() {
            @Override public boolean apply(SimpleImmutableEntry<String, String> input) {
                return input != null;
            }
        }).append(generateBootstrap(fontFamilies)).toList();

    }

    private SimpleImmutableEntry<String, String> generateBootstrap(List<FontFamily> fontFamilies) {
        final VelocityContext vc = templateEngine.getVelocityContext();
        vc.internalPut("packageName", packageName);
        vc.internalPut("className", BOOTSTRAP_CLASS_NAME);
        vc.internalPut("fontFamilies", Joiner.on(",").join(transform(fontFamilies, new Function<FontFamily, String>() {
            @Override public String apply(FontFamily input) {
                return "new " + input.getName() + "()";
            }
        })));

        final StringWriter writer = new StringWriter();
        templateEngine.getBootstrapClassTemplate().merge(vc, writer);
        return new SimpleImmutableEntry<String, String>(BOOTSTRAP_CLASS_NAME + ".java", writer.toString());
    }

    @Override
    public SimpleImmutableEntry<String, String> generate(FontFamily fontFamily) {
        final VelocityContext vc = templateEngine.getVelocityContext();
        vc.internalPut("packageName", packageName);
        vc.internalPut("className", fontFamily.getName());
        vc.internalPut("fontFamily", fontFamily);
        vc.internalPut("params", Joiner.on(",").join(transform(fontFamily.getFonts(), new Function<Font, String>() {
            @Override
            public String apply(Font input) {
                return input.getFieldName();
            }
        })));

        final StringWriter writer = new StringWriter();
        templateEngine.getClassTemplate().merge(vc, writer);
        return new SimpleImmutableEntry<String, String>(fontFamily.getName() + ".java", writer.toString());
    }
}
