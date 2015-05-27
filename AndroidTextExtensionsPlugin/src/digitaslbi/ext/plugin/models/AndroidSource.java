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

package digitaslbi.ext.plugin.models;

import com.google.common.base.Objects;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;

/**
 * @author Evelina Vrabie on 26/05/15.
 */
public class AndroidSource {
    private final String name;
    private final Module module;
    private final String packageName;

    private VirtualFile parentDir;
    private VirtualFile assetsDir;
    private File javaDir;
    private File resDir;

    public AndroidSource(String name, Module module, String packageName) {
        this.name = name;
        this.module = module;
        this.packageName = packageName;
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public VirtualFile getAssetsDir() {
        return assetsDir;
    }

    public void setAssetsDir(VirtualFile assetsDir) {
        this.assetsDir = assetsDir;
        this.parentDir = assetsDir.getParent();
    }

    public VirtualFile getParentDir() {
        return parentDir;
    }

    public File getJavaDir() {
        return javaDir;
    }

    public void setJavaDir(File javaDir) {
        this.javaDir = javaDir;
    }

    public File getResDir() {
        return resDir;
    }

    public void setResDir(File resDir) {
        this.resDir = resDir;
    }

    public Module getModule() {
        return module;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AndroidSource source = (AndroidSource) o;
        return Objects.equal(name, source.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AndroidSource{");
        sb.append("name='").append(name).append('\'');
        sb.append(", packageName='").append(packageName).append('\'');
        sb.append(", parentDir=").append(parentDir.getPath());
        sb.append(", assetsDir=").append(assetsDir.getPath());
        sb.append(", javaDir=").append(javaDir.getPath());
        sb.append(", resDir=").append(resDir.getPath());
        sb.append('}');
        return sb.toString();
    }
}
