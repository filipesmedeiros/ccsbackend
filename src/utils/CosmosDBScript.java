package utils;

import com.microsoft.azure.cosmosdb.*;
import com.microsoft.azure.cosmosdb.Database;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;

import java.util.Arrays;
import java.util.List;

public class CosmosDBScript {

    private static final String AZURE_DB_URL = "https://ccsbackend-database.documents.azure.com";
    private static final String AZURE_DB_ENDPOINT = AZURE_DB_URL + ":443/";
    private static final String AZURE_DB_ID = "ccsbackend-database";

    private static AsyncDocumentClient client;

    public static void main(String[] args) {

    }

    private static void build() {
        try {
            AsyncDocumentClient client = getDocumentClient();

            List<com.microsoft.azure.cosmosdb.Database> databaseList = client
                    .queryDatabases("SELECT * FROM root r WHERE r.id='" + AZURE_DB_ID + "'", null).toBlocking()
                    .first().getResults();
            if (databaseList.size() == 0) {
                try {
                    com.microsoft.azure.cosmosdb.Database databaseDefinition = new Database();
                    databaseDefinition.setId(AZURE_DB_ID);
                    client.createDatabase(databaseDefinition, null).toCompletable().await();
                } catch (Exception e) {
                    // TODO: Something has gone terribly wrong.
                    e.printStackTrace();
                    return;
                }
            }

            String collectionName = "Users";
            List<DocumentCollection> collectionList = client.queryCollections(getDatabaseString(),
                    "SELECT * FROM root r WHERE r.id='" + collectionName + "'", null).toBlocking().first().getResults();

            if (collectionList.size() == 0) {
                try {
                    String databaseLink = getDatabaseString();
                    DocumentCollection collectionDefinition = new DocumentCollection();
                    collectionDefinition.setId(collectionName);
                    PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
                    partitionKeyDef.setPaths(Arrays.asList("/name"));
                    collectionDefinition.setPartitionKey(partitionKeyDef);

                    client.createCollection(databaseLink, collectionDefinition, null).toCompletable().await();
                } catch (Exception e) {
                    // TODO: Something has gone terribly wrong.
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            client.close();
        }
    }

    private static void destroy() {

    }

    private static synchronized AsyncDocumentClient getDocumentClient() {
        if (client == null) {
            ConnectionPolicy connectionPolicy = new ConnectionPolicy();
            connectionPolicy.setConnectionMode(ConnectionMode.Direct);
            client = new AsyncDocumentClient.Builder().withServiceEndpoint(AZURE_DB_ENDPOINT)
                    .withMasterKeyOrResourceToken(Secrets.AZURE_DB_PRIMARY_KEY).withConnectionPolicy(connectionPolicy)
                    .withConsistencyLevel(ConsistencyLevel.Eventual).build();
        }
        return client;
    }

    private static String getDatabaseString() {
        return String.format("/dbs/%s", AZURE_DB_ID);
    }

    static String getCollectionString(String col) {
        return String.format("/dbs/%s/colls/%s", AZURE_DB_ID, col);
    }
}
