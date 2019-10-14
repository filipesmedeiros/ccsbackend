package utils;

import com.microsoft.azure.cosmosdb.*;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import rx.Observable;

public class Database {

    private static final String AZURE_DB_ENDPOINT = "https://ccsbackend-database.documents.azure.com:443/";
    private static final String AZURE_DB_ID = "ccsbackend-database";
    private static AsyncDocumentClient dbClient;

    private static synchronized AsyncDocumentClient initializeDatabase() {
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

    private static String getCollectionString(String col) {

        return String.format("/dbs/%s/colls/%s", AZURE_DB_ID, col);
    }

    public static String createResource(String col, Document doc, RequestOptions requestOptions, boolean autoGenId) {
        initializeDatabase();

        String collection = getCollectionString(col);

        System.out.println(doc.get("title"));

        Observable<ResourceResponse<Document>> resp = dbClient.createDocument(collection, doc, requestOptions, !autoGenId);
        return resp.toBlocking().first().getResource().getId();
    }

    /*
    public Document getResource(String col, String query) {
        String collection = getCollectionString(col);

    }
    */
}
