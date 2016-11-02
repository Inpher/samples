package org.inpher.search.cli;

import org.inpher.search.SearchClient;

import java.io.IOException;
import java.security.SecureRandom;

/**
 * keygen command handler: optional key size param. Generate the key for the given or default size.
 * Print it on stdout
 */
class KeygenCmdHandler {
    static void handle(String[] args) {
        int keyLen = SearchClient.MIN_KEY_BYTE_SIZE;
        if (args.length > 0) {
            try {
                keyLen = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                showHelp();
                System.exit(1);
            }
        }
        SecureRandom rand = new SecureRandom();
        byte[] key = new byte[keyLen];
        rand.nextBytes(key);

        try {
            System.out.write(key);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void showHelp() {
        System.out.println("usage: keygen [keyLength]");
        System.out.println(
                " keyLength - length in bytes of the key to generate. Minimum and default size is 16");
    }
}
