package scc.srv;

import com.google.gson.Gson;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.cosmosdb.RequestOptions;
import resources.Post;
import resources.User;
import utils.Database;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/users")
public class UsersResource {

    private static final String USERS_COL = "Users";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addUser(String jsonUser) {
        System.out.println(jsonUser);
        Document postDoc = new Document(jsonUser);
        return Database.createResource(USERS_COL, postDoc, new RequestOptions(), false);
    }

    @GET
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@PathParam("username") String username) {
        System.out.println(username);
        String postJson = Database.getResourceJson(USERS_COL,
                "SELECT * FROM " + USERS_COL + " p WHERE p.id = " + username);
        return new Gson().fromJson(postJson, User.class);
    }
}
