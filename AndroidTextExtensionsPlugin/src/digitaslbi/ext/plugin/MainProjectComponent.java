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

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import digitaslbi.ext.generator.BootstrapClassGenerator;
import digitaslbi.ext.generator.FontFamilyClassGenerator;
import digitaslbi.ext.generator.FontFamilyStyleGenerator;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.jetbrains.annotations.NotNull;

import javax.xml.transform.TransformerConfigurationException;

import static digitaslbi.ext.common.Constants.GENERATED_PACKAGE_NAME;
import static digitaslbi.ext.plugin.CommandHelper.runWhenInitialized;


/**
 * Created by vrabiee on 18/05/15.
 */
public class MainProjectComponent extends AbstractProjectComponent {

    private AssetsWatcher assetsWatcher;

    public MainProjectComponent(Project project) {
        super(project);
    }

    @Override
    public void initComponent() {
        runWhenInitialized(myProject, new Runnable() {
            public void run() {
                try {

                    final VelocityEngine ve = initVelocity(getClass());
                    final VelocityContext vc = new VelocityContext();
                    final Template classTemplate = ve.getTemplate("FontFamily.java.vm");
                    final Template bootstrapTemplate = ve.getTemplate("FontFamilies.java.vm");

                    final FontFamilyClassGenerator classGenerator = new FontFamilyClassGenerator(GENERATED_PACKAGE_NAME, classTemplate, vc);
                    final BootstrapClassGenerator bootstrapClassGenerator = new BootstrapClassGenerator(GENERATED_PACKAGE_NAME, bootstrapTemplate, vc);

                    final FontFamilyClassProcessor classProcessor = new FontFamilyClassProcessor(classGenerator, bootstrapClassGenerator, myProject);
                    final FontFamilyStyleProcessor styleProcessor = new FontFamilyStyleProcessor(new FontFamilyStyleGenerator(), myProject);

                    assetsWatcher = new AssetsWatcher(myProject, classProcessor, styleProcessor);
                    assetsWatcher.start();

                } catch (TransformerConfigurationException e) {
                    Log.e(getClass(), e, "Exception when initializing AssetWatcher.");
                    Dialogs.showErrorDialog("Can't instantiate the AssetWatcher.", myProject);
                }
            }
        });
    }

    @Override
    public void disposeComponent() {
        if (assetsWatcher != null) {
            assetsWatcher.stop();
        }
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "Android Text Extensions Plugin";
    }

    static VelocityEngine initVelocity(Class<?> cls) {
        Thread thread = Thread.currentThread();
        ClassLoader loader = thread.getContextClassLoader();
        thread.setContextClassLoader(cls.getClassLoader());
        try {
            final VelocityEngine ve = new VelocityEngine();
            ve.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, new VelocityLog());
            ve.setProperty(Velocity.RESOURCE_LOADER, "file");
            ve.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            ve.init();
            return ve;
        } finally {
            thread.setContextClassLoader(loader);
        }
    }
}
