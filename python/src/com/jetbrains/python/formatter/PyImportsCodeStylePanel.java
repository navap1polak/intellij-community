/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jetbrains.python.formatter;

import com.intellij.application.options.CodeStyleAbstractPanel;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.ui.components.JBCheckBox;
import com.jetbrains.python.PythonFileType;
import com.jetbrains.python.PythonLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * @author Mikhail Golubev
 */
public class PyImportsCodeStylePanel extends CodeStyleAbstractPanel {
  private JBCheckBox mySortImportsAlphabetically;
  private JBCheckBox mySortNamesInFromImports;
  private JBCheckBox myJoinFromImportsWithSameSource;
  private JPanel myRootPanel;

  public PyImportsCodeStylePanel(@NotNull CodeStyleSettings settings) {
    super(PythonLanguage.getInstance(), null, settings);
    addPanelToWatch(myRootPanel);

    mySortImportsAlphabetically.addActionListener(e -> mySortNamesInFromImports.setEnabled(mySortImportsAlphabetically.isSelected()));
  }

  @Override
  protected String getTabTitle() {
    return "Imports";
  }

  @NotNull
  @Override
  protected FileType getFileType() {
    return PythonFileType.INSTANCE;
  }

  @Nullable
  @Override
  protected EditorHighlighter createHighlighter(EditorColorsScheme scheme) {
    return null;
  }

  @Nullable
  @Override
  protected String getPreviewText() {
    return null;
  }

  @Override
  protected int getRightMargin() {
    return 0;
  }

  @Override
  public void apply(CodeStyleSettings settings) throws ConfigurationException {
    final PyCodeStyleSettings pySettings = settings.getCustomSettings(PyCodeStyleSettings.class);

    pySettings.OPTIMIZE_IMPORTS_SORT_ALPHABETICALLY = mySortImportsAlphabetically.isSelected();
    pySettings.OPTIMIZE_IMPORTS_SORT_NAMES_IN_FROM_IMPORTS = mySortNamesInFromImports.isSelected();
    pySettings.OPTIMIZE_IMPORTS_JOIN_FROM_IMPORTS_WITH_SAME_SOURCE = myJoinFromImportsWithSameSource.isSelected();
  }

  @Override
  public boolean isModified(CodeStyleSettings settings) {
    final PyCodeStyleSettings pySettings = settings.getCustomSettings(PyCodeStyleSettings.class);

    return mySortImportsAlphabetically.isSelected() != pySettings.OPTIMIZE_IMPORTS_SORT_ALPHABETICALLY ||
           mySortNamesInFromImports.isSelected() != pySettings.OPTIMIZE_IMPORTS_SORT_NAMES_IN_FROM_IMPORTS ||
           myJoinFromImportsWithSameSource.isSelected() != pySettings.OPTIMIZE_IMPORTS_JOIN_FROM_IMPORTS_WITH_SAME_SOURCE;
  }

  @Nullable
  @Override
  public JComponent getPanel() {
    return myRootPanel;
  }

  @Override
  protected void resetImpl(CodeStyleSettings settings) {
    final PyCodeStyleSettings pySettings = settings.getCustomSettings(PyCodeStyleSettings.class);

    mySortImportsAlphabetically.setSelected(pySettings.OPTIMIZE_IMPORTS_SORT_ALPHABETICALLY);
    mySortNamesInFromImports.setSelected(pySettings.OPTIMIZE_IMPORTS_SORT_NAMES_IN_FROM_IMPORTS);
    mySortNamesInFromImports.setEnabled(mySortImportsAlphabetically.isSelected());
    myJoinFromImportsWithSameSource.setSelected(pySettings.OPTIMIZE_IMPORTS_JOIN_FROM_IMPORTS_WITH_SAME_SOURCE);
  }
}
