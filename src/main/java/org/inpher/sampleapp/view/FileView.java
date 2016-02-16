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

package org.inpher.sampleapp.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.inpher.sampleapp.controller.FileViewerController;

import java.io.File;
import java.io.IOException;

/**
 * FileView shows the content of a file in a window.
 *
 * @Deprecated this class was replaced by the Desktop.getDesktop().open() which
 * uses the default OS file opener.
 */
public class FileView {

    private FileViewerController controller;
    private Stage stage;

    public FileView(File file) {
        try {
            init(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init(File file) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/file_viewer.fxml"));

        Scene scene = new Scene(loader.load());
        controller = loader.<FileViewerController>getController();

        stage = new Stage();
        stage.setScene(scene);

        controller.viewFile(file, stage);
    }

    public void show() {
        stage.show();
    }

}
