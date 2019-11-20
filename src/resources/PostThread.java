package resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.*;

public class PostThread {

    private Post root;
    private Map<Post, List<Post>> firstChildren;

    public PostThread(Post root) {
        this.root = root;
        this.firstChildren = new HashMap<>();
    }

    public void addFirstChildren(Post post) {
        firstChildren.put(post, new LinkedList<>());
    }

    public void addSecondChildren(Post parent, Post post) {
        List<Post> secondChildren = firstChildren.get(parent);

        if(secondChildren != null)
            secondChildren.add(post);
    }

    public String toJson() {
        JsonObject obj = new JsonObject();
        obj.add("root", root.toJson());

        Post[] firstC = firstChildren.keySet().toArray(new Post[0]);

        JsonArray firstChildrenArray = new JsonArray();
        for(Post post : firstC) {
            JsonObject child = new JsonObject();
            child.add("comment", post.toJson());

            JsonArray secondChildren = new JsonArray();
            firstChildren.get(post).forEach(secondChild -> secondChildren.add(secondChild.toJson()));
            child.add("children", secondChildren);

            firstChildrenArray.add(child);
        }

        obj.add("children", firstChildrenArray);

        return obj.getAsString();
    }
}
