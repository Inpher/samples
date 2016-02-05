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
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.inpher.clientapi.DecryptedSearchResponse;
import org.inpher.sampleapp.model.ClientManager;
import org.inpher.sampleapp.model.SearchManager;
import org.inpher.sampleapp.model.SearchResultObserver;

import java.io.File;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Created by sduc on 2/3/16.
 */
public class MainController implements Initializable, SearchResultObserver {

    private ClientManager clientManager;

    private SearchManager searchManager;

    @FXML
    private AnchorPane fileTreePane;

    @FXML
    private TextField searchField;

    @FXML
    private FileTreeController fileTreeController;

    @FXML
    private FilePreviewController filePreviewController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //fileTreeController.update();
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchManager.setSearchContent(newValue);
        });
    }

    public void initClientManager(ClientManager clientManager) {
        this.clientManager = clientManager;

        this.fileTreeController.setClientManager(clientManager);
        this.filePreviewController.setClientManager(clientManager);

        this.fileTreeController.addObserver(filePreviewController);

        // TODO: move this around
        fileTreeController.update();
    }

    public void initSearchManager(SearchManager searchManager) {
        this.searchManager = searchManager;

        this.searchManager.addSearchObserver(this);
    }

    public void onNewDirectoryAction(ActionEvent actionEvent) {
        String path = fileTreeController.getSelectedPath();
        Optional<String> dirName = DialogController.showTextUnputAndGetResult(
                "New Directory", "untitled directory");
        if (dirName.isPresent()) {
            clientManager.createDirectory(path + dirName.get());
            fileTreeController.update();
        }
    }

    public void onNewFileUploadAction(ActionEvent actionEvent) {
        String path = fileTreeController.getSelectedPath();
        Optional<File> file = DialogController.showFileChooserAndGetAbsPath();
        if (file.isPresent()) {
            Optional<String> name = DialogController.showTextUnputAndGetResult(
                    "File Name", file.get().getName());
            if (name.isPresent()) {
                clientManager.uploadFile(file.get(), path, name.get());
                fileTreeController.update();
            }
        }
    }

    @Override
    public void notify(DecryptedSearchResponse searchResult) {
        System.out.println(searchResult);
    }
}
