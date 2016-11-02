package org.inpher.search.cli;

import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.inpher.search.Schema;
import org.inpher.search.SearchAccessLayer;
import org.inpher.search.SearchClient;
import org.inpher.search.SearchClientFactory;
import org.inpher.search.elasticsearch.ElasticSearchAccessLayer;

/**
 * Connector to the elastic backend
 */
class Connector {

    SearchCmdCli connect(TransportClient elasticCli, Schema schema, byte[] key)
            throws ElasticConnectException {
        try {
            testElasticClient(elasticCli);
        } catch (Exception e) {
            throw new ElasticConnectException(e);
        }

        SearchAccessLayer backendConnector = new ElasticSearchAccessLayer(elasticCli);
        SearchClient client =
                new SearchClientFactory().newSearchClient(schema, key, backendConnector);

        return new SearchCmdCli(client);
    }

    private void testElasticClient(TransportClient elasticCli) {
        elasticCli.get(new GetRequest());
    }

}
