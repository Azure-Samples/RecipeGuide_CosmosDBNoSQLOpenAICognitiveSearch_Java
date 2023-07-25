package com.azure.recipe;

import com.azure.cosmos.*;
import com.azure.cosmos.models.CosmosBulkOperations;
import com.azure.cosmos.models.CosmosItemOperation;
import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.util.CosmosPagedIterable;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class CosmosDbService {

    CosmosContainer container;

    public CosmosDbService(String endpoint, String key, String databaseName, String containerName) {

        CosmosClient cosmosClient = new CosmosClientBuilder()
                .endpoint(endpoint)
                .key(key)
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .contentResponseOnWriteEnabled(true)
                .buildClient();

        CosmosDatabase database = cosmosClient.getDatabase(databaseName);

        this.container = database.getContainer(containerName);
    }

    public int getRecipeCount(boolean withEmbedding) {
        List<SqlParameter> parameters = List.of(new SqlParameter("@status", withEmbedding));
        SqlQuerySpec query = new SqlQuerySpec("SELECT value Count(c.id) FROM c WHERE IS_ARRAY(c.embedding)=@status", parameters);

        CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
        CosmosPagedIterable<Integer> results = container.queryItems(query, options, Integer.class);
        Iterator<Integer> iterator = results.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        } else {
            return 0;
        }

    }

    public void uploadRecipes(List<Recipe> recipes) {
        List<CosmosItemOperation> itemOperations = recipes
                .stream()
                .map(recipe -> CosmosBulkOperations
                        .getCreateItemOperation(recipe,
                                new PartitionKey(recipe.getId()))
                ).collect(Collectors.toList());

        container.executeBulkOperations(itemOperations);
    }

    public List<Recipe> getRecipes(List<String> ids) {
        String join = String.join(",", ids);
        String querystring = "SELECT * FROM c WHERE c.id IN(" + join + ")";

        log.info(querystring);

        SqlQuerySpec query = new SqlQuerySpec(querystring);

        CosmosPagedIterable<Recipe> recipes = container.queryItems(query, new CosmosQueryRequestOptions(), Recipe.class);
        return recipes.stream().collect(Collectors.toList());
    }

    public List<Recipe> getRecipesToVectorize() {
        SqlQuerySpec query = new SqlQuerySpec("SELECT * FROM c WHERE IS_ARRAY(c.embedding)=false");

        CosmosPagedIterable<Recipe> pagedIterable = container.queryItems(query, new CosmosQueryRequestOptions(), Recipe.class);
        return pagedIterable.stream().collect(Collectors.toList());
    }


    public void updateRecipesAsync(Map<String, List<Double>> dictInput) {
        List<CosmosItemOperation> itemOperations = dictInput
                .entrySet()
                .stream()
                .map(s -> {
                    return CosmosBulkOperations.getPatchItemOperation(s.getKey(),
                            new PartitionKey(s.getKey()),
                            CosmosPatchOperations.create()
                                    .add("/embedding", s.getValue())
                    );
                })
                .collect(Collectors.toList());
        container.executeBulkOperations(itemOperations);
    }

}
