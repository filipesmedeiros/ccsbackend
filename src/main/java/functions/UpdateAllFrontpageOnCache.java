package functions;

import api.PostsResource;
import com.microsoft.azure.cosmosdb.Document;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;
import utils.*;

import java.util.List;
import java.util.ListIterator;

public class UpdateAllFrontpageOnCache {

    @FunctionName("update_frontpages_on_cache")
    public void updateFrontpagesOnCache(@TimerTrigger(name = "keepAliveTrigger",
            schedule = "* * */" + AppConfig.ALL_FRONTPAGE_UPDATE_PERIOD + " * * *") String timerInfo,
                                        ExecutionContext context) {

        String query = "SELECT TOP " + AppConfig.ALL_FRONTPAGE_SIZE + " * FROM " + PostsResource.POST_COL + " p" +
                " WHERE p.timestamp >= " + Date.timestampMinusHours(AppConfig.FRONTPAGE_TIME_WINDOW) +
                " AND p.parentPost = ''" +
                " ORDER BY p.score DESC";

        List<Document> timeWindowPosts = Database.getResourceListDocs(PostsResource.POST_COL, query);

        String[] array = new String[timeWindowPosts.size()];
        ListIterator<Document> it = timeWindowPosts.listIterator(timeWindowPosts.size());
        int counter = 0;

        while(it.hasPrevious())
            array[counter++] = it.previous().toJson();

        if(RedisCache.entryExists("allfrontpage:posts"))
            RedisCache.removeEntry("allfrontpage:posts");

        RedisCache.lpush("allfrontpage:posts", array);
    }
}
