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
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.inpher.clientapi.Element;
import org.inpher.clientapi.ElementVisitResult;
import org.inpher.clientapi.ElementVisitor;
import org.inpher.sampleapp.model.ClientManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sduc on 2/4/16.
 */
public class FileTreeController {

    @FXML
    private TreeView<String> fileTreeView;

    private ClientManager clientManager;

    private String selectedPath = "root:/";

    private List<SelectedFileObserver> observers = new ArrayList<>();

    private final Image dirIcon =
            new Image(getClass().getResourceAsStream("/icons/folder_small.png"));

    private final Image fileIcon =
            new Image(getClass().getResourceAsStream("/icons/file_small.png"));

    protected void setClientManager(ClientManager cli) {
        this.clientManager = cli;
    }

    public String getSelectedPath() {
        return selectedPath;
    }

    private static String absolutePathTo(TreeItem<String> node) {
        String path = "";
        for (TreeItem<String> it = node;
             it.getValue() != null;
             it = it.getParent()) {
            path = it.getValue() + ((path == "") ? "" : "/" + path);
        }
        return path;
    }

    private void setFileTreeViewSelectionListener() {
        fileTreeView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue == null) return;

                    selectedPath = absolutePathTo(newValue);
                    notifyAllObserver(selectedPath);
                });
    }

    public void initialize() {
        setFileTreeViewSelectionListener();
    }

    private void notifyAllObserver(String selectedPath) {
        for (SelectedFileObserver observer :
                observers) {
            observer.update(selectedPath);
        }
    }

    public void update() {
        ElementVisitor<TreeItem<String>> elementVisitor = new ElementVisitor<TreeItem<String>>() {
            @Override
            public ElementVisitResult visitDocument(Element element, TreeItem<String> treeItem) {
                TreeItem<String> docItem = new TreeItem<>(element.getElementName(), new ImageView(fileIcon));
                treeItem.getChildren().add(docItem);
                return ElementVisitResult.CONTINUE;
            }

            @Override
            public ElementVisitResult postVisitDirectory(Element element, TreeItem<String> treeItem) {
                return ElementVisitResult.CONTINUE;
            }

            @Override
            public ElementVisitResult preVisitDirectory(Element element, TreeItem<String> treeItem, Object[] objects) {
                TreeItem<String> dirItem = new TreeItem<>(element.getElementName(), new ImageView(dirIcon));
                treeItem.getChildren().add(dirItem);
                objects[0] = dirItem;
                return ElementVisitResult.CONTINUE;
            }
        };

        TreeItem<String> root = clientManager.visitFileTree(elementVisitor);
        fileTreeView.setRoot(root);

        notifyAllObserver(selectedPath);
    }

    public void addObserver(SelectedFileObserver observer) {
        observers.add(observer);
    }
}
