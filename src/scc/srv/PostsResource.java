package scc.srv;

import com.google.gson.Gson;
import com.microsoft.azure.cosmosdb.Document;
import resources.Post;
import resources.PostVote;
import utils.Database;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Date;


@Path("/posts")
public class PostsResource {

    public static final String POST_COL = "Posts";
    public static final String POSTVOTE_COL = "PostVotes";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String addPost(String jsonPost) {
        Document postDoc = new Document(jsonPost);
        if(!Database.testClientJsonWithDoc(postDoc, Post.PostDTOInitialAttributes.class))
            throw new BadRequestException();

        String username = postDoc.get("opUsername").toString();
        String subreddit = postDoc.get("subreddit").toString();
        if(!Database.resourceExists(UsersResource.USERS_COL, username))
            throw new BadRequestException("The author of the post does not exist.");
        if(!Database.resourceExists(SubredditResource.SUBREDDIT_COL, subreddit))
            throw new BadRequestException("The subreddit of the post does not exist.");
        postDoc.set("creationDate", new Date().getTime());
        return Database.createResourceIfNotExists(postDoc, POST_COL, true).getId();
    }

    @GET
    @Path("/{postId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Post getPost(@PathParam("postId") String postId) {
        Document postDoc = Database.getResourceDocById(POST_COL, postId);
        return new Gson().fromJson(postDoc.toJson(), Post.class);
    }

    @POST
    @Path("/{postId}/setupvote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String setLike(@PathParam("postId") String postId, String jsonUsername) {
        return addPostVote(postId, jsonUsername, true);
    }

    @POST
    @Path("/{postId}/unsetupvote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void unsetLike(@PathParam("postId") String postId, String jsonUsername) {
        deletePostVote(postId, jsonUsername);
    }

    @POST
    @Path("/{postId}/setdownvote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String setDislike(@PathParam("postId") String postId, String jsonUsername) {
        return addPostVote(postId, jsonUsername, false);
    }

    @POST
    @Path("/{postId}/unsetdownvote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void unsetDislike(@PathParam("postId") String postId, String jsonUsername) {
        deletePostVote(postId, jsonUsername);
    }

    // TODO having this private method is good?
    private void deletePostVote(String postId, String jsonUsername) {
        Document usernameDoc = new Document(jsonUsername);
        String userId = usernameDoc.getId();
        String postVoteId = PostVote.generateId(postId, userId);
        Database.deleteResource(POSTVOTE_COL, postVoteId);
    }

    private String addPostVote(String postId, String jsonUsername, boolean up) {
        Gson gson = new Gson();
        Document usernameDoc = new Document(jsonUsername);
        String userId = usernameDoc.getId();
        if(!Database.resourceExists(UsersResource.USERS_COL, userId))
            throw new BadRequestException("The author of the vote does not exist.");


        if(!Database.resourceExists(POST_COL, postId))
            throw new NotFoundException("No post with that id was found");

        // TODO Verify is we need atomic operations

        String postVoteId = PostVote.generateId(postId, userId);
        PostVote newPostVote = new PostVote(postVoteId, up, postId, userId);

        try {
            PostVote previousPostVote =
                    gson.fromJson(Database.getResourceDocById(POSTVOTE_COL, postVoteId).toJson(), PostVote.class);
            if(previousPostVote.isUp() != up) {
                return Database.putResourceOverwrite(newPostVote.toDocument(), POSTVOTE_COL).getId();
            }
        } catch(NotFoundException nfe) {
            return Database.putResourceOverwrite(newPostVote.toDocument(), POSTVOTE_COL).getId();
        }
        throw new BadRequestException("User already has upvote on this post.");
    }
}
