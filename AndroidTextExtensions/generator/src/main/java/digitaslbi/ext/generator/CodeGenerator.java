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

import digitaslbi.ext.common.FontFamily;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.List;

/**
 * Represents a code generator interface.
 */
public interface CodeGenerator {

    SimpleImmutableEntry<String, String> generate(FontFamily fontFamily);

    List<SimpleImmutableEntry<String, String>> generate(List<FontFamily> fontFamilies);

    abstract class AbstractBuilder<T, B extends AbstractBuilder<T, B>> {
        protected String packageName;
        protected TemplateEngine templateEngine;

        public abstract B self();

        public abstract T build();

        public B withPackageName(String packageName) {
            this.packageName = packageName;
            return self();
        }

        public B withTemplateEngine(TemplateEngine templateEngine) {
            this.templateEngine = templateEngine;
            return self();
        }
    }
}
