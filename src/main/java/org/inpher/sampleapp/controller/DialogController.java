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

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

/**
 * Dialog controllers contains all the routines to open a dialog window.
 */
public class DialogController {

    /**
     * showError opens an error dialog.
     *
     * @param title       title of the window.
     * @param headerText  header text to show in the dialog.
     * @param contentText content of the dialog.
     */
    public static void showError(String title, String headerText, String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(contentText);

        alert.showAndWait();
    }

    /**
     * showTextInputAndGetResult asks the user for a text input in a dialog window and returns
     * the input as an optional string.
     *
     * @param header  header to show in the dialog.
     * @param content content to show in the dialog.
     * @return an optional string. The option will contain the entered input if ok was pressed;
     * the option will not have a value if cancel was pressed.
     */
    public static Optional<String> showTextInputAndGetResult(String header, String content) {
        TextInputDialog dialog = new TextInputDialog(content);
        dialog.setHeaderText(header);
        return dialog.showAndWait();
    }

    /**
     * showFileChooserAndGetAbsPath asks the user for a file to open in a dialog window and
     * returns the chosen file as an option.
     *
     * @return an optional file. The option will contain the selected file if open was selected;
     * the option will not have a value if cancel was pressed.
     */
    public static Optional<File> showFileChooserAndGetAbsPath() {
        FileChooser fileChooser = new FileChooser();
        File selected = fileChooser.showOpenDialog(new Stage());
        return Optional.ofNullable(selected);
    }

    /**
     * showFileSaverAndGetAbsPath asks the user for a file to create in a dialog window and
     * returns the chose file as an option.
     *
     * @return an optional file. The option will contain the selected file if save was selected;
     * the option will not have a value otherwise.
     */
    public static Optional<File> showFileSaverAndGetAbsPath() {
        FileChooser fileChooser = new FileChooser();
        File saved = fileChooser.showSaveDialog(new Stage());
        return Optional.ofNullable(saved);
    }

    public static boolean confirmPassword(String passwordToConfirm) {
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Confirm password");
        dialog.setHeaderText("Re-enter your password");
        ButtonType confirmButtonType = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        PasswordField passwordField = new PasswordField();
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType);
        dialog.getDialogPane().setContent(passwordField);
        dialog.getDialogPane().setPadding(new Insets(15, 15, 15, 15));
        Platform.runLater(() -> passwordField.requestFocus());
        dialog.setResultConverter(dialogButton -> {
            return passwordToConfirm.equals(passwordField.getText());
        });
        return dialog.showAndWait().get();
    }

}
