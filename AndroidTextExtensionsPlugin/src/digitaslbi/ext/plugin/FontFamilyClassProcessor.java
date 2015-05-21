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

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import digitaslbi.ext.common.FontFamily;
import digitaslbi.ext.generator.BootstrapClassGenerator;
import digitaslbi.ext.generator.CodeGenerator;
import digitaslbi.ext.generator.CodeGenerator.FileType;

import java.util.List;

import static com.intellij.ide.util.PackageUtil.findOrCreateDirectoryForPackage;
import static digitaslbi.ext.common.Constants.BOOTSTRAP_CLASS_NAME;
import static digitaslbi.ext.common.Constants.GENERATED_PACKAGE_NAME;
import static digitaslbi.ext.plugin.CommandHelper.runWriteCommand;
import static java.lang.String.format;

/**
 * Created by evelina on 15/05/15.
 */
public class FontFamilyClassProcessor extends VirtualFileProcessor {

    private static final String JAVA_PACKAGE = "java";

    public FontFamilyClassProcessor(CodeGenerator generator, BootstrapClassGenerator bootstrapClassGenerator, Project project) {
        super(generator, bootstrapClassGenerator, project);
    }

    @Override
    public void generate(final VirtualFile input) throws Exception {
        final Module module = ModuleUtil.findModuleForFile(input, project);
        if (module == null) {
            Dialogs.showModuleNotFoundError(input.getName(), project);
            return;
        }

        final PsiDirectory directory = getJavaDir(project);
        if (directory == null) {
            Log.e(getClass(), "Can't find the '%s' package.", JAVA_PACKAGE);
            Dialogs.showFolderNotFoundDialog(JAVA_PACKAGE, project);
            return;
        }

        final List<FontFamily> fontFamilies = process(input);
        for (FontFamily fontFamily : fontFamilies) {
            generateJavaClass(module, directory, fontFamily);
        }

        if (bootstrapClassGenerator != null) {
            generateBootstrapJavaClass(module, directory, fontFamilies);
        }
    }

    private void generateJavaClass(final Module module, final PsiDirectory directory, final FontFamily fontFamily) {
        try {
            final StringBuilder appendable = new StringBuilder();
            generator.generate(fontFamily, appendable);
            createJavaClass(module, directory, fontFamily.getName() + FileType.JAVA.getExtension(), appendable.toString());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void generateBootstrapJavaClass(final Module module, final PsiDirectory directory, final List<FontFamily> fontFamilies) {
        try {
            final StringBuilder appendable = new StringBuilder();
            bootstrapClassGenerator.generate(fontFamilies, appendable);
            createJavaClass(module, directory, BOOTSTRAP_CLASS_NAME + FileType.JAVA.getExtension(), appendable.toString());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void createJavaClass(final Module module, final PsiDirectory directory, final String fileName, final String content) {
        runWriteCommand(project, new Runnable() {
            @Override
            public void run() {
                final PsiDirectory psiDir = findOrCreateDirectoryForPackage(module, GENERATED_PACKAGE_NAME, directory, false, false);
                if (psiDir == null) {
                    Log.e(getClass(), "Failed to create package %s inside %s.", GENERATED_PACKAGE_NAME, directory.getName());
                    Dialogs.showErrorDialog(format("Failed to create package %s inside %s.", GENERATED_PACKAGE_NAME, directory.getName()), project);
                    return;
                }

                //final PsiPackage psiPackage = JavaDirectoryService.getInstance().getPackage(psiDir);
                final PsiFile newFile = psiDir.findFile(fileName);
                if (newFile == null) {
                    final PsiFile element = PsiFileFactory.getInstance(project).createFileFromText(fileName, JavaFileType.INSTANCE, content);
                    final PsiJavaFile javaFile = (PsiJavaFile) psiDir.add(element);
                    if (javaFile != null) {
                        Dialogs.showInfoDialog(format("Successfully created %s.", fileName), project);
                    }
                } else {
                    Log.d(getClass(), "Skipping %s, file already exists.", fileName);
                }
            }
        });
    }

    private PsiDirectory getJavaDir(Project project) {
        final ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
        final PsiManager psiManager = PsiManager.getInstance(project);
        for (final VirtualFile root : projectRootManager.getContentSourceRoots()) {
            if (root.getName().equals(JAVA_PACKAGE)) {
                return psiManager.findDirectory(root);
            }
        }
        return null;
    }
}
