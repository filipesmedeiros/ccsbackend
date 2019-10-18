package resources;

import com.microsoft.azure.cosmosdb.Document;

public class Subreddit {

    private String id;

    public Subreddit(String id) {
        this.id = id;
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
        return doc;
    }
}
