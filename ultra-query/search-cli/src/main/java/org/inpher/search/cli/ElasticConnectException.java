package org.inpher.search.cli;

/**
 * Thrown when the connector cannot reach elastic
 */
class ElasticConnectException extends Exception {
    ElasticConnectException(Throwable cause) {
        super(cause);
    }
}
