package functions;

import api.PostsResource;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import resources.Vote;
import utils.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateSubredditScoresOnCache {

    @FunctionName("update_subreddit_scores_on_cache")
    public void updateSubredditScoresOnCache(@TimerTrigger(name = "keepAliveTrigger",
            schedule = "0 0 */" + AppConfig.SUBREDDIT_SCORE_UPDATE_PERIOD_ON_CACHE + " * * *") String timerInfo,
                                 ExecutionContext context) {

        String query = "SELECT * FROM " + Votes.VOTE_COL + " v" +
                " WHERE v.timestamp >= " + Date.timestampMinusHours(3);

        List<Document> timeWindowVotes = Database.getResourceListDocs(Votes.VOTE_COL, query);

        Map<String, Long> accScore = new HashMap<>(timeWindowVotes.size());

        for(Document voteDoc : timeWindowVotes) {
            Vote vote = Vote.fromDocument(voteDoc);

            if(RedisCache.get(vote.getSubredditId() + ":score") == null)
                continue;

            Long score = accScore.get(vote.getSubredditId());

            if (score == null)
                accScore.put(vote.getSubredditId(), vote.isUp() ? AppConfig.UPVOTE_SCORE_VALUE : AppConfig.DOWNVOTE_SCORE_VALUE);
            else {
                score += vote.isUp() ? AppConfig.UPVOTE_SCORE_VALUE : AppConfig.DOWNVOTE_SCORE_VALUE;
                accScore.put(vote.getSubredditId(), score);
            }
        }

        query = "SELECT VALUE COUNT(1) AS commentCount, c.subredditId FROM " + PostsResource.POST_COL + " c" +
                " WHERE c.timestamp >= " + Date.timestampMinusHours(3) +
                " GROUP BY c.subredditId";

        Document commentCount = Database.getResourceDoc(PostsResource.POST_COL, query);

        System.out.println(commentCount.toJson());

        List<JsonObject> commentCounts = new Gson().fromJson(commentCount.toJson(), List.class);
        for(JsonObject count : commentCounts) {
            String subredditId = count.get("subredditId").getAsString();

            if(RedisCache.get(subredditId + ":score") == null)
                continue;

            Long score = accScore.get(subredditId);

            if (score == null)
                accScore.put(subredditId, AppConfig.COMMENT_SCORE_VALUE);
            else {
                score += AppConfig.COMMENT_SCORE_VALUE;
                accScore.put(subredditId, score);
            }
        }

        accScore.forEach((subredditId, newScore) -> {
            if(RedisCache.get(subredditId + ":score") == null)
                return;

            RedisCache.incrBy(subredditId + ":score", accScore.get(subredditId));
        });
    }
}
