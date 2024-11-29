package com.alex.alekseev;

import com.alex.alekseev.settings.CombineFilesSettingsState;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CombineFilesAction extends AnAction implements ClipboardOwner {

    @Override
    public void actionPerformed(AnActionEvent e) {
        // Get selected files
        VirtualFile[] files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);

        if (files == null || files.length == 0) {
            Messages.showInfoMessage("No files selected", "Information");
            return;
        }

        // Get user-defined exclusions from settings
        CombineFilesSettingsState settings = CombineFilesSettingsState.getInstance();
        List<String> excludedExtensions = settings.getExcludedExtensionsAsList();
        List<String> excludedDirectories = settings.getExcludedDirectoriesAsList();
        List<String> excludedFileNames = settings.getExcludedFileNamesAsList(); // Get excluded file names

        // Find the common base path
        String commonBasePath = findCommonBasePath(files);
        if (commonBasePath == null) {
            Messages.showErrorDialog("Could not determine a common directory for the selected files", "Error");
            return;
        }

        // Use a StringBuilder to collect combined content
        StringBuilder combinedContent = new StringBuilder();

        // Process each selected file/directory
        for (VirtualFile file : files) {
            processFile(file, commonBasePath, excludedExtensions, excludedDirectories, excludedFileNames, combinedContent);
        }

        // Copy combined content to clipboard
        try {
            StringSelection stringSelection = new StringSelection(combinedContent.toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            clipboard.setContents(stringSelection, this);
            // No success message as per your previous request
        } catch (Exception ex) {
            ex.printStackTrace();
            Messages.showErrorDialog("Failed to copy to clipboard: " + ex.getMessage(), "Error");
        }
    }

    // Recursive method to process files and directories
    private void processFile(VirtualFile file, String commonBasePath, List<String> excludedExtensions,
                             List<String> excludedDirectories, List<String> excludedFileNames, StringBuilder combinedContent) {
        if (file.isDirectory()) {
            if (shouldExcludeDirectory(file, excludedDirectories)) {
                // Skip excluded directories
                return;
            }
            // Process children
            for (VirtualFile child : file.getChildren()) {
                processFile(child, commonBasePath, excludedExtensions, excludedDirectories, excludedFileNames, combinedContent);
            }
        } else {
            if (shouldExcludeFile(file, excludedExtensions, excludedFileNames)) {
                // Skip excluded files
                return;
            }

            // Read the file content
            try {
                String content = new String(file.contentsToByteArray(), StandardCharsets.UTF_8);

                // Check if the file is empty or contains only whitespace
                if (content.trim().isEmpty()) {
                    // Skip files that are empty or only contain spaces
                    return;
                }

                String relativePath = getRelativePath(commonBasePath, file.getPath());
                combinedContent.append("=== ").append(relativePath).append(" ===\n");
                combinedContent.append(content).append("\n\n");
            } catch (IOException ex) {
                ex.printStackTrace();
                Messages.showErrorDialog("Failed to read file: " + file.getName(), "Error");
            }
        }
    }

    private boolean shouldExcludeDirectory(VirtualFile directory, List<String> excludedDirectories) {
        String dirName = directory.getName();
        return excludedDirectories.stream().anyMatch(excludedDir -> excludedDir.equalsIgnoreCase(dirName));
    }

    // Updated method to check both extensions and file names
    private boolean shouldExcludeFile(VirtualFile file, List<String> excludedExtensions, List<String> excludedFileNames) {
        String extension = file.getExtension();
        String fileName = file.getName();
        boolean excludeByExtension = extension != null && excludedExtensions.contains(extension.toLowerCase());
        boolean excludeByName = excludedFileNames.contains(fileName);
        return excludeByExtension || excludeByName;
    }

    @Override
    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // No action needed when clipboard ownership is lost
    }

    private String findCommonBasePath(VirtualFile[] files) {
        Path commonPath = Paths.get(files[0].getPath()).getParent();
        for (VirtualFile file : files) {
            Path filePath = Paths.get(file.getPath()).getParent();
            commonPath = commonPath(filePath, commonPath);
            if (commonPath == null) {
                break;
            }
        }
        return commonPath != null ? commonPath.toString() : null;
    }

    private Path commonPath(Path path1, Path path2) {
        if (path1 == null || path2 == null) {
            return null;
        }
        int count = Math.min(path1.getNameCount(), path2.getNameCount());
        Path result = path1.getRoot();
        for (int i = 0; i < count; i++) {
            if (path1.getName(i).equals(path2.getName(i))) {
                result = result == null ? path1.getName(i) : result.resolve(path1.getName(i));
            } else {
                break;
            }
        }
        return result;
    }

    private String getRelativePath(String basePath, String fullPath) {
        Path base = Paths.get(basePath);
        Path full = Paths.get(fullPath);
        return base.relativize(full).toString();
    }
}
