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

import com.android.builder.model.SourceProvider;
import com.android.tools.idea.gradle.parser.BuildFileKey;
import com.android.tools.idea.gradle.parser.Dependency;
import com.android.tools.idea.gradle.parser.Dependency.Scope;
import com.android.tools.idea.gradle.parser.Dependency.Type;
import com.android.tools.idea.gradle.parser.GradleBuildFile;
import com.android.tools.idea.gradle.parser.GradleSettingsFile;
import com.google.common.base.Charsets;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.*;
import digitaslbi.ext.common.FontFamily;
import digitaslbi.ext.generator.FontFamilyClassGenerator;
import digitaslbi.ext.generator.FontFamilyStyleGenerator;
import digitaslbi.ext.plugin.utils.CommandHelper;
import digitaslbi.ext.plugin.utils.Dialogs;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.facet.IdeaSourceProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;

import static com.intellij.ide.util.PackageUtil.findOrCreateDirectoryForPackage;
import static digitaslbi.ext.generator.Streams.asByteSink;
import static java.lang.String.format;
import static org.jetbrains.android.util.AndroidUtils.createChildDirectoryIfNotExist;

/**
 * @author Evelina Vrabie on 26/05/15.
 */
public class AndroidTextExtensionsPlugin {

    public static String GRADLE_LIBRARY_DEPENDENCY = "digitaslbi.ext:library:1.+";
    public static final String GENERATED_PACKAGE_NAME = "digitaslbi.ext.fonts";

    private final Project project;
    private final Module module;
    private final AndroidFacet androidFacet;
    private final LocalFileSystem localFileSystem = LocalFileSystem.getInstance();

    private final Set<GeneratorSource> sources = new HashSet<GeneratorSource>();
    private final VirtualTemplateEngine templateEngine = new VirtualTemplateEngine(getClass());

    public AndroidTextExtensionsPlugin(
            @NotNull Project project, @NotNull VirtualFile selectedFile) throws PluginException {
        this.project = project;

        module = ModuleUtil.findModuleForFile(selectedFile, project);
        if (module == null) {
            throw new PluginException("Select an app or library module inside your Android project.");
        }

        androidFacet = AndroidFacet.getInstance(module);
        if (androidFacet == null) {
            throw new PluginException("Select an app or library module inside your Android project.", module.getName());
        }

        collectSourceDirs();
    }

    private void collectSourceDirs() {
        // TODO find a way to customise the package name and let the FontManager know where to look for FontFamilies.java class
        final String packageName = GENERATED_PACKAGE_NAME;

        final Collection<SourceProvider> sourceProviders = collectSourceProviders(androidFacet);
        final Collection<IdeaSourceProvider> ideaSourceProviders = collectIdeaSourceProviders(androidFacet);

        for (IdeaSourceProvider sourceProvider : ideaSourceProviders) {
            final Collection<VirtualFile> assetsDirectories = sourceProvider.getAssetsDirectories();
            for (VirtualFile assetDir : assetsDirectories) {
                final VirtualFile parentDir = assetDir.getParent();
                final File javaDir = findJavaDir(parentDir.getPath(), sourceProviders);
                final File resDir = findResDir(parentDir.getPath(), sourceProviders);
                if (javaDir != null && resDir != null) {
                    final GeneratorSource source = new GeneratorSource(sourceProvider.getName(), module, packageName);
                    source.setAssetsDir(assetDir);
                    source.setJavaDir(javaDir);
                    source.setResDir(resDir);
                    sources.add(source);
                }
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

    public Project getProject() {
        return project;
    }

    public Module getModule() {
        return module;
    }

    public void generate() throws PluginException {
        for (final GeneratorSource source : sources) {
            final List<FontFamily> fontFamilies = new VirtualFileProcessor().process(source.getAssetsDir());
            writeJavaClasses(source, fontFamilies);
            writeResources(source, fontFamilies);
        }
    }

    private void writeResources(GeneratorSource source, List<FontFamily> fontFamilies) throws PluginException {
        final FontFamilyStyleGenerator styleGenerator = new FontFamilyStyleGenerator.Builder().build();
        final VirtualFile valuesDir = ensureValuesDir(source);
        for (SimpleImmutableEntry<String, String> entry : styleGenerator.generate(fontFamilies)) {
            writeResource(entry, valuesDir);
        }
    }

    private void writeResource(final SimpleImmutableEntry<String, String> entry, final VirtualFile outputDir) {
        CommandHelper.runWriteCommand(project, new Runnable() {
            @Override public void run() {
                try {
                    final VirtualFile oldFile = outputDir.findChild(entry.getKey());
                    if (oldFile != null && oldFile.exists()) {
                        oldFile.delete(null);
                    }
                    final VirtualFile newFile = outputDir.createChildData(null, entry.getKey());
                    asByteSink(newFile.getOutputStream(null)).asCharSink(Charsets.UTF_8).write(entry.getValue());

                } catch (IOException e) {
                    Dialogs.showError(project, format("Can't write %s in %s with exception: %s.", entry.getKey(), outputDir.getName(), e.getMessage()));
                }
            }
        });
    }

    private void writeJavaClasses(GeneratorSource source, List<FontFamily> fontFamilies) throws PluginException {
        final VirtualFile javaDir = ensureJavaDir(source);
        final PsiDirectory psiJavaDir = PsiManager.getInstance(project).findDirectory(javaDir);
        final PsiDirectory psiPackageDir = findOrCreateDirectoryForPackage(source.getModule(), source.getPackageName(), psiJavaDir, false, false);
        if (psiPackageDir == null) {
            throw new PluginException("Can't find PsiDirectory for %s.", source.getPackageName());
        }

        final FontFamilyClassGenerator classGenerator = new FontFamilyClassGenerator.Builder()
                .withPackageName(source.getPackageName())
                .withTemplateEngine(templateEngine)
                .build();

        for (SimpleImmutableEntry<String, String> entry : classGenerator.generate(fontFamilies)) {
            writeJavaClass(entry, psiPackageDir);
        }
    }

    private VirtualFile ensureJavaDir(GeneratorSource source) throws PluginException {
        final VirtualFile javaDir = localFileSystem.findFileByIoFile(source.getJavaDir());
        if (javaDir == null) {
            try {
                return createChildDirectoryIfNotExist(project, source.getParentDir(), source.getJavaDir().getName());
            } catch (IOException e) {
                throw new PluginException(e, "Can't create %s inside %s.", source.getJavaDir().getName(), source.getParentDir().getName());
            }
        }
        return javaDir;
    }

    private VirtualFile ensureResDir(GeneratorSource source) throws PluginException {
        final VirtualFile resDir = localFileSystem.findFileByIoFile(source.getResDir());
        if (resDir == null) {
            try {
                return createChildDirectoryIfNotExist(project, source.getParentDir(), source.getResDir().getName());
            } catch (IOException e) {
                throw new PluginException(e, "Can't create the res folder inside %s.", source.getParentDir().getName());
            }
        }
        return resDir;
    }

    private VirtualFile ensureValuesDir(GeneratorSource source) throws PluginException {
        final VirtualFile resDir = ensureResDir(source);
        final VirtualFile valuesDir = localFileSystem.findFileByIoFile(new File(source.getResDir(), "values"));
        if (valuesDir == null) {
            try {
                return createChildDirectoryIfNotExist(project, resDir, "values");
            } catch (IOException e) {
                throw new PluginException(e, "Can't create the values folder inside %s.", source.getParentDir().getName());
            }
        }
        return valuesDir;
    }

    private void writeJavaClass(final SimpleImmutableEntry<String, String> entry, final PsiDirectory psiPackageDir) {
        CommandHelper.runWriteCommand(project, new Runnable() {
            @Override
            public void run() {
                final PsiFile oldJavaClass = psiPackageDir.findFile(entry.getKey());
                if (oldJavaClass != null) {
                    oldJavaClass.delete();
                }

                final PsiFile newJavaClass = PsiFileFactory.getInstance(project).createFileFromText(entry.getKey(), JavaFileType.INSTANCE, entry.getValue());
                final PsiJavaFile javaFile = (PsiJavaFile) psiPackageDir.add(newJavaClass);

                if (javaFile != null) {
                    VirtualFileManager.getInstance().syncRefresh();
                    Dialogs.showInfo(project, format("Generated %s inside %s.", entry.getKey(), psiPackageDir.getName()));
                }
            }
        });
    }

    private GradleBuildFile getGradleBuildFile() throws PluginException {
        final String moduleGradlePath = GradleSettingsFile.getModuleGradlePath(module);
        if (moduleGradlePath == null) {
            throw new PluginException("Gradle module graph is null.");
        }

        final GradleSettingsFile settingsFile = GradleSettingsFile.get(project);
        if (settingsFile == null) {
            throw new PluginException("Gradle settings file is null.");
        }

        final GradleBuildFile buildFile = settingsFile.getModuleBuildFile(moduleGradlePath);
        if (buildFile == null) {
            throw new PluginException("Gradle build file is null.");
        }
        return buildFile;
    }

    private List<Dependency> getGradleDependencies(@NotNull GradleBuildFile buildFile) throws PluginException {
        return (List<Dependency>) buildFile.getValue(BuildFileKey.DEPENDENCIES);
    }

    private boolean findLibraryDependency(@NotNull List<Dependency> dependencies) {
        for (Dependency dependency : dependencies) {
            if (dependency.scope == Scope.COMPILE && dependency.type == Type.EXTERNAL &&
                    dependency.data != null && String.valueOf(dependency.data).contains(GRADLE_LIBRARY_DEPENDENCY)) {
                return true;
            }
        }
        return false;
    }

    private void addLibraryDependency(
            @NotNull final GradleBuildFile buildFile,
            @NotNull final List<Dependency> dependencies) throws PluginException {
        final Dependency dependency = new Dependency(Dependency.Scope.COMPILE, Type.EXTERNAL, GRADLE_LIBRARY_DEPENDENCY);
        dependencies.add(dependency);
        new WriteCommandAction<Void>(project, "Adding Dependencies", buildFile.getPsiFile()) {
            @Override
            protected void run(@NotNull Result<Void> result) throws Throwable {
                buildFile.setValue(BuildFileKey.DEPENDENCIES, dependencies);
            }
        }.execute();
    }

    public boolean detectLibraryDependency() throws PluginException {
        final GradleBuildFile buildFile = getGradleBuildFile();
        final List<Dependency> dependencies = getGradleDependencies(buildFile);
        return dependencies != null && findLibraryDependency(dependencies);
    }
}
