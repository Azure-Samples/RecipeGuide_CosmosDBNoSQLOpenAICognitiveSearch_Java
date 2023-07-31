package com.azure.recipe;

import com.azure.recipe.model.Recipe;
import com.azure.recipe.service.CognitiveSearchService;
import com.azure.recipe.service.CosmosDbService;
import com.azure.recipe.service.OpenAIService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Main {
    public static CosmosDbService cosmosDbService = null;
    public static OpenAIService openAIEmbeddingService = null;
    public static CognitiveSearchService cogSearchService = null;

    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);

        System.out.println("1.\tUpload recipe(s) to Cosmos DB");
        System.out.println("2.\tVectorize the recipe(s) and store it in Cosmos DB");
        System.out.println("3.\tAsk AI Assistant (search for a recipe by name or description, or ask a question)");
        System.out.println("4.\tExit this Application");

        AppConfig config = readAppConfig();

        cosmosDbService = initCosmosDbService(config);

        while (true) {
            int selectedOption = Integer.parseInt(scanner.nextLine());
            switch (selectedOption) {
                case 1 -> uploadRecipes(config);
                case 2 -> generateEmbeddings(config);
                case 3 -> performSearch(config, scanner);
                default -> {
                    return;
                }
            }

        }
    }

    private static CosmosDbService initCosmosDbService(AppConfig config) {
        CosmosDbService cosmosDbService = new CosmosDbService(config.getCosmosUri(),
                config.getCosmosKey(),
                config.getCosmosDatabase(),
                config.getCosmosContainer()
        );
        int recipeWithEmbedding = cosmosDbService.getRecipeCount(true);
        int recipeWithNoEmbedding = cosmosDbService.getRecipeCount(false);

        System.out.printf("We have %d vectorized recipe(s) and %d non vectorized recipe(s).",
                recipeWithEmbedding, recipeWithNoEmbedding);

        return cosmosDbService;
    }

    private static OpenAIService initOpenAIService(AppConfig config) {
        return new OpenAIService(config.getOpenAIEndpoint(),
                config.getOpenAIKey(),
                config.getOpenAIEmbeddingDeployment(),
                config.getOpenAICompletionsDeployment(),
                config.getOpenAIMaxToken());
    }

    public static void uploadRecipes(AppConfig config) {
        List<Recipe> recipes = Utility.parseDocuments(config.getRecipeLocalFolder());

        cosmosDbService.uploadRecipes(recipes);

        int recipeWithEmbedding = cosmosDbService.getRecipeCount(true);
        int recipeWithNoEmbedding = cosmosDbService.getRecipeCount(false);

        System.out.printf("We have %d vectorized recipe(s) and %d non vectorized recipe(s).",
                recipeWithEmbedding, recipeWithNoEmbedding);
    }

    private static AppConfig readAppConfig() throws IOException {
        return new ObjectMapper().readValue(Main.class.getClassLoader().getResource("appsettings.json"), AppConfig.class);
    }

    public static void performSearch(AppConfig config, Scanner scanner) throws JsonProcessingException {

        if (openAIEmbeddingService == null) {
            log.info("Connecting to Open AI Service..");
            openAIEmbeddingService = initOpenAIService(config);
        }

        if (cogSearchService == null) {
            log.info("Connecting to Azure Cognitive Search..");
            cogSearchService = new CognitiveSearchService(config);

            log.info("Checking for Index in Azure Cognitive Search..");
            if (!cogSearchService.checkIndexIfExists()) {
                log.error("Cognitive Search Index not Found, Please Build the index first.");
                return;
            }
        }

        System.out.println("Type the recipe name or your question, hit enter when ready.");
        String userQuery = scanner.nextLine();

        log.info("Converting User Query to Vector..");
        var embeddingVector = openAIEmbeddingService.getEmbeddings(userQuery);

        log.info("Performing Vector Search..");
        List<Float> embeddings = embeddingVector.stream().map(aDouble -> {
            return (Float) (float) aDouble.doubleValue();
        }).collect(Collectors.toList());

        var ids = cogSearchService.singleVectorSearch(embeddings);

        log.info("Retriving recipe(s) from Cosmos DB (RAG pattern)..");
        var retrivedDocs = cosmosDbService.getRecipes(ids);

        log.info("Priocessing {} to generate Chat Response  using OpenAI Service..", retrivedDocs.size());

        StringBuilder retrivedReceipeNames = new StringBuilder();

        for (Recipe recipe : retrivedDocs) {
            recipe.embedding = null; //removing embedding to reduce tokens during chat completion
            retrivedReceipeNames.append(", ").append(recipe.name); //to dispay recipes submitted for Completion
        }

        log.info("Processing '{}' to generate Completion using OpenAI Service..", retrivedReceipeNames);

        String completion =
                openAIEmbeddingService
                        .getChatCompletionAsync(userQuery, Utility.OBJECT_MAPPER.writeValueAsString(retrivedDocs));

        String chatCompletion = completion;

        log.info("AI Assistant Response {}", chatCompletion);
    }

    private static void generateEmbeddings(AppConfig config) throws JsonProcessingException {
        Map<String, List<Double>> dictEmbeddings = new HashMap<>();
        int recipeWithEmbedding = 0;
        int recipeWithNoEmbedding = 0;
        int recipeCount = 0;

        if (openAIEmbeddingService == null) {
            openAIEmbeddingService = initOpenAIService(config);
        }


        if (cogSearchService == null) {
            log.info("Connecting to Azure Cognitive Search..");
            cogSearchService = new CognitiveSearchService(config);

            log.info("Checking for Index in Azure Cognitive Search..");
            if (!cogSearchService.checkIndexIfExists()) {
                log.info("Building Azure Cognitive Search Index..");
                cogSearchService.buildIndex();
            }

        }

        log.info("Getting recipe(s) to vectorize..");
        var recipes = cosmosDbService.getRecipesToVectorize();
        for (Recipe recipe : recipes) {
            recipeCount++;
            log.info("Vectorizing Recipe# {}..", recipeCount);
            var embeddingVector = openAIEmbeddingService.getEmbeddings(Utility.OBJECT_MAPPER.writeValueAsString(recipe));
            recipe.embedding = embeddingVector;
            dictEmbeddings.put(recipe.id, embeddingVector);
        }

        log.info("Updating {} recipe(s) in Cosmos DB for vectors..", recipes.size());
        cosmosDbService.updateRecipesAsync(dictEmbeddings);


        log.info("Indexing {} document(s) on Azure Cognitive Search..", recipes.size());
        cogSearchService.uploadandIndexDocuments(recipes);

        log.info("Getting Updated Recipe Stats");
        recipeWithEmbedding = cosmosDbService.getRecipeCount(true);
        recipeWithNoEmbedding = cosmosDbService.getRecipeCount(false);

        log.info("Vectorized {}}recipe(s). We have {} vectorized recipe(s) and {} non vectorized recipe(s).", recipeCount, recipeWithEmbedding, recipeWithNoEmbedding);
    }


}