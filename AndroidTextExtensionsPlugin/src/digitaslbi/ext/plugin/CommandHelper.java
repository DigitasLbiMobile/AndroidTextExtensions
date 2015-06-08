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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;

/**
 * Created by evelina on 15/05/15.
 */
public final class CommandHelper {

    public static void runReadCommand(Project project, Runnable cmd) {
        CommandProcessor.getInstance().executeCommand(project, new ReadAction(cmd), "read", "read");
    }

    public static void runWriteCommand(Project project, Runnable cmd) {
        CommandProcessor.getInstance().executeCommand(project, new WriteAction(cmd), "write", "write");
    }

    public static void runWhenInitialized(final Project project, final Runnable r) {
        if (project.isDisposed()) {
            return;
        }

        if (!project.isInitialized()) {
            StartupManager.getInstance(project).registerPostStartupActivity(r);
            return;
        }

        runDumbAware(project, r);
    }

    public static void runDumbAware(final Project project, final Runnable r) {
        if (r instanceof DumbAware) {
            r.run();
        } else {
            DumbService.getInstance(project).runWhenSmart(new Runnable() {
                public void run() {
                    if (project.isDisposed()) {
                        return;
                    }
                    r.run();
                }
            });
        }
    }

    public static void runLater(final Project project, final Runnable r) {
        DumbService.getInstance(project).smartInvokeLater(r);
    }

    static class ReadAction implements Runnable {
        ReadAction(Runnable cmd) {
            this.cmd = cmd;
        }

        public void run() {
            ApplicationManager.getApplication().runReadAction(cmd);
        }

        Runnable cmd;
    }

    static class WriteAction implements Runnable {
        WriteAction(Runnable cmd) {
            this.cmd = cmd;
        }

        public void run() {
            ApplicationManager.getApplication().runWriteAction(cmd);
        }

        Runnable cmd;
    }

    private CommandHelper() {
    }
}
