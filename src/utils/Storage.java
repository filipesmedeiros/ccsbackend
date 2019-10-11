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

    public static String upload(byte[] data, String container, boolean override)
            throws ErrorConnectingToDatabaseException, URISyntaxException, StorageException, InvalidKeyException {

        MainApplication.initializeStorage();

        try {
            CloudBlobContainer blobContainer = MainApplication.blobClient.getContainerReference(container);
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
        } catch (IOException e) {
            e.printStackTrace();
            throw new ErrorConnectingToDatabaseException();
        }
    }

    public static byte[] download(String blobId, String container)
            throws ContainerDoesNotExistException, URISyntaxException,
            InvalidKeyException, ErrorConnectingToDatabaseException {

        MainApplication.initializeStorage();

        try{
            CloudBlobContainer blobContainer = MainApplication.blobClient.getContainerReference(container);
            if(!blobContainer.exists())
                throw new ContainerDoesNotExistException();
            CloudBlob blob = blobContainer.getBlobReferenceFromServer(blobId);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            blob.download(out);
            return out.toByteArray();
        } catch(URISyntaxException e) {
            e.printStackTrace();
            throw e;
        } catch(StorageException e) {
            e.printStackTrace();
            throw new ErrorConnectingToDatabaseException();
        }
    }



}
