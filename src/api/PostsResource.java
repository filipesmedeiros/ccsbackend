package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.azure.cosmosdb.Document;
import redis.clients.jedis.Tuple;
import resources.Post;
import resources.Thread;
import utils.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;


@Path("/posts")
public class PostsResource {

    public static final String POST_COL = "Posts";

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
        if(!Database.resourceExists(SubredditsResource.SUBREDDIT_COL, subreddit))
            throw new BadRequestException("The subreddit of the post does not exist.");
        postDoc.set("timestamp", new Date().getTime());

        return Database.createResourceIfNotExists(postDoc, POST_COL, true).getId();
    }

    @GET
    @Path("/{postId}/thread")
    @Produces(MediaType.APPLICATION_JSON)
    public String getPostThread(@PathParam("postId") String postId) {
        Thread t = new Thread(Posts.getPostThread(postId));
        return t.toJson();
    }

    //TODO
    @GET
    @Path("/frontpageall")
    @Produces(MediaType.APPLICATION_JSON)
    public String getFrontPageAll() {
        return null;
    }

    //TODO
    @GET
    @Path("/{subredditId}/frontpage")
    @Produces(MediaType.APPLICATION_JSON)
    public String getFrontPageOfSubreddit(@PathParam("subredditId") String subredditId) {

        SortedSet<Tuple> topPostsOfSubreddit = RedisCache.getSortedSet(Scores.getSubredditTopCacheKey(subredditId));
        Gson g = new Gson();
        List<Post> topPosts = new LinkedList<>();

        topPostsOfSubreddit.forEach( tuple -> {
            Post post = g.fromJson(tuple.getElement(), Post.class);
            topPosts.add(post);
        });

        return g.toJson(topPosts);
    }

    @GET
    @Path("/{postId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Post getPost(@PathParam("postId") String postId) {
        Document postDoc = Database.getResourceDocById(POST_COL, postId);
        postDoc.set("upvotes", Votes.getVotes(postId, true));
        postDoc.set("downvotes", Votes.getVotes(postId, false));
        return new Gson().fromJson(postDoc.toJson(), Post.class);
    }

    @POST
    @Path("/{postId}/setupvote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String setLike(@PathParam("postId") String postId, String jsonUsername) {
        return Votes.addVote(postId, POST_COL, jsonUsername, true);
    }

    @POST
    @Path("/{postId}/unsetupvote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void unsetLike(@PathParam("postId") String postId, String jsonUsername) {
        Votes.deleteVote(postId, jsonUsername, true);
    }

    @POST
    @Path("/{postId}/setdownvote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String setDislike(@PathParam("postId") String postId, String jsonUsername) {
        return Votes.addVote(postId, POST_COL, jsonUsername, false);
    }

    @POST
    @Path("/{postId}/unsetdownvote")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public void unsetDislike(@PathParam("postId") String postId, String jsonUsername) {
        Votes.deleteVote(postId, jsonUsername, false);
    }
}
