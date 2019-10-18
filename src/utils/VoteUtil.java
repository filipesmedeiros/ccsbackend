package utils;

import com.google.gson.Gson;
import com.microsoft.azure.cosmosdb.Document;
import resources.Vote;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

public class VoteUtil {

    public static final String VOTE_COL = "Votes";

    public static String addVote(String submissionId, String col, String jsonUsername, boolean up, boolean isPost) {
        Gson gson = new Gson();
        Document usernameDoc = new Document(jsonUsername);
        String userId = usernameDoc.getId();
        if(!Database.resourceExists(col, userId))
            throw new BadRequestException("The author of the vote does not exist.");

        if(!Database.resourceExists(col, submissionId))
            throw new NotFoundException("No comment with that id was found");

        String commentVoteId = Vote.generateId(submissionId, userId);

        Vote newVote = new Vote(commentVoteId, up, submissionId, userId, isPost);

        try {
            Vote previousPostVote =
                    gson.fromJson(Database.getResourceDocById(VOTE_COL, commentVoteId).toJson(), Vote.class);
            if(previousPostVote.isUp() != up) {
                return Database.putResourceOverwrite(newVote.toDocument(), VOTE_COL).getId();
            }
        } catch(NotFoundException nfe) {
            return Database.putResourceOverwrite(newVote.toDocument(), VOTE_COL).getId();
        }
        throw new BadRequestException("User already has upvote on this post.");
    }

    public static void deleteVote(String submissionId, String jsonUsername, boolean up) {
        Document usernameDoc = new Document(jsonUsername);
        String userId = usernameDoc.getId();
        String voteId = Vote.generateId(submissionId, userId);

        if((boolean) Database.getResourceDocById(VOTE_COL, voteId).get("up") != up)
            throw new BadRequestException();

        Database.deleteResource(VOTE_COL, voteId);
    }
}
