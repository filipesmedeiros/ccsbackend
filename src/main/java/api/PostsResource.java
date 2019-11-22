package api;

import com.google.gson.Gson;
import com.microsoft.azure.cosmosdb.Document;
import resources.Post;
import resources.PostThread;
import utils.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Date;


@Path("/posts")
public class PostsResource {

    public static final String POST_COL = "Posts";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String addPost(String jsonPost) {
        Document postDoc = new Document(jsonPost);
        new Gson().fromJson(jsonPost, Post.PostDTOInitialAttributes.class);

        // TODO ir a cache
        String username = postDoc.get("opUsername").toString();
        String subreddit = postDoc.get("subreddit").toString();
        String rootPost = postDoc.get("rootPost").toString();
        String parentPost = postDoc.get("parentPost").toString();
        if(!Database.resourceExists(UsersResource.USERS_COL, username))
            throw new BadRequestException("The author of the post does not exist.");
        if(!Database.resourceExists(SubredditsResource.SUBREDDIT_COL, subreddit))
            throw new BadRequestException("The subreddit of the post does not exist.");

        if(!rootPost.equals("") && !Database.resourceExists(PostsResource.POST_COL, rootPost))
            throw new BadRequestException("The root post of the comment does not exist.");
        if(!parentPost.equals("") && !Database.resourceExists(PostsResource.POST_COL, parentPost))
            throw new BadRequestException("The parent post/comment of the comment does not exist.");
        postDoc.set("timestamp", new Date().getTime());
        postDoc.set("score", 0);

        return Database.createResourceIfNotExists(postDoc, POST_COL, true).getId();
    }

    @GET
    @Path("/{postId}/thread")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPostThread(@PathParam("postId") String postId) {
        PostThread t = Posts.calcPostThread(postId);
        return t.toJson();
    }

    //TODO
    @GET
    @Path("/frontpageall")
    @Produces(MediaType.APPLICATION_JSON)
    public String getFrontPageAll() {
        return new Gson().toJson(Frontpages.getAllFrontpage());
    }

    //TODO
    @GET
    @Path("/{subredditId}/frontpage")
    @Produces(MediaType.APPLICATION_JSON)
    public String getFrontPageOfSubreddit(@PathParam("subredditId") String subredditId) {
        return new Gson().toJson(Frontpages.getSubredditFrontpage(subredditId));
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
    public String setLike(@PathParam("postId") String postId, String voteData) {
        return Votes.addVote(postId, voteData, true);
    }

    @POST
    @Path("/{postId}/unsetupvote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void unsetLike(@PathParam("postId") String postId, String voteData) {
        Votes.deleteVote(postId, voteData, true);
    }

    @POST
    @Path("/{postId}/setdownvote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String setDislike(@PathParam("postId") String postId, String voteData) {
        return Votes.addVote(postId, voteData, false);
    }

    @POST
    @Path("/{postId}/unsetdownvote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void unsetDislike(@PathParam("postId") String postId, String voteData) {
        Votes.deleteVote(postId, voteData, false);
    }
}
