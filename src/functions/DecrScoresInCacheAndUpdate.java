package functions;

import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import redis.clients.jedis.Tuple;
import resources.Post;
import utils.AppConfig;
import utils.RedisCache;
import utils.Scores;

import java.util.List;
import java.util.SortedSet;

public class DecrScoresInCacheAndUpdate {

    @FunctionName("decr_scores_in_cache")
    public void decrScoresInCacheAndUpdate(
            @TimerTrigger(name = "keepAliveTrigger", schedule = "* */30 * * * *") String timerInfo,
            ExecutionContext context) {

        SortedSet<Tuple> sortedSet = RedisCache.getSortedSet(Scores.TOP_SUBREDDITS);
        if(sortedSet == null)
            return;

        sortedSet.forEach(tuple -> {
            String subredditId = new Document(tuple.getElement()).getId();
            String subredditTopPostsKey = Scores.getSubredditTopCacheKey(subredditId);

            SortedSet<Tuple> topPostsInCache = RedisCache.getSortedSet(subredditTopPostsKey);

            if(topPostsInCache == null)
                return;

            topPostsInCache.forEach(postTuple -> {
                double whatToLose = postTuple.getScore() - (postTuple.getScore() * AppConfig.SCORE_LOST_PER_PERIOD);
                RedisCache.sortedSetIncr(subredditTopPostsKey, whatToLose, postTuple.getElement());
            });

            List<Post> topPosts = Scores.calcTopPostsOfSubredditOnDB(subredditId);
            for(Post post : topPosts) {
                if(post.getScore() <= topPostsInCache.last().getScore())
                    break;

                RedisCache.addToSortedSet(subredditTopPostsKey, post.getScore(), post.toDocument().toJson());
                if(topPostsInCache.size() + 1 > AppConfig.SUBREDDIT_FRONTPAGE_SIZE)
                    RedisCache.popLastFromSortedSet(subredditTopPostsKey);
            }
        });
    }
}
