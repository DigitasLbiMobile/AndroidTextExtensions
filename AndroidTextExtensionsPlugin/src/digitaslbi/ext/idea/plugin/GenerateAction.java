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

package digitaslbi.ext.idea.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import digitaslbi.ext.generator.Constants;
import digitaslbi.ext.generator.JavaClassGenerator;
import digitaslbi.ext.generator.XmlResourceGenerator;

import static digitaslbi.ext.generator.Constants.ASSETS_FOLDER;
import static digitaslbi.ext.generator.Constants.GENERATED_PACKAGE_NAME;

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

        VirtualFile selectedFile = DataKeys.VIRTUAL_FILE.getData(actionEvent.getDataContext());
        if (isAssetsFolder(selectedFile)) {
            try {
                new JavaClassFileProcessor(new JavaClassGenerator(getMainPackageName(project)), project).generate(selectedFile);
                new XmlResourceFileProcessor(new XmlResourceGenerator(), project).generate(selectedFile);
            } catch (Exception e) {
                Log.e(getClass(), e, "Failed to generate files with exception.");
                Dialogs.showErrorDialog("AndroidExtensionsPlugin failed with exception:\n" + e.getMessage(), project);
            }
        } else if (selectedFile != null) {
            Dialogs.showErrorDialog("You need to select the '" + Constants.ASSETS_FOLDER + "' folder.", project);
        }
    }

    private boolean isAssetsFolder(VirtualFile selectedFile) {
        return selectedFile != null &&
                selectedFile.exists() &&
                selectedFile.isDirectory() &&
                selectedFile.getName().equals(ASSETS_FOLDER);
    }

    private String getMainPackageName(Project project) {
        return GENERATED_PACKAGE_NAME;
    }
}
