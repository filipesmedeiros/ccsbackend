package utils;

import api.PostsResource;
import api.SubredditsResource;
import com.google.gson.Gson;
import com.microsoft.azure.cosmosdb.Document;
import redis.clients.jedis.Tuple;
import resources.Post;
import resources.Subreddit;

import java.util.*;

public class Scores {

    public static final String TOP_SUBREDDITS = "topsubreddits";
    public static final String RALL_FRONTPAGE_POSTS = "rallfrontpageposts";

    private static class SubredditWithTopPostList {
        private Subreddit subreddit;
        private List<Post> topPosts;

        public SubredditWithTopPostList(Subreddit subreddit, List<Post> topPosts) {
            this.subreddit = subreddit;
            this.topPosts = topPosts;
        }
    }

    public static String getRallFrontpage() {
        String s = RedisCache.get(RALL_FRONTPAGE_POSTS);
        if(s == null)
            return new Gson().toJson(calcAndPutRallFrontpageInCache());

        return s;
    }

    public static List<Post> calcAndPutRallFrontpageInCache() {
        // TODO optimize query
        String query = "SELECT * FROM " + SubredditsResource.SUBREDDIT_COL + " s " +
                " ORDER BY s.score DESC " +
                "OFFSET 0 LIMIT " + AppConfig.NUMBER_TOP_SUBREDDITS;
        List<Document> topSubsDocs = Database.getResourceListDocs(SubredditsResource.SUBREDDIT_COL, query);

        List<Post> rallFrontpagePosts = new ArrayList<>(AppConfig.ALL_FRONTPAGE_SIZE);

        SortedSet<SubredditWithTopPostList> topSubredditsPosts = new TreeSet<>(
                Comparator.comparingLong(sub -> sub.subreddit.getScore()));

        topSubsDocs.forEach(subDoc -> {
            Subreddit subreddit = Subreddit.fromDocument(subDoc);
            List<Post> topPosts = getTopPostsOfSubreddit(subreddit.getId());
            topSubredditsPosts.add(new SubredditWithTopPostList(subreddit, topPosts));

            RedisCache.addToSortedSet(TOP_SUBREDDITS, subreddit.getScore(), subDoc.toJson(),
                    AppConfig.EXPIRE_TIMEOUT_TOP);

            topPosts.forEach(post -> RedisCache.addToSortedSet(getSubredditTopCacheKey(subreddit.getId()),
                    post.getScore(), post.toDocument().toJson()));
        });

        while(rallFrontpagePosts.size() < AppConfig.ALL_FRONTPAGE_SIZE) {
            SubredditWithTopPostList nextSub = topSubredditsPosts.first();

            if(nextSub.topPosts.size() == 0)
                break;

            Post addedPost = nextSub.topPosts.remove(0);
            rallFrontpagePosts.add(addedPost);
            topSubredditsPosts.remove(nextSub);
            nextSub.subreddit.decrScore(addedPost.getScore());
            topSubredditsPosts.add(nextSub);
        }

        RedisCache.set(RALL_FRONTPAGE_POSTS, new Gson().toJson(rallFrontpagePosts));

        return rallFrontpagePosts;
    }

    public static boolean addPostOfSubredditToCacheIfTop(Post post) {
        String entryKey = getSubredditTopCacheKey(post.getSubreddit());
        SortedSet<Tuple> topPostsOfSubreddit = RedisCache.getSortedSet(entryKey);

        if(topPostsOfSubreddit == null || topPostsOfSubreddit.size() < AppConfig.SUBREDDIT_FRONTPAGE_SIZE) {
            RedisCache.addToSortedSet(entryKey, post.getScore(), post.toDocument().toJson());
            return true;
        }

        if(post.getScore() > topPostsOfSubreddit.last().getScore()) {
            RedisCache.addToSortedSet(TOP_SUBREDDITS, post.getScore(), post.toDocument().toJson());
            if(topPostsOfSubreddit.size() + 1 > AppConfig.NUMBER_TOP_SUBREDDITS) {
                RedisCache.popLastFromSortedSet(TOP_SUBREDDITS);
            }
            return true;
        }

        return false;
    }

    public static boolean removeSubredditTopPosts(String subredditId) {
        return RedisCache.removeEntry(getSubredditTopCacheKey(subredditId));
    }

    public static void updateTopPostsOfSubredditOnCache(String subredditId) {
        List<Post> topSubredditPosts = calcTopPostsOfSubredditOnDB(subredditId);
        topSubredditPosts.forEach(Scores::addPostOfSubredditToCacheIfTop);
    }

    public static boolean calcAndAddTopPostsOfSubredditToCache(String subredditId) {
        List<Post> topSubredditPosts = calcTopPostsOfSubredditOnDB(subredditId);
        topSubredditPosts.forEach(post ->
                RedisCache.addToSortedSet(getSubredditTopCacheKey(subredditId), post.getScore(),
                        post.toDocument().toJson()));

        return true;
    }

    public static boolean addSubbreditToCacheIfTop(Subreddit subreddit) {
        SortedSet<Tuple> topSubreddits = RedisCache.getSortedSet(TOP_SUBREDDITS);
        if(topSubreddits == null || topSubreddits.size() < AppConfig.NUMBER_TOP_SUBREDDITS) {
            RedisCache.addToSortedSet(TOP_SUBREDDITS, subreddit.getScore(), subreddit.toDocument().toJson());
            return true;
        }

        if(subreddit.getScore() > topSubreddits.last().getScore()) {
            RedisCache.addToSortedSet(TOP_SUBREDDITS, subreddit.getScore(), subreddit.toDocument().toJson());
            if(topSubreddits.size() + 1 > AppConfig.NUMBER_TOP_SUBREDDITS) {
                String subredditAsString = RedisCache.popLastFromSortedSet(TOP_SUBREDDITS);
                if(subredditAsString == null)
                    return true;

                Subreddit removedSubreddit = Subreddit.fromDocument(new Document(subredditAsString));
                RedisCache.setExpireTimeout(getSubredditTopCacheKey(removedSubreddit.getId()),
                        AppConfig.EXPIRE_TIMEOUT_OLD_TOP_SUBREDDIT);
            }

            String entryKey = getSubredditTopCacheKey(subreddit.getId());
            if(RedisCache.entryExists(entryKey))
                calcAndAddTopPostsOfSubredditToCache(subreddit.getId());

            return true;
        }

        return false;
    }

    public static boolean isTopSubreddit(String subredditId) {
        SortedSet<Tuple> topSubreddits = RedisCache.getSortedSet(TOP_SUBREDDITS);
        if(topSubreddits == null || topSubreddits.size() < AppConfig.NUMBER_TOP_SUBREDDITS)
            return true;
        long subredditScore = getSubredditScore(subredditId);
        return subredditScore > topSubreddits.last().getScore();
    }

    public static boolean isTopSubreddit(Subreddit subreddit, SortedSet<Tuple> topSubreddits) {
        if(!RedisCache.entryExists(TOP_SUBREDDITS))
            return true;
        if(topSubreddits.size() < AppConfig.NUMBER_TOP_SUBREDDITS)
            return true;
        return subreddit.getScore() > topSubreddits.last().getScore();
    }

    public static List<Post> getTopPostsOfSubreddit(String subredit) {
        SortedSet<Tuple> topPosts = RedisCache.getSortedSet(getSubredditTopCacheKey(subredit));

        if(topPosts != null) {
            List<Post> topPostList = new ArrayList<>(topPosts.size());
            for(Tuple t : topPosts)
                topPostList.add(Post.fromDocument(new Document(t.getElement())));
            return topPostList;
        } else
            return calcTopPostsOfSubredditOnDB(subredit);
    }

    public static List<Post> calcTopPostsOfSubredditOnDB(String subreddit) {
        // TODO optimize query
        String query = "SELECT * FROM " + PostsResource.POST_COL + " p WHERE p.subreddit = '" + subreddit + "' AND " +
                "p.timestamp >= " + Date.timestampMinusHours(AppConfig.FRONTPAGE_TIME_WINDOW) +
                " ORDER BY p.score DESC " +
                "OFFSET 0 LIMIT " + AppConfig.SUBREDDIT_FRONTPAGE_SIZE;
        List<Document> results = Database.getResourceListDocs(PostsResource.POST_COL, query);
        List<Post> topPosts = new ArrayList<>(results.size());
        for(Document result : results)
            topPosts.add(Post.fromDocument(result));
        return topPosts;
    }

    public static long calcPostScore(String postId) {
        Document postDoc = Database.getResourceDocById(PostsResource.POST_COL, postId);
        Post post = Post.fromDocument(postDoc);
        long score = post.getScore();
        RedisCache.getOrSetLong(postId + ":score", score);
        return score;
    }

    public static long getPostScore(String postId) {
        Long score = RedisCache.getLong(postId + ":score");
        if(score == null)
            return calcPostScore(postId);
        return score;
    }

    public static long getSubredditScore(String subredditId) {
        Long score = RedisCache.getLong(subredditId + ":score");
        if(score == null) {
            Document subredditDoc = Database.getResourceDocById(SubredditsResource.SUBREDDIT_COL, subredditId);
            score = subredditDoc.getLong("score");
            RedisCache.getOrSetLong(subredditId + ":score", score);
        }
        return score;
    }

    /*
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
        long otherVoteCount = Votes.countVotesOnDB(postId, !alreadyHaveThisCount);

        long score = alreadyHaveThisCount ?
                count - (long) (otherVoteCount * 1.2) :
                otherVoteCount - (long) (count * 1.2);

        long commentCount = Posts.countCommentsOnPost(postId);
        score += commentCount * 2;

        return score;
    }

    public static long calcPostScoreOnDB(String postId) {
        long upvotes = Votes.countVotesOnDB(postId, true);
        return calcPostScoreOnDB(postId, true, upvotes);
    }
    */

    public static String getSubredditTopCacheKey(String subreddit) {
        return subreddit + ":topposts";
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
