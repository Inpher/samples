package org.inpher.search.cli;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * TODO: add class Doc
 */
class HelpingParseException extends Exception {

    private final String name;
    private final Options options;

    HelpingParseException(ParseException e, String name, Options options) {
        super(e);
        this.name = name;
        this.options = options;
    }

    public void printHelp() {
        new HelpFormatter().printHelp(name, options);
    }

}
