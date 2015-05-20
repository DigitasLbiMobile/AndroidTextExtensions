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

import java.io.File;
import java.io.OutputStream;

/**
 * Represents a code generator interface.
 */
public interface Generator {

    enum FileType {
        XML(".xml"), JAVA(".java");

        String extension;

        FileType(String extension) {
            this.extension = extension;
        }

        public String getExtension() {
            return extension;
        }
    }

    void generate(FontFamily fontFamily, File file) throws Exception;

    void generate(FontFamily fontFamily, OutputStream outputStream) throws Exception;

    void generate(FontFamily fontFamily, Appendable appendable) throws Exception;
}
