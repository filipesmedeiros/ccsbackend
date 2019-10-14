package scc.srv;

import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.RequestOptions;
import resources.Post;
import utils.Database;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Date;


@Path("/posts")
public class PostsResource {

    private static final String POST_COL = "Posts";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addPost(String jsonPost) {
        try {
            System.out.println(jsonPost);
            Post post = Post.fromJson(jsonPost, new Date());
            System.out.println(post.getCreationDate());
            System.out.println(post.getSubreddit());
            System.out.println(post.getOpUsername());
            Document postDoc = new Document(jsonPost);
            return Database.createResource(POST_COL, postDoc, new RequestOptions(), true);
        } catch (IOException e) {
            e.printStackTrace();
            throw new BadRequestException();
        }
    }
/*
    @GET
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addPost(String jsonPost) {

    }
*/
}
