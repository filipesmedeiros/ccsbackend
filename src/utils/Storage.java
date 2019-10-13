package utils;

import com.microsoft.azure.storage.OperationContext;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.blob.*;
import exceptions.ContainerDoesNotExistException;
import exceptions.ErrorConnectingToDatabaseException;
import scc.srv.MainApplication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Arrays;

public class Storage {

    private static final String CONTAINER = "images";

    public static String upload(byte[] data, boolean override)
            throws ErrorConnectingToDatabaseException, URISyntaxException, StorageException, InvalidKeyException {

        MainApplication.initializeStorage();

        try {
            CloudBlobContainer blobContainer = MainApplication.blobClient.getContainerReference(CONTAINER);
            blobContainer.createIfNotExists(BlobContainerPublicAccessType.BLOB,
                    new BlobRequestOptions(),
                    new OperationContext());

            String blobId = Integer.toString(Arrays.hashCode(data));
            CloudBlockBlob blob = blobContainer.getBlockBlobReference(blobId);
            if (override || !blob.exists())
                blob.uploadFromByteArray(data, 0, data.length);

            return blob.getUri().toString();
        } catch (StorageException | URISyntaxException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new ErrorConnectingToDatabaseException();
        }
    }

    public static byte[] download(String blobId)
            throws ContainerDoesNotExistException, URISyntaxException,
            InvalidKeyException, ErrorConnectingToDatabaseException {

        MainApplication.initializeStorage();

        try{
            CloudBlobContainer blobContainer = MainApplication.blobClient.getContainerReference(CONTAINER);
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



}
