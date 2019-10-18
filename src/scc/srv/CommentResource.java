package scc.srv;

import com.google.gson.Gson;
import com.microsoft.azure.cosmosdb.Document;
import resources.Comment;
import utils.Database;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Date;

@Path("/comments")
public class CommentResource {
    public static final String COMMENTS_COL = "Comments";

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String addComment(String jsonComment) {
        Document commentDoc = new Document(jsonComment);
        if(!Database.testClientJsonWithDoc(commentDoc, Comment.CommentDTOInitialAttributes.class))
            throw new BadRequestException();

        String username = commentDoc.get("opUsername").toString();
        String post = commentDoc.get("post").toString();
        String parent = commentDoc.get("parent").toString();
        if(!Database.resourceExists(UsersResource.USERS_COL, username))
            throw new BadRequestException("The author of the comment does not exist.");
        if(!Database.resourceExists(PostsResource.POST_COL, post))
            throw new BadRequestException("The post does not exist.");
        if(!Database.resourceExists(CommentResource.COMMENTS_COL, parent))
            throw new BadRequestException("The parent comment does not exist.");
        commentDoc.set("creationDate", new Date().getTime());
        return Database.createResourceIfNotExists(commentDoc, COMMENTS_COL, true).getId();
    }

    @GET
    @Path("/{commentId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Comment getComment(@PathParam("commentId") String commentId) {
        Document commentDoc = Database.getResourceDocById(COMMENTS_COL, commentId);
        return new Gson().fromJson(commentDoc.toJson(), Comment.class);
    }
}
