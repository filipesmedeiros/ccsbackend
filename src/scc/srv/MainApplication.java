package scc.srv;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class MainApplication extends Application {

    public static String storageConnectionString = "DefaultEndpointsProtocol=https;AccountName=ccsbackend47967;AccountKey=GMFXjCsXBa2iGrLslyuQ3yeugh/uEFoTzdp/F/25J3y0Bev4dyGpm3kOCgiic5wMWtEBwZKJsflsM+aMlmSBBw==;EndpointSuffix=core.windows.net";
    public static CloudStorageAccount storageAccount;
    public static CloudBlobClient blobClient;

    public static void initializeStorageConnection()
            throws URISyntaxException, InvalidKeyException {
        if(storageAccount == null)
            try {
                storageAccount = CloudStorageAccount.parse(storageConnectionString);
            } catch(InvalidKeyException ike) {
                System.out.println("Invalid storageConnectionString. Try again.");
                ike.printStackTrace();
                throw ike;
            } catch(URISyntaxException use) {
                System.out.println("Invalid URI. Try again.");
                use.printStackTrace();
                throw use;
            }
        if(blobClient == null)
            blobClient = storageAccount.createCloudBlobClient();
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> set = new HashSet<>();
        set.add(MediaResource.class);
        return set;
    }
}