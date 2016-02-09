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

import com.google.common.collect.Lists;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import org.inpher.clientapi.RankedSearchResult;

import java.util.List;

/**
 * SearchResultController is the controller for the search result.
 * The search result is shown as a table view.
 */
public class SearchResultController {

    @FXML
    private TableColumn<RankedSearchResultTableItem, String> fileColumn;

    @FXML
    private TableView<RankedSearchResultTableItem> searchResultTableView;

    private ObservableList<RankedSearchResultTableItem> tableEntries = FXCollections.observableArrayList();

    public void initialize() {
        searchResultTableView.setItems(tableEntries);
        setupColumns();
        setupSelectionListener();
    }

    private void setupColumns() {
        fileColumn.setCellValueFactory(
                new PropertyValueFactory<>(RankedSearchResultTableItem.FILE_NAME_PROP));

        /*
        TableColumn<RankedSearchResultTableItem, Double> scores = new TableColumn<>("Score");
        scores.setCellValueFactory(new PropertyValueFactory<>("score"));
        searchResultTableView.getColumns().setAll(fileColumn, scores);
        */
    }

    private void setupSelectionListener() {
        searchResultTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    System.out.println("selected " +
                            ((newValue != null) ? newValue.getFileName() : "null"));
                    // TODO change file preview for the search in file.
                });
    }

    public TableView getSearchResultTableView() {
        return searchResultTableView;
    }

    /**
     * updateSearchResult updates the content of the search result view with the
     * provided new search result.
     *
     * @param resultList new search result as a list of RankedSearchResult
     */
    public void updateSearchResult(List<RankedSearchResult> resultList) {
        List<RankedSearchResultTableItem> l = Lists.transform(
                resultList,
                o -> new RankedSearchResultTableItem(o.getScore(), o.getDocId()));
        tableEntries.setAll(l);
    }

    /**
     * RankedSearchResultTableItem is a class that wraps RankedSearchResult such that it
     * can be used to show it in a TableView
     */
    public class RankedSearchResultTableItem {

        public static final String FILE_NAME_PROP = "fileName";
        public static final String SCORE_PROP = "score";

        private DoubleProperty score;
        private StringProperty fileName;

        RankedSearchResultTableItem(double score, String fileName) {
            setFileName(fileName);
            setScore(score);
        }

        public String getFileName() {
            return fileNameProperty().get();
        }

        public void setFileName(String fileName) {
            fileNameProperty().set(fileName);
        }

        public StringProperty fileNameProperty() {
            if (fileName == null)
                fileName = new SimpleStringProperty(this, FILE_NAME_PROP);
            return fileName;
        }

        public double getScore() {
            return scoreProperty().get();
        }

        public void setScore(double score) {
            scoreProperty().set(score);
        }

        public DoubleProperty scoreProperty() {
            if (score == null)
                score = new SimpleDoubleProperty(this, SCORE_PROP);
            return score;
        }

    }

}
