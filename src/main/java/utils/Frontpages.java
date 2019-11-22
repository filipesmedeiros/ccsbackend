package utils;

import api.PostsResource;
import com.microsoft.azure.cosmosdb.Document;
import resources.Post;

import java.util.*;

public class Frontpages {

    public static List<Post> getSubredditFrontpage(String subredditId) {
        if(AppConfig.IS_CACHE_ON) {
            List<String> postStrings = RedisCache.lrange(subredditId + "frontpage:posts", 0, -1);

            if(postStrings == null || postStrings.size() == 0) {
                System.out.println("Went to cache for subreddit frontpage");

                List<Post> posts = calcSubredditFrontpage(subredditId);

                String[] newArray = new String[posts.size()];
                Iterator<Post> newIt = posts.iterator();
                int newCounter = 0;

                while(newIt.hasNext())
                    newArray[newCounter++] = newIt.next().toJson().toString();

                if(RedisCache.entryExists(subredditId + "frontpage:posts"))
                    RedisCache.removeEntry(subredditId + "frontpage:posts");

                RedisCache.rpush(subredditId + "frontpage:posts", newArray);

                RedisCache.setExpireTimeout(subredditId + "frontpage:posts",
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
                " WHERE p.subreddit = " + subredditId +
                " AND p.parentPost = ''" +
                " ORDER BY p.score DESC";

        List<Document> topPostsSub = Database.getResourceListDocs(PostsResource.POST_COL, newQuery);

        List<Post> posts = new ArrayList<>(topPostsSub.size());

        topPostsSub.forEach(doc -> posts.add(Post.fromDocument(doc)));

        return posts;
    }

    public static List<Post> getAllFrontpage() {
        if(AppConfig.IS_CACHE_ON) {
            List<String> postStrings = RedisCache.lrange("allfrontpage:posts", 0, -1);

            if(postStrings == null || postStrings.size() == 0) {
                System.out.println("Went to DB for all frontpage");

                List<Post> posts = calcAllFrontpage();

                String[] newArray = new String[posts.size()];
                Iterator<Post> newIt = posts.iterator();
                int newCounter = 0;

                while(newIt.hasNext())
                    newArray[newCounter++] = newIt.next().toJson().toString();

                if(RedisCache.entryExists("allfrontpage:posts"))
                    RedisCache.removeEntry("allfrontpage:posts");

                RedisCache.rpush("allfrontpage:posts", newArray);

                RedisCache.setExpireTimeout("allfrontpage:posts",
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
                " ORDER BY p.score DESC";

        List<Document> topPosts = Database.getResourceListDocs(PostsResource.POST_COL, query);

        List<Post> posts = new ArrayList<>(topPosts.size());

        topPosts.forEach(doc -> posts.add(Post.fromDocument(doc)));

        return posts;
    }
}
