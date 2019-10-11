package scc.srv;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import utils.Secrets;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class MainApplication extends Application {

    private static CloudStorageAccount storage;
    public static CloudBlobClient blobClient;

    public static void initializeStorage()
            throws URISyntaxException, InvalidKeyException {
        try {
            if(storage == null)
                storage = CloudStorageAccount.parse(Secrets.AZURE_STORAGE_KEY);
        } catch(URISyntaxException | InvalidKeyException e) {
            System.out.println("Something went wrong with init storage. Check key.");
            e.printStackTrace();
            throw e;
        }
        if(blobClient == null)
            blobClient = storage.createCloudBlobClient();
    }

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> set = new HashSet<>();
        set.add(MediaResource.class);
        return set;
    }
}