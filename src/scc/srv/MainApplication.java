package scc.srv;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class MainApplication extends Application {

    // TODO fazer verificacoes quando vamos fazer posts (user existe, etc)
    // TODO verificar se estamos a tratar bem das excecoes que podem aparecer a aceder ao cosmos
    // TODO fazer sistema de likes e dislikes
    // TODO fazer os comentarios
    // TODO decidir e implementar o sistema de frontpage (all)
    // TODO decidir e implementar o sistema de frontpage (subreddit)
    // TODO decidir e implementar os comentários de um post

    // TODO ------ CENAS OPCIONAIS MAS QUE DEVEM SER FIXES E NAO MUITO DIFICEIS ----
    // TODO sistema de subscricao a subreddits, para um user ter a sua propria frontpage

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