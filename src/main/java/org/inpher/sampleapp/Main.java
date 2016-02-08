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

package org.inpher.sampleapp;

import javafx.application.Application;
import javafx.stage.Stage;
import org.inpher.clientapi.InpherClient;
import org.inpher.sampleapp.model.UserManager;
import org.inpher.sampleapp.view.LoginView;
import org.inpher.sampleapp.view.ViewFactory;

/**
 * This is the main class to start the application.
 */
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("INPHER");

        ViewFactory factory = new ViewFactory(primaryStage);

        LoginView login = factory.getLoginView();
        login.show();
    }
}
