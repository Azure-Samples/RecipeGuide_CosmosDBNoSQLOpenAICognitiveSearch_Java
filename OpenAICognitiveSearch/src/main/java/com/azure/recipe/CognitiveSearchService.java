package com.azure.recipe;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.Context;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.search.documents.indexes.models.*;
import com.azure.search.documents.models.SearchOptions;
import com.azure.search.documents.models.SearchQueryVector;
import com.azure.search.documents.util.SearchPagedIterable;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CognitiveSearchService {
    private AppConfig config;
    private static final String MY_SEMANTIC_CONFIG = "my-semantic-config";
    private String searchIndexName;
    private SearchIndexClient indexClient = null;
    private SearchClient searchClient = null;


    public CognitiveSearchService(AppConfig config) {
        this.config = config;
        searchIndexName = config.getSearchIndexName();
        indexClient = createSearchIndexClient();
        searchClient = indexClient.getSearchClient(searchIndexName);
    }

    private SearchIndexClient createSearchIndexClient() {
        String searchServiceEndPoint = config.getSearchServiceEndPoint();
        String adminApiKey = config.getSearchServiceAdminApiKey();

        return new SearchIndexClientBuilder()
                .endpoint(searchServiceEndPoint)
                .credential(new AzureKeyCredential(adminApiKey))
                .buildClient();
    }

    public boolean checkIndexIfExists() {
        try {
            if (indexClient.getIndex(searchIndexName) != null) {
                return true;
            }
        } catch (Exception e) {
            log.error("Index does not exist", e);
            return false;
        }
        return false;
    }

    public List<String> singleVectorSearch(List<Float> queryEmbeddings) {

        var vector = new SearchQueryVector();
        vector.setKNearestNeighborsCount(3);
        vector.setFields("embedding");
        vector.setValue(queryEmbeddings);

        var searchOptions = new SearchOptions();
        searchOptions.setVector(vector);
        searchOptions.setSelect("id");
        searchOptions.setTop(5);

        SearchPagedIterable response = searchClient.search(null, searchOptions, Context.NONE);

        return response
                .stream()
                .map(result -> "result") // TODO
                .collect(Collectors.toList());
    }

    //build the index
    public void buildIndex() {
        deleteIndexIfExists(searchIndexName);
        createIndex(searchIndexName);
    }

    void uploadAndIndexDocuments(List<Recipe> Recipes) {
        searchClient.indexDocuments(new IndexDocumentsBatch().addUploadActions(convertRecipeToCogSarchDoc(Recipes)));

    }

    public void uploadandIndexDocuments(List<Recipe> Recipes) {
        searchClient.indexDocuments(new IndexDocumentsBatch().addUploadActions(convertRecipeToCogSarchDoc(Recipes)));

    }

    private List<CogSearchDoc> convertRecipeToCogSarchDoc(List<Recipe> recipes) {
        return recipes
                .stream()
                .map(recipe -> {
                    CogSearchDoc cdoc = new CogSearchDoc();
                    cdoc.setId(recipe.getId());
                    cdoc.setName(recipe.name);
                    cdoc.setDescription(recipe.description);
                    cdoc.setEmbedding(recipe.embedding);
                    return cdoc;
                }).collect(Collectors.toList());

    }

    private void deleteIndexIfExists(String indexName) {
        if (indexClient.getIndex(indexName) != null) {
            indexClient.deleteIndex(indexName);
        }
    }

    private void createIndex(String indexName) {
        indexClient.createOrUpdateIndex(buildVectorSearchIndex(indexName));
    }

    SearchIndex buildVectorSearchIndex(String name) {
        String vectorSearchConfigName = "my-vector-config";

        SearchIndex searchIndex = new SearchIndex(name);
        VectorSearch vectorSearch = new VectorSearch();
        vectorSearch.setAlgorithmConfigurations(List.of(new HnswVectorSearchAlgorithmConfiguration(vectorSearchConfigName)));

        searchIndex.setVectorSearch(vectorSearch);
        SemanticSettings semanticSettings = new SemanticSettings();
        PrioritizedFields prioritizedFields = new PrioritizedFields();

        SemanticField titleField = new SemanticField();
        titleField.setFieldName("name");

        prioritizedFields.setTitleField(titleField);
        SemanticField contentField = new SemanticField();
        contentField.setFieldName("description");
        prioritizedFields.setPrioritizedContentFields(List.of(contentField));

        semanticSettings.setConfigurations(List.of(new SemanticConfiguration(MY_SEMANTIC_CONFIG, prioritizedFields)));
        searchIndex.setSemanticSettings(semanticSettings);

        SearchField idSearchField = new SearchField("id", SearchFieldDataType.STRING);
        idSearchField.setKey(true);
        idSearchField.setFilterable(true);
        idSearchField.setSortable(true);

        SearchField nameSearchField = new SearchField("name", SearchFieldDataType.STRING);
        nameSearchField.setFilterable(true);
        nameSearchField.setSortable(true);

        SearchField descSearchField = new SearchField("description", SearchFieldDataType.STRING);
        descSearchField.setFilterable(true);

        SearchField embedingSearchField = new SearchField("embedding", SearchFieldDataType.collection(SearchFieldDataType.SINGLE));
        embedingSearchField.setSearchable(true);
        embedingSearchField.setVectorSearchDimensions(1536);
        embedingSearchField.setVectorSearchConfiguration(vectorSearchConfigName);
        searchIndex.setFields(List.of(
                idSearchField,
                nameSearchField,
                descSearchField,
                embedingSearchField
        ));

        return searchIndex;
    }


}
