package org.inpher.search.cli;

import org.inpher.search.SearchClient;
import org.inpher.search.SearchResponse;
import org.inpher.search.SearchRow;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class SelectCmdHandlerTest {

    private final SearchClient client = mock(SearchClient.class);
    private final SelectCmdHandler handler = new SelectCmdHandler(client);

    @Before
    public void setUp() throws Exception {
        doReturn(new SearchResponse(Collections.<SearchRow>emptyList())).when(client)
                .query(anyList(), anyString(), anyInt(), anyInt());

        doReturn(new SearchResponse(Collections.<SearchRow>emptyList())).when(client)
                .query(anyList(), isNull(String.class), anyInt(), anyInt());
    }

    @Test
    public void testHandleSimpleCmd() throws Exception {
        String cmd = "\"f1\", \"f2\" WHERE chicken: \"toto\" OR \"bla\"";
        List<String> expectedFields = Arrays.asList("f1", "f2");
        String expectedFilter = "chicken: \"toto\" OR \"bla\"";

        runTestCase(cmd, expectedFields, expectedFilter);
    }

    @Test
    public void testHandleNoFilter() throws Exception {
        String cmd = "\"F1\", \"address\"  ";
        List<String> expectedFields = Arrays.asList("F1", "address");
        String expectedFilter = null;

        runTestCase(cmd, expectedFields, expectedFilter);
    }

    @Test
    public void testHandleStar() throws Exception {
        String cmd = "* where bla: \"blu\"";
        List<String> expectedFields = Collections.emptyList();
        String expectedFilter = "bla: \"blu\"";

        runTestCase(cmd, expectedFields, expectedFilter);
    }

    @Test
    public void testHandleWithWhereInField() throws Exception {
        String cmd = "\"where\" , \"WHERE\" , \"f1\" where chicken: \"toto\"";
        List<String> expectedFields = Arrays.asList("where", "WHERE", "f1");
        String expectedFilter = "chicken: \"toto\"";

        runTestCase(cmd, expectedFields, expectedFilter);
    }

    private void runTestCase(String cmd, List<String> expectedFields, String expectedFilter) {
        handler.handle(cmd);
        verify(client).query(eq(expectedFields), eq(expectedFilter), anyInt(), anyInt());
    }
}
