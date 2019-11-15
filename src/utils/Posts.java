package utils;

import com.google.gson.Gson;
import com.microsoft.azure.cosmosdb.Document;
import resources.Post;

import java.util.TreeSet;

import static api.PostsResource.POST_COL;

public class Posts {

    public static long countCommentsOnPost(String postId) {
        Document doc = Database.count(POST_COL, "SELECT VALUE COUNT(1) as commentCount FROM " + POST_COL +
                " c WHERE c.parentPost = '" + postId + "'");
        return (Long) doc.get("commentCount");
    }

    public static TreeSet<Post> getPostThread(String postId) {
        return null;
    }
}
