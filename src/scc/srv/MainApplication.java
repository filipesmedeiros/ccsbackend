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

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> set = new HashSet<>();
        set.add(ImageResource.class);
        set.add(PostsResource.class);
        return set;
    }
}