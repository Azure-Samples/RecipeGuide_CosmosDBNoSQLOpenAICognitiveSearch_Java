package com.azure.cosmos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {

        Scanner scanner = new Scanner(System.in);

        System.out.println("1.\tUpload recipe(s) to Cosmos DB");
        System.out.println("2.\tVectorize the recipe(s) and store it in Cosmos DB");
        System.out.println("3.\tAsk AI Assistant (search for a recipe by name or description, or ask a question)");
        System.out.println("4.\tExit this Application");

        AppConfig config = readAppConfig();

        while (true) {
            int selectedOption = Integer.parseInt(scanner.nextLine());
            switch (selectedOption) {
                case 1:
                    uploadRecipes(config);
                    break;
//                case 2:
//                    GenerateEmbeddings(config);
//                    break;
//                case 3:
//                    PerformSearch(config);
//                    break;
                default:
                    return;
            }

        }
    }

    public static void uploadRecipes(AppConfig config) {
        System.out.println(config);
    }

    private static AppConfig readAppConfig() throws IOException {
        return new ObjectMapper().readValue(Main.class.getClassLoader().getResource("appsettings.json"), AppConfig.class);
    }


}