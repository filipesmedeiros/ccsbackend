package functions;

import api.PostsResource;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.CosmosDBTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;
import redis.clients.jedis.Tuple;
import resources.Post;
import resources.Vote;
import utils.*;

import java.util.SortedSet;

public class OnVoteUpdateCache {

    @FunctionName("update_cache_score_on_vote")
    public void updateCacheScoreOnVote(
            @CosmosDBTrigger(name = "votes",
                    databaseName = "",
                    collectionName = "Votes",
                    createLeaseCollectionIfNotExists = true,
                    connectionStringSetting = "AzureCosmosDBConnection") String[] votes,
            final ExecutionContext context) {

        for(String voteString : votes) {
            Vote vote = Vote.fromDocument(new Document(voteString));
            String cachePostScore = vote.getPostId() + ":score";

            Long score = RedisCache.getLong(cachePostScore);
            if(score != null) {
                RedisCache.incrBy(cachePostScore, vote.isUp() ?
                        AppConfig.UPVOTE_SCORE_VALUE :
                        AppConfig.DOWNVOTE_SCORE_VALUE);

                score += vote.isUp() ?
                        AppConfig.UPVOTE_SCORE_VALUE :
                        AppConfig.DOWNVOTE_SCORE_VALUE;
            }

            String voteCacheKey = vote.getPostId() + (vote.isUp() ? ":upvotes" : ":downvotes");
            Long votesInCache = RedisCache.getLong(voteCacheKey);
            if(votesInCache != null)
                RedisCache.incr(voteCacheKey);

            Post post = Post.fromDocument(Database.getResourceDocById(PostsResource.POST_COL, vote.getPostId()));

            String postStringFromCache = RedisCache.get(post.getId());
            if(postStringFromCache != null) {
                Post postFromCache = Post.fromDocument(new Document(postStringFromCache));
                post.setScore(score);
            }

            SortedSet<Tuple> topInCache = RedisCache.getSortedSet(vot);
        }
    }
}
