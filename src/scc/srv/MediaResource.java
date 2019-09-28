package scc.srv;

import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.CloudBlob;
import com.microsoft.azure.storage.blob.CloudBlobContainer;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Arrays;

@Path("/media")
public class MediaResource {

    private static CloudBlobContainer mediaContainer;

    private static void initializeMediaContainer()
            throws URISyntaxException, StorageException, InvalidKeyException {
        MainApplication.initializeStorageConnection();
        if(mediaContainer == null)
            mediaContainer = MainApplication.blobClient.getContainerReference("container1");
    }

    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public String upload(byte[] data)
            throws URISyntaxException, StorageException, IOException, InvalidKeyException {

        // TODO Check if the blob already exists, so you don't store it twice, or waste bandwith uploading
        // Use .exists() on CloudBlob to check

        try {
            initializeMediaContainer();
        } catch(Exception e) {
            System.out.println("Something went wrong while initializing storage client. Please see log.");
            e.printStackTrace();
            throw e;
        }

        try {
            String blobId = Integer.toString(Arrays.hashCode(data));
            CloudBlob blob = mediaContainer.getBlockBlobReference(blobId);
            blob.uploadFromByteArray(data, 0, data.length);
            return blobId;
        } catch (URISyntaxException use) {
            System.out.println("For some reason the name is invalid, check again.");
            use.printStackTrace();
            throw use;
        } catch (StorageException | IOException e) {
            System.out.println("Something went wrong while uploading to Azure. Please see log.");
            e.printStackTrace();
            throw e;
        }
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] download(String blobId)
            throws URISyntaxException, StorageException, InvalidKeyException, IOException {
        try {
            initializeMediaContainer();
        } catch(Exception e) {
            System.out.println("Something went wrong while initializing storage client. Please see log.");
            e.printStackTrace();
            throw e;
        }

        try {
            CloudBlob blob = mediaContainer.getBlobReferenceFromServer(blobId);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            blob.download(out);
            out.close();
            return out.toByteArray();
        } catch(StorageException se) {
            System.out.println("Blob " + blobId + " not found.");
            se.printStackTrace();
            throw new ForbiddenException();
        } catch(IOException ioe) {
            System.out.println("Something went wrong closing the download stream. Please see log.");
            ioe.printStackTrace();
            throw ioe;
        }
    }
}
