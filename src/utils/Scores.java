package utils;

import api.PostsResource;
import com.google.gson.Gson;
import com.microsoft.azure.cosmosdb.Document;
import redis.clients.jedis.Tuple;
import resources.Post;

import javax.print.Doc;
import java.util.*;

import static utils.Database.getResourceListDocs;
import static utils.RedisCache.getSortedSet;

public class Scores {

    public static long getSubredditScore() {
        if()
    }

    public static long calcSubredditScoreOnDB(String subreddit) {
        List<TopPostInCacheWithScore> topPosts = calcTopPostsOfSubredditOnDB(subreddit);
        long subredditScore = 0;
        for(TopPostInCacheWithScore post : topPosts)
            subredditScore += post.score;
        return subredditScore;
    }

    public static List<TopPostInCacheWithScore> calcTopPostsOfSubredditOnDB(String subreddit) {
        // TODO optimize query
        String query = "SELECT * FROM " + PostsResource.POST_COL + " p WHERE p.subreddit = '" + subreddit + "' AND " +
                "p.timestamp >= " + Date.timestampMinusHours(3);
        List<Document> results = Database.getResourceListDocs(PostsResource.POST_COL, query);

        SortedSet<TopPostInCacheWithScore> sortedPosts = new TreeSet<>(Comparator.comparingLong(post -> post.score));

        results.forEach(postDoc -> {
            String postId = postDoc.getId();
            Long score = getPostScore(postId).score;
            sortedPosts.add(new TopPostInCacheWithScore(new TopPostInCache(Post.fromDocument(postDoc),
                    System.currentTimeMillis()), score));
        });

        List<TopPostInCacheWithScore> frontPage = new LinkedList<>();
        Iterator<TopPostInCacheWithScore> iterator = sortedPosts.iterator();
        int counter = AppConfig.SUBREDDIT_FRONTPAGE_SIZE * 2;
        while(iterator.hasNext())
            if(counter-- > 0) // TODO >=?
                frontPage.add(iterator.next());
            else
                break;

        return frontPage;
    }

    public static ScoreWithSource getPostScore(String postId) {
        // TODO how to do this better?
        Document postDoc = Database.getResourceDocById(PostsResource.POST_COL, postId);
        String subreddit = postDoc.getString("subreddit");

        Set<Tuple> topPostsOfSubreddit = RedisCache.getSortedSet(getSubredditTopCacheKey(subreddit));
        for(Tuple post : topPostsOfSubreddit) {
            TopPostInCache formattedPost = new Gson().fromJson(post.getElement(), TopPostInCache.class);
            if(formattedPost.post.getId().equals(postId))
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

        private Post post;
        private long cacheTimestamp;

        public TopPostInCache(Post post, long cacheTimestamp) {
            this.post = post;
            this.cacheTimestamp = cacheTimestamp;
        }

        public TopPostInCache() {

        }
    }

    public static class TopPostInCacheWithScore {

        private TopPostInCache post;
        private long score;

        public TopPostInCacheWithScore(TopPostInCache post, long score) {
            this.post = post;
            this.score = score;
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
