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
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.*;
import com.intellij.util.Processor;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.update.MergingUpdateQueue;
import digitaslbi.ext.generator.CodeGenerator.FileType;
import org.jetbrains.android.facet.AndroidFacet;
import org.jetbrains.android.util.AndroidUtils;
import org.jetbrains.annotations.NotNull;

import javax.xml.transform.TransformerConfigurationException;
import java.util.List;

import static digitaslbi.ext.common.Constants.BOOTSTRAP_CLASS_NAME;
import static digitaslbi.ext.plugin.CommandHelper.runLater;
import static digitaslbi.ext.plugin.VirtualFileProcessor.*;

/**
 * Created by vrabiee on 18/05/15.
 */
public class AssetsWatcher {

    private final Project project;
    private final VirtualFileProcessor[] processors;
    private final MergingUpdateQueue updateQueue;
    private final VirtualFile assetsFolder;
    private MessageBusConnection connection;

    public AssetsWatcher(Project project, VirtualFileProcessor... processors) throws TransformerConfigurationException {
        this.project = project;
        this.processors = processors;
        this.assetsFolder = findAssetsFolder(project);
        this.updateQueue = new MergingUpdateQueue(getClass() + ": Assets changes queue", 1000, false, MergingUpdateQueue.ANY_COMPONENT);
    }

    public synchronized void start() {
        List<AndroidFacet> facets = AndroidUtils.getApplicationFacets(project);
        if(facets.isEmpty()) {
            Log.d(getClass(), "Not an Android project. Skipping.");
            return;
        }

        if (assetsFolder == null) {
            Log.e(getClass(), "Can't find the 'src/main/assets' package.");
            Dialogs.showFolderNotFoundDialog(ASSETS_PACKAGE, project);
            return;
        }

        connection = project.getMessageBus().connect(updateQueue);
        connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener.Adapter() {
            @Override public void before(@NotNull List<? extends VFileEvent> events) {
                for (VFileEvent event : events) {
                    if (isRelevantEvent(event)) {
                        processBeforeEvent(event);
                    }
                }
            }

            @Override public void after(@NotNull List<? extends VFileEvent> events) {
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
        return event.getFile() != null && event.getPath().contains(ASSETS_PACKAGE) &&
                (event.getFile().isDirectory() || isValidFontFile(event.getFile()));
    }

    private void processBeforeEvent(VFileEvent event) {
        if (event.getFile() == null) {
            Log.e(getClass(), "Event file %s is null.", event.getPath());
            return;
        }

        if (event instanceof VFileDeleteEvent) {
            if (event.getFile().isDirectory()) {
                VfsUtilCore.processFilesRecursively(event.getFile(), new Processor<VirtualFile>() {
                    @Override public boolean process(VirtualFile virtualFile) {
                        if (isValidFontFile(virtualFile)) {
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

    private void processAfterEvent(VFileEvent event) {
        runLater(project, new Runnable() {
            @Override public void run() {
                for (VirtualFileProcessor processor : processors) {
                    try {
                        processor.generate(assetsFolder);
                    } catch (Exception e) {
                        Log.e(getClass(), e, "Failed to generate files with exception.");
                        Dialogs.showErrorDialog("AndroidExtensionsPlugin failed with exception:\n" + e.getMessage(), project);
                    }
                }

            }
        });
    }

    private void deleteFile(String fileName) {
        for (VirtualFileProcessor processor : processors) {
            processor.delete(getFileName(BOOTSTRAP_CLASS_NAME, FileType.JAVA), project);
            processor.delete(getFileName(getFontFamilyName(fileName), FileType.JAVA), project);
        }
    }

    public synchronized void stop() {
        if (connection != null) {
            connection.disconnect();
        }
    }
}


