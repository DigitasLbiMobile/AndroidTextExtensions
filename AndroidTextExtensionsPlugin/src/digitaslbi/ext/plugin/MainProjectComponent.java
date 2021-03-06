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
import org.jetbrains.annotations.NotNull;

import static digitaslbi.ext.plugin.utils.CommandHelper.runWhenInitialized;


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
                assetsWatcher = new AssetsWatcher(myProject);
                assetsWatcher.start();
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
}
