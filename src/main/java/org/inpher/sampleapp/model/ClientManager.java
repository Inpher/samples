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
import java.util.List;

/**
 * ClientManager is a Singleton class that implements the main inpher client functionalities
 * of the application.
 */
public class ClientManager {

    private static ClientManager instance;

    private InpherClient inpherClient;

    private ClientManager(InpherClient client) {
        this.inpherClient = client;
    }

    /**
     * Get the singleton instance.
     *
     * @return the unique instance of the class.
     */
    public static ClientManager getInstance() {
        if (instance == null) {
            try {
                synchronized (ClientManager.class) {
                    if (instance == null)
                        instance = new ClientManager(InpherClient.getClient());
                }
            } catch (InpherException e) {
            }
        }
        return instance;
    }

    /**
     * login logs user username with password if correct into the inpher client.
     *
     * @param username Username to login.
     * @param password Password to use to login.
     * @return true if login was successful; false otherwise.
     */
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

    /**
     * Register registers a new user with username and password. Register fails if the username
     * is already used.
     *
     * @param username Username to register.
     * @param password Password to use to login.
     * @return true if register was successful; false otherwise.
     */
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

    /**
     * CreateDirectory creates a new directory dirName on the server at the given location.
     *
     * @param location Location in the remote file system where the directory needs to be created.
     * @param dirName  Name of the directory to create.
     */
    public void createDirectory(String location, String dirName) {
        FrontendPath newDir = FrontendPath.parse(location);
        newDir = newDir.resolve(dirName);
        System.out.println("New Dir:" + newDir);
        inpherClient.makeDirectory(new MakeDirectoryRequest(newDir));
    }

    /**
     * visitFileTree visits the remote file system using the given visitor callback.
     *
     * @param elementVisitor Visitor callback to use when visiting the remote file system.
     * @param <T>            Type of the item tree on the client side.
     * @return The root of the generated tree resulting from visiting the file system.
     */
    public <T> TreeItem<T> visitFileTree(ElementVisitor<TreeItem<T>> elementVisitor) {
        TreeItem<T> root = new TreeItem<>(null);
        inpherClient.visitElementTree(
                new VisitElementTreeRequest(getRootPath(), elementVisitor, root));
        return root;
    }

    /**
     * getRootPath returns the path to the root of the remote file system.
     *
     * @return the root of the filesystem.
     */
    public FrontendPath getRootPath() {
        String root = "root:/";
        return FrontendPath.parse(root);
    }

    /**
     * uploadFile uploads a new file on the remote file system.
     *
     * @param file       File to upload.
     * @param path       remote path where the file will be stored.
     * @param uploadName name of the uploaded file on the remote file system.
     */
    public void uploadFile(File file, String path, String uploadName) {
        FrontendPath fPath = FrontendPath.parse(path);
        fPath = fPath.resolve(uploadName);
        inpherClient.uploadDocument(new UploadDocumentRequest(file, fPath));
    }

    /**
     * getElement gets the metadata of an element on the remote file system.
     *
     * @param newSelectedFilePath path to the file system element.
     * @return the metadata of the element at newSelectedFilePath
     */
    public Element getElement(String newSelectedFilePath) {
        FrontendPath fPath = FrontendPath.parse(newSelectedFilePath);
        return inpherClient.listElement(new ListElementRequest(fPath));
    }

    /**
     * openFile gets the content of a document from the remote file system.
     *
     * @param selectedPath path of the document to open.
     * @return a temp file containing the content of the file.
     * @throws IOException
     */
    public File openFile(String selectedPath) throws IOException {
        FrontendPath fPath = FrontendPath.parse(selectedPath);
        File tmp = File.createTempFile(selectedPath.replace('/', '_'), ".tmp");
        inpherClient.readDocument(new ReadDocumentRequest(fPath, tmp));
        return tmp;
    }

    /**
     * search will perform a search in all the documents on the remote file system that contain
     * the provided keywords.
     *
     * @param keywords Keywords to search.
     * @return a list of documents with scores that contain the keywords.
     */
    public DecryptedSearchResponse search(List<String> keywords) {
        return inpherClient.search(keywords);
    }

    /**
     * isDirectory tests whether a given path is a directory or not.
     *
     * @param selectedPath Path to test if it is a directory.
     * @return true if the element at the given path is a directory; false otherwise.
     */
    public boolean isDirectory(String selectedPath) {
        Element el = inpherClient.listElement(new ListElementRequest(selectedPath));
        return el.getType() == ElementType.DIRECTORY;
    }
}
