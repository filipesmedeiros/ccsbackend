package utils;

import com.microsoft.azure.cosmosdb.Document;
import resources.Post;
import resources.PostThread;

import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;

import static api.PostsResource.POST_COL;
import static utils.Frontpages.ALL_FRONTPAGE_POSTS_CACHEKEY;

public class Posts {

    public static Post getPost(String postId) {
        if(AppConfig.IS_CACHE_ON) {
            String post = RedisCache.get(postId + ":post");
            Long postScore = RedisCache.getLong(postId + ":score");

            if(post == null || postScore == null) {
                Document postDoc = Database.getResourceDocById(POST_COL, postId);

                if(postDoc.getBoolean("isArchived"))
                    throw new NotFoundException("No post with that id was found");

                RedisCache.set(postId + ":post", postDoc.toJson());
                RedisCache.set(postId + ":score", postDoc.getLong("score").toString());
                RedisCache.setExpireTimeout(postId + ":post", AppConfig.POST_AND_THREAD_CACHE_TIMEOUT);
                RedisCache.setExpireTimeout(postId + ":score", AppConfig.POST_AND_THREAD_CACHE_TIMEOUT);
                return Post.fromDocument(postDoc);
            } else {
                Post postFinal = Post.fromDocument(new Document(post));
                postFinal.setScore(postScore);
                return postFinal;
            }
        }
        Post p = Post.fromDocument(Database.getResourceDocById(POST_COL, postId));
        if(p.isArchived())
            throw new NotFoundException("No post with that id was found");
        return p;
    }

    public static List<Post> postChildren(String postId) {
        String query = "SELECT * FROM " + POST_COL +
                " c WHERE c.parentPost = '" + postId +
                "' AND c.isArchived = false" +
                " ORDER BY c.score DESC";

        List<Document> commentDocs = Database.getResourceListDocs(POST_COL, query);

        List<Post> topComments = new ArrayList<>(commentDocs.size());
        for(Document result : commentDocs)
            topComments.add(Post.fromDocument(result));

        return topComments;
    }

    public static List<Post> topPostChildren(String postId, int count) {
        String query = "SELECT * FROM " + POST_COL +
                " c WHERE c.parentPost = '" + postId +
                "' AND c.isArchived = false" +
                " ORDER BY c.score DESC" +
                " OFFSET 0 LIMIT " + count;

        List<Document> commentDocs = Database.getResourceListDocs(POST_COL, query);

        List<Post> topComments = new ArrayList<>(commentDocs.size());
        for(Document result : commentDocs)
            topComments.add(Post.fromDocument(result));

        return topComments;
    }

    public static long countCommentsOnPost(String postId) {
        String query = "SELECT VALUE COUNT(1) as commentCount" +
                " FROM " + POST_COL +
                " c WHERE c.rootPost = '" + postId +
                "' AND c.isArchived = false";

        Document doc = Database.count(POST_COL, query);
        return (Long) doc.get("commentCount");
    }

    public static PostThread calcPostThread(String postId) {
        Post root = getPost(postId);
        PostThread thread = new PostThread(root);

        List<Post> firstComments = topPostChildren(postId, AppConfig.NUMBER_CHILDREN_POSTS);

        firstComments.forEach(post -> {
            thread.addFirstChildren(post);
            List<Post> secondComments = topPostChildren(post.getId(), AppConfig.NUMBER_CHILDREN_COMMENTS);

            secondComments.forEach(comment -> thread.addSecondChildren(post, comment));
        });

        return thread;
    }

    public static PostThread getPostThread(String postId) {
        if(AppConfig.IS_CACHE_ON) {
            String thread = RedisCache.get(postId + ":thread");

            if(thread == null) {
                PostThread postThread = calcPostThread(postId);
                RedisCache.set(postId + ":thread", postThread.toJson());
                RedisCache.setExpireTimeout(postId + ":thread", AppConfig.POST_AND_THREAD_CACHE_TIMEOUT);
                return postThread;
            } else {
                PostThread postThread = PostThread.fromJson(thread);
                return postThread.cleanArchived();
            }
        } else
            return calcPostThread(postId);
    }

    public static void archivePost(String postId) {
        Document postDoc = Database.getResourceDocById(POST_COL, postId);
        if(postDoc.getBoolean("isArchived"))
            throw new NotFoundException("No post with that id was found");

        postDoc.set("isArchived", true);

        Database.putResourceOverwrite(postDoc, POST_COL);

        RedisCache.lrem(ALL_FRONTPAGE_POSTS_CACHEKEY, 0, postDoc.toJson()); // Remove all (shouldn't be more than 1)

        RedisCache.removeEntry(postId + ":post");
        RedisCache.removeEntry(postId + ":score");
        RedisCache.removeEntry(postId + ":thread");
    }
}
