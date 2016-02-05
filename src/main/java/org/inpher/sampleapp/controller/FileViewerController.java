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

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

/**
 * Created by sduc on 2/4/16.
 */
public class FileViewerController {

    @FXML
    private VBox documentViewer;

    @FXML
    private TextFlow documentTextFlow;

    private File file;
    private Stage stage;

    public void viewFile(File file, Stage onStage) throws IOException {
        this.file = file;
        this.stage = onStage;

        Files.lines(file.toPath()).forEach(
                string -> {
                    Text t = new Text(string + "\n");
                    documentTextFlow.getChildren().add(t);
                }
        );
    }

    @FXML
    private void onDownloadAction(ActionEvent actionEvent) {
        Optional<File> path = DialogController.showFileSaverAndGetAbsPath();
        if (path.isPresent()) {
            try {
                Files.copy(file.toPath(), path.get().toPath());
            } catch (IOException e) {
                DialogController.showError(
                        "Download error",
                        "Download Error",
                        "Failed to download file");
            }
        }
        stage.close();
    }
}
