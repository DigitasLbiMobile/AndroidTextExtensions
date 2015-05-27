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

package digitaslbi.ext.plugin;

import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import digitaslbi.ext.common.Font;
import digitaslbi.ext.common.FontFamily;
import digitaslbi.ext.generator.FileProcessor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.CharMatcher.anyOf;
import static com.google.common.collect.FluentIterable.from;
import static digitaslbi.ext.common.Font.ASSET_FILE_SEPARATORS;
import static digitaslbi.ext.common.Font.NAME_PARTS_COUNT;
import static digitaslbi.ext.common.FontFamily.capitalize;

/**
 * @author Evelina Vrabie on 27/05/15.
 */
public class VirtualFileProcessor {

    private final FileProcessor fileProcessor;

    public VirtualFileProcessor() {
        fileProcessor = new FileProcessor.Builder().build();
    }

    public List<FontFamily> process(VirtualFile inputDir) {
        final Map<String, FontFamily> map = new HashMap<String, FontFamily>();

        final List<VirtualFile> files = from(VfsUtil.collectChildrenRecursively(inputDir))
                .filter(new Predicate<VirtualFile>() {
                    @Override public boolean apply(VirtualFile input) {
                        return isValidFontFile(input);
                    }
                }).toList();


        for (VirtualFile file : files) {
            final String fileName = fileNameWithRelativePath(inputDir, file);
            final String nameWithoutExtension = Files.getNameWithoutExtension(fileName);
            final String fontFamilyName = getFontFamilyName(nameWithoutExtension);
            final Font font = new Font(nameWithoutExtension, fileName);

            if (map.containsKey(fontFamilyName)) {
                map.get(fontFamilyName).addFont(font);
            } else {
                map.put(fontFamilyName, new FontFamily(fontFamilyName, font));
            }
        }

        return ImmutableList.copyOf(map.values());
    }

    protected void collectFiles(final VirtualFile input, final Collection<VirtualFile> files) {
        if (input.isDirectory()) {
            VfsUtilCore.visitChildrenRecursively(input, new VirtualFileVisitor(VirtualFileVisitor.SKIP_ROOT) {
                @Override
                public boolean visitFile(@NotNull VirtualFile file) {
                    if (isValidFontFile(file)) {
                        files.add(file);
                    }
                    return true;
                }
            });
        } else if (isValidFontFile(input)) {
            files.add(input);
        }
    }

    public boolean isValidFontFile(VirtualFile file) {
        return file.getFileType().isBinary() && fileProcessor.isValidFontExtension(file.getName());
    }

    protected String getFontFamilyName(String fileName) {
        final String assetName = Files.getNameWithoutExtension(fileName);
        return capitalize(Splitter.on(anyOf(ASSET_FILE_SEPARATORS))
                .trimResults()
                .omitEmptyStrings()
                .limit(NAME_PARTS_COUNT)
                .split(assetName).iterator().next());
    }

    protected String fileNameWithRelativePath(VirtualFile parentDir, VirtualFile file) {
        final String path = file.getPath().replace(parentDir.getPath(), "");
        if (path.startsWith(File.separator)) {
            return path.substring(1);
        }
        return path;
    }
}
