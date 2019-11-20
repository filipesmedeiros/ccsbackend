package functions;

import api.PostsResource;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import utils.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateScoresOnDB {

    @FunctionName("update_scores_on_db")
    public void updateScoresOnDB(@TimerTrigger(name = "keepAliveTrigger", schedule = "* */10 * * * *") String timerInfo,
                                    ExecutionContext context) {

        // TODO Group By???

        long window = Date.timestampMinusMinutes(AppConfig.SCORE_UPDATE_PERIOD);

        String query = "SELECT * FROM " + Votes.VOTE_COL + " v WHERE v.timestamp >= " + window;
        List<Document> voteDocs = Database.getResourceListDocs(Votes.VOTE_COL, query);

        query = "SELECT c.id, c.rootPost FROM " + PostsResource.POST_COL + " c " +
                "WHERE c.timestamp >= " + window + " AND c.rootPost != ''";
        List<Document> commentDocs = Database.getResourceListDocs(PostsResource.POST_COL, query);

        int maxNumOfPosts = Math.max(voteDocs.size(), commentDocs.size());
        Map<String, Long> postScores = new HashMap<>(maxNumOfPosts);
        Map<String, Boolean> isInCache = new HashMap<>(maxNumOfPosts);

        voteDocs.forEach(voteDoc -> {
            String postId = voteDoc.getString("submissionId");
            Boolean isInCachePost = isInCache.get(postId);
            if(isInCachePost == null)
                isInCache.put(postId, RedisCache.entryExists(postId + ":score"));
            if(isInCachePost != null && isInCachePost)
                return;

            Boolean up = voteDoc.getBoolean("up");
            postScores.putIfAbsent(postId, 0L);

            Long score = postScores.get(postId);
            score += up ? AppConfig.UPVOTE_SCORE_VALUE : AppConfig.DOWNVOTE_SCORE_VALUE;

            postScores.put(postId, score);
        });

        commentDocs.forEach(commentDoc -> {
            String postId = commentDoc.getString("rootPost");
            Boolean isInCachePost = isInCache.get(postId);
            if(isInCachePost == null)
                isInCache.put(postId, RedisCache.entryExists(postId + ":score"));
            if(isInCachePost != null && isInCachePost)
                return;

            postScores.putIfAbsent(postId, 0L);

            Long score = postScores.get(postId);
            score += AppConfig.COMMENT_SCORE_VALUE;
            postScores.put(postId, score);
        });

        postScores.forEach((postId, score) -> {
            Boolean isInCachePost = isInCache.get(postId);
            if(isInCachePost != null && !isInCachePost) {
                Document postDoc = Database.getResourceDocById(PostsResource.POST_COL, postId);
                long oldScore = postDoc.getLong("score");
                postDoc.set("score", score + oldScore);
                Database.replaceDocument(postDoc);
            }
        });
    }
}
