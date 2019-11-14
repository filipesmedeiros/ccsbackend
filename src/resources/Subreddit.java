package resources;

import com.microsoft.azure.cosmosdb.Document;

public class Subreddit {

    private String id;
    private long score;

    public Subreddit() {}

    public Subreddit(String id, long score) {
        this.id = id;
        this.score = score;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public static Subreddit fromDocument(Document doc) {
        Subreddit subreddit = new Subreddit();
        subreddit.setId(doc.getId());
        subreddit.setScore(doc.getLong("score"));
        return subreddit;
    }

    public Document toDocument() {
        Document doc = new Document();
        doc.setId(id);
        doc.set("score", score);
        return doc;
    }
}
