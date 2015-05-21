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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;

import static java.lang.String.format;

/**
 * Created by evelina on 14/05/15.
 */
public class Dialogs {

    public static final String MISSING_FOLDER = "Can't find the '%s' folder inside project %s.";
    public static final String MISSING_MODULE = "Can't find module for file %s inside project %s.";

    public static void showInfoDialog(String text, Project project) {
        showDialog(MessageType.INFO, text, project);
    }

    public static void showErrorDialog(String text, Project project) {
        showDialog(MessageType.ERROR, text, project);
    }

    public static void showFolderNotFoundDialog(String folderName, Project project) {
        showDialog(MessageType.ERROR, format(MISSING_FOLDER, folderName, project.getName()), project);
    }

    public static void showModuleNotFoundError(String fileName, Project project) {
        showErrorDialog(format(MISSING_MODULE, fileName, project.getName()), project);
    }

    public static void showDialog(MessageType type, String text, Project project) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (statusBar != null) {
            JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder(text, type, null)
                    .setFadeoutTime(10000)
                    .createBalloon()
                    .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
        }
    }
}
