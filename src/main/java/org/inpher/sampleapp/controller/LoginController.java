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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.inpher.sampleapp.model.ClientManager;
import org.inpher.sampleapp.model.UserManager;
import org.inpher.sampleapp.view.LoginView;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * LoginController is the controller for the login view.
 * The login controller handles login and register buttons and the username/password fields.
 */
public class LoginController implements Initializable {

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    private ClientManager clientManager;

    private LoginView view;

    public void handleLoginButton(ActionEvent actionEvent) {
        String passStr = password.getText();
        String userStr = username.getText();

        if (clientManager.login(userStr, passStr)) {
            System.out.println("LOGIN");
            view.gotoMainView();
        } else {
            DialogController.showError("Login Error",
                    "Login Failed",
                    "Username or password are invalid!");
        }
    }

    public void handleRegisterButton(ActionEvent actionEvent) {
        String passStr = password.getText();
        String userStr = username.getText();

        if (clientManager.register(userStr, passStr)) {
            System.out.println("REGISTERED");
            // TODO: maybe need to login here
            view.gotoMainView();
        } else {
            DialogController.showError("Register Error",
                    "Register Failed",
                    "User already exists!");
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    /**
     * initClientManager sets the client manager of the controller.
     * Client manager handles all the calls to the Inpher backend.
     *
     * @param cm
     */
    public void initClientManager(ClientManager cm) {
        clientManager = cm;
    }

    public void initView(LoginView view) {
        this.view = view;
    }
}
