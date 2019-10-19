package resources;

import com.microsoft.azure.cosmosdb.Document;

public class Post {

    private String id, subreddit, opUsername, title, text, imageUrl;
    public long creationDate;

    public Document toDocument() {
        Document doc = new Document();
        doc.setId(id);
        doc.set("subreddit", subreddit);
        doc.set("opUsername", opUsername);
        doc.set("text", text);
        doc.set("title", title);
        doc.set("creationDate", creationDate);
        doc.set("imageUrl", imageUrl);
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

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
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
        public String id, subreddit, opUsername, title, text, imageUrl, creationDate, upvotes, downvotes;
        // TODO karma


    }
}
