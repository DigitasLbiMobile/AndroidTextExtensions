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
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ResourceBundle;

public class SettingsDialog extends DialogWrapper {

    private final Project project;
    private final String packageName;

    private JPanel contentPane;
    private JCheckBox automaticallyGenerateFilesFromAssetsCheckBox;
    private JCheckBox useDefaultPackageCheckBox;
    private JTextField packageNameTextField;
    private JLabel descriptionLabel;

    public SettingsDialog(Project project, boolean canBeParent, boolean modal, String packageName) {
        super(project, canBeParent);
        this.project = project;
        this.packageName = packageName;
        init();
        setModal(modal);
        setTitle(ResourceBundle.getBundle("digitaslbi.ext.plugin.labels").getString("title"));
    }

    @Override protected void init() {
        super.init();
        if (packageName != null && !packageName.isEmpty()) {
            packageNameTextField.setText(packageName);
        }
        useDefaultPackageCheckBox.addItemListener(new ItemListener() {
            @Override public void itemStateChanged(ItemEvent itemEvent) {
                if (itemEvent.getStateChange() == ItemEvent.DESELECTED) {
                    packageNameTextField.setEnabled(true);
                    packageNameTextField.requestFocus();
                } else {
                    packageNameTextField.setEnabled(false);
                }
            }
        });
    }

    public SettingsDialog(Project project, String packageName) {
        this(project, true, true, packageName);
    }

    @Nullable @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    @Override protected void doOKAction() {
        super.doOKAction();
    }

    @Override public void doCancelAction() {
        super.doCancelAction();
    }

    //    @Nullable @Override protected String getDimensionServiceKey() {
//        return "#digitaslbi.ext.plugin.SettingsDialog";
//    }


    @Nullable @Override public JComponent getPreferredFocusedComponent() {
        return automaticallyGenerateFilesFromAssetsCheckBox;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                SettingsDialog dialog = new SettingsDialog(null, true, true, "digitaslbi.font.ext");
                dialog.show();
            }
        });
    }
}
