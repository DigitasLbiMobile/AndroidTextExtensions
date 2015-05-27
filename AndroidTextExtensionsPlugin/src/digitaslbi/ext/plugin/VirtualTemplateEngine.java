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

import digitaslbi.ext.generator.TemplateEngine;
import digitaslbi.ext.plugin.utils.VelocityLog;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;

/**
 * @author Evelina Vrabie on 26/05/15.
 */
public class VirtualTemplateEngine extends TemplateEngine {

    public VirtualTemplateEngine(Class<?> cls) {
        init(cls);
    }

    @Override
    protected void init() {}

    protected void init(Class<?> cls) {
        Thread thread = Thread.currentThread();
        ClassLoader loader = thread.getContextClassLoader();
        thread.setContextClassLoader(cls.getClassLoader());
        try {
            final VelocityEngine ve = new VelocityEngine();
            ve.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, new VelocityLog());
            ve.setProperty(Velocity.RESOURCE_LOADER, "file");
            ve.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
            ve.init();

            velocityContext = new VelocityContext();
            classTemplate = ve.getTemplate("FontFamily.java.vm");
            bootstrapClassTemplate = ve.getTemplate("FontFamilies.java.vm");


        } finally {
            thread.setContextClassLoader(loader);
        }
    }
}
