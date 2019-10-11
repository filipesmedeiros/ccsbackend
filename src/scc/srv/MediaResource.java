package scc.srv;

import com.microsoft.azure.storage.StorageException;
import exceptions.ContainerDoesNotExistException;
import exceptions.ErrorConnectingToDatabaseException;
import utils.Storage;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@Path("/media")
public class MediaResource {

    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public String upload(byte[] data) {
        try {
            return Storage.upload(data, "user-avatar", true);
        } catch (ErrorConnectingToDatabaseException | StorageException e) {
            e.printStackTrace();
            throw new ServiceUnavailableException();
        } catch (URISyntaxException | InvalidKeyException e) {
            e.printStackTrace();
            throw new InternalServerErrorException();
        }
    }

    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] download(String blobId) {
        try {
            return Storage.download(blobId, "user-avatar");
        } catch (ErrorConnectingToDatabaseException e) {
            e.printStackTrace();
            throw new ServiceUnavailableException();
        } catch (URISyntaxException | InvalidKeyException e) {
            e.printStackTrace();
            throw new InternalServerErrorException();
        } catch (ContainerDoesNotExistException e) {
            e.printStackTrace();
            throw new NotFoundException();
        }
    }
}
