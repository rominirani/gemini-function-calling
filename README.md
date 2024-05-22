# Gemini Function calling
This project contains Java sample code to demonstrate Gemini Function Calling. 

## What is Function calling
As per documentation available [here](https://cloud.google.com/vertex-ai/generative-ai/docs/multimodal/function-calling): 

You can use function calling to define custom functions and provide these to a generative AI model. While processing a query, the model can choose to delegate certain data processing tasks to these functions. It does not call the functions. Instead, it provides structured data output that includes the name of a selected function and the arguments that the model proposes the function to be called with. You can use this output to invoke external APIs. You can then provide the API output back to the model, allowing it to complete its answer to the query. When it is used this way, function calling enables LLMs to access real-time information and interact with various services, such as SQL databases, customer relationship management systems, and document repositories.

The following diagram taken from [Codelab on Function calling - Python by Kristopher Overholt](https://codelabs.developers.google.com/codelabs/gemini-function-calling#0) provides a high level understanding of how Function calling works. [Kristopher](https://github.com/koverholt) and [Ishana](https://github.com/ishana7) have written a lovely Colab notebook available [here](https://github.com/GoogleCloudPlatform/generative-ai/blob/main/gemini/function-calling/use_case_company_news_and_insights.ipynb) that demonstrates Function calling in Python and I set about to humbly replicate it in Java over here. 

<img src="https://codelabs.developers.google.com/static/codelabs/gemini-function-calling/img/gemini-function-calling-overview_1440.png"/>

## What does the code demonstrate
The Java implementation provided looks at the following scenario:
- We have a product inventory solution with multiple warehouses, where the product inventory is stored.
- We have a dummy API (implemented as a Java class with hard coded values for now) that has the following methods:
  - ```String getInventoryCount(String productId, String location)```
  - ```String getWarehouseDetails(String location)```
 
Our goal is to invoke the Gemini model with prompts/queries in Natural Language that allow us to give it the following sample queries:
- How much of P101 do we have in warehouse w101?
- How much of P1 and P2 do we have in warehouse w10?
- Where are the following warehouses located: w10 and w1?
- Where is warehouse w10 located and how many unit of p1 are there?
- For product id : P678, how much inventory of that do we have in warehouse w100
 
In other words, via the code, we will expect that it will interpret the requests as either for getInventoryCount and/or getWarehouseDetails, as many times as required. 

For e.g. if the query is "How much of P101 do we have in warehouse w101?", it should ideally indicate that we need to invoke the ```getInventoryCount``` method with the parameter name/value pairs of ```{location:w101}``` and ```{productId:P101}```

Similarly, if the query is "Where are the following warehouses located: w10 and w1?", it should result in the program being told to invoke the ```getWarehouseDetails`` method twice, once with ```{location:w10}``` and then with ```{location:w1}```th

## How do I run the code
All the code is present in the ```src/main/java/com/geminidemo/AutomateFunctionCalling.java```. This is a standard Maven project with a pom.xml in the root folder, so you should be able to take the dependencies from there, should you want to recreate it in the different way. 

To run the Java main program, you will need to do the following in the ```src/main/java/com/geminidemo/AutomateFunctionCalling.java``` file:
- Replace ```YOUR_GOOGLE_CLOUD_PROJECT_ID``` with your Google Project Id
- Replace ```us-central1``` with another Google Cloud location should you want to change that.
- In the ```main()``` method, you will find the different prompts all commented out. Uncomment any of the prompts before running the program.

### Sample output #1
For the prompt ```"How much of P1 and P2 do we have in warehouse w10?"```, we get the following output. You can see that we are being asked by the model to 
invoke the ```getInventoryCount``` method twice. Once for ```P1``` and the other for ```P2```:

```
User provided Prompt: How much of P1 and P2 do we have in warehouse w10?
Initial response: 
role: "model"
parts {
  function_call {
    name: "getInventoryCount"
    args {
      fields {
        key: "location"
        value {
          string_value: "w10"
        }
      }
      fields {
        key: "productid"
        value {
          string_value: "P1"
        }
      }
    }
  }
}

Need to invoke function: getInventoryCount
Executing function with parameters: P1 w10
Response: role: "model"
parts {
  text: "We have 50 units of P1 in w10. \n\n"
}
parts {
  function_call {
    name: "getInventoryCount"
    args {
      fields {
        key: "location"
        value {
          string_value: "w10"
        }
      }
      fields {
        key: "productid"
        value {
          string_value: "P2"
        }
      }
    }
  }
}

Need to invoke function: getInventoryCount
Executing function with parameters: P2 w10
Response: role: "model"
parts {
  text: "We also have 50 units of P2 in w10."
}

No more function calls found in response
```
### Sample output #2
For the prompt ```"Where is warehouse w10 located and how many unit of p1 are there??"```, we get the following output. You can see that we are being asked by the model to 
invoke the ```getWarehouseDetails``` method first with the warehouse location as ```w10``` and then we are asked to invoke the ```getInventoryCount``` method with the ```location``` as ```w10``` and the ```productId``` as ```p1```:

```
User provided Prompt: Where is warehouse w10 located and how many unit of p1 are there?
Initial response: 
role: "model"
parts {
  function_call {
    name: "getWarehouseDetails"
    args {
      fields {
        key: "location"
        value {
          string_value: "w10"
        }
      }
    }
  }
}

Need to invoke function: getWarehouseDetails
Executing function with parameters: w10
Response: role: "model"
parts {
  text: "w10 is located at 123 Main Street. \n\n"
}
parts {
  function_call {
    name: "getInventoryCount"
    args {
      fields {
        key: "location"
        value {
          string_value: "w10"
        }
      }
      fields {
        key: "productid"
        value {
          string_value: "p1"
        }
      }
    }
  }
}

Need to invoke function: getInventoryCount
Executing function with parameters: p1 w10
Response: role: "model"
parts {
  text: "There are 50 units of p1 in w10."
}

No more function calls found in response
```

## Parallel Function Calling
You would have noticed that the model response gives us a sequence of function calls to make one after the other. But if you look at it, it could have just determined that the functions can be invoked in parallel and could have given us a list of functions to invoke in the first original response itself. For e.g. if we had provided the following prompt: ```Where are warehouse w1 and w2 located?```, it should have given us two function calls that we need to make to getWarehouseLocation, one with warehouse location as ```w1``` and the other with warehouse location as ```w2```. In that case, we could have made both the API calls ourselves, collected the response and given it back to the model in one shot to form the final response. 

From Gemini 1.5 Pro and Gemini 1.5 Flash models, *** the model model can proposed several parallel function calls ***. This means that we will need to modify our code to expect not just one function call or multiple ones that we will then need to make before handing the API results from those function calls back to the model. The documentation does highlight a [parallel function call sample](https://cloud.google.com/vertex-ai/generative-ai/docs/multimodal/function-calling#parallel-samples). 

I have provided another Java program in ```src/main/java/com/geminidemo/ParallelFunctionCalling.java``` and you will notice that we use another model here: ```gemini-1.5-pro-preview-0514```. 

If you now run the following prompt in the sample, ```How much of P1 and P2 do we have in warehouse w10?``` , you will find the response output is now as follows:

```
role: "model"
parts {
  function_call {
    name: "getInventoryCount"
    args {
      fields {
        key: "location"
        value {
          string_value: "w10"
        }
      }
      fields {
        key: "productid"
        value {
          string_value: "P1"
        }
      }
    }
  }
}
parts {
  function_call {
    name: "getInventoryCount"
    args {
      fields {
        key: "location"
        value {
          string_value: "w10"
        }
      }
      fields {
        key: "productid"
        value {
          string_value: "P2"
        }
      }
    }
  }
}
```
You can see that in the initial response itself, we have 2 parts that are function calls. We can now parse this response and make the function calls in parallel and return back the response to the model for the final output. 

The code also needed to be changed to iterate through each of the ```function_call``` responses and not just the first one. We iterate through the ```function_call```, invoke the function, collect the response and then send the aggregated response from our function call back into the model for the final response. 

```
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
```

## References
- [Guillaume Laforge's article on Function calling](https://medium.com/google-cloud/gemini-function-calling-1585c044d28d)
- [Function calling documentation](https://cloud.google.com/vertex-ai/generative-ai/docs/multimodal/function-calling)
- [Codelab on Function calling - Python by Kristopher Overholt](https://codelabs.developers.google.com/codelabs/gemini-function-calling#0)
- [Java sample for Vertex AI Function calling](https://github.com/GoogleCloudPlatform/java-docs-samples/blob/main/vertexai/snippets/src/main/java/vertexai/gemini/FunctionCalling.java)

