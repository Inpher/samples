package org.inpher.search.cli;

import com.google.common.base.Joiner;
import org.inpher.search.SearchClient;

import java.util.Arrays;
import java.util.Scanner;

class SearchCmdCli {

    private static final String HELP = "help";
    private static final String INSERT = "insert";
    private static final String INSERT_CAP = "INSERT";
    private static final String SELECT = "select";
    private static final String SELECT_CAP = "SELECT";
    private static final String DELETE = "delete";
    private static final String DELETE_CAP = "DELETE";
    private static final String QUIT = "quit";

    private final SearchClient client;

    SearchCmdCli(SearchClient client) {
        this.client = client;
    }

    void runHandler() {
        Scanner stdin = new Scanner(System.in);
        System.out.println("Enter your command. Type help to see all available commands...");
        boolean isContinue = true;
        while(isContinue) {
            System.out.print("> ");
            String[] args = stdin.nextLine().trim().split(" ");
            if (args.length < 1) {
                showHelp();
            }

            switch(args[0]) {
                case HELP:
                    showHelp();
                    break;
                case INSERT:
                case INSERT_CAP:
                    handleInsert();
                    break;
                case SELECT:
                case SELECT_CAP:
                    handleSelect(Arrays.copyOfRange(args, 1, args.length));
                    break;
                case DELETE:
                case DELETE_CAP:
                    handleDelete(Arrays.copyOfRange(args, 1, args.length));
                    break;
                case QUIT:
                    isContinue = false;
                    break;
                default:
                    System.out.println("Invalid command : ");
                    showHelp();
            }
        }
    }

    private void handleInsert() {
        try {
            new InsertCmdHandler(this.client).handle();
        } catch(Exception e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    private void handleSelect(String[] args) {
        try {
            String cmd = Joiner.on(" ").join(args).trim();
            new SelectCmdHandler(this.client).handle(cmd);
        } catch(Exception e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    private void handleDelete(String[] args) {
        try {
            String cmd = Joiner.on(" ").join(args);
            new DeleteCmdHandler(this.client).handle(cmd);
        } catch(Exception e) {
            System.err.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private void showHelp() {
        System.out.println("Available commands are: ");
        System.out.println(" " + HELP + "\t - Show this help message");
        System.out.println(" " + INSERT + "\t - send an insert query to elastic");
        System.out.println(" " + SELECT + "\t - send a search query to elastic");
        System.out.println(" " + DELETE + "\t - send a delete query to elastic");
        System.out.println(" " + QUIT + "\t - exit this REPL");
    }

}
