package functions;

import api.PostsResource;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import resources.Post;
import resources.Vote;
import utils.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdatePostScoresOnCache {

    @FunctionName("update_post_scores_on_cache")
    public void updatePostScoresOnCache(@TimerTrigger(name = "keepAliveTrigger", schedule = "* */30 * * * *") String timerInfo,
                                 ExecutionContext context) {

        String query = "SELECT * FROM " + Votes.VOTE_COL + " v" +
                " WHERE v.timestamp >= " + Date.timestampMinusMinutes(30);

        List<Document> timeWindowVotes = Database.getResourceListDocs(Votes.VOTE_COL, query);

        Map<String, Long> accScore = new HashMap<>(timeWindowVotes.size());

        for(Document voteDoc : timeWindowVotes) {
            Vote vote = Vote.fromDocument(voteDoc);

            if(RedisCache.get(vote.getPostId() + ":score") == null)
                continue;

            Long score = accScore.get(vote.getPostId());

            if (score == null)
                accScore.put(vote.getPostId(), vote.isUp() ? AppConfig.UPVOTE_SCORE_VALUE : AppConfig.DOWNVOTE_SCORE_VALUE);
            else {
                score += vote.isUp() ? AppConfig.UPVOTE_SCORE_VALUE : AppConfig.DOWNVOTE_SCORE_VALUE;
                accScore.put(vote.getPostId(), score);
            }
        }

        query = "SELECT VALUE COUNT(1) AS commentCount, c.rootPost FROM " + PostsResource.POST_COL + " c" +
                " WHERE c.timestamp >= " + Date.timestampMinusMinutes(30) +
                " AND c.parentPost != ''" +
                " GROUP BY c.rootPost";

        Document commentCount = Database.getResourceDoc(PostsResource.POST_COL, query);

        System.out.println(commentCount.toJson());

        List<JsonObject> commentCounts = new Gson().fromJson(commentCount.toJson(), List.class);
        for(JsonObject count : commentCounts) {
            String rootPostId = count.get("rootPost").getAsString();

            if(RedisCache.get(rootPostId + ":score") == null)
                continue;

            Long score = accScore.get(rootPostId);

            if (score == null)
                accScore.put(rootPostId, AppConfig.COMMENT_SCORE_VALUE);
            else {
                score += AppConfig.COMMENT_SCORE_VALUE;
                accScore.put(rootPostId, score);
            }
        }

        accScore.forEach((postId, newScore) -> {
            if(RedisCache.get(postId + ":score") == null)
                return;

            RedisCache.incrBy(postId + ":score", accScore.get(postId));
        });
    }
}
