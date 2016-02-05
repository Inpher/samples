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

package org.inpher.sampleapp.model;

import javafx.scene.control.TreeItem;
import org.inpher.clientapi.*;
import org.inpher.clientapi.exceptions.InpherException;
import org.inpher.clientapi.exceptions.InvalidCredentialsException;
import org.inpher.clientapi.exceptions.NonRegisteredUserException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sduc on 2/3/16.
 */
public class ClientManager {

    private static ClientManager instance;

    private InpherClient inpherClient;

    private ClientManager(InpherClient client) {
        this.inpherClient = client;
    }

    public static ClientManager getInstance() {
        if (instance == null) {
            try {
                synchronized (ClientManager.class) {
                    if (instance == null)
                        instance = new ClientManager(InpherClient.getClient());
                }
            } catch (InpherException e) {}
        }
        return instance;
    }

    public boolean login(String username, String password) {
        if (inpherClient == null)
            return false;
        if (!isUserPasswordValid(username, password))
            return false;

        InpherUser user = new InpherUser(username, password);
        try {
            inpherClient.loginUser(user);
        } catch (InvalidCredentialsException e) {
            return false;
        } catch (NonRegisteredUserException e) {
            return false;
        }
        return true;
    }

    public boolean register(String username, String password) {
        if (inpherClient == null)
            return false;
        if (!isUserPasswordValid(username, password))
            return false;

        InpherUser user = new InpherUser(username, password);
        try {
            inpherClient.registerUser(user);
        } catch (InpherException e) {
            return false;
        }
        return true;
    }

    private boolean isUserPasswordValid(String user, String password) {
        if (user == null || password == null)
            return false;
        if (user == "" || password == "")
            return false;
        return true;
    }

    public void createDirectory(String path) {
        FrontendPath newDir = FrontendPath.parse(path);
        System.out.println("New Dir:" + newDir);
        inpherClient.makeDirectory(new MakeDirectoryRequest(newDir));
    }

    public <T> TreeItem<T> visitFileTree(ElementVisitor<TreeItem<T>> elementVisitor) {
        TreeItem<T> root = new TreeItem<>(null);
        inpherClient.visitElementTree(
                new VisitElementTreeRequest(getRootPath(), elementVisitor, root));
        return root;
    }

    public FrontendPath getRootPath() {
        String root = "root:/";
        return FrontendPath.parse(root);
    }

    public void uploadFile(File file, String path, String uploadName) {
        FrontendPath fPath = FrontendPath.parse(path);
        fPath = fPath.resolve(uploadName);
        inpherClient.uploadDocument(new UploadDocumentRequest(file, fPath));
    }

    public Element getElement(String newSelectedFilePath) {
        FrontendPath fPath = FrontendPath.parse(newSelectedFilePath);
        return inpherClient.listElement(new ListElementRequest(fPath));
    }

    public File openFile(String selectedPath) throws IOException {
        FrontendPath fPath = FrontendPath.parse(selectedPath);
        File tmp = File.createTempFile(selectedPath.replace('/','_'), ".tmp");
        inpherClient.readDocument(new ReadDocumentRequest(fPath, tmp));
        return tmp;
    }

    public DecryptedSearchResponse search(List<String> keywords) {
        return inpherClient.search(keywords);
    }
}
