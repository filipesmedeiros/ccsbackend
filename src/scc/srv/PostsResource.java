package scc.srv;

import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.RequestOptions;
import utils.Database;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Date;


@Path("/posts")
public class PostsResource {

    private static final String POST_COL = "Posts";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addPost(String jsonPost) {
        Document postDoc = new Document(jsonPost);
        postDoc.set("creationDate", new Date());
        return Database.createResource(POST_COL, postDoc, new RequestOptions(), true);

    }
/*
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addPost(String jsonPost) {

    }
*/
}
