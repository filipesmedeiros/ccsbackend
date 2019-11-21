package utils;

import com.microsoft.azure.cosmosdb.Document;
import resources.Post;
import resources.PostThread;

import java.util.ArrayList;
import java.util.List;

import static api.PostsResource.POST_COL;

public class Posts {

    public static Post getPost(String postId) {
        String post = RedisCache.get(postId);

        if(post == null) {
            post = Database.getResourceDocById(POST_COL, postId).toJson();
            RedisCache.set(postId, post);
        }
        return Post.fromDocument(new Document(post));
    }

    public static List<Post> postChildren(String postId) {
        String query = "SELECT * FROM " + POST_COL +
                " c WHERE c.parentPost = '" + postId + "'" +
                " ORDER BY c.score DESC";

        List<Document> commentDocs = Database.getResourceListDocs(POST_COL, query);

        List<Post> topComments = new ArrayList<>(commentDocs.size());
        for(Document result : commentDocs)
            topComments.add(Post.fromDocument(result));

        return topComments;
    }

    public static List<Post> topPostChildren(String postId, int count) {
        String query = "SELECT * FROM " + POST_COL +
                " c WHERE c.parentPost = '" + postId + "'" +
                " ORDER BY c.score DESC" +
                " OFFSET 0 LIMIT " + count;

        List<Document> commentDocs = Database.getResourceListDocs(POST_COL, query);

        List<Post> topComments = new ArrayList<>(commentDocs.size());
        for(Document result : commentDocs)
            topComments.add(Post.fromDocument(result));

        return topComments;
    }

    public static long countCommentsOnPost(String postId) {
        Document doc = Database.count(POST_COL, "SELECT VALUE COUNT(1) as commentCount FROM " + POST_COL +
                " c WHERE c.rootPost = '" + postId + "'");
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
            String thread = RedisCache.get(postId);

            if(thread == null) {
                PostThread postThread = calcPostThread(postId);
                RedisCache.set(postId + ":thread", postThread.toJson());
                RedisCache.setExpireTimeout(postId + ":thread", AppConfig.POST_AND_THREAD_CACHE_TIMEOUT);
                return postThread;
            } else
                return PostThread.fromJson(thread);
        } else
            return calcPostThread(postId);
    }
}
