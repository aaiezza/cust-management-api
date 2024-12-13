#!/bin/bash

echo "=== Redeploying Kubernetes Service ==="

# Ensure the script is always run from the same directory
# shellcheck disable=SC2164
cd "$(dirname "$0")/.."

# Step 1: Build Application JAR with Maven (Dockerized)
echo "Building application JAR using Maven Docker container..."
docker-compose up maven-install --build -d
docker-compose logs -f maven-install
docker-compose down

# Step 2: Build Docker Image with Docker-Compose
echo "Rebuilding Docker image for the application using docker-compose..."
docker-compose build app

# Step 3: Load Docker Image into Minikube
echo "Loading Docker image into Minikube..."
minikube image load customer-management-api-app:latest

# Step 4: Restart Application Deployment
echo "Restarting Kubernetes Deployment for the application..."
kubectl rollout restart deployment/customer-management-api

# Step 5: Wait for Application Pods to Be Ready
echo "Waiting for application pods to be ready..."
kubectl wait --for=condition=ready pod -l app=customer-management-api --timeout=300s
if [ $? -ne 0 ]; then
    echo "Error: Application pods failed to become ready. Exiting."
    exit 1
fi

# Step 6: Get Minikube Service URL
echo "Fetching Minikube service URL..."
minikube service customer-management-api --url

# You _can_ do this, but if the pods go down, this command dies.
#echo "Using kubectl to forward the port..."
#kubectl port-forward service/customer-management-api 8080:80

echo "=== Redeployment Completed ==="
