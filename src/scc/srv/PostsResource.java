package scc.srv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.FeedResponse;
import com.microsoft.azure.cosmosdb.RequestOptions;
import resources.Post;
import utils.Database;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;


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

    @GET
    @Path("/{postId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Post getPost(@PathParam("postId") String postId) {
        String postJson = Database.getResourceJson(POST_COL,
                "SELECT * FROM " + POST_COL + " p WHERE p.id = " + postId);
        return new Gson().fromJson(postJson, Post.class);
    }

    @POST
    @Path("/setlike")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String setLike() {
        return "";
    }

    @POST
    @Path("/unsetlike")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String unsetLike() {
        return "";
    }

    @POST
    @Path("/setdislike")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String setDislike() {
        return "";
    }

    @POST
    @Path("/unsetdislike")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String unsetDislike() {
        return "";
    }

}
