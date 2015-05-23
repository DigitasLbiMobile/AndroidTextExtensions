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

import com.android.tools.idea.rendering.LocalResourceRepository;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import digitaslbi.ext.common.Constants;
import org.jetbrains.android.facet.AndroidFacet;

import java.io.File;
import java.util.Collection;

import static com.intellij.openapi.actionSystem.DataKeys.VIRTUAL_FILE;
import static digitaslbi.ext.common.Constants.*;

/**
 * Created by evelina on 14/05/15.
 */
public class GenerateAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent actionEvent) {
        final Project project = actionEvent.getProject();
        if (project == null) {
            Log.d(getClass(), "Project is null.");
            return;
        }

        final VirtualFile selectedFile = actionEvent.getData(VIRTUAL_FILE);
        if (selectedFile == null) {
            Log.d(getClass(), "Selected file is null.");
            return;
        }

        final Module selectedModule = ModuleUtil.findModuleForFile(selectedFile, project);
        if (selectedModule == null) {
            Log.d(getClass(), "Selected module is null.");
            return;
        }

        final AndroidFacet androidFacet = AndroidFacet.getInstance(selectedModule);
        if (androidFacet == null) {
            Log.d(getClass(), "Not an Android project.");
            return;
        }

        final String packageName = androidFacet.getAndroidModuleInfo().getPackage();
        if (packageName == null) {
            Log.e(getClass(), "The package name for module %s not found (null).", selectedModule.getName());
            return;
        }


        VirtualFile rootDir = androidFacet.getIdeaAndroidProject().getRootDir();


        final String generatedPackageName = packageName + "." + Constants.FONTS_PACKAGE;
        new SettingsDialog(project, generatedPackageName).show();

//        final VelocityEngine ve = initVelocity(getClass());
//        final VelocityContext vc = new VelocityContext();
//
//        final Template classTemplate = ve.getTemplate("FontFamily.java.vm");
//        final Template bootstrapTemplate = ve.getTemplate("FontFamilies.java.vm");
//
//        final FontFamilyClassGenerator classGenerator = new FontFamilyClassGenerator(GENERATED_PACKAGE_NAME, classTemplate, vc);
//        final BootstrapClassGenerator bootstrapClassGenerator = new BootstrapClassGenerator(GENERATED_PACKAGE_NAME, bootstrapTemplate, vc);
//
//        if (isAssetsFolder(selectedFile)) {
//            try {
//                new FontFamilyClassProcessor(classGenerator, bootstrapClassGenerator, project).generate(selectedFile);
//                new FontFamilyStyleProcessor(new FontFamilyStyleGenerator(), project).generate(selectedFile);
//            } catch (Exception e) {
//                Log.e(getClass(), e, "Failed to generate files with exception.");
//                Dialogs.showErrorDialog("AndroidExtensionsPlugin failed with exception:\n" + e.getMessage(), project);
//            }
//        } else {
//            Dialogs.showErrorDialog("You need to select the '" + Constants.ASSETS_FOLDER + "' folder.", project);
//        }
    }

    private boolean isAssetsFolder(VirtualFile selectedFile) {
        return selectedFile != null &&
                selectedFile.exists() &&
                selectedFile.isDirectory() &&
                selectedFile.getName().equals(ASSETS_FOLDER);
    }


}
