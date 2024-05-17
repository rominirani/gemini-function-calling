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

Similarly, if the query is "Where are the following warehouses located: w10 and w1?", it should result in the program being told to invoke the ```getWarehouseDetails`` method twice, once with ```{location:w10}``` and then with ```{location:w1}```

## How do I run the code
All the code is present in the ```src/main/java/com/geminidemo/AutomateFunctionCalling.java```. 

## References
- [Guillaume Laforge's article on Function calling](https://medium.com/google-cloud/gemini-function-calling-1585c044d28d)
- [Function calling documentation](https://cloud.google.com/vertex-ai/generative-ai/docs/multimodal/function-calling)
- [Codelab on Function calling - Python by Kristopher Overholt](https://codelabs.developers.google.com/codelabs/gemini-function-calling#0)
- [Java sample for Vertex AI Function calling](https://github.com/GoogleCloudPlatform/java-docs-samples/blob/main/vertexai/snippets/src/main/java/vertexai/gemini/FunctionCalling.java)

