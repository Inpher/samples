package org.inpher.search.cli;

import org.inpher.search.SearchClient;
import org.inpher.search.SearchResponse;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Search query handler
 */
class SelectCmdHandler {

    private final SearchClient client;

    SelectCmdHandler(SearchClient client) {
        this.client = client;
    }

    void handle(String cmd) {
        Pattern regex = Pattern.compile("\\s(WHERE|where)\\s");
        String[] splitCmd = regex.split(cmd, 2);

        String filter = null;
        if (splitCmd.length > 1) {
            filter = splitCmd[1];
        }
        List<String> selects = parseFieldSelect(splitCmd[0]);

        SearchResponse response = client.query(selects, filter, 1, 50);
        showResponse(response);
    }

    private void showResponse(SearchResponse response) {
        for (int i = 0; i < response.getRows().size(); i++) {
            System.out.println("" + (i + 1) + ". " + response.getRows().get(i).getData());
        }
    }

    private void showHelp() {
        System.out.println("SELECT \"field_1\",\"field_2\",...,\"field_k\" [WHERE condition]");
    }

    private List<String> parseFieldSelect(String fieldSelectPart) {
        try {
            return Arrays.asList(SelectFieldParser.parse(fieldSelectPart));
        } catch (ParseException e) {
            System.out.println("Invalid select command:");
            showHelp();
            throw new RuntimeException(e);
        }
    }
}
