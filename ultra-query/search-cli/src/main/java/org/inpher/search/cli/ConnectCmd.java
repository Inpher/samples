package org.inpher.search.cli;

/**
 * Result of the Connect command parse
 */
class ConnectCmd {
    private final String elasticSettingsPath;
    private final String keyPath;
    private final String schemaPath;

    ConnectCmd(String elasticSettingsPath, String keyPath, String schemaPath) {
        this.elasticSettingsPath = elasticSettingsPath;
        this.keyPath = keyPath;
        this.schemaPath = schemaPath;
    }

    String getElasticSettingsPath() {
        return elasticSettingsPath;
    }

    String getKeyPath() {
        return keyPath;
    }

    String getSchemaPath() {
        return schemaPath;
    }

}
