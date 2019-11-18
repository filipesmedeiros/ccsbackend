package functions;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.CosmosDBOutput;
import com.microsoft.azure.functions.annotation.CosmosDBTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;

public class OnNewInteractionUpdateCache {

    @FunctionName("update_cache_score_on_interaction")
    @CosmosDBOutput(name = "database",
            databaseName = "ToDoList",
            collectionName = "Items",
            connectionStringSetting = "AzureCosmosDBConnection")
    public void updateScoresOnDB(@CosmosDBTrigger(name = "", databaseName = "", collectionName = "",
            createLeaseCollectionIfNotExists = true, connectionStringSetting = "AzureCosmosDBConnection"),
                                 ExecutionContext context) {

    }
}
