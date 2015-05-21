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

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.util.Processor;
import digitaslbi.ext.common.FontFamily;
import digitaslbi.ext.generator.BootstrapClassGenerator;
import digitaslbi.ext.generator.CodeGenerator;
import digitaslbi.ext.generator.CodeGenerator.FileType;
import digitaslbi.ext.generator.FileProcessor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static digitaslbi.ext.common.Constants.ASSETS_FOLDER;
import static digitaslbi.ext.plugin.CommandHelper.runReadCommand;
import static java.lang.String.format;

/**
 * Created by vrabiee on 18/05/15.
 */
public abstract class VirtualFileProcessor extends FileProcessor {

    public static final String ASSETS_PACKAGE = "src/main/assets";

    protected final Project project;
    protected final VirtualFile assetsFolder;

    public VirtualFileProcessor(CodeGenerator generator, BootstrapClassGenerator bootstrapClassGenerator, Project project) {
        super(generator, bootstrapClassGenerator);
        this.project = project;
        this.assetsFolder = findAssetsFolder(project);
    }

    public VirtualFileProcessor(CodeGenerator generator, Project project) {
        super(generator);
        this.project = project;
        this.assetsFolder = findAssetsFolder(project);
    }

    public abstract void generate(final VirtualFile input) throws Exception;

    private void collectFiles(final VirtualFile input, final Collection<VirtualFile> files) {
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

    protected List<FontFamily> process(final VirtualFile input) {
        if (assetsFolder == null) {
            Log.e(getClass(), "Can't find the '%s' folder", ASSETS_FOLDER);
            Dialogs.showFolderNotFoundDialog(ASSETS_FOLDER, project);
            return Collections.emptyList();
        }

        final HashMap<String, FontFamily> map = new HashMap<String, FontFamily>();
        final Collection<VirtualFile> files = new ArrayList<VirtualFile>();
        collectFiles(input, files);

        for (final VirtualFile file : files) {
            runReadCommand(project, new Runnable() {
                @Override
                public void run() {
                    process(fileNameWithRelativePath(assetsFolder, file), map);
                }
            });
        }

        return ImmutableList.copyOf(map.values());
    }

    public static String fileNameWithRelativePath(VirtualFile parentDir, VirtualFile file) {
        String path = file.getPath().replace(parentDir.getPath(), "");
        if (path.startsWith(File.separator)) {
            return path.substring(1);
        }
        return path;
    }

    public static boolean isValidFontFile(VirtualFile file) {
        return file.getFileType().isBinary() && isValidFontExtension(file.getName());
    }

    public void delete(final String fileName, final Project project) {
        Log.d(getClass(), "Deleting %s.", fileName);

        VfsUtilCore.processFilesRecursively(project.getBaseDir(), new Processor<VirtualFile>() {
            @Override public boolean process(VirtualFile file) {
                if (file.getName().equals(fileName)) {
                    try {
                        file.delete(null);
                    } catch (IOException e) {
                        Log.e(getClass(), e, "Failed to delete file %s.", file);
                        Dialogs.showErrorDialog(format("Failed to delete file %s:\n%s", file, e.getMessage()), project);
                    }
                    return false;
                }
                return true;
            }
        });
    }


    public static VirtualFile findAssetsFolder(Project project) {
        final VirtualFile[] sourceRoots = ProjectRootManager.getInstance(project).getContentSourceRoots();
        for (VirtualFile file : sourceRoots) {
            if (file.getPath().endsWith(ASSETS_PACKAGE)) {
                return file;
            }
        }
        return null;
    }
}
