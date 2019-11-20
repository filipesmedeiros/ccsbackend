package utils;

import com.google.gson.Gson;
import com.microsoft.azure.cosmosdb.Document;
import resources.Vote;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;

public class Votes {

    public static final String VOTE_COL = "Votes";

    public static String addVote(String postId, String col, String voteData, boolean up) {
        Gson gson = new Gson();
        Document usernameDoc = new Document(voteData);
        String userId = usernameDoc.getString("username");
        String subreddit = usernameDoc.getString("subredditId");
        if(!Database.resourceExists(col, userId))
            throw new BadRequestException("The author of the vote does not exist.");

        if(!Database.resourceExists(col, postId))
            throw new NotFoundException("No comment with that id was found");

        String postVoteId = Vote.generateId(postId, userId);

        Vote newVote = new Vote(postVoteId, up, postId, userId, subreddit);

        try {
            Vote previousPostVote =
                    gson.fromJson(Database.getResourceDocById(VOTE_COL, postVoteId).toJson(), Vote.class);
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

    // TODO Prints to debug before deploy
    public static Long getVotes(String postId, boolean up) {
        String cacheKey = getCacheKey(postId, up);
        Long voteCount = RedisCache.getLong(cacheKey);
        if(voteCount == null) {
            voteCount = countVotesOnDB(postId, up);
            RedisCache.newCounter(cacheKey, voteCount);
        }
        return voteCount;
    }

    public static Long countVotesOnDB(String postId, boolean up) {
        Document doc = Database.count(VOTE_COL, "SELECT VALUE COUNT(1) as voteCount FROM " + VOTE_COL +
                " v WHERE v.submissionId = '" + postId + "' AND v.up = " + up);
        return (Long) doc.get("voteCount");
    }

    public static String getCacheKey(String postId, boolean up) {
        return postId + (up ? ":upvotes" : ":downvotes");
    }
}
