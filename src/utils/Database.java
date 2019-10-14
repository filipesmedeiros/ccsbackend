package utils;

import com.microsoft.azure.cosmosdb.ConnectionMode;
import com.microsoft.azure.cosmosdb.ConnectionPolicy;
import com.microsoft.azure.cosmosdb.ConsistencyLevel;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

public class Database {

    private static final String AZURE_DB_ENDPOINT = "https://ccsbackend-database.documents.azure.com:443/";
    private static final String AZURE_DB_ID = "ccsbackend-database";
    private static AsyncDocumentClient dbClient;

    static synchronized AsyncDocumentClient initializeDatabase() {
        if (dbClient == null) {
            ConnectionPolicy connectionPolicy = new ConnectionPolicy();
            connectionPolicy.setConnectionMode(ConnectionMode.Direct);
            dbClient = new AsyncDocumentClient.Builder()
                    .withServiceEndpoint(AZURE_DB_ENDPOINT)
                    .withMasterKeyOrResourceToken(Secrets.AZURE_DB_PRIMARY_KEY)
                    .withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.Session).build();
        }
        return dbClient;
    }

    static String getCollectionString(String col) {
        return String.format("/dbs/%s/colls/%s", AZURE_DB_ID, col);
    }
}
