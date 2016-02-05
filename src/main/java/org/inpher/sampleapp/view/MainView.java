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
import org.inpher.sampleapp.controller.MainController;
import org.inpher.sampleapp.model.ClientManager;
import org.inpher.sampleapp.model.SearchManager;

import java.io.IOException;

/**
 * Created by sduc on 2/3/16.
 */
public class MainView {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private Stage stage;

    public MainView(
            Stage stage,
            ClientManager clientManager,
            SearchManager searchManager) {
        this.stage = stage;
        try {
            init(clientManager, searchManager);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init(ClientManager clientManager, SearchManager searchManager) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_layout.fxml"));
        Scene scene = new Scene(loader.load(), WIDTH, HEIGHT);

        MainController lc = loader.<MainController>getController();
        lc.initClientManager(clientManager);
        lc.initSearchManager(searchManager);

        stage.setScene(scene);
    }

    public void show() {
        stage.centerOnScreen();
        stage.show();
    }

}
