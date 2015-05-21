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
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.VirtualFileSystem;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileCopyEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.update.MergingUpdateQueue;
import digitaslbi.ext.generator.BootstrapClassGenerator;
import digitaslbi.ext.generator.CodeGenerator.FileType;
import digitaslbi.ext.generator.FontFamilyClassGenerator;
import digitaslbi.ext.generator.FontFamilyStyleGenerator;
import org.jetbrains.annotations.NotNull;

import javax.xml.transform.TransformerConfigurationException;
import java.util.List;

import static digitaslbi.ext.common.Constants.ASSETS_FOLDER;
import static digitaslbi.ext.common.Constants.GENERATED_PACKAGE_NAME;
import static digitaslbi.ext.generator.FileProcessor.getFontFamilyName;
import static digitaslbi.ext.generator.FileProcessor.isValidFontExtension;
import static digitaslbi.ext.plugin.CommandHelper.runLater;
import static digitaslbi.ext.plugin.VirtualFileProcessor.findAssetsFolder;

/**
 * Created by vrabiee on 18/05/15.
 */
public class AssetsWatcher {

    private final Project project;
    private final VirtualFileSystem fileSystem;
    private final FontFamilyClassFileProcessor classProcessor;
    private final FontFamilyStyleFileProcessor styleProcessor;
    private final MergingUpdateQueue updateQueue;
    private MessageBusConnection connection;

    public AssetsWatcher(Project project) throws TransformerConfigurationException {
        this.project = project;
        this.updateQueue = new MergingUpdateQueue(getClass() + ": Assets changes queue", 1000, false, MergingUpdateQueue.ANY_COMPONENT);
        this.fileSystem = project.getBaseDir().getFileSystem();
        this.classProcessor = new FontFamilyClassFileProcessor(new FontFamilyClassGenerator(GENERATED_PACKAGE_NAME), new BootstrapClassGenerator(GENERATED_PACKAGE_NAME), project);
        this.styleProcessor = new FontFamilyStyleFileProcessor(new FontFamilyStyleGenerator(), project);
    }

    public synchronized void start() {
        connection = project.getMessageBus().connect(updateQueue);
        connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener.Adapter() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                for (VFileEvent event : events) {
                    if (isRelevantEvent(event)) {
                        Log.d(getClass(), "File %s changed: %s", event.getPath(), event.getClass().getSimpleName());
                        runLater(new ProcessEvent(event));
                    }
                }
            }
        });
        updateQueue.activate();
    }

    private boolean isRelevantEvent(VFileEvent event) {
        return event.getFile() != null && event.getFile().getFileType().isBinary() && isValidFontExtension(event.getFile().getName());
    }


    public synchronized void stop() {
        connection.disconnect();
    }

    private class ProcessEvent implements Runnable {

        private final VFileEvent event;

        public ProcessEvent(VFileEvent event) {
            this.event = event;

        }

        @Override
        public void run() {
            processEvent(event);
        }

        private void processEvent(VFileEvent event) {
            if (event.getFile() == null) {
                Log.e(getClass(), "Event file %s is null.", event.getPath());
                return;
            }

            final String fontFamilyName = getFontFamilyName(event.getFile().getName());
            Log.d(getClass(), "Deleting .java and .xml files for %s.", fontFamilyName);

            // delete Java and XML files for this font family
            classProcessor.delete(fontFamilyName, FileType.JAVA, project);
            styleProcessor.delete(fontFamilyName, FileType.XML, project);

            // re-create the files
            if (event instanceof VFileCopyEvent || event instanceof VFileContentChangeEvent) {
                Log.d(getClass(), "Re-generating .java and .xml files for %s.", fontFamilyName);

                final VirtualFile assetsFolder = findAssetsFolder(fileSystem.findFileByPath(event.getPath()));
                if (assetsFolder == null) {
                    Log.e(getClass(), "Can't find the '%s' folder.", ASSETS_FOLDER);
                    Dialogs.showFolderNotFoundDialog(ASSETS_FOLDER, project);
                    return;
                }

                try {
                    classProcessor.generate(assetsFolder);
                    styleProcessor.generate(assetsFolder);
                } catch (Exception e) {
                    Log.e(getClass(), e, "Failed to generate files with exception.");
                    Dialogs.showErrorDialog("AndroidExtensionsPlugin failed with exception:\n" + e.getMessage(), project);
                }
            }
        }
    }
}


