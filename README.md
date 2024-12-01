# AWS-Bedrock-Based-LLM-Conversation-API-with-Ollama-Integration

This project demonstrates the integration of AWS Bedrock, Ollama, and AWS Lambda to create a conversational API. The system mimics a chatbot conversation where AWS Bedrock generates text, and Ollama responds accordingly, facilitated through AWS Lambda. The project uses Akka HTTP to handle requests and responses, gRPC for service communication, and AWS API Gateway for managing the API.

## Architecture Overview
- **Client Interface**: The interaction begins with Postman or curl, where a client sends a request.
- **Akka HTTP Server**: An Akka HTTP server listens for incoming requests and routes them to appropriate endpoints.
- **AWS Lambda**: The Akka HTTP server makes a gRPC call to AWS Lambda, which then communicates with AWS Bedrock to generate text.
- **AWS Bedrock**: AWS Bedrock is used for generating responses for the chatbot.
- **Ollama**: After the response from AWS Bedrock, it is passed to Ollama, and Ollama provides further responses to mimic the conversation flow.
- **AWS API Gateway**: It acts as the front-facing service for API requests.

## Project Structure


Project Structure:

├── src
│   ├── main
│   │   ├── scala
│   │   │   ├── AkkaHttpServer.scala            # Akka HTTP server for managing requests/responses
│   │   │   ├── Endpoint.scala                 # Defines API endpoints for communication
│   │   │   ├── LambdaInvoker.scala            # Code for invoking AWS Lambda functions for backend processing
│   │   │   ├── OllamaAPIClient.scala          # Defines API to interact with Ollama
│   │   ├── resources
│   │   │   ├── application.conf               # Configuration file for settings like API endpoints, model details, etc.
│   ├── test...
├── target                                     # Compiled files and build output
├── .gitignore                                 # Git ignore file
├── build.sbt                                  # Build configuration file for SBT
└── README.md                                  # Project documentation


## Prerequisites
Ensure the following are installed and configured:
- **Scala (version 2.12 or compatible)**
- **Akka HTTP** (for building RESTful API)
- **AWS EC2**
- **AWS Lambda and API Gateway setup** (for invoking Lambda functions)
- **AWS Bedrock** (for text generation via LLM)
- **Ollama setup** (for local LLM model)
- **gRPC** (for interaction between services)
- **SBT** (Scala Build Tool for building the project)
- **Java Development Kit (JDK) 8 or higher**
- **Docker** (for containerized services if needed)

## Setting Up AWS Environment
1. **AWS Lambda**:
   - Create a Lambda function to interact with AWS Bedrock for text generation.
   - Ensure the Lambda function has necessary permissions for invoking AWS Bedrock.

2. **AWS Bedrock**:
   - Set up AWS Bedrock as the text generation service.
   - Ensure Bedrock is configured with access permissions.

3. **API Gateway**:
   - Configure AWS API Gateway to expose the Lambda function via a RESTful API.

4. **Ollama Setup**:
   - Install and configure Ollama for local LLM responses.
   - Integrate Ollama with the Lambda function to process the responses from AWS Bedrock.

## Building and Running the Project
1. Clone the repository:
   ```bash
   git clone https://github.com/SunilKuruba/AWS-Bedrock-Based-LLM-Conversation-API-with-Ollama-Integration.git
   cd AWS-Bedrock-Based-LLM-Conversation-API-with-Ollama-Integration

## Testing
To validate the implementation, run the provided test cases locally using SBT:
```
sbt test
```
Ensure that your test environment has the necessary libraries and dependencies installed for successful test execution.

## Example API Request (Postman or curl)
Send a POST request to the API endpoint (/chat) with a JSON payload containing the user's message:

Request:
```json```
{
"message": "Hello, how are you?"
}
```json```
Response:
```json```
{
"response": "I'm doing great, thank you for asking! How can I assist you today?"
}
```json```
