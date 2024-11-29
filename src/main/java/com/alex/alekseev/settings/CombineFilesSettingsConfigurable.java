package com.alex.alekseev.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CombineFilesSettingsConfigurable implements Configurable {
    private JPanel mainPanel;
    private JTextField excludedExtensionsField;
    private JTextField excludedDirectoriesField;
    private JTextField excludedFileNamesField; // New field for excluded file names

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Copy Files for LLM Settings";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        JLabel extensionsLabel = new JLabel("Excluded File Extensions (comma-separated):");
        excludedExtensionsField = new JTextField(30);

        JLabel directoriesLabel = new JLabel("Excluded Directories (comma-separated):");
        excludedDirectoriesField = new JTextField(30);

        JLabel fileNamesLabel = new JLabel("Excluded File Names (comma-separated):"); // New label
        excludedFileNamesField = new JTextField(30); // New text field

        mainPanel.add(extensionsLabel);
        mainPanel.add(excludedExtensionsField);
        mainPanel.add(Box.createVerticalStrut(10)); // Space between fields
        mainPanel.add(directoriesLabel);
        mainPanel.add(excludedDirectoriesField);
        mainPanel.add(Box.createVerticalStrut(10)); // Space between fields
        mainPanel.add(fileNamesLabel);
        mainPanel.add(excludedFileNamesField);

        return mainPanel;
    }

    @Override
    public boolean isModified() {
        CombineFilesSettingsState settingsState = CombineFilesSettingsState.getInstance();
        if (settingsState == null) {
            return false;
        }

        boolean modified = false;
        if (!excludedExtensionsField.getText().equals(settingsState.getExcludedExtensions())) {
            modified = true;
        }
        if (!excludedDirectoriesField.getText().equals(settingsState.getExcludedDirectories())) {
            modified = true;
        }
        if (!excludedFileNamesField.getText().equals(settingsState.getExcludedFileNames())) { // Check for changes
            modified = true;
        }
        return modified;
    }

    @Override
    public void apply() {
        CombineFilesSettingsState settingsState = CombineFilesSettingsState.getInstance();
        if (settingsState != null) {
            settingsState.setExcludedExtensions(excludedExtensionsField.getText());
            settingsState.setExcludedDirectories(excludedDirectoriesField.getText());
            settingsState.setExcludedFileNames(excludedFileNamesField.getText()); // Save excluded file names
        }
    }

    @Override
    public void reset() {
        CombineFilesSettingsState settingsState = CombineFilesSettingsState.getInstance();
        if (settingsState != null) {
            excludedExtensionsField.setText(settingsState.getExcludedExtensions());
            excludedDirectoriesField.setText(settingsState.getExcludedDirectories());
            excludedFileNamesField.setText(settingsState.getExcludedFileNames()); // Reset excluded file names
        }
    }

    @Override
    public void disposeUIResources() {
        mainPanel = null;
        excludedExtensionsField = null;
        excludedDirectoriesField = null;
        excludedFileNamesField = null; // Dispose of the new field
    }
}
