package resources;

import com.microsoft.azure.cosmosdb.Document;

public class Post {

    private String id, subreddit, opUsername, title, content, parentPost, rootPost;
    private boolean isLink;
    private long timestamp, score;

    public static Post fromDocument(Document doc) {
        Post post = new Post();
        post.id = doc.getId();
        post.subreddit = doc.getString("subreddit");
        post.opUsername = doc.getString("opUsername");
        post.content = doc.getString("content");
        post.title = doc.getString("title");
        post.parentPost = doc.getString("parentPost");
        post.rootPost = doc.getString("rootPost");
        post.timestamp = doc.getLong("timestamp");
        post.isLink = doc.getBoolean("isLink");
        post.score = doc.getLong("score");
        return post;
    }

    public Document toDocument() {
        Document doc = new Document();
        doc.setId(id);
        doc.set("subreddit", subreddit);
        doc.set("opUsername", opUsername);
        doc.set("content", content);
        doc.set("title", title);
        doc.set("parentPost", parentPost);
        doc.set("rootPost", rootPost);
        doc.set("timestamp", timestamp);
        doc.set("isLink", isLink);
        doc.set("score", score);
        return doc;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public String getOpUsername() {
        return opUsername;
    }

    public void setOpUsername(String opUsername) {
        this.opUsername = opUsername;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean getIsLink() {
        return isLink;
    }

    public void setIsLink(boolean isLink) {
        this.isLink = isLink;
    }

    public String getParentPost() {
        return parentPost;
    }

    public void setParentPost(String parentPost) {
        this.parentPost = parentPost;
    }

    public String getRootPost() {
        return rootPost;
    }

    public void setRootPost(String rootPost) {
        this.rootPost = rootPost;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public boolean isLink() {
        return isLink;
    }

    public void setLink(boolean link) {
        isLink = link;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public static class PostDTOInitialAttributes {
        public String subreddit, opUsername, title, text, imageUrl;
    }

    public static class PostDTOWithVotes {
        public String id, subreddit, opUsername, title, text, imageUrl, timestamp, upvotes, downvotes;
        // TODO karma


    }
}
