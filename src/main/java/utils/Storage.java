package utils;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import exceptions.ContainerDoesNotExistException;
import exceptions.ErrorConnectingToDatabaseException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Arrays;

public class Storage {

    private static final String CONTAINER = "images";

    private static CloudStorageAccount storageEuWest;
    private static CloudStorageAccount storageJapanEast;
    private static CloudBlobClient blobClientEuWest;
    private static CloudBlobClient blobClientJapanEast;

    private static void initializeStorage()
            throws URISyntaxException, InvalidKeyException {
        try {
            if(storageEuWest == null)
                storageEuWest = CloudStorageAccount.parse(Secrets.AZURE_STORAGE_KEY_EU_WEST);
            if(storageJapanEast == null)
                storageJapanEast = CloudStorageAccount.parse(Secrets.AZURE_STORAGE_KEY_JAPAN_EAST);
        } catch(URISyntaxException | InvalidKeyException e) {
            System.out.println("Something went wrong with init storageEuWest. Check key.");
            e.printStackTrace();
            throw e;
        }
        if(blobClientEuWest == null)
            blobClientEuWest = storageEuWest.createCloudBlobClient();
        if(blobClientJapanEast == null)
            blobClientJapanEast = storageJapanEast.createCloudBlobClient();
    }

    public static String upload(byte[] data, boolean override)
            throws ErrorConnectingToDatabaseException, URISyntaxException, StorageException, InvalidKeyException {

        initializeStorage();

        System.out.println("-----------------------/n");
        System.out.println("Uploading image");
        System.out.println("-----------------------/n");

        uploadToBoth(data, override, blobClientEuWest);
        return uploadToBoth(data, override, blobClientJapanEast);
    }

    public static byte[] download(String blobId)
            throws ContainerDoesNotExistException, URISyntaxException,
            InvalidKeyException, ErrorConnectingToDatabaseException {

        initializeStorage();

        try{
            CloudBlobContainer blobContainer = blobClientEuWest.getContainerReference(CONTAINER);
            if(!blobContainer.exists())
                throw new ContainerDoesNotExistException();
            CloudBlob blob = blobContainer.getBlobReferenceFromServer(blobId);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            blob.download(out);
            return out.toByteArray();
        } catch(URISyntaxException urise) {
            urise.printStackTrace();
            throw urise;
        } catch(StorageException se) {
            se.printStackTrace();
            throw new ErrorConnectingToDatabaseException();
        }
    }

    private static String uploadToBoth(byte[] data, boolean override, CloudBlobClient blobClient)
            throws URISyntaxException, StorageException, ErrorConnectingToDatabaseException {

        try {
            CloudBlobContainer blobContainer = blobClient.getContainerReference(CONTAINER);
            String blobId = Integer.toString(Arrays.hashCode(data));
            CloudBlockBlob blob = blobContainer.getBlockBlobReference(blobId);

            if(override || !blob.exists())
                blob.uploadFromByteArray(data, 0, data.length);

            return blob.getUri().toString();
        } catch (StorageException | URISyntaxException e) {
            e.printStackTrace();
            throw e;
        } catch(IOException ioe) {
            ioe.printStackTrace();
            throw new ErrorConnectingToDatabaseException();
        }
    }
}
