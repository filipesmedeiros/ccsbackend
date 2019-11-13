package utils;

import api.PostsResource;
import com.google.gson.Gson;
import com.microsoft.azure.cosmosdb.Document;
import redis.clients.jedis.Tuple;

import java.util.Set;

import static utils.RedisCache.getSortedSet;

public class Scores {

    public static ScoreWithSource getPostScore(String postId) {
        // TODO how to do this better?
        Document postDoc = Database.getResourceDocById(PostsResource.POST_COL, postId);
        String subreddit = postDoc.getString("subreddit");

        Set<Tuple> topPostsOfSubreddit = RedisCache.getSortedSet(getSubredditTopCacheKey(subreddit));
        for(Tuple post : topPostsOfSubreddit) {
            TopPostInCache formattedPost = new Gson().fromJson(post.getElement(), TopPostInCache.class);
            if(formattedPost.getId().equals(postId))
                return new ScoreWithSource((long) post.getScore(), true); // TODO cast to long???
        }

        return new ScoreWithSource(calcPostScoreOnDB(postId), false);
    }

    public static long calcPostScoreOnDB(String postId, boolean alreadyHaveThisCount, long count) {
        long otherVoteCount = Votes.countVotesOnBD(postId, !alreadyHaveThisCount);

        long score = alreadyHaveThisCount ?
                count - (long) (otherVoteCount * 1.2) :
                otherVoteCount - (long) (count * 1.2);

        long commentCount = Posts.countCommentsOnPost(postId);
        score += commentCount * 2;

        return score;
    }

    public static long calcPostScoreOnDB(String postId) {
        long upvotes = Votes.countVotesOnBD(postId, true);
        return calcPostScoreOnDB(postId, true, upvotes);
    }

    public static String getSubredditTopCacheKey(String subreddit) {
        return subreddit + ":topposts";
    }

    private static class TopPostInCache {

        private String id;
        private long timestamp, cacheTimestamp;

        public TopPostInCache(String id, long timestamp, long cacheTimestamp) {
            this.id = id;
            this.timestamp = timestamp;
            this.cacheTimestamp = cacheTimestamp;
        }

        public TopPostInCache() {

        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public long getCacheTimestamp() {
            return cacheTimestamp;
        }

        public void setCacheTimestamp(long cacheTimestamp) {
            this.cacheTimestamp = cacheTimestamp;
        }
    }

    public static class ScoreWithSource {

        public long score;
        public boolean fromCache;

        public ScoreWithSource(long score, boolean fromCache) {
            this.score = score;
            this.fromCache = fromCache;
        }
    }
}
