/*
 * Copyright 2015 Inpher, Inc.
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

package org.inpher.sampleapp.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.inpher.clientapi.Element;
import org.inpher.clientapi.ElementType;
import org.inpher.sampleapp.model.ClientManager;

import java.awt.*;
import java.io.IOException;

/**
 * FilePreviewController controls the file preview view. The file preview only shows
 * the metadata of the selected item in the file tree view. The controller handles
 * the update when another file is selected in the tree view. It also handles the button
 * to open the file. Open will show the file in the file viewer view.
 */
public class FilePreviewController implements SelectedFileObserver {

    @FXML
    private Label documentName;

    @FXML
    private Label typeLabel;

    @FXML
    private Label pathLabel;

    @FXML
    private Label ownerLabel;

    @FXML
    private Label sizeLabel;

    @FXML
    private Label contentTypeLabel;

    @FXML
    private Button openButton;

    private ClientManager clientManager;

    public void initialize() {
        openButton.setOnAction(event -> {
            try {
                Desktop.getDesktop().open(clientManager.openFile(getSelectedPath()));
            } catch (IOException e) {
                DialogController.showError("Document open error", "IO error", e.getMessage());
            }
        });
    }

    /**
     * setClientManager sets the client manager to use in the controller.
     *
     * @param clientManager
     */
    public void setClientManager(ClientManager clientManager) {
        this.clientManager = clientManager;
    }

    private static String longToHumanReadableSize(long bytes) {
        int unit = 1000;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        char mag = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), mag);
    }

    @Override
    public void update(String newSelectedFilePath) {
        Element document = clientManager.getElement(newSelectedFilePath);

        documentName.setText(document.getElementName());
        sizeLabel.setText(longToHumanReadableSize(document.getSize()));
        typeLabel.setText(document.getType().toString());
        contentTypeLabel.setText(document.getContentType());
        ownerLabel.setText(document.getOwnName());

        pathLabel.setText(newSelectedFilePath);

        openButton.setVisible(true);
        if (document.getType() != ElementType.DOCUMENT) {
            openButton.setVisible(false);
        }
    }

    public String getSelectedPath() {
        return pathLabel.getText();
    }
}
