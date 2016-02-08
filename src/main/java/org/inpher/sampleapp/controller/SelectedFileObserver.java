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

/**
 * SelectedFileObserver is notified whenever a new element is selected in
 * the file tree view controller by the FileTreeController.
 */
public interface SelectedFileObserver {

    /**
     * update is called by the FileTreeController to update the observer that a new element
     * is selected in the view.
     *
     * @param newSelectedFilePath path of the new selected element.
     */
    void update(String newSelectedFilePath);
}
