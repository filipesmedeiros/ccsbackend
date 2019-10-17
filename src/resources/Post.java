package resources;

import com.microsoft.azure.cosmosdb.Document;

public class Post {

    private String id, subreddit, opUsername, title, text, imageUrl;
    public long creationDate;
    public int upvotes, downvotes, karma;

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public String getOpUsername() {
        return opUsername;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public int getDownvotes() {
        return downvotes;
    }

    public void setDownvotes(int downvotes) {
        this.downvotes = downvotes;
    }

    public int getKarma() {
        return karma;
    }

    public void setKarma(int karma) {
        this.karma = karma;
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

    public Document toDocument() {
        Document doc = new Document();
        doc.setId(id);
        doc.set("subreddit", subreddit);
        doc.set("opUsername", opUsername);
        doc.set("text", text);
        doc.set("title", title);
        doc.set("imageUrl", imageUrl);
        doc.set("creationDate", creationDate);
        doc.set("upvotes", upvotes);
        doc.set("downvotes", downvotes);
        doc.set("karma", karma);
        return doc;
    }

    public Post swapOneVote(boolean up) {
        if(up) {
            upvotes++;
            downvotes--;
        } else {
            upvotes--;
            downvotes++;
        }
        return this;
    }

    public static class PostDTOInitialAttributes {
        public String subreddit, opUsername, title, text, imageUrl;

        /**
        public static boolean testDocument(Document doc) {
            int c = PostDTOInitialAttributes.class.getDeclaredFields().length;
            return doc.has("subreddit")
                    && doc.has("opUsername")
                    && doc.has("title")
                    && doc.has("text")
                    && doc.has("imageUrl");
        }
         **/
    }
}
