package com.geminidemo;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.Content;
import com.google.cloud.vertexai.api.FunctionCall;
import com.google.cloud.vertexai.api.FunctionDeclaration;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.api.GenerationConfig;
import com.google.cloud.vertexai.api.HarmCategory;
import com.google.cloud.vertexai.api.SafetySetting;
import com.google.cloud.vertexai.api.Schema;
import com.google.cloud.vertexai.api.Tool;
import com.google.cloud.vertexai.api.Type;
import com.google.cloud.vertexai.generativeai.ChatSession;
import com.google.cloud.vertexai.generativeai.ContentMaker;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.PartMaker;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import java.lang.reflect.Method;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class ParallelFunctionCalling {
        public static void main(String[] args) throws Exception {

                //UNCOMMENT ANY OF THE send_chat_message methods below
                //send_chat_message("How much of P101 do we have in warehouse w101?");
                //send_chat_message("Where is warehouse w1 located?");
                //send_chat_message("Where are warehouse w1 and w2 located?");
                //send_chat_message("How much of P1 and P2 do we have in warehouse w10?");
                //send_chat_message("What is the inventory P1, P2 and P3 in warehouse w101?");
                //send_chat_message("Where is warehouse w10 located and how many unit of p1 are there?");
        }

        private static void send_chat_message(String promptText)
                        throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {

                String projectId = "YOUR-GOOGLE-CLOUD-PROJECT-ID"; // REPLACE THIS VALUES WITH YOUR GOOGLE CLOUD PROJECT ID
                String location = "us-central1"; // REPLACE THIS VALUE WITH GOOGLE CLOUD PROJECT LOCATION
                String modelName = "gemini-1.5-pro-preview-0514";

                // Building a map of functions to call
                Map<String, Method> function_handler = new HashMap<>();
                function_handler.put("getWarehouseDetails",
                                MyAPI.class.getMethod("getWarehouseDetails_api", String.class));
                function_handler.put("getInventoryCount",
                                MyAPI.class.getMethod("getInventoryCount_api", String.class, String.class));

                try (VertexAI vertexAI = new VertexAI(projectId, location)) {

                        // Declare the getInventoryCount function
                        FunctionDeclaration getInventoryCountFunctionDeclaration = FunctionDeclaration.newBuilder()
                                        .setName("getInventoryCount")
                                        .setDescription("Get the current inventory count for a product id in a given location")
                                        .setParameters(
                                                        Schema.newBuilder()
                                                                        .setType(Type.OBJECT)
                                                                        .putProperties("productid", Schema.newBuilder()
                                                                                        .setType(Type.STRING)
                                                                                        .setDescription("product identification")
                                                                                        .build())
                                                                        .putProperties("location", Schema.newBuilder()
                                                                                        .setType(Type.STRING)
                                                                                        .setDescription("location")
                                                                                        .build())
                                                                        .addRequired("location")
                                                                        .addRequired("productid")
                                                                        .build())
                                        .build();

                        // Declare the getWarehouseDetails function
                        FunctionDeclaration getWarehouseDetailsFunctionDeclaration = FunctionDeclaration.newBuilder()
                                        .setName("getWarehouseDetails")
                                        .setDescription("Get the warehouse address for a given warehouse location")
                                        .setParameters(
                                                        Schema.newBuilder()
                                                                        .setType(Type.OBJECT)
                                                                        .putProperties("location", Schema.newBuilder()
                                                                                        .setType(Type.STRING)
                                                                                        .setDescription("location")
                                                                                        .build())
                                                                        .addRequired("location")
                                                                        .build())
                                        .build();

                        // Add the functions to a "tool"
                        Tool warehouseInventoryTools = Tool.newBuilder()
                                        .addFunctionDeclarations(getWarehouseDetailsFunctionDeclaration)
                                        .addFunctionDeclarations(getInventoryCountFunctionDeclaration)
                                        .build();

                        // Model Settings
                        List<SafetySetting> safetySettings = Arrays.asList(
                                        SafetySetting.newBuilder()
                                                        .setCategory(HarmCategory.HARM_CATEGORY_HATE_SPEECH)
                                                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_ONLY_HIGH)
                                                        .build(),
                                        SafetySetting.newBuilder()
                                                        .setCategory(HarmCategory.HARM_CATEGORY_DANGEROUS_CONTENT)
                                                        .setThreshold(SafetySetting.HarmBlockThreshold.BLOCK_ONLY_HIGH)
                                                        .build());

                        // Model parameters
                        GenerationConfig generationConfig = GenerationConfig.newBuilder().setTemperature(0.0f).build();

                        // Start a chat session from a model, with the use of the declared function.
                        GenerativeModel model = new GenerativeModel(modelName, vertexAI)
                                        .withTools(Arrays.asList(warehouseInventoryTools))
                                        .withSafetySettings(safetySettings)
                                        .withGenerationConfig(generationConfig);
                        ChatSession chat = model.startChat();

                        System.out.println("User provided Prompt: " + promptText);
                        // Send the first prompt out
                        GenerateContentResponse response = chat.sendMessage(promptText);
                        System.out.println("Initial response: \n" + ResponseHandler.getContent(response));

                        // Handle cases with multiple chained function calls
                        List<FunctionCall> functionCalls = response.getCandidatesList().stream()
                                        .flatMap(candidate -> candidate.getContent().getPartsList().stream())
                                        .filter(part -> part.getFunctionCall().getName().length() > 0)
                                        .map(part -> part.getFunctionCall())
                                        .collect(Collectors.toList());

                        StringBuilder sb = new StringBuilder();
                        for (FunctionCall functionCall : functionCalls) {
                                String functionCallName = functionCall.getName();
                                System.out.println("Need to invoke function: " + functionCallName);

                                // Check for a function call or a natural language response
                                if (function_handler.containsKey(functionCallName)) {
                                        // Invoke the function using reflection
                                        Object api_object = new MyAPI();
                                        Method function_method = function_handler.get(functionCallName);

                                        // Extract the function call parameters
                                        Map<String, String> functionCallParameters = functionCall.getArgs()
                                                        .getFieldsMap().entrySet()
                                                        .stream()
                                                        .collect(Collectors.toMap(
                                                                        Map.Entry::getKey,
                                                                        entry -> entry.getValue()
                                                                                        .getStringValue()));

                                        // Extract all the parameter values into an array
                                        Object[] functionParameters = functionCallParameters.values().toArray();

                                        Object result = function_method.invoke(api_object, functionParameters);
                                        sb.append(result);

                                }

                        }
                        // Send the API response back to Gemini, which will generate a natural
                        // language summary or another function call
                        Content content = ContentMaker.fromMultiModalData(
                                        PartMaker.fromFunctionResponse(
                                                        function_handler.entrySet().stream().findFirst()
                                                                        .get().getKey(),
                                                        Collections.singletonMap("content",
                                                                        sb.toString())));
                        response = chat.sendMessage(content);
                        System.out.println("Response: " + ResponseHandler.getContent(response));

                }
        }
}

// Utility class to simulate the API for getting warehouse details and inventory
// count in a specific warehouse
class MyAPI {
        public String getWarehouseDetails_api(String location) throws IOException {
                System.out.println("Executing function with parameters: " + location);
                return "warehouse: " + location + " address: 123 Main Street";
        }

        public String getInventoryCount_api(String productid, String location) throws IOException {
                System.out.println("Executing function with parameters: " + productid + " " + location);
                return "product: " + productid + " location: " + location + " count: 50";
        }
}
