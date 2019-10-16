package resources;

import com.microsoft.azure.cosmosdb.Document;

public class PostVote {

    private String id, postId, userId;
    private boolean up;

    public PostVote(boolean up, String postId, String userId) {
        this.id = generateId(postId, userId);
        this.up = up;
        this.postId = postId;
        this.userId = userId;
    }

    public PostVote(String id, boolean up, String postId, String userId) {
        this.id = id;
        this.up = up;
        this.postId = postId;
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isUp() {
        return up;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public static String generateId(String postId, String userId) {
        return Integer.toString((postId + userId).hashCode());
    }

    public Document toDocument() {
        Document doc = new Document();
        doc.set("up", up);
        doc.set("postId", postId);
        doc.set("userId", userId);
        return doc;
    }
}
