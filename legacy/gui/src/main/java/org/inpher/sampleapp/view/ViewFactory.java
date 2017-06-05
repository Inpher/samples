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

import javafx.stage.Stage;
import org.inpher.clientapi.exceptions.InpherException;
import org.inpher.sampleapp.model.ClientManager;
import org.inpher.sampleapp.model.SearchManager;

/**
 * ViewFactory is used to create the views.
 */
public class ViewFactory {

    private final Stage stage;

    public ViewFactory(Stage stage) {
        this.stage = stage;
    }

    public LoginView getLoginView() throws InpherException {
        return new LoginView(stage, ClientManager.getInstance());
    }

    public MainView getMainView() throws InpherException {
        SearchManager searchManager = new SearchManager(ClientManager.getInstance());
        return new MainView(stage, ClientManager.getInstance(), searchManager);
    }
}
