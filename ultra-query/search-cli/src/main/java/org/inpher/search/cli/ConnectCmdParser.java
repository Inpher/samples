package org.inpher.search.cli;

import org.apache.commons.cli.*;

/**
 * Parse the connect command and return the paths to the different required files.
 */
class ConnectCmdParser {

    private static final String ES_SETTINGS_OPT = "settings";
    private static final String KEY_OPT = "key";
    private static final String SCHEMA_OPT = "schema";


    static ConnectCmd parse(String[] args) throws HelpingParseException {
        CommandLineParser parser = new DefaultParser();
        Options options = new Options();
        options.addOption(Option.builder(ES_SETTINGS_OPT)
                .argName( "file" )
                .hasArg(true)
                .desc("path to the elastic search settings")
                .required(true)
                .build());
        options.addOption(Option.builder(KEY_OPT)
                .argName( "file" )
                .hasArg(true)
                .desc("path to the master key")
                .required(true)
                .build());
        options.addOption(Option.builder("schema")
                .argName( "file" )
                .hasArg(true)
                .desc("path to the query module schema")
                .required(true)
                .build());

        try {
            CommandLine line = parser.parse(options, args);
            String esPath = line.getOptionValue(ES_SETTINGS_OPT);
            String keyPath = line.getOptionValue(KEY_OPT);
            String schemaPath = line.getOptionValue(SCHEMA_OPT);
            return new ConnectCmd(esPath, keyPath, schemaPath);
        } catch (ParseException e) {
            throw new HelpingParseException(e, "connect", options);
        }
    }

}
