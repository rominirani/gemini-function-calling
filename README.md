# Gemini Function calling
This project contains Java sample code to demonstrate Gemini Function Calling. 

## What is Function calling
As per documentation available [here](https://cloud.google.com/vertex-ai/generative-ai/docs/multimodal/function-calling): 

You can use function calling to define custom functions and provide these to a generative AI model. While processing a query, the model can choose to delegate certain data processing tasks to these functions. It does not call the functions. Instead, it provides structured data output that includes the name of a selected function and the arguments that the model proposes the function to be called with. You can use this output to invoke external APIs. You can then provide the API output back to the model, allowing it to complete its answer to the query. When it is used this way, function calling enables LLMs to access real-time information and interact with various services, such as SQL databases, customer relationship management systems, and document repositories.

The following diagram taken from [Codelab on Function calling - Python by Kristopher Overholt](https://codelabs.developers.google.com/codelabs/gemini-function-calling#0) provides a high level understanding of how Function calling works.

<img src="https://codelabs.developers.google.com/static/codelabs/gemini-function-calling/img/gemini-function-calling-overview_1440.png"/>

## References
- [Guillaume Laforge's article on Function calling](https://medium.com/google-cloud/gemini-function-calling-1585c044d28d)
- [Function calling documentation](https://cloud.google.com/vertex-ai/generative-ai/docs/multimodal/function-calling)
- [Codelab on Function calling - Python by Kristopher Overholt](https://codelabs.developers.google.com/codelabs/gemini-function-calling#0)

