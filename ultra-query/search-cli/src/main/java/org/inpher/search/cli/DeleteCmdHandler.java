package org.inpher.search.cli;

import com.google.common.base.Joiner;
import org.inpher.search.SearchClient;

/**
 * Handle the delete command.
 */
class DeleteCmdHandler {

    private final SearchClient client;

    DeleteCmdHandler(SearchClient client) {
        this.client = client;
    }

    void handle(String cmd) {
        this.client.deleteFromIndex(cmd, true);
    }

}
