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

package digitaslbi.ext.generator;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import java.util.Properties;

/**
 * @author Evelina Vrabie on 26/05/15.
 */
public class TemplateEngine {

    protected VelocityContext velocityContext;
    protected Template classTemplate;
    protected Template bootstrapClassTemplate;


    public TemplateEngine() {
       init();
    }

    protected void init() {
        final VelocityEngine engine = new VelocityEngine();
        Properties properties = new Properties();
        properties.setProperty("resource.loader", "file");
        properties.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        engine.init(properties);

        velocityContext = new VelocityContext();
        classTemplate = engine.getTemplate("FontFamily.java.vm");
        bootstrapClassTemplate = engine.getTemplate("FontFamilies.java.vm");

        System.out.println("PARENT");
    }

    public VelocityContext getVelocityContext() {
        return velocityContext;
    }

    public Template getClassTemplate() {
        return classTemplate;
    }

    public Template getBootstrapClassTemplate() {
        return bootstrapClassTemplate;
    }
}
