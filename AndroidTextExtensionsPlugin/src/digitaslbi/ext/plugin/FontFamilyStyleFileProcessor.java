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
import digitaslbi.ext.common.FontFamily;
import digitaslbi.ext.generator.CodeGenerator;
import digitaslbi.ext.generator.CodeGenerator.FileType;
import groovy.json.internal.Charsets;

import java.io.IOException;

import static digitaslbi.ext.common.Constants.*;
import static digitaslbi.ext.generator.Streams.asByteSink;
import static digitaslbi.ext.plugin.CommandHelper.runWriteCommand;

/**
 * Created by evelina on 15/05/15.
 */
public class FontFamilyStyleFileProcessor extends VirtualFileProcessor {

    public FontFamilyStyleFileProcessor(CodeGenerator generator, Project project) {
        super(generator, project);
    }

    public void generate(final VirtualFile input) throws Exception {
        final VirtualFile assetsFolder = findAssetsFolder(input);
        if (assetsFolder == null) {
            Log.e(getClass(), "Can't find the '%s' folder.", ASSETS_FOLDER);
            Dialogs.showFolderNotFoundDialog(ASSETS_FOLDER, project);
            return;
        }

        final VirtualFile resFolder = assetsFolder.getParent().findChild(RES_FOLDER);
        if (resFolder == null) {
            Log.e(getClass(), "Can't find the '%s' folder.", RES_FOLDER);
            Dialogs.showFolderNotFoundDialog(RES_FOLDER, project);
            return;
        }

        for (FontFamily fontFamily : process(input)) {
            final StringBuilder appendable = new StringBuilder();
            try {
                final String fileName = fontFamily.getName() + FileType.XML.getExtension();
                generator.generate(fontFamily, appendable);

                runWriteCommand(project, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            VirtualFile valuesFolder = resFolder.findChild(VALUES_FOLDER);
                            if (valuesFolder == null || !valuesFolder.exists()) {
                                valuesFolder = resFolder.createChildDirectory(null, VALUES_FOLDER);
                                Log.d(getClass(), "Successfully created folder %s.", valuesFolder.getName());
                            }

                            VirtualFile newFile = valuesFolder.findChild(fileName);
                            if (newFile == null || !newFile.exists()) {
                                newFile = valuesFolder.createChildData(null, fileName);
                                asByteSink(newFile.getOutputStream(null)).asCharSink(Charsets.UTF_8).write(appendable.toString());
                                Log.d(getClass(), "Successfully wrote %s in %s.", fileName, valuesFolder.getName());

                            } else {
                                Log.e(getClass(), "Skipping %s, file already exists.", fileName);
                            }

                        } catch (IOException e) {
                            Log.d(getClass(), "Exception while writing files: %s", e.getMessage());
                        }
                    }
                });

            } catch (Exception e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
}

