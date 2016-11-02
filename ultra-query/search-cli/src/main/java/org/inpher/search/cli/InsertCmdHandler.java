package org.inpher.search.cli;

import org.inpher.search.Document;
import org.inpher.search.SearchClient;

import java.util.Scanner;

/**
 * Insert command handler: insert a new document on elastic
 */
class InsertCmdHandler {

    private final SearchClient client;

    private static final String FINISH_LINE = "";

    InsertCmdHandler(SearchClient client) {
        this.client = client;
    }

    void handle() {
        System.out.println("Enter you document on multiple lines."
                + " To send the query you need to finished with an empty line:");
        String readLine;
        Scanner stdin = new Scanner(System.in);
        String json = "";
        while (true) {
            readLine = stdin.nextLine();
            if (readLine.equals(FINISH_LINE))
                break;

            json += readLine;
        }

        client.insert(Document.fromJSONString(json), true);
    }
}
