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
import org.inpher.clientapi.exceptions.InpherException;
import org.inpher.sampleapp.controller.LoginController;
import org.inpher.sampleapp.model.ClientManager;
import org.inpher.sampleapp.model.UserManager;

import java.io.IOException;

/**
 * Created by sduc on 2/3/16.
 */
public class LoginView {

    private static final int HEIGHT = 300;
    private static final int WIDTH = 160;

    private Stage stage;

    public LoginView(Stage stage, ClientManager clientManager) {
        this.stage = stage;
        try {
            init(clientManager);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init(ClientManager clientManager) throws IOException {
        FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
        Scene scene = new Scene(loginLoader.load(), HEIGHT, WIDTH);

        LoginController lc = loginLoader.<LoginController>getController();
        lc.initClientManager(clientManager);
        lc.initView(this);

        stage.setScene(scene);
    }

    public void show() {
        stage.show();
    }

    public void gotoMainView() {
        ViewFactory factory = new ViewFactory(this.stage);
        MainView main = null;
        try {
            main = factory.getMainView();
            main.show();
        } catch (InpherException e) {}
    }

}
