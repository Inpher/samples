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
import javafx.scene.Node;
import javafx.scene.Parent;
import org.inpher.sampleapp.controller.SearchResultController;

import java.io.IOException;

/**
 * SearchResultView is a view to show the search result in a table
 */
public class SearchResultView {

    private SearchResultController controller;

    public SearchResultView() {
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/search_result.fxml"));

        Parent root = loader.load();
        controller = loader.<SearchResultController>getController();
    }

    /**
     * getController returns the controller of the view.
     *
     * @return the controller of the view.
     */
    public SearchResultController getController() {
        return controller;
    }

    /**
     * getNode return the node representation of the view
     *
     * @return the Node representation of the view.
     */
    public Node getNode() {
        return controller.getSearchResultTableView();
    }

}
