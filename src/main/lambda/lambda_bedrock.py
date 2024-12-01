import json
import boto3
import base64

def lambda_handler(event, context):
    """
    AWS Lambda function to handle requests and interact with the AWS Bedrock LLM model.

    This function processes an incoming event, which contains a Base64-encoded string with
    user input and additional parameters. It decodes the input, invokes the Bedrock model with
    the provided user input, and returns the generated text from the model as part of the Lambda
    response.

    Parameters:
    -----------
    event : dict
        The event object passed by the AWS Lambda service, which contains input data in Base64
        encoded format. The event is expected to contain the following keys:
        - 'body': A Base64-encoded string containing the input and optional parameters such as 'maxWords'.

    context : object
        The context object passed by the AWS Lambda service, providing information about the invocation,
        function, and execution environment. (Not used in this implementation)

    Returns:
    --------
    dict
        The response object returned by the Lambda function, which contains:
        - 'statusCode': HTTP status code (200 for success, 500 for error).
        - 'body': A JSON string containing the processed output or error message.

    Example:
    --------
    {
        'statusCode': 200,
        'body': '{"input": "Hello, world!", "output": "Generated text..."}'
    }

    In case of an error:
    {
        'statusCode': 500,
        'body': '{"error": "Error message"}'
    }

    Error Handling:
    ---------------
    If any error occurs during processing, the function catches the exception and returns a 500
    status code with an error message. Common errors might include issues with the event format,
    failed invocation of the Bedrock model, or connection issues with AWS services.

    Note:
    -----
    The function uses a fixed model ARN and assumes that the input body is properly formatted in
    Base64-encoded JSON.
    """
    try:
        # Decode input
        input_data = base64.b64decode(event.get('body', "")).decode('utf-8')
        req_body = dict(line.split(":", 1) for line in input_data.strip().split("\n"))
        user_input = req_body.get('input', 'Hello, world!').strip('"')
        max_words = int(req_body.get('maxWords', 100))

        # Invoke Bedrock model
        client = boto3.client('bedrock-runtime')
        response = client.invoke_model(
            modelId='arn:aws:bedrock:us-east-2:872515284062:inference-profile/us.meta.llama3-1-8b-instruct-v1:0',
            body=json.dumps({"prompt": user_input}),
            contentType='application/json'
        )

        # Process response
        generated_text = json.loads(response['body'].read().decode('utf-8')).get('generation', "")
        truncated_output = " ".join(generated_text.split()[:max_words])

        return {'statusCode': 200, 'body': json.dumps({'input': user_input, 'output': truncated_output})}

    except Exception as e:
        return {'statusCode': 500, 'body': json.dumps({'error': str(e)})}
