package utils;

import com.google.gson.Gson;
import com.microsoft.azure.cosmosdb.*;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import resources.Post;
import rx.Observable;

import javax.print.Doc;
import javax.ws.rs.NotFoundException;
import java.util.Iterator;
import java.util.List;

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
        Observable<ResourceResponse<Document>> resp = dbClient.createDocument(collection, doc, requestOptions, !autoGenId);
        return resp.toBlocking().first().getResource().getId();
    }

    public static String getResourceJson(String col, String query) {
        String collection = getCollectionString(col);

        FeedOptions queryOptions = new FeedOptions();
        queryOptions.setEnableCrossPartitionQuery(true);
        queryOptions.setMaxDegreeOfParallelism(-1);

        Iterator<FeedResponse<Document>> it = dbClient.queryDocuments(collection, query, queryOptions)
                .toBlocking()
                .getIterator();

        while(it.hasNext()) {
            List<Document> documentsInFragment = it.next().getResults();
            System.out.println(documentsInFragment.size());
            if(documentsInFragment.size() > 0) {
                Document d = documentsInFragment.get(0);
                String docJson = d.toJson();
                System.out.println(d.toJson());
                return docJson;
            }
        }
        throw new NotFoundException();
    }
}
