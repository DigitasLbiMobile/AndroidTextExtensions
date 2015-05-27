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

package digitaslbi.ext.plugin.models;

import com.android.builder.model.SourceProvider;
import com.google.common.base.Charsets;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import digitaslbi.ext.common.FontFamily;
import digitaslbi.ext.generator.FontFamilyClassGenerator;
import digitaslbi.ext.generator.FontFamilyStyleGenerator;
import digitaslbi.ext.plugin.VirtualFileProcessor;
import digitaslbi.ext.plugin.VirtualTemplateEngine;
import digitaslbi.ext.plugin.utils.CommandHelper;
import digitaslbi.ext.plugin.utils.Dialogs;
import digitaslbi.ext.plugin.utils.Log;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.facet.IdeaSourceProvider;

import java.io.File;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.intellij.ide.util.PackageUtil.findOrCreateDirectoryForPackage;
import static digitaslbi.ext.generator.Streams.asByteSink;
import static java.lang.String.format;
import static org.jetbrains.android.util.AndroidUtils.createChildDirectoryIfNotExist;

/**
 * @author Evelina Vrabie on 26/05/15.
 */
public class AndroidProject {

    private static final String GENERATED_PACKAGE_NAME = "digitaslbi.ext.fonts";

    private final Project project;
    private final Set<AndroidSource> sources = new HashSet<AndroidSource>();
    private final VirtualTemplateEngine templateEngine = new VirtualTemplateEngine(getClass());

    public AndroidProject(Project project, VirtualFile selectedFile) {
        checkNotNull(project, "The project must not be null.");
        checkNotNull(selectedFile, "The selected file must not be null.");

        this.project = project;
        final Module selectedModule = ModuleUtil.findModuleForFile(selectedFile, project);
        if (selectedModule == null) {
            Dialogs.showErrorDialog("Please select the module inside your Android Studio/IntelliJ Android project " +
                    "for which you want to generate the files.\n[No Android module selected]", project);
            return;
        }
        collectSourceDirs(selectedModule);
    }

    public AndroidProject(Project project) {
        checkNotNull(project, "The project must not be null.");
        this.project = project;
        for (Module module : ModuleManager.getInstance(project).getModules()) {
            collectSourceDirs(module);
        }
    }

    private void collectSourceDirs(Module module) {
        final AndroidFacet androidFacet = AndroidFacet.getInstance(module);
        if (androidFacet == null) {
            Dialogs.showErrorDialog("Please select the module inside your Android Studio/IntelliJ Android project " +
                    "for which you want to generate the files.\n[No Android module selected]", module.getProject());
            return;
        }

        // TODO find a way to customise the package name and let the FontManager know where to look for FontFamilies.java class
        final String packageName = GENERATED_PACKAGE_NAME;

        final Collection<SourceProvider> sourceProviders = collectSourceProviders(androidFacet);
        final Collection<IdeaSourceProvider> ideaSourceProviders = collectIdeaSourceProviders(androidFacet);

        for (IdeaSourceProvider sourceProvider : ideaSourceProviders) {
            final Collection<VirtualFile> assetsDirectories = sourceProvider.getAssetsDirectories();
            for (VirtualFile assetDir : assetsDirectories) {
                final AndroidSource source = new AndroidSource(sourceProvider.getName(), module, packageName);
                source.setAssetsDir(assetDir);
                source.setJavaDir(findJavaDir(source.getParentDir().getPath(), sourceProviders));
                source.setResDir(findResDir(source.getParentDir().getPath(), sourceProviders));
                sources.add(source);
            }
        }
    }

    private Collection<SourceProvider> collectSourceProviders(AndroidFacet androidFacet) {
        final List<SourceProvider> sourceProviders = new ArrayList<SourceProvider>();
        sourceProviders.add(androidFacet.getMainSourceProvider());
        sourceProviders.add(androidFacet.getBuildTypeSourceProvider());

        final List<SourceProvider> flavoredSourceProviders = androidFacet.getFlavorSourceProviders();
        if (flavoredSourceProviders != null) {
            sourceProviders.addAll(flavoredSourceProviders);
        }
        return sourceProviders;
    }

    private Collection<IdeaSourceProvider> collectIdeaSourceProviders(AndroidFacet androidFacet) {
        final List<IdeaSourceProvider> sourceProviders = new ArrayList<IdeaSourceProvider>();
        sourceProviders.add(androidFacet.getMainIdeaSourceProvider());
        sourceProviders.add(androidFacet.getIdeaBuildTypeSourceProvider());

        final List<IdeaSourceProvider> flavoredSourceProviders = androidFacet.getIdeaFlavorSourceProviders();
        if (flavoredSourceProviders != null) {
            sourceProviders.addAll(flavoredSourceProviders);
        }
        return sourceProviders;
    }

    private File findJavaDir(String path, Collection<SourceProvider> sourceProviders) {
        for (SourceProvider sourceProvider : sourceProviders) {
            for (File file : sourceProvider.getJavaDirectories()) {
                if (file.getParent().equals(path)) {
                    return file;
                }
            }
        }
        return null;
    }


    private File findResDir(String path, Collection<SourceProvider> sourceProviders) {
        for (SourceProvider sourceProvider : sourceProviders) {
            for (File file : sourceProvider.getResDirectories()) {
                if (file.getParent().equals(path)) {
                    return file;
                }
            }
        }
        return null;
    }

    public void generate() {
        for (final AndroidSource source : sources) {
            try {
                generateFromSource(source);
            } catch (Exception e) {
                final String msg = format("%s while generating files: %s", e.getClass().getSimpleName(), e.getMessage());
                Log.e(getClass(), e, msg);
                Dialogs.showErrorDialog(msg, project);
            }
        }
    }

    private void generateFromSource(AndroidSource source) throws Exception {
        Log.d(getClass(), "Processing %s.", source);
        final LocalFileSystem lfs = LocalFileSystem.getInstance();

        VirtualFile javaDir = lfs.findFileByIoFile(source.getJavaDir());
        if (javaDir == null) {
            javaDir = createChildDirectoryIfNotExist(project, source.getParentDir(), source.getJavaDir().getName());
        }

        VirtualFile resDir = lfs.findFileByIoFile(source.getResDir());
        if (resDir == null) {
            resDir = createChildDirectoryIfNotExist(project, source.getParentDir(), source.getResDir().getName());
        }
        VirtualFile valuesDir = lfs.findFileByIoFile(new File(source.getResDir(), "values"));
        if (valuesDir == null) {
            valuesDir = createChildDirectoryIfNotExist(project, resDir, "values");
        }

        final PsiDirectory psiJavaDir = PsiManager.getInstance(project).findDirectory(javaDir);
        final PsiDirectory psiPackageDir = findOrCreateDirectoryForPackage(source.getModule(), source.getPackageName(), psiJavaDir, false, false);
        if (psiPackageDir == null) {
            Log.e(getClass(), "PsiDirectory for %s is null.", source.getPackageName());
            return;
        }

        final FontFamilyClassGenerator classGenerator = new FontFamilyClassGenerator.Builder()
                .withPackageName(source.getPackageName())
                .withTemplateEngine(templateEngine)
                .build();

        final FontFamilyStyleGenerator styleGenerator = new FontFamilyStyleGenerator.Builder().build();

        final List<FontFamily> fontFamilies = new VirtualFileProcessor().process(source.getAssetsDir());

        for (SimpleImmutableEntry<String, String> entry : classGenerator.generate(fontFamilies)) {
            writeJavaClass(entry, psiPackageDir);
        }

        for (SimpleImmutableEntry<String, String> entry : styleGenerator.generate(fontFamilies)) {
            writeToFile(entry, valuesDir);
        }
    }

    private void writeJavaClass(final SimpleImmutableEntry<String, String> entry, final PsiDirectory psiPackageDir) {
        CommandHelper.runWriteCommand(project, new Runnable() {
            @Override
            public void run() {
                try {
                    final PsiFile oldJavaClass = psiPackageDir.findFile(entry.getKey());
                    if (oldJavaClass != null) {
                        oldJavaClass.delete();
                    }

                    final PsiFile newJavaClass = PsiFileFactory.getInstance(project).createFileFromText(entry.getKey(), JavaFileType.INSTANCE, entry.getValue());
                    final PsiJavaFile javaFile = (PsiJavaFile) psiPackageDir.add(newJavaClass);

                    if (javaFile != null) {
                        VirtualFileManager.getInstance().syncRefresh();
                        Log.d(getClass(), "Successfully wrote %s in %s.", entry.getKey(), psiPackageDir.getName());
                    }

                } catch (Exception e) {
                    final String msg = format("Exception when writing %s in %s: %s.", entry.getKey(), psiPackageDir.getName(), e.getMessage());
                    Log.e(getClass(), e, msg);
                    Dialogs.showErrorDialog(msg, project);
                }
            }
        });
    }

    private void writeToFile(final SimpleImmutableEntry<String, String> entry, final VirtualFile outputDir) {
        CommandHelper.runWriteCommand(project, new Runnable() {
            @Override public void run() {
                try {
                    final VirtualFile oldFile = outputDir.findChild(entry.getKey());
                    if (oldFile != null && oldFile.exists()) {
                        oldFile.delete(null);
                    }

                    final VirtualFile newFile = outputDir.createChildData(null, entry.getKey());
                    asByteSink(newFile.getOutputStream(null)).asCharSink(Charsets.UTF_8).write(entry.getValue());
                } catch (Exception e) {
                    final String msg = format("Exception when writing %s in %s: %s.", entry.getKey(), outputDir.getName(), e.getMessage());
                    Log.e(getClass(), e, msg);
                    Dialogs.showErrorDialog(msg, project);
                }
            }
        });

    }
}
