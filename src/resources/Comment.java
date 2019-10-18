package resources;

import com.microsoft.azure.cosmosdb.Document;

public class Comment {

    private String id, username, parentComment, text, post;
    private long creationDate;

    public Document toDocument() {
        Document doc = new Document();
        doc.setId(id);
        doc.set("username", username);
        doc.set("parentComment", parentComment);
        doc.set("text", text);
        doc.set("post", post);
        doc.set("creationDate", creationDate);
        return doc;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getParentComment() {
        return parentComment;
    }

    public void setParentComment(String parentComment) {
        this.parentComment = parentComment;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPost() {
        return post;
    }

    public void setPost(String post) {
        this.post = post;
    }

    public static class CommentDTOInitialAttributes {
        public String opUsername, title, text, post;
    }
}
