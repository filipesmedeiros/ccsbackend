package scc.srv;

import com.google.gson.Gson;
import com.microsoft.azure.cosmosdb.Document;
import resources.User;
import utils.Database;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/users")
public class UsersResource {

    public static final String USERS_COL = "Users";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addUser(String jsonUser) {
        Document userDoc = new Document(jsonUser);
        return Database.createResourceIfNotExists(userDoc, USERS_COL);
    }

    @GET
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@PathParam("username") String username) {
        System.out.println(username);
        String userJson = Database.getResourceJson(USERS_COL,
                "SELECT * FROM " + USERS_COL + " p WHERE p.id = '" + username + "'");
        return new Gson().fromJson(userJson, User.class);
    }
}
