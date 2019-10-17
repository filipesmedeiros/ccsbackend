package scc.srv;

import com.google.gson.Gson;
import com.microsoft.azure.cosmosdb.Document;
import resources.Subreddit;
import utils.Database;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/subreddits")
public class SubredditResource {

    public static final String SUBREDDIT_COL = "Subreddits";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addSubreddit(String jsonSubreddit) {
        Document subredditDoc = new Document(jsonSubreddit);
        return Database.createResourceIfNotExists(subredditDoc, SUBREDDIT_COL, false);
    }

    @GET
    @Path("/{subreddit}")
    @Produces(MediaType.APPLICATION_JSON)
    public Subreddit getSubreddit(@PathParam("subreddit") String subreddit) {
        System.out.println(subreddit);
        String postJson = Database.getResourceJson(SUBREDDIT_COL,
                "SELECT * FROM " + SUBREDDIT_COL + " p WHERE p.id = '" + subreddit + "'");
        return new Gson().fromJson(postJson, Subreddit.class);
    }
}


