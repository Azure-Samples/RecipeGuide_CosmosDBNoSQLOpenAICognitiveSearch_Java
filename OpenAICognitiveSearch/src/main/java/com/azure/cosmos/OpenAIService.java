package com.azure.cosmos;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.*;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.RetryOptions;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
public class OpenAIService {
    private String openAIEmbeddingDeployment;
    private String openAICompletionDeployment;
    private int openAIMaxTokens;

    private OpenAIClient openAIClient;
    private String systemPromptRecipeAssistant = """
            You are an intelligent assistant for Contoso Recipes. 
            You are designed to provide helpful answers to user questions about using
            recipes, cooking instructions only using the provided JSON strings.

            Instructions:
            - In case a recipe is not provided in the prompt politely refuse to answer all queries regarding it. 
            - Never refer to a recipe not provided as input to you.
            - If you're unsure of an answer, you can say ""I don't know"" or ""I'm not sure"" and recommend users search themselves.        
            - Your response  should be complete. 
            - List the Name of the Recipe at the start of your response folowed by step by step cooking instructions
            - Assume the user is not an expert in cooking.
            - Format the content so that it can be printed to the Command Line 
            - In case there are more than one recipes you find let the user pick the most appropiate recipe.""";


    public OpenAIService(String endpoint,
                         String key,
                         String embeddingsDeployment,
                         String CompletionDeployment,
                         String maxTokens) {

        RetryOptions retryOptions = new RetryOptions(
                new ExponentialBackoffOptions()
                        .setMaxRetries(10)
                        .setMaxDelay(Duration.of(2, ChronoUnit.SECONDS))
        );

        if (endpoint.contains("api.openai.com")) {
            this.openAIClient = new OpenAIClientBuilder()
                    .endpoint(endpoint)
                    .credential(new AzureKeyCredential(key))
                    .retryOptions(retryOptions
                    )
                    .buildClient();
        } else {
            this.openAIClient = new OpenAIClientBuilder()
                    .endpoint(endpoint)
                    .credential(new NonAzureOpenAIKeyCredential(key))
                    .retryOptions(retryOptions
                    )
                    .buildClient();
        }
    }

    public List<Double> getEmbeddings(String query) {
        try {
            EmbeddingsOptions options = new EmbeddingsOptions(List.of(query));

            var response = openAIClient.getEmbeddings(openAIEmbeddingDeployment, options);

            List<EmbeddingItem> embeddings = response.getData();

            return embeddings.get(0).getEmbedding().stream().toList();
        } catch (Exception ex) {
            log.error("GetEmbeddingsAsync Exception:", ex);
            return null;
        }
    }

    public String getChatCompletionAsync(String userPrompt, String documents) {


        ChatMessage systemMessage = new ChatMessage(ChatRole.SYSTEM, systemPromptRecipeAssistant + documents);
        ChatMessage userMessage = new ChatMessage(ChatRole.USER, userPrompt);


        ChatCompletionsOptions options = new ChatCompletionsOptions(List.of(systemMessage, userMessage));
        options.setMaxTokens(openAIMaxTokens);
        options.setTemperature(0.5);
        options.setFrequencyPenalty(0d);
        options.setPresencePenalty(0d);
//            options.setNucleusSamplingFactor(0d);// TODO


        ChatCompletions completions = openAIClient.getChatCompletions(openAICompletionDeployment, options);

        return completions.getChoices().get(0).getMessage().getContent();

    }
}
