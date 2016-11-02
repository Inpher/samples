package org.inpher.search.cli;

import org.elasticsearch.client.transport.TransportClient;
import org.inpher.search.Schema;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

/**
 * TODO: add class Doc
 */
class ConnectCmdHandler {

    static void handle(String[] args) {
        try {
            ConnectCmd cmd = ConnectCmdParser.parse(args);

            byte[] key = readKey(cmd.getKeyPath());
            Schema schema = readSchema(cmd.getSchemaPath());
            TransportClient client =
                    ElasticUtils.getElasticClient(Paths.get(cmd.getElasticSettingsPath()));

            Connector connector = new Connector();
            SearchCmdCli sccli = connector.connect(client, schema, key);

            System.out.println("START connection ... ");

            sccli.runHandler();

            client.close();
        } catch (FileNotFoundException | NoSuchFileException e) {
            System.err.println("File not found : " + e.getMessage());
            System.exit(1);
        } catch (IOException | ElasticConnectException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        } catch (HelpingParseException e) {
            System.err.println(e.getMessage());
            e.printHelp();
            System.exit(1);
        }

        System.out.println("END connection");
        System.exit(0);
    }

    private static Schema readSchema(String pathToSchema) throws IOException {
        return Schema.fromJSON(new FileInputStream(pathToSchema));
    }

    private static byte[] readKey(String pathToKey) throws IOException {
        return Files.readAllBytes(Paths.get(pathToKey));
    }
}
