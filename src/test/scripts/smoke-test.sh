#!/bin/bash

IMAGE_NAME=$1
RESOURCE_PATH="/tabelle/bl1/2024"

is_port_available() {
  local port=$1
  netstat -an | grep LISTEN | grep -q ":$port\b"
}

find_available_port() {
  local starting_port=$1
  local max_attempts=10
  local port=$starting_port
  
  while [ $port -lt $((starting_port + max_attempts)) ]; do
    if ! is_port_available $port; then
      echo $port
      return 0
    fi
    port=$((port + 1))
  done
  
  return 1
}

wait_for_application() {
  local port=$1
  local max_attempts=30
  local attempt=0
  
  until [ $attempt -ge $max_attempts ]; do
    # Make a generic HTTP request to check if the application is reachable
    if curl -s -I "http://localhost:${port}" | grep -q "HTTP/1.1 2\|3"; then
      echo "Application is reachable on port $port"
      return 0
    fi
    
    echo "Waiting for the application to start on port $port..."
    sleep 5
    attempt=$((attempt+1))
  done
  
  echo "Application did not start within the expected time."
  exit 1
}

check_resource_non_empty_json_array() {
  local port=$1
  local url="http://localhost:${port}${RESOURCE_PATH}"

  local response=$(curl -s -w "%{http_code}" $url)

  local http_status="${response:(-3)}"
  local body="${response::-3}"

  # Format body if jq is available
  command -v jq &> /dev/null && body=$(echo "$body" | jq)

  # Check if the HTTP status is 200 and the response is a non-empty JSON array
  if [ "$http_status" -eq 200 ] && [[ $body == \[*\] && $body != "[]" ]]; then
    echo "Resource is a non-empty JSON array: $body"

    command -v jq &> /dev/null && if [[ $(echo "$body" | jq 'length') -ne 18 ]]; then
        echo "Array does not have 18 entries."
        exit 1
    fi

    return 0
  else
    echo "Error: Unexpected response received for the resource: $body with HTTP status: $http_status"
    exit 1
  fi
}

cleanup() {
  local container_id=$(docker ps -q --filter "ancestor=$IMAGE_NAME")
  if [ -n "$container_id" ]; then
    echo "Stopping and removing the Docker container..."
    docker stop $container_id > /dev/null 2>&1
    docker rm $container_id > /dev/null 2>&1
  fi
}

trap cleanup EXIT

# Find an available port
AVAILABLE_PORT=$(find_available_port 8080)

if [ -z "$AVAILABLE_PORT" ]; then
  echo "No available ports found within the specified range."
  exit 1
fi

if docker run -d -p $AVAILABLE_PORT:8080 $IMAGE_NAME; then
  echo "Docker container started successfully."
else
  echo "Error: Failed to start Docker container."
  exit 1
fi

wait_for_application $AVAILABLE_PORT
check_resource_non_empty_json_array $AVAILABLE_PORT

echo "Smoke tests passed!"

