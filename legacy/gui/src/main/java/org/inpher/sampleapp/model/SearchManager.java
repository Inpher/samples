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

import java.util.*;

/**
 * SearchManager is a class to handle the search. It performs the search and notifies
 * all its observers of the response. The search is delayed whenever the keywords are updated.
 * The search is stopped if the keywords is an empty string.
 */
public class SearchManager {

    // 1 sec delay when typing in the search box before doing the search
    private static final long SEARCH_DELAY = 1000;

    private ClientManager client;

    private String searchContent;

    private Timer processSearchDelay;

    private List<SearchResultObserver> observers = new ArrayList<>();

    public SearchManager(ClientManager client) {
        this.client = client;
    }

    /**
     * addSearchObserver adds a new observer to be notified on a search result.
     *
     * @param o search result observer to add to the list of observers of the SearchManager.
     */
    public void addSearchObserver(SearchResultObserver o) {
        observers.add(o);
    }

    /**
     * setSearchContent sets the keywords to search. This will start a timer which once timeouts will
     * notify the observers of the result. If a timer is already set it will cancel it and restart
     * the timer. If the content is empty it will stop the running timer and notify the observers
     * that the SearchManager has stopped its search.
     *
     * @param content keywords to search.
     */
    public void setSearchContent(String content) {
        searchContent = content;

        updateTimer();
    }

    private void updateTimer() {
        if (processSearchDelay != null) {
            processSearchDelay.cancel();
            processSearchDelay.purge();
            processSearchDelay = null;
        }
        if (!searchContent.equals("")) {
            doDelayedSearch(SEARCH_DELAY);
        } else {
            notifyStopSearch();
        }
    }

    private void notifyStopSearch() {
        for (SearchResultObserver o : observers)
            o.stopSearching();
    }

    private void doDelayedSearch(long delay) {
        processSearchDelay = new Timer(true);
        processSearchDelay.schedule(new TimerTask() {
            @Override
            public void run() {
                processSearch();
            }
        }, delay);
    }

    private void processSearch() {
        DecryptedSearchResponse resp = client.search(getKeywords());
        for (SearchResultObserver o : observers) {
            o.notify(resp);
        }
    }

    private List<String> getKeywords() {
        return Arrays.asList(searchContent.split(" "));
    }
}
