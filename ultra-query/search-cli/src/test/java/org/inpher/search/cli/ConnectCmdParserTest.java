package org.inpher.search.cli;

import org.apache.commons.cli.ParseException;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class ConnectCmdParserTest {

    private static final String SETTINGS_OPT = "-settings";
    private static final String KEY_OPT = "-key";
    private static final String SCHEMA_OPT = "-schema";

    @Test
    public void testParseOnValid() throws Exception {
        String pathToKey = "/path/to/key";
        String pathToSchema = "/path/to/schema";
        String pathToESSettings = "/path/to/esSettings";
        String[] args =
                {KEY_OPT, pathToKey, SETTINGS_OPT, pathToESSettings, SCHEMA_OPT, pathToSchema};
        ConnectCmd cmd = ConnectCmdParser.parse(args);

        assertThat(cmd.getElasticSettingsPath(), is(pathToESSettings));
        assertThat(cmd.getKeyPath(), is(pathToKey));
        assertThat(cmd.getSchemaPath(), is(pathToSchema));
    }

    @Test(expected = ParseException.class)
    public void testParseMissingKey_ParseException() throws Exception {
        ConnectCmdParser.parse(new String[] {SETTINGS_OPT, "bla", SCHEMA_OPT, "blu"});
    }

    @Test(expected = ParseException.class)
    public void testParseMissingSchema_ParseException() throws Exception {
        ConnectCmdParser.parse(new String[] {SETTINGS_OPT, "bla", KEY_OPT, "bli"});
    }

    @Test(expected = ParseException.class)
    public void testParseMissingSettings_ParseException() throws Exception {
        ConnectCmdParser.parse(new String[] {KEY_OPT, "ble", SCHEMA_OPT, "bly"});
    }

}
