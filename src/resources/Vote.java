package resources;

import com.microsoft.azure.cosmosdb.Document;

public class Vote {

    private String id, submissionId, userId;
    private long timestamp;
    private boolean up, isPost;

    public Vote(String id, boolean up, String submissionId, String userId, boolean isPost) {
        this.id = id;
        this.up = up;
        this.submissionId = submissionId;
        this.userId = userId;
        this.isPost = isPost;
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

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
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

    public boolean isPost() {
        return isPost;
    }

    public void setPost(boolean post) {
        isPost = post;
    }

    public Document toDocument() {
        Document doc = new Document();
        doc.setId(id);
        doc.set("submissionId", submissionId);
        doc.set("userId", userId);
        doc.set("up", up);
        doc.set("timestamp", timestamp);
        doc.set("isPost", isPost);
        return doc;
    }
}
