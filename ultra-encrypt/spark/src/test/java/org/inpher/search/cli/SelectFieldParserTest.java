package org.inpher.search.cli;

import org.junit.Test;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class SelectFieldParserTest {

    @Test
    public void testParseValid() throws Exception {
        Map<String, String[]> testCases = new HashMap<>();
        testCases.put("    \"f1\",    \"f2\"  , \"f3\",\"f4\"   ",
                new String[] {"f1", "f2", "f3", "f4"});
        testCases.put("\"f1\"", new String[] {"f1"});
        testCases.put("\"f2\" , ", new String[] {"f2"});
        testCases.put("\"_f1\", \"f_2\" ", new String[] {"_f1", "f_2"});
        testCases.put("*", new String[] {});

        for (String s : testCases.keySet()) {
            assertThat(SelectFieldParser.parse(s), is(testCases.get(s)));
        }
    }

    @Test
    public void testParseInvalid() throws Exception {
        String[] testCases = {
                "f1, \"f2\"",
                "\"f1\" \"f2\"",
                "\"1f\"",
                "\"*\""
        };

        for (String s : testCases) {
            //noinspection EmptyCatchBlock
            try {
                SelectFieldParser.parse(s);
                fail("Test case " + s + " failed.");
            } catch (ParseException e) {
            }
        }
    }
}
