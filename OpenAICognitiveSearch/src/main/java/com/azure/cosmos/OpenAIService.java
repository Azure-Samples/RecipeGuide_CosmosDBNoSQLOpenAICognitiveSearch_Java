package com.azure.cosmos;

import com.azure.ai.openai.OpenAIClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.ai.openai.models.NonAzureOpenAIKeyCredential;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.policy.ExponentialBackoffOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.util.ClientOptions;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

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
}
