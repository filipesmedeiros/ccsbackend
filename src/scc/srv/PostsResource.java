package scc.srv;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import resources.Post;
import resources.User;
import utils.Database;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Date;

import static resources.Post.fromJson;

@Path("/posts")
public class PostsResource {

    private static final String POST_COL = "Posts";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String addPost(String jsonPost) {
        try {
            System.out.println(jsonPost);
            Post post = fromJson(jsonPost, new Date());
            //User user = new User();
            //user.setName("amigo");
            System.out.println(post.getCreationDate());
            System.out.println(post.getSubreddit());
            System.out.println(post.getOpUsername());
            return Database.createResource(POST_COL, post, null, false);
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
