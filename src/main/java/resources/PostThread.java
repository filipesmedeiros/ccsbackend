package resources;

import com.microsoft.azure.cosmosdb.Document;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Consumer;

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

    public PostThread cleanArchived() {
        if(root.isArchived())
            return null;

        firstChildren.forEach((comment, subComments) -> {
            if(comment.isArchived())
                firstChildren.remove(comment);
            else
                subComments.forEach(subComment -> {
                    if(subComment.isArchived())
                        subComments.remove(subComment);
                });
        });

        return this;
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

        return obj.toString();
    }

    public static PostThread fromJson(String json) {
        JSONObject jsonObject = new JSONObject(json);

        JSONObject rootJson = jsonObject.getJSONObject("root");
        Post root = Post.fromDocument(new Document(rootJson.toString()));

        PostThread thread = new PostThread(root);

        JSONArray rootChildrenJson = jsonObject.getJSONArray("children");
        for(int i = 0; i < rootChildrenJson.length(); i++) {
            JSONObject firstChildJson = rootChildrenJson.getJSONObject(i);
            Post childComment = Post.fromDocument(new Document(firstChildJson.getJSONObject("comment").toString()));

            thread.addFirstChildren(childComment);

            JSONArray secondChildrenJson = firstChildJson.getJSONArray("children");
            for(int j = 0; j < secondChildrenJson.length(); j++) {
                Post secondChild = Post.fromDocument(new Document(secondChildrenJson.getJSONObject(j).toString()));
                thread.addSecondChildren(childComment, secondChild);
            }
        }

        return thread;
    }
}
