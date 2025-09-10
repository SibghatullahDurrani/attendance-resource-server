#!/bin/bash
set -e

IMAGE="172.235.26.156:5000/attendance-resource-server:latest"

echo "Cleaning and compiling project..."
mvn clean compile

echo "Building Docker image: $IMAGE with Jib..."
mvn compile jib:dockerBuild -Dimage=$IMAGE

# Get the latest image ID
LATEST_ID=$(docker images --format "{{.Repository}}:{{.Tag}} {{.ID}}" \
    | grep "$IMAGE" \
    | awk '{print $2}')

echo "Removing old attendance-resource-server images (keeping $LATEST_ID)..."

# Find all attendance-resource-server images except the latest and remove them
docker images --format "{{.Repository}}:{{.Tag}} {{.ID}}" \
    | grep "172.235.26.156:5000/attendance-resource-server" \
    | awk -v latest="$LATEST_ID" '{ if ($2 != latest) print $2 }' \
    | xargs -r docker rmi -f

echo "Done. Current attendance-resource-server images:"
docker images | grep "172.235.26.156:5000/attendance-resource-server"
