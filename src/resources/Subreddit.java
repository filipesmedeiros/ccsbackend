package resources;

public class Subreddit {
    private String id;

    public Subreddit(String id) {
        this.id = id;
    }

    public String getName() {
        return id;
    }

    public void setName(String id) {
        this.id = id;
    }
}
