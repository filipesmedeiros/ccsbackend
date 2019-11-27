package functions;

import api.SubredditsResource;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import resources.Post;
import utils.*;

import java.util.Iterator;
import java.util.List;

public class UpdateAllFrontpageOnCache {

    @FunctionName("update_frontpages_on_cache")
    public void updateFrontpagesOnCache(@TimerTrigger(name = "keepAliveTrigger",
            schedule = "0 0 */" + AppConfig.FRONTPAGES_UPDATE_PERIOD + " * * *") String timerInfo,
                                        ExecutionContext context) {

        List<Post> topPosts = Frontpages.calcAllFrontpage();

        String[] array = new String[topPosts.size()];
        Iterator<Post> it = topPosts.iterator();
        int counter = 0;

        while(it.hasNext())
            array[counter++] = it.next().toJson().toString();

        if(RedisCache.entryExists("allfrontpage:posts"))
            RedisCache.removeEntry("allfrontpage:posts");

        RedisCache.rpush("allfrontpage:posts", array);

        RedisCache.setExpireTimeout("allfrontpage:posts", AppConfig.FRONTPAGES_UPDATE_PERIOD);

        String query = "SELECT TOP " + AppConfig.NUMBER_TOP_SUBREDDITS + " * FROM " + SubredditsResource.SUBREDDIT_COL + " s" +
                " ORDER BY s.score DESC";

        List<Document> topSubs = Database.getResourceListDocs(SubredditsResource.SUBREDDIT_COL, query);

        topSubs.forEach(subDoc -> {
            List<Post> posts = Frontpages.calcSubredditFrontpage(subDoc.getId());

            String[] newArray = new String[posts.size()];
            Iterator<Post> newIt = posts.iterator();
            int newCounter = 0;

            while(newIt.hasNext())
                newArray[newCounter++] = newIt.next().toJson().toString();

            if(RedisCache.entryExists(subDoc.getId() + "frontpage:posts"))
                RedisCache.removeEntry(subDoc.getId() + "frontpage:posts");

            RedisCache.rpush(subDoc.getId() + "frontpage:posts", newArray);

            RedisCache.setExpireTimeout(subDoc.getId() + "frontpage:posts", AppConfig.FRONTPAGES_UPDATE_PERIOD);
        });
    }
}
