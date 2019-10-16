package scc.srv;

import resources.Subreddit;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class MainApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> set = new HashSet<>();
        set.add(ImagesResource.class);
        set.add(PostsResource.class);
        set.add(UsersResource.class);
        set.add(SubredditResource.class);
        return set;
    }
}