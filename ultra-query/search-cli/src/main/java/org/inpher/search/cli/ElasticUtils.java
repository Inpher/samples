package org.inpher.search.cli;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.plugin.deletebyquery.DeleteByQueryPlugin;

import java.net.*;
import java.nio.file.Path;

/**
 * TODO: add class Doc
 */
class ElasticUtils {

    private static final String TRANSPORT_CLIENT_INITIAL_NODES = "transport.client.initial_nodes";

    static TransportClient getElasticClient(Path pathToElasticSettings)
            throws UnknownHostException {
        TransportClient.Builder elasticCliFact = TransportClient.builder();
        elasticCliFact.addPlugin(DeleteByQueryPlugin.class);
        Settings settings = Settings.settingsBuilder().loadFromPath(pathToElasticSettings).build();
        elasticCliFact.settings(settings);
        return addTransportAddresses(elasticCliFact.build(),
                settings.getAsArray(TRANSPORT_CLIENT_INITIAL_NODES));
    }

    private static TransportClient addTransportAddresses(TransportClient client, String[] addresses)
            throws UnknownHostException {
        for (String address : addresses) {
            addTransportAddress(client, address);
        }
        return client;
    }

    private static void addTransportAddress(TransportClient client, String address)
            throws UnknownHostException {
        URI uri = URI.create("es://" + address);
        String host = uri.getHost();
        int port = uri.getPort();
        client.addTransportAddress(
                new InetSocketTransportAddress(InetAddress.getByName(host), port));
    }

}
