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
import com.google.common.io.ByteSink;
import com.google.common.io.ByteSource;
import com.google.common.io.CharSink;
import com.google.common.io.CharSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility methods to wrap an {@link InputStream} or {@link OutputStream} into Guava's
 * {@link ByteSink} and {@link CharSink}.
 */
public final class Streams {

    private Streams() {
    }

    public static ByteSink asByteSink(final OutputStream outputStream) {
        return new ByteSink() {
            public OutputStream openStream() throws IOException {
                return outputStream;
            }
        };
    }

    public ByteSource asByteSource(final InputStream inputStream) {
        return new ByteSource() {
            public InputStream openStream() throws IOException {
                return inputStream;
            }
        };
    }

    public CharSink asCharSink(OutputStream outputStream) {
        return asByteSink(outputStream).asCharSink(Charsets.UTF_8);
    }

    public CharSource asCharSource(InputStream inputStream) {
        return asByteSource(inputStream).asCharSource(Charsets.UTF_8);
    }
}
