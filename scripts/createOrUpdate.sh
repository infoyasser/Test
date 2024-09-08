#!/bin/bash

# Variables
FUNCTION_NAME="Test"
ZIP_FILE="build/distributions/demo-1.0-SNAPSHOT.zip"
HANDLER="com.sample.demo.LambdaHandler::handleRequest"
ROLE_ARN="arn:aws:iam::633742649825:role/lambda-Role"
RUNTIME="java17"
REGION="ap-southeast-1"

# Check if the Lambda function exists
aws lambda get-function --function-name $FUNCTION_NAME --region $REGION > /dev/null 2>&1

if [ $? -eq 0 ]; then
  echo "Lambda function exists. Updating..."
  aws lambda update-function-code \
    --function-name $FUNCTION_NAME \
    --zip-file fileb://$ZIP_FILE \
    --region $REGION
else
  echo "Lambda function does not exist. Creating..."
  aws lambda create-function \
    --function-name $FUNCTION_NAME \
    --runtime $RUNTIME \
    --handler $HANDLER \
    --role $ROLE_ARN \
    --zip-file fileb://$ZIP_FILE \
    --region $REGION
fi
