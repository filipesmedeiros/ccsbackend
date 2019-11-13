package utils;

import com.microsoft.azure.cosmosdb.Document;

import static api.PostsResource.POST_COL;

public class Posts {

    public static long countCommentsOnPost(String postId) {
        Document doc = Database.count(POST_COL, "SELECT VALUE COUNT(1) as commentCount FROM " + POST_COL +
                " c WHERE c.parentPost = '" + postId + "'");
        return (Long) doc.get("commentCount");
    }
}
