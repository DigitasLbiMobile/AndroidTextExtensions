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


import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import digitaslbi.ext.common.Font;
import digitaslbi.ext.common.FontFamily;

import java.io.File;
import java.util.*;

import static com.google.common.base.CharMatcher.anyOf;
import static com.google.common.collect.Iterators.any;
import static com.google.common.io.Files.fileTreeTraverser;
import static com.google.common.io.Files.getFileExtension;
import static digitaslbi.ext.common.Font.ASSET_FILE_SEPARATORS;
import static digitaslbi.ext.common.Font.NAME_PARTS_COUNT;
import static digitaslbi.ext.common.FontFamily.capitalize;

/**
 * Processes a directory, usually the `assets` folder and calls a code {@link CodeGenerator} to create either Java classes
 * or XML resources.
 */
public class FileProcessor {

    protected final List<String> fontTypes = new ArrayList<String>() {{
        add("ttf");
        add("otf");
    }};

    public FileProcessor(Builder builder) {
        if (builder.fontTypes != null) {
            fontTypes.clear();
            fontTypes.addAll(builder.fontTypes);
        }
    }

    public static class Builder {
        private List<String> fontTypes;

        public Builder withFontTypes(String... fontTypes) {
            this.fontTypes = Arrays.asList(fontTypes);
            return this;
        }

        public FileProcessor build() {
            return new FileProcessor(this);
        }
    }

    public List<FontFamily> process(File inputDir) {
        final Map<String, FontFamily> map = new HashMap<String, FontFamily>();
        final Collection<File> files = new ArrayList<File>();
        collectFiles(inputDir, files);

        for (File file : files) {
            process(fileNameWithRelativePath(inputDir, file), map);
        }

        return ImmutableList.copyOf(map.values());
    }

    protected void collectFiles(File input, Collection<File> files) {
        if (input.isDirectory()) {
            for (File file : fileTreeTraverser().children(input)) {
                if (file.isDirectory()) {
                    collectFiles(file, files);
                } else if (isValidFontFile(file)) {
                    files.add(file);
                }
            }
        } else if (isValidFontFile(input)) {
            files.add(input);
        }
    }

    protected void process(String fileName, Map<String, FontFamily> map) {
        final String nameWithoutExtension = Files.getNameWithoutExtension(fileName);
        final String fontFamilyName = getFontFamilyName(nameWithoutExtension);
        final Font font = new Font(nameWithoutExtension, fileName);

        if (map.containsKey(fontFamilyName)) {
            map.get(fontFamilyName).addFont(font);
        } else {
            map.put(fontFamilyName, new FontFamily(fontFamilyName, font));
        }
    }


    protected String getFontFamilyName(String fileName) {
        final String assetName = Files.getNameWithoutExtension(fileName);
        return capitalize(Splitter.on(anyOf(ASSET_FILE_SEPARATORS))
                .trimResults()
                .omitEmptyStrings()
                .limit(NAME_PARTS_COUNT)
                .split(assetName).iterator().next());
    }

    protected String fileNameWithRelativePath(File parentDir, File file) {
        String path = file.getAbsolutePath().replace(parentDir.getAbsolutePath(), "");
        if (path.startsWith(File.separator)) {
            return path.substring(1);
        }
        return path;
    }

    public boolean isValidFontFile(File input) {
        return input.isFile() && isValidFontExtension(input.getName());
    }

    public boolean isValidFontExtension(String fileName) {
        final String extension = getFileExtension(fileName);
        return any(fontTypes.iterator(), new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return input.equals(extension);
            }
        });
    }

}
