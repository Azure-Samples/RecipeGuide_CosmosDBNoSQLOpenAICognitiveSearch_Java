package com.azure.cosmos;

import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedFlux;
import com.azure.cosmos.util.CosmosPagedIterable;

import java.util.List;

public class CosmosDbService {

    CosmosContainer container;

    public CosmosDbService(String endpoint, String key, String databaseName, String containerName) {

        CosmosClient cosmosClient = new CosmosClientBuilder()
                .endpoint(endpoint)
                .key(key)
                .buildClient();

        CosmosDatabase database = cosmosClient.getDatabase(databaseName);

        this.container = database.getContainer(containerName);
    }

    public int getRecipeCount(boolean withEmbedding) {
        List<SqlParameter> parameters = List.of(new SqlParameter("@status", withEmbedding));
        SqlQuerySpec query = new SqlQuerySpec("SELECT value Count(c.id) FROM c WHERE IS_ARRAY(c.embedding)=@status", parameters);

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        CosmosPagedIterable<Integer> results = container.queryItems(query, options, Integer.class);
        return results.iterator().next();
    }

}
