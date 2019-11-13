package resources;

import com.microsoft.azure.cosmosdb.Document;

public class Post {

    private String id, subreddit, opUsername, title, content, isLink, parentPost, rootPost;
    public long timestamp;

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

    public String getIsLink() {
        return isLink;
    }

    public void setIsLink(String isLink) {
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

    public static class PostDTOInitialAttributes {
        public String subreddit, opUsername, title, text, imageUrl;
    }

    public static class PostDTOWithVotes {
        public String id, subreddit, opUsername, title, text, imageUrl, timestamp, upvotes, downvotes;
        // TODO karma


    }
}
