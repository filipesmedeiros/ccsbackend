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

    public static Document putResourceOverwrite(Document doc, String col) {
        initializeDatabase();
        return dbClient.upsertDocument(getCollectionString(col), doc,
                new RequestOptions(), false).toBlocking().first().getResource();
    }

    //RunTimeException is when CosmosClient finds a conflict
    public static Document createResourceIfNotExists(Document doc, String col, boolean autoGenId) {
        initializeDatabase();
        try{
            return dbClient.createDocument(getCollectionString(col), doc,
                    new RequestOptions(), !autoGenId).toBlocking().first().getResource();
        } catch(RuntimeException e){
            e.printStackTrace();
            throw new ConflictException();
        }
    }

    public static void deleteResource(String col, String id) {
        initializeDatabase();
        Observable<ResourceResponse<Document>> resp =
                dbClient.deleteDocument(getDocumentURI(col, id), new RequestOptions());

        resp.doOnError(error -> {throw new NotFoundException();});
    }

    public static Document getResourceDocById(String col, String id) {
        System.out.println(col + " ----- "+ id);
        return getResourceDoc(col, "SELECT * from " + col + " x WHERE x.id = '" + id + "'");
    }

    public static Document getResourceDoc(String col, String query) {
        initializeDatabase();

        String collection = getCollectionString(col);

        System.out.println(query);

        try {
            Iterator<FeedResponse<Document>> it = dbClient.queryDocuments(collection, query, buildDefaultFeedOptions())
                    .toBlocking()
                    .getIterator();

            System.out.println("out iterator");

            while(it.hasNext()) {
                System.out.println("in iterator");
                List<Document> documentsInFragment = it.next().getResults();
                if(documentsInFragment.size() > 0) {
                    return documentsInFragment.get(0);
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw new NotFoundException();
        }
        System.out.println("Will throw ex");
        throw new NotFoundException();
    }

    public static boolean resourceExists(String col, String id) {
        try {
            getResourceDocById(col, id);
            return true;
        } catch(NotFoundException nfe) {
            return false;
        }
    }

    public static boolean testClientJsonWithDoc(Document doc, Class<?> clazz) {
        for(Field f : clazz.getDeclaredFields()) {
            System.out.println(f.getName());
            if(!doc.has(f.getName()))
                return false;
        }
        return true;
    }

    private static FeedOptions buildDefaultFeedOptions(){
        FeedOptions queryOptions = new FeedOptions();
        queryOptions.setEnableCrossPartitionQuery(true);
        queryOptions.setMaxDegreeOfParallelism(-1);

        return queryOptions;
    }

}
