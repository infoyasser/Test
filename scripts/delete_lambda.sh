#!/bin/bash

# Set the Lambda function name and API Gateway name
LAMBDA_FUNCTION_NAME="Test"
API_NAME="MyAPIForLambda_$LAMBDA_FUNCTION_NAME"
AWS_REGION=ap-southeast-1

# Get the API Gateway ID based on the API name
API_GATEWAY_ID=$(aws apigateway get-rest-apis --query 'items[?name==`'"${API_NAME}"'`].id' --output text)

echo "API_NAME '$API_NAME'"
echo "API_GATEWAY_ID '$API_GATEWAY_ID'"

# Function to delete API Gateway
delete_api_gateway() {
  echo "Deleting API Gateway with ID '$API_GATEWAY_ID'..."

  # Delete all stages of the API Gateway (if any)
  STAGES=$(aws apigateway get-stages --rest-api-id "$API_GATEWAY_ID" --region "$AWS_REGION" | jq -r '.item[].stageName')

  if [ -n "$STAGES" ]; then
    for STAGE in $STAGES; do
      echo "Deleting stage '$STAGE' in API Gateway '$API_GATEWAY_ID'..."
      aws apigateway delete-stage --rest-api-id "$API_GATEWAY_ID" --stage-name "$STAGE" --region "$AWS_REGION"
    done
  fi

  # Delete the API Gateway
  aws apigateway delete-rest-api --rest-api-id "$API_GATEWAY_ID" --region "$AWS_REGION"

  if [ $? -eq 0 ]; then
    echo "API Gateway '$API_GATEWAY_ID' has been successfully deleted."
  else
    echo "Failed to delete API Gateway '$API_GATEWAY_ID'."
    exit 1
  fi
}

# Check if the API Gateway ID is valid before deleting
if [ "$API_GATEWAY_ID" != "None" ] && [ -n "$API_GATEWAY_ID" ]; then
  delete_api_gateway
else
  echo "API Gateway with name '$API_NAME' not found."
  exit 0
fi

# Delete the Lambda function
echo "Deleting Lambda function '$LAMBDA_FUNCTION_NAME' in region '$AWS_REGION'..."
aws lambda delete-function --function-name "$LAMBDA_FUNCTION_NAME" --region "$AWS_REGION"

if [ $? -eq 0 ]; then
  echo "Lambda function '$LAMBDA_FUNCTION_NAME' has been successfully deleted."
else
  echo "Failed to delete Lambda function '$LAMBDA_FUNCTION_NAME'."
  exit 1
fi

echo "API Gateway and Lambda function have been successfully deleted."
