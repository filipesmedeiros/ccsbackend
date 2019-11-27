package resources;

import com.microsoft.azure.cosmosdb.Document;

public class Vote {

    private String id, postId, userId, subredditId;
    private long timestamp;
    private boolean up;

    public Vote(String id, boolean up, String postId, String userId, String subredditId) {
        this.id = id;
        this.up = up;
        this.postId = postId;
        this.userId = userId;
        this.subredditId = subredditId;
        timestamp = System.currentTimeMillis();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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

    public String getSubredditId() {
        return subredditId;
    }

    public void setSubredditId(String subredditId) {
        this.subredditId = subredditId;
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

    public static String generateId(String postCommentId, String userId) {
        return Integer.toString((postCommentId + userId).hashCode());
    }

    public Document toDocument() {
        Document doc = new Document();
        doc.setId(id);
        doc.set("postId", postId);
        doc.set("userId", userId);
        doc.set("subredditId", subredditId);
        doc.set("up", up);
        doc.set("timestamp", timestamp);
        return doc;
    }

    public static Vote fromDocument(Document doc) {
        Vote v = new Vote(doc.getId(),
                doc.getBoolean("up"),
                doc.getString("postId"),
                doc.getString("userId"),
                doc.getString("subredditId"));
        v.setTimestamp(doc.getLong("timestamp"));

        return v;
    }
}
