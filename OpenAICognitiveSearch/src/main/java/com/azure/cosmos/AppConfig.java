package com.azure.cosmos;

public class AppConfig {
    private String allowedHosts;
    private String cosmosUri;
    private String cosmosKey;
    private String cosmosDatabase;
    private String cosmosContainer;
    private String recipeLocalFolder;
    private String openAIEndpoint;
    private String openAIKey;
    private String openAIEmbeddingDeployment;
    private String openAICompletionsDeployment;
    private String openAIMaxToken;
    private String searchServiceEndPoint;
    private String searchIndexName;
    private String searchServiceAdminApiKey;
    private String searchServiceQueryApiKey;

    public String getAllowedHosts() {
        return allowedHosts;
    }

    public void setAllowedHosts(String allowedHosts) {
        this.allowedHosts = allowedHosts;
    }

    public String getCosmosUri() {
        return cosmosUri;
    }

    public void setCosmosUri(String cosmosUri) {
        this.cosmosUri = cosmosUri;
    }

    public String getCosmosKey() {
        return cosmosKey;
    }

    public void setCosmosKey(String cosmosKey) {
        this.cosmosKey = cosmosKey;
    }

    public String getCosmosDatabase() {
        return cosmosDatabase;
    }

    public void setCosmosDatabase(String cosmosDatabase) {
        this.cosmosDatabase = cosmosDatabase;
    }

    public String getCosmosContainer() {
        return cosmosContainer;
    }

    public void setCosmosContainer(String cosmosContainer) {
        this.cosmosContainer = cosmosContainer;
    }

    public String getRecipeLocalFolder() {
        return recipeLocalFolder;
    }

    public void setRecipeLocalFolder(String recipeLocalFolder) {
        this.recipeLocalFolder = recipeLocalFolder;
    }

    public String getOpenAIEndpoint() {
        return openAIEndpoint;
    }

    public void setOpenAIEndpoint(String openAIEndpoint) {
        this.openAIEndpoint = openAIEndpoint;
    }

    public String getOpenAIKey() {
        return openAIKey;
    }

    public void setOpenAIKey(String openAIKey) {
        this.openAIKey = openAIKey;
    }

    public String getOpenAIEmbeddingDeployment() {
        return openAIEmbeddingDeployment;
    }

    public void setOpenAIEmbeddingDeployment(String openAIEmbeddingDeployment) {
        this.openAIEmbeddingDeployment = openAIEmbeddingDeployment;
    }

    public String getOpenAICompletionsDeployment() {
        return openAICompletionsDeployment;
    }

    public void setOpenAICompletionsDeployment(String openAICompletionsDeployment) {
        this.openAICompletionsDeployment = openAICompletionsDeployment;
    }

    public String getOpenAIMaxToken() {
        return openAIMaxToken;
    }

    public void setOpenAIMaxToken(String openAIMaxToken) {
        this.openAIMaxToken = openAIMaxToken;
    }

    public String getSearchServiceEndPoint() {
        return searchServiceEndPoint;
    }

    public void setSearchServiceEndPoint(String searchServiceEndPoint) {
        this.searchServiceEndPoint = searchServiceEndPoint;
    }

    public String getSearchIndexName() {
        return searchIndexName;
    }

    public void setSearchIndexName(String searchIndexName) {
        this.searchIndexName = searchIndexName;
    }

    public String getSearchServiceAdminApiKey() {
        return searchServiceAdminApiKey;
    }

    public void setSearchServiceAdminApiKey(String searchServiceAdminApiKey) {
        this.searchServiceAdminApiKey = searchServiceAdminApiKey;
    }

    public String getSearchServiceQueryApiKey() {
        return searchServiceQueryApiKey;
    }

    public void setSearchServiceQueryApiKey(String searchServiceQueryApiKey) {
        this.searchServiceQueryApiKey = searchServiceQueryApiKey;
    }
}
