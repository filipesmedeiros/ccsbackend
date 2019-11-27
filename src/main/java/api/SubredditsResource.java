package api;

import com.google.gson.Gson;
import com.microsoft.azure.cosmosdb.Document;
import resources.Subreddit;
import utils.Database;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/subreddits")
public class SubredditsResource {

    public static final String SUBREDDIT_COL = "Subreddits";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String addSubreddit(String jsonSubreddit) {
        Document subredditDoc = new Document(jsonSubreddit);
        subredditDoc.set("score", 0);
        System.out.println(jsonSubreddit + "/n -----------------------");
        return Database.createResourceIfNotExists(subredditDoc, SUBREDDIT_COL, false).getId();
    }

    @GET
    @Path("/{subreddit}")
    @Produces(MediaType.APPLICATION_JSON)
    public Subreddit getSubreddit(@PathParam("subreddit") String subreddit) {
        Document subredditDoc = Database.getResourceDocById(SUBREDDIT_COL, subreddit);
        return new Gson().fromJson(subredditDoc.toJson(), Subreddit.class);
    }
}


