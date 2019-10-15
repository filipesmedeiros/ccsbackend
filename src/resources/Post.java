package resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.cosmosdb.Document;

import java.io.IOException;
import java.util.Date;

public class Post extends Document {

    public String postId, subreddit, opUsername, title, text, imageUrl;
    public Date creationDate;

    public Post(String postId,String subreddit, String opUsername, String title, String text, String imageUrl, Date creationDate) {
        super();
        this.postId = postId;
        this.subreddit = subreddit;
        this.opUsername = opUsername;
        this.title = title;
        this.text = text;
        this.imageUrl = imageUrl;
        this.creationDate = creationDate;
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

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public static Post fromJson(String jsonPost, Date date) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(jsonPost);

        return new Post(jsonNode.get("postId").asText(),
                        jsonNode.get("subreddit").asText(),
                        jsonNode.get("opUsername").asText(),
                        jsonNode.get("title").asText(),
                        jsonNode.get("text").asText(),
                        jsonNode.get("imageUrl").asText(),
                        date);
    }
}
