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

import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

/**
 * Created by sduc on 2/3/16.
 */
public class DialogController {

    static void showError(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
    }

    static Optional<String> showTextUnputAndGetResult(String header, String content) {
        TextInputDialog dialog = new TextInputDialog(content);
        dialog.setHeaderText(header);
        return dialog.showAndWait();
    }

    static Optional<File> showFileChooserAndGetAbsPath() {
        FileChooser fileChooser = new FileChooser();
        File selected = fileChooser.showOpenDialog(new Stage());
        return Optional.ofNullable(selected);
    }

    static Optional<File> showFileSaverAndGetAbsPath() {
        FileChooser fileChooser = new FileChooser();
        File saved = fileChooser.showSaveDialog(new Stage());
        return Optional.ofNullable(saved);
    }

}
