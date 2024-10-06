#!/bin/bash

# Variables
LAMBDA_FUNCTION_NAME="Test"
ZIP_FILE="build/distributions/demo-1.0-SNAPSHOT.zip"
HANDLER="com.sample.demo.LambdaHandler::handleRequest"
ROLE_ARN="arn:aws:iam::633742649825:role/LambdaToS3Role"
RUNTIME="java17"
AWS_REGION="ap-southeast-1"
TIMEOUT=10
MEMORY_SIZE=512

# Check if the Lambda function exists
aws lambda get-function --function-name $LAMBDA_FUNCTION_NAME --region $AWS_REGION > /dev/null 2>&1

if [ $? -eq 0 ]; then
  echo "Lambda function exists. Updating..."
  aws lambda update-function-code \
    --function-name $LAMBDA_FUNCTION_NAME \
    --zip-file fileb://$ZIP_FILE \
    --region $AWS_REGION

  aws lambda update-function-configuration \
    --function-name $LAMBDA_FUNCTION_NAME \
    --timeout $TIMEOUT \
    --memory-size $MEMORY_SIZE \
    --region $AWS_REGION
else
  echo "Lambda function does not exist. Creating..."
  aws lambda create-function \
    --function-name $LAMBDA_FUNCTION_NAME \
    --runtime $RUNTIME \
    --handler $HANDLER \
    --role $ROLE_ARN \
    --zip-file fileb://$ZIP_FILE \
    --timeout $TIMEOUT \
    --memory-size $MEMORY_SIZE \
    --region $AWS_REGION
fi

if [ $? -eq 0 ]; then
# API name (will be the same whether creating or updating)
API_NAME="MyAPIForLambda_$LAMBDA_FUNCTION_NAME"

# Step 1: Check if the API Gateway already exists
echo "Checking if API Gateway with name '$API_NAME' already exists..."
API_ID=$(aws apigateway get-rest-apis \
  --query "items[?name=='$API_NAME'].id | [0]" \
  --output text \
  --region "$AWS_REGION")

if [ "$API_ID" == "None" ]; then
  # API Gateway doesn't exist, so we need to create it
  echo "API Gateway not found. Creating new API Gateway..."
  API_ID=$(aws apigateway create-rest-api \
    --name "$API_NAME" \
    --region "$AWS_REGION" \
    --query 'id' \
    --output text)

  if [ -z "$API_ID" ]; then
    echo "Failed to create API Gateway."
    exit 1
  else
    echo "API Gateway created with ID: $API_ID"
  fi
else
  # API Gateway exists
  echo "API Gateway found with ID: $API_ID"
fi

# Step 2: Get the Root Resource ID for the API
echo "Getting Root Resource ID..."
ROOT_RESOURCE_ID=$(aws apigateway get-resources \
  --rest-api-id "$API_ID" \
  --region "$AWS_REGION" \
  --query 'items[?path==`"/"`].id' \
  --output text)

if [ -z "$ROOT_RESOURCE_ID" ]; then
  echo "Failed to get the root resource ID."
  exit 1
else
  echo "Root Resource ID: $ROOT_RESOURCE_ID"
fi

# Step 3: Check if the {proxy+} Resource already exists
echo "Checking if resource '/{proxy+}' already exists..."
RESOURCE_ID=$(aws apigateway get-resources \
  --rest-api-id "$API_ID" \
  --region "$AWS_REGION" \
  --query "items[?pathPart=='{proxy+}'].id | [0]" \
  --output text)

if [ "$RESOURCE_ID" == "None" ]; then
  # {proxy+} resource doesn't exist, create it
  echo "Creating resource '/{proxy+}' under root..."
  RESOURCE_ID=$(aws apigateway create-resource \
    --rest-api-id "$API_ID" \
    --parent-id "$ROOT_RESOURCE_ID" \
    --path-part "{proxy+}" \
    --region "$AWS_REGION" \
    --query 'id' \
    --output text)

  if [ -z "$RESOURCE_ID" ]; then
    echo "Failed to create resource '/{proxy+}'."
    exit 1
  else
    echo "Resource '/{proxy+}' created with ID: $RESOURCE_ID"
  fi
else
  echo "Resource '/{proxy+}' already exists with ID: $RESOURCE_ID"
fi

# Step 4: Create or update the ANY Method on the {proxy+} Resource
echo "Creating or updating ANY method on resource '/{proxy+}'..."
aws apigateway put-method \
  --rest-api-id "$API_ID" \
  --resource-id "$RESOURCE_ID" \
  --http-method ANY \
  --authorization-type NONE \
  --region "$AWS_REGION"

# Step 5: Set Lambda Integration for the ANY Method
echo "Setting up or updating Lambda integration for ANY method..."
LAMBDA_ARN="arn:aws:lambda:$AWS_REGION:$(aws sts get-caller-identity --query 'Account' --output text):function:$LAMBDA_FUNCTION_NAME"

aws apigateway put-integration \
  --rest-api-id "$API_ID" \
  --resource-id "$RESOURCE_ID" \
  --http-method ANY \
  --type AWS_PROXY \
  --integration-http-method POST \
  --uri "arn:aws:apigateway:$AWS_REGION:lambda:path/2015-03-31/functions/$LAMBDA_ARN/invocations" \
  --region "$AWS_REGION"

# Step 6: Grant API Gateway permission to invoke the Lambda function
echo "Granting API Gateway permission to invoke Lambda..."
aws lambda add-permission \
  --function-name "$LAMBDA_FUNCTION_NAME" \
  --statement-id "apigateway-invoke-lambda-$RANDOM" \
  --action "lambda:InvokeFunction" \
  --principal apigateway.amazonaws.com \
  --source-arn "arn:aws:execute-api:$AWS_REGION:$(aws sts get-caller-identity --query 'Account' --output text):$API_ID/*/ANY/{proxy+}" \
  --region "$AWS_REGION" \
  || echo "Permission may already exist, skipping..."

# Step 7: Deploy the API Gateway to a stage (e.g., 'prod')
echo "Deploying API Gateway to stage 'prod'..."
aws apigateway create-deployment \
  --rest-api-id "$API_ID" \
  --stage-name prod \
  --region "$AWS_REGION" \
  || echo "Failed to deploy, but may already exist"

# Output API Gateway URL
API_URL="https://$API_ID.execute-api.$AWS_REGION.amazonaws.com/prod/{proxy+}"
echo "API Gateway URL: $API_URL"

fi