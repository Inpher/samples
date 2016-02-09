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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.inpher.clientapi.DecryptedSearchResponse;
import org.inpher.clientapi.RankedSearchResult;
import org.inpher.sampleapp.model.ClientManager;
import org.inpher.sampleapp.model.SearchManager;
import org.inpher.sampleapp.model.SearchResultObserver;
import org.inpher.sampleapp.view.SearchResultView;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * MainController is the main controller of the application. It controls the main window
 * of the application. The main window consists of:
 * - a search box to search in all the remote documents.
 * - a file tree view to display the remote file system structure in a tree.
 * - a file preview view to show a preview of a filesystem element.
 */
public class MainController implements Initializable, SearchResultObserver {

    private ClientManager clientManager;

    private SearchManager searchManager;

    private SearchResultView srv;
    private boolean isSearching = false;
    private Node fileTreeNode;

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
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            searchManager.setSearchContent(newValue);
        });

        srv = new SearchResultView();
        fileTreeNode = fileTreePane.getChildren().get(0);
    }

    /**
     * initClientManager sets the client manager to use in the controller.
     * The client manager is the client that communicates with the remote server.
     *
     * @param clientManager
     */
    public void initClientManager(ClientManager clientManager) {
        this.clientManager = clientManager;

        this.fileTreeController.setClientManager(clientManager);
        this.filePreviewController.setClientManager(clientManager);

        this.fileTreeController.addObserver(filePreviewController);

        // TODO: change this to avoid the dot dot
        this.srv.getController().addSelectedFileObserver(filePreviewController);

        fileTreeController.update();
    }

    /**
     * initSearchManager sets the searchManager to use in the controller.
     * The search manager is used to handle the search.
     *
     * @param searchManager
     */
    public void initSearchManager(SearchManager searchManager) {
        this.searchManager = searchManager;

        this.searchManager.addSearchObserver(this);
    }

    public void onNewDirectoryAction(ActionEvent actionEvent) {
        String path = fileTreeController.getPathToClosestDirectoryToSelection();//getSelectedPath();
        Optional<String> dirName = DialogController.showTextInputAndGetResult(
                "New Directory", "untitled directory");
        if (dirName.isPresent()) {
            clientManager.createDirectory(path, dirName.get());
            fileTreeController.update();
        }
    }

    public void onNewFileUploadAction(ActionEvent actionEvent) {
        String path = fileTreeController.getPathToClosestDirectoryToSelection();//getSelectedPath();
        Optional<File> file = DialogController.showFileChooserAndGetAbsPath();
        if (file.isPresent()) {
            Optional<String> name = DialogController.showTextInputAndGetResult(
                    "File Name", file.get().getName());
            if (name.isPresent()) {
                clientManager.uploadFile(file.get(), path, name.get());
                fileTreeController.update();
            }
        }
    }

    @Override
    public void notify(DecryptedSearchResponse searchResult) {
        showSearchResult(searchResult.getDocumentIds());
    }

    @Override
    public void stopSearching() {
        showFileTreeView();
    }

    private void showSearchResult(List<RankedSearchResult> documentIds) {
        // we cannot run this in that thread. It needs to be in the FX main application thread.
        Platform.runLater(() -> {
            srv.getController().updateSearchResult(documentIds);

            Node node = srv.getNode();
            AnchorPane.setTopAnchor(node, 0.0);
            AnchorPane.setLeftAnchor(node, 0.0);
            AnchorPane.setBottomAnchor(node, 0.0);
            AnchorPane.setRightAnchor(node, 0.0);

            if (!isSearching) {
                isSearching = true;
                fileTreePane.getChildren().setAll(node);
            }
        });
    }

    private void showFileTreeView() {
        Platform.runLater(() -> {
            if (isSearching) {
                isSearching = false;
                fileTreePane.getChildren().setAll(fileTreeNode);
            }
        });
    }

}
