package api;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/")
public class MainApplication extends Application {

    // TODO geo replicate storage com funcao / gerar imagens pequenas com aquele servico do azure
    // TODO artillery

    //Perguntar como especificar para que servidor/storage é que os pedidos vao, se ha maneita de fazer a cache barata
    //se da para fazer a cache replicada sem paga 1000000000000000 de euros por mes

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> set = new HashSet<>();
        set.add(ImagesResource.class);
        set.add(PostsResource.class);
        set.add(UsersResource.class);
        set.add(SubredditsResource.class);
        return set;
    }
}