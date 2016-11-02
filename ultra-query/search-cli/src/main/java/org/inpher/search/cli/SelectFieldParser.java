package org.inpher.search.cli;

import java.text.ParseException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse the select field part of the search query.
 * Fields are comma ',' separated.
 */
class SelectFieldParser {

    private static final String[] STAR = {"*"};

    static String[] parse(String fieldSelectPart) throws ParseException {
        fieldSelectPart = fieldSelectPart.trim();
        Pattern regex = Pattern.compile("\\s*,\\s*");
        String[] selectFields = regex.split(fieldSelectPart);
        if (isStar(selectFields))
            return new String[] {};
        removeQuotes(selectFields);
        return selectFields;
    }

    private static boolean isStar(String[] selectFields) {
        return Arrays.equals(selectFields, STAR);
    }

    private static void removeQuotes(String[] split) throws ParseException {
        Pattern regex = Pattern.compile("^\"([a-zA-Z_][a-zA-Z0-9_]*)\"$");
        for (int i = 0; i < split.length; i++) {
            Matcher matcher = regex.matcher(split[i]);
            if (!matcher.matches())
                throw new ParseException(
                        "Invalid list of select fields. Invalid field name " + split[i], i);
            split[i] = matcher.group(1);
        }
    }
}
