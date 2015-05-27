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
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import com.intellij.util.Processor;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.update.MergingUpdateQueue;
import digitaslbi.ext.common.Constants;
import digitaslbi.ext.plugin.models.AndroidProject;
import digitaslbi.ext.plugin.utils.Dialogs;
import digitaslbi.ext.plugin.utils.Log;
import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.transform.TransformerConfigurationException;
import java.io.IOException;
import java.util.List;

import static digitaslbi.ext.plugin.utils.CommandHelper.runLater;
import static digitaslbi.ext.plugin.utils.CommandHelper.runWriteCommand;
import static java.lang.String.format;

/**
 * Created by vrabiee on 18/05/15.
 */
public class AssetsWatcher {

    private final Project project;
    private final VirtualFileProcessor fileProcessor;
    private final MergingUpdateQueue updateQueue;
    private MessageBusConnection connection;
    private AndroidProject androidProject;

    public AssetsWatcher(Project project) throws TransformerConfigurationException {
        this.project = project;
        this.fileProcessor = new VirtualFileProcessor();
        this.updateQueue = new MergingUpdateQueue(getClass() + ": Assets changes queue", 1000, false, MergingUpdateQueue.ANY_COMPONENT);
    }

    public synchronized void start() {
        if (AndroidUtils.getApplicationFacets(project).isEmpty()) {
            Log.d(getClass(), "Not an Android module/project.", project);
            return;
        }

        connection = project.getMessageBus().connect(updateQueue);
        connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener.Adapter() {
            @Override
            public void before(@NotNull List<? extends VFileEvent> events) {
                for (VFileEvent event : events) {
                    if (isRelevantEvent(event)) {
                        processBeforeEvent(event);
                    }
                }
            }

            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                for (VFileEvent event : events) {
                    if (isRelevantEvent(event)) {
                        processAfterEvent(event);
                    }
                }
            }
        });
        updateQueue.activate();
    }

    private boolean isRelevantEvent(VFileEvent event) {
        return event.getFile() != null && event.getPath().contains(Constants.ASSETS_FOLDER) &&
                (event.getFile().isDirectory() || fileProcessor.isValidFontFile(event.getFile()));
    }

    private void processBeforeEvent(VFileEvent event) {
        if (event.getFile() == null) {
            Log.e(getClass(), "Event file %s is null.", event.getPath());
            return;
        }

        androidProject = new AndroidProject(project, event.getFile());

        if (event instanceof VFileDeleteEvent) {
            if (event.getFile().isDirectory()) {
                VfsUtilCore.processFilesRecursively(event.getFile(), new Processor<VirtualFile>() {
                    @Override
                    public boolean process(VirtualFile virtualFile) {
                        if (fileProcessor.isValidFontFile(virtualFile)) {
                            deleteFile(virtualFile.getPath());
                        }
                        return true;
                    }
                });
            } else {
                deleteFile(event.getPath());
            }
        }

        if (event instanceof VFileCopyEvent || event instanceof VFileContentChangeEvent) {
            deleteFile(event.getPath());
        } else if (event instanceof VFileMoveEvent) {
            deleteFile(((VFileMoveEvent) event).getOldPath());
        } else if (event instanceof VFilePropertyChangeEvent && ((VFilePropertyChangeEvent) event).getPropertyName().endsWith(VirtualFile.PROP_NAME)) {
            deleteFile(((VFilePropertyChangeEvent) event).getOldPath());
        }
    }

    private void processAfterEvent(final VFileEvent event) {
        runLater(project, new Runnable() {
            @Override
            public void run() {
                androidProject.generate();
            }
        });
    }

    private void deleteFile(final String fontFile) {
        final String fontFamilyName = fileProcessor.getFontFamilyName(fontFile);
        final String[] filesToDelete = new String[]{
                fontFamilyName + ".java", fontFamilyName + ".xml", Constants.BOOTSTRAP_CLASS_NAME + ".java"
        };

        for (final String fileToDelete : filesToDelete) {
            runWriteCommand(project, new Runnable() {
                @Override
                public void run() {
                    VfsUtilCore.visitChildrenRecursively(project.getBaseDir(), new VirtualFileVisitor(VirtualFileVisitor.SKIP_ROOT) {
                        @Override
                        public boolean visitFile(@NotNull VirtualFile file) {
                            if (file.getName().equals(fileToDelete)) {
                                try {
                                    file.delete(null);
                                } catch (IOException e) {
                                    Log.e(getClass(), e, "Failed to delete %s.", fileToDelete);
                                    Dialogs.showErrorDialog(format("Failed to delete %s:\n%s", fileToDelete, e.getMessage()), project);
                                }
                                return false;
                            }
                            return true;
                        }
                    });
                }
            });
        }
    }

    public synchronized void stop() {
        if (connection != null) {
            connection.disconnect();
        }
    }
}


