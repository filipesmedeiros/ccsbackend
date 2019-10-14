package scc.srv;

import com.microsoft.azure.storage.StorageException;
import exceptions.ContainerDoesNotExistException;
import exceptions.ErrorConnectingToDatabaseException;
import utils.Storage;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;

@Path("/images")
public class ImageResource {

    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public String upload(byte[] data) {
        try {
            return Storage.upload(data, false);
        } catch (ErrorConnectingToDatabaseException | StorageException e) {
            e.printStackTrace();
            throw new ServiceUnavailableException();
        } catch (URISyntaxException | InvalidKeyException e) {
            e.printStackTrace();
            throw new InternalServerErrorException();
        }
    }

    @GET
    @Path("/{blobId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] download(@PathParam("blobId") String blobId) {
        try {
            return Storage.download(blobId);
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
