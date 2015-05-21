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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import digitaslbi.ext.common.Constants;
import digitaslbi.ext.generator.BootstrapClassGenerator;
import digitaslbi.ext.generator.FontFamilyClassGenerator;
import digitaslbi.ext.generator.FontFamilyStyleGenerator;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.sdk.AndroidSdkUtils;
import org.jetbrains.android.util.AndroidUtils;

import java.util.List;

import static com.intellij.openapi.actionSystem.DataKeys.VIRTUAL_FILE;
import static digitaslbi.ext.common.Constants.ASSETS_FOLDER;
import static digitaslbi.ext.common.Constants.GENERATED_PACKAGE_NAME;
import static digitaslbi.ext.plugin.MainProjectComponent.initVelocity;

/**
 * Created by evelina on 14/05/15.
 */
public class GenerateAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent actionEvent) {
        final Project project = actionEvent.getProject();
        if (project == null) {
            return;
        }

        List<AndroidFacet> facets = AndroidUtils.getApplicationFacets(project);
        if(facets.isEmpty()) {
            Log.d(getClass(), "Not an Android project. Skipping.");
            return;
        }

        final VelocityEngine ve = initVelocity(getClass());
        final VelocityContext vc = new VelocityContext();

        final Template classTemplate = ve.getTemplate("FontFamily.java.vm");
        final Template bootstrapTemplate = ve.getTemplate("FontFamilies.java.vm");

        final FontFamilyClassGenerator classGenerator = new FontFamilyClassGenerator(GENERATED_PACKAGE_NAME, classTemplate, vc);
        final BootstrapClassGenerator bootstrapClassGenerator = new BootstrapClassGenerator(GENERATED_PACKAGE_NAME, bootstrapTemplate, vc);

        final VirtualFile selectedFile = VIRTUAL_FILE.getData(actionEvent.getDataContext());
        if (isAssetsFolder(selectedFile)) {
            try {
                new FontFamilyClassProcessor(classGenerator, bootstrapClassGenerator, project).generate(selectedFile);
                new FontFamilyStyleProcessor(new FontFamilyStyleGenerator(), project).generate(selectedFile);
            } catch (Exception e) {
                Log.e(getClass(), e, "Failed to generate files with exception.");
                Dialogs.showErrorDialog("AndroidExtensionsPlugin failed with exception:\n" + e.getMessage(), project);
            }
        } else {
            Dialogs.showErrorDialog("You need to select the '" + Constants.ASSETS_FOLDER + "' folder.", project);
        }
    }

    private boolean isAssetsFolder(VirtualFile selectedFile) {
        return selectedFile != null &&
                selectedFile.exists() &&
                selectedFile.isDirectory() &&
                selectedFile.getName().equals(ASSETS_FOLDER);
    }
}
