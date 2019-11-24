package utils;

import com.microsoft.azure.cosmos.CosmosClient;
import com.microsoft.azure.cosmos.CosmosItem;
import com.microsoft.azure.cosmosdb.*;
import com.microsoft.azure.cosmosdb.rx.AsyncDocumentClient;
import exceptions.ConflictException;
import management.AzureManagement;
import rx.Observable;

import javax.ws.rs.NotFoundException;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Database {

    private static final String AZURE_DB_URL = "https://scccosmos4770147967.documents.azure.com";
    private static final String AZURE_DB_ENDPOINT = AZURE_DB_URL + ":443/";
    private static final String AZURE_DB_ID = AzureManagement.AZURE_COSMOSDB_DATABASE;
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
            System.out.println(doc.getId());
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

            while(it.hasNext()) {
                System.out.println("in iterator");
                List<Document> documentsInFragment = it.next().getResults();
                if(documentsInFragment.size() > 0)
                    return documentsInFragment.get(0);
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw new NotFoundException();
        }
        System.out.println("Will throw ex");
        throw new NotFoundException();
    }

    public static List<Document> getResourceListDocs(String col, String query) {
        initializeDatabase();

        String collection = getCollectionString(col);

        System.out.println(query);

        try {
            Iterator<FeedResponse<Document>> it = dbClient.queryDocuments(collection, query, buildDefaultFeedOptions())
                    .toBlocking()
                    .getIterator();

            List<Document> allResults = new LinkedList<>();

            while(it.hasNext()) {
                List<Document> documentsInFragment = it.next().getResults();
                if(documentsInFragment.size() > 0)
                    allResults.addAll(documentsInFragment); // TODO optimize when partition key??
            }

            return allResults;
        } catch(Exception e) {
            e.printStackTrace();
            throw new NotFoundException();
        }
    }

    // TODO check with preguica
    public static CosmosItem getResourceById(String container, String id) {
        return CosmosClient.create(AZURE_DB_ENDPOINT, Secrets.AZURE_DB_PRIMARY_KEY)
                .getDatabase(AZURE_DB_ID).getContainer(container).getItem(id);
    }

    public static void replaceDocument(Document newDocument) {
        initializeDatabase();

        dbClient.replaceDocument(newDocument,  new RequestOptions());
    }

    public static boolean resourceExists(String col, String id) {
        try {
            getResourceDocById(col, id);
            return true;
        } catch(NotFoundException nfe) {
            return false;
        }
    }

    public static Document count(String col, String query) {
        initializeDatabase();

        return dbClient.queryDocuments(getCollectionString(col), query,
                buildDefaultFeedOptions())
                .toBlocking().first().getResults().get(0);
    }

    private static FeedOptions buildDefaultFeedOptions(){
        FeedOptions queryOptions = new FeedOptions();
        queryOptions.setEnableCrossPartitionQuery(true);
        queryOptions.setMaxDegreeOfParallelism(-1);

        return queryOptions;
    }

}
