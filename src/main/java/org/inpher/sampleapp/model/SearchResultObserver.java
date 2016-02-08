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

import org.inpher.clientapi.DecryptedSearchResponse;

/**
 * SearchResultObserver is notified by a SearchManager with the search result when
 * the search is completed.
 */
public interface SearchResultObserver {

    /**
     * notify is called by the SearchManger to notify the observer of the search
     * result.
     *
     * @param searchResult result of the search.
     */
    void notify(DecryptedSearchResponse searchResult);

    /**
     * stopSearching is called by the SearchManager to notify the observer that the search
     * has stopped.
     */
    void stopSearching();

}
