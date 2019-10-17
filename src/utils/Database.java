package utils;

import com.microsoft.azure.cosmosdb.*;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import exceptions.ConflictException;
import rx.Observable;

import javax.ws.rs.NotFoundException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;

public class Database {

    private static final String AZURE_DB_URL = "https://ccsbackend-database.documents.azure.com";
    private static final String AZURE_DB_ENDPOINT = AZURE_DB_URL + ":443/";
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

    private static String getDocumentURI(String col, String id) {
        return AZURE_DB_URL + getCollectionString(col) + "/docs/" + id;
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

    public static String putResourceOverwrite(Document doc, String col) {
        return createResource(col, doc, new RequestOptions(), false);
    }

    public static String createResourceIfNotExists(Document doc, String col, boolean autoGenId) {
        if(!Database.resourceExists(col, doc.getId()))
            return createResource(col, doc, new RequestOptions(), autoGenId);
        throw new ConflictException();
    }

    public static void deleteResource(String col, String id) {
        Observable<ResourceResponse<Document>> resp =
                dbClient.deleteDocument(getDocumentURI(col, id), new RequestOptions());

        resp.doOnError(error -> {throw new NotFoundException();});
    }

    public static String getResourceJson(String col, String query) {
        initializeDatabase();

        String collection = getCollectionString(col);

        Iterator<FeedResponse<Document>> it = dbClient.queryDocuments(collection, query, buildDefaultFeedOptions())
                .toBlocking()
                .getIterator();

        while(it.hasNext()) {
            List<Document> documentsInFragment = it.next().getResults();
            if(documentsInFragment.size() > 0) {
                Document d = documentsInFragment.get(0);
                return d.toJson();
            }
        }
        throw new NotFoundException();
    }

    public static boolean resourceExists(String col, String id){
        initializeDatabase();

        String collection = getCollectionString(col);

        Iterator<FeedResponse<Document>> it = dbClient.queryDocuments(collection,
                "SELECT * FROM " + col + " p where p.id = '" + id + "'", buildDefaultFeedOptions())
                .toBlocking()
                .getIterator();

        while(it.hasNext()) {
            List<Document> documentsInFragment = it.next().getResults();
            if(documentsInFragment.size() > 0)
                return true;
        }
        return false;
    }

    public static boolean testClientJsonWithDoc(Document doc, Class<?> clazz) {
        for(Field f : clazz.getDeclaredFields())
            if(!doc.has(f.getName()))
                return false;
            return true;
    }

    private static FeedOptions buildDefaultFeedOptions(){
        FeedOptions queryOptions = new FeedOptions();
        queryOptions.setEnableCrossPartitionQuery(true);
        queryOptions.setMaxDegreeOfParallelism(-1);

        return queryOptions;
    }

}
