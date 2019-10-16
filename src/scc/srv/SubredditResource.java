package scc.srv;

import com.google.gson.Gson;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.RequestOptions;
import resources.Subreddit;
import resources.User;
import utils.Database;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

public class SubredditResource {

    @Path("/subreddits")
    public class UsersResource {

        private static final String SUBREDDIT_COL = "Subreddits";

        @POST
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public String addSubreddit(String jsonUser) {
            System.out.println(jsonUser);
            Document postDoc = new Document(jsonUser);
            return Database.createResource(SUBREDDIT_COL, postDoc, new RequestOptions(), false);
        }

        @GET
        @Path("/{subreddit}")
        @Consumes(MediaType.APPLICATION_JSON)
        @Produces(MediaType.APPLICATION_JSON)
        public Subreddit getSubreddit(@PathParam("subreddit") String subreddit) {
            System.out.println(subreddit);
            String postJson = Database.getResourceJson(SUBREDDIT_COL,
                    "SELECT * FROM " + SUBREDDIT_COL + " p WHERE p.id = '" + subreddit + "'");
            return new Gson().fromJson(postJson, Subreddit.class);
        }
    }

}
