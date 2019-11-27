package api;

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
    @Produces(MediaType.TEXT_PLAIN)
    public String addUser(String jsonUser) {
        Document userDoc = new Document(jsonUser);
        return Database.createResourceIfNotExists(userDoc, USERS_COL, false).getId();
    }

    @GET
    @Path("/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public User getUser(@PathParam("username") String username) {
        Document userDoc = Database.getResourceDocById(USERS_COL, username);
        return new Gson().fromJson(userDoc.toJson(), User.class);
    }
}
