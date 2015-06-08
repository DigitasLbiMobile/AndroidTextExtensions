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
import digitaslbi.ext.plugin.utils.Dialogs;
import digitaslbi.ext.plugin.utils.Log;

import static com.intellij.openapi.actionSystem.DataKeys.VIRTUAL_FILE;

/**
 * Created by evelina on 14/05/15.
 */
public class GenerateAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent actionEvent) {
        final Project project = actionEvent.getProject();
        if (project == null) {
            Log.e(getClass(), "The selected project is null.");
            return;
        }

        final VirtualFile selectedFile = actionEvent.getData(VIRTUAL_FILE);
        if (selectedFile == null) {
            Dialogs.showError(project, "Select an app or library module inside your Android project.");
            return;
        }

        try {
            new SettingsDialog(new AndroidTextExtensionsPlugin(project, selectedFile)).show();
        } catch (PluginException e) {
            Dialogs.showError(project, e.getMessage());
        }
    }
}
