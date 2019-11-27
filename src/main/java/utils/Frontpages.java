package utils;

import api.PostsResource;
import com.microsoft.azure.cosmosdb.Document;
import resources.Post;

import java.util.*;

import static api.PostsResource.POST_COL;

public class Frontpages {

    public static final String FRONTPAGE_POSTS_CACHEKEY = "frontpage:posts";
    public static final String ALL_FRONTPAGE_POSTS_CACHEKEY = "all" + FRONTPAGE_POSTS_CACHEKEY;

    public static List<Post> getSubredditFrontpage(String subredditId) {
        if(AppConfig.IS_CACHE_ON) {
            List<String> postStrings = RedisCache.lrange(subredditId + FRONTPAGE_POSTS_CACHEKEY, 0, -1);

            if(postStrings == null || postStrings.size() == 0) {
                System.out.println("Went to cache for subreddit frontpage");

                List<Post> posts = calcSubredditFrontpage(subredditId);

                String[] newArray = new String[posts.size()];
                Iterator<Post> newIt = posts.iterator();
                int newCounter = 0;

                while(newIt.hasNext())
                    newArray[newCounter++] = newIt.next().toJson().toString();

                RedisCache.removeEntry(subredditId + FRONTPAGE_POSTS_CACHEKEY);

                RedisCache.rpush(subredditId + FRONTPAGE_POSTS_CACHEKEY, newArray);

                RedisCache.setExpireTimeout(subredditId + FRONTPAGE_POSTS_CACHEKEY,
                        AppConfig.FRONTPAGES_UPDATE_PERIOD * 3600);

                return posts;
            } else {
                System.out.println("Went to cache for subreddit frontpage");

                List<Post> posts = new ArrayList<>(postStrings.size());
                postStrings.forEach(post -> posts.add(Post.fromDocument(new Document(post))));
                return posts;
            }
        } else
            return calcSubredditFrontpage(subredditId);
    }

    public static List<Post> calcSubredditFrontpage(String subredditId) {
        String newQuery = "SELECT TOP " + AppConfig.SUBREDDIT_FRONTPAGE_SIZE + " * FROM Posts p " +
                " WHERE p.timestamp >= " + Date.timestampMinusHours(AppConfig.FRONTPAGE_TIME_WINDOW) +
                " AND p.subreddit = '" + subredditId +
                "' AND p.parentPost = ''" +
                " AND p.isArchived = false" +
                " ORDER BY p.score DESC";

        List<Document> topPostsSub = Database.getResourceListDocs(PostsResource.POST_COL, newQuery);

        List<Post> posts = new ArrayList<>(topPostsSub.size());

        topPostsSub.forEach(doc -> posts.add(Post.fromDocument(doc)));

        return posts;
    }

    public static List<Post> getAllFrontpage() {
        if(AppConfig.IS_CACHE_ON) {
            List<String> postStrings = RedisCache.lrange(ALL_FRONTPAGE_POSTS_CACHEKEY, 0, -1);

            if(postStrings == null || postStrings.size() == 0) {
                System.out.println("Went to DB for all frontpage");

                List<Post> posts = calcAllFrontpage();

                if(posts.size() == 0)
                    return posts;

                String[] newArray = new String[posts.size()];
                Iterator<Post> newIt = posts.iterator();
                int newCounter = 0;

                while(newIt.hasNext())
                    newArray[newCounter++] = newIt.next().toJson().toString();

                RedisCache.removeEntry(ALL_FRONTPAGE_POSTS_CACHEKEY);

                RedisCache.rpush(ALL_FRONTPAGE_POSTS_CACHEKEY, newArray);

                RedisCache.setExpireTimeout(ALL_FRONTPAGE_POSTS_CACHEKEY,
                        AppConfig.FRONTPAGES_UPDATE_PERIOD * 3600);

                return posts;
            } else {
                System.out.println("Went to cache for all frontpage");

                List<Post> posts = new ArrayList<>(postStrings.size());
                postStrings.forEach(post -> posts.add(Post.fromDocument(new Document(post))));
                return posts;
            }
        } else
            return calcAllFrontpage();
    }

    public static List<Post> calcAllFrontpage() {
        String query = "SELECT TOP " + AppConfig.ALL_FRONTPAGE_SIZE + " * FROM " + PostsResource.POST_COL + " p" +
                " WHERE p.timestamp >= " + Date.timestampMinusHours(AppConfig.FRONTPAGE_TIME_WINDOW) +
                " AND p.parentPost = ''" +
                " AND p.isArchived = false" +
                " ORDER BY p.score DESC";

        List<Document> topPosts = Database.getResourceListDocs(PostsResource.POST_COL, query);

        List<Post> posts = new ArrayList<>(topPosts.size());

        topPosts.forEach(doc -> posts.add(Post.fromDocument(doc)));

        return posts;
    }

    private static List<Post> executeQueryAndGetPosts(String query) {
        List<Document> docs = Database.getResourceListDocs(POST_COL, query);
        List<Post> posts = new ArrayList<>(docs.size());
        docs.forEach(post -> posts.add(Post.fromDocument(post)));
        return posts;
    }

    public static List<Post> getLatest(String subreddit, int start, int count) {
        String query = "SELECT * FROM " + POST_COL + " p" +
                " WHERE p.subreddit = '" + subreddit +
                "' AND p.isArchived = false" +
                " ORDER BY p.timestamp DESC" +
                " OFFSET " + start + " LIMIT " + count;
        return executeQueryAndGetPosts(query);
    }

    public static List<Post> getLatest(int start, int count) {
        String query = "SELECT * FROM " + POST_COL + " p" +
                " WHERE p.isArchived = false" +
                " ORDER BY p.timestamp DESC" +
                " OFFSET " + start + " LIMIT " + count;
        return executeQueryAndGetPosts(query);
    }
}
