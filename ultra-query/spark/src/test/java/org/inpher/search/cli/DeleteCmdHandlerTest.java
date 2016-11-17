package org.inpher.search.cli;

import org.inpher.search.SearchClient;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DeleteCmdHandlerTest {

    private SearchClient searchClient = mock(SearchClient.class);
    private DeleteCmdHandler handler = new DeleteCmdHandler(searchClient);

    @Test
    public void testHandle() throws Exception {
        String cmd = "doc_id : \"ta ta\"";
        handler.handle(cmd);

        verify(searchClient, atLeastOnce()).deleteFromIndex(eq(cmd), eq(true));
    }

}
