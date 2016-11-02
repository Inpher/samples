package org.inpher.search.cli;

import java.util.Arrays;

/**
 * Main entry point of the client
 */
public class StartCli {
    private static final String CONNECT_CMD = "connect";
    private static final String KEYGEN_CMD = "keygen";

    public static void main(String[] args) {
        if (args.length < 1) {
            showHelp();
            System.exit(1);
        }

        String cmd = args[0];

        switch(cmd) {
            case CONNECT_CMD:
                ConnectCmdHandler.handle(Arrays.copyOfRange(args, 1, args.length));
                break;
            case KEYGEN_CMD:
                KeygenCmdHandler.handle(Arrays.copyOfRange(args, 1, args.length));
                break;
            default:
                showHelp();
                System.exit(1);
        }
        System.exit(0);
    }

    private static void showHelp() {
        System.out.println("Available commands:");
        System.out.println(CONNECT_CMD + "\t- connect to the backend");
        System.out.println(KEYGEN_CMD + "\t- generate a new key");
    }

}
