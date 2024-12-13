#!/bin/bash

echo "=== Starting Kubernetes Service from Scratch ==="
echo "=== (Please run this from the project root and call like this 'scripts/start-kubernetes.sh') ==="
# Ensure the script is always run from the same directory
# shellcheck disable=SC2164
cd "$(dirname "$0")/.."

# Step 1: Build Application JAR with Maven (Dockerized)
echo "Building application JAR using Maven Docker container..."
docker-compose up maven-install --build -d
docker-compose logs -f maven-install
docker-compose down

# Step 2: Start Minikube with Increased Resources
echo "Starting Minikube with increased resources..."
minikube start --memory=6144 --cpus=4

# Step 3: Preload Docker Images
echo "Preloading Flyway and App images into Minikube..."
minikube image load flyway/flyway:latest
docker-compose build app
minikube image load customer-management-api-app:latest

# Step 4: Deploy PostgreSQL
echo "Deploying PostgreSQL..."
kubectl apply -f kubernetes/postgres.yaml

# Step 5: Wait for PostgreSQL to Be Ready
echo "Waiting for PostgreSQL to be ready..."
kubectl wait --for=condition=ready pod -l app=postgres --timeout=300s
if [ $? -ne 0 ]; then
    echo "Error: PostgreSQL failed to become ready. Exiting."
    exit 1
fi

# Step 6: Run Flyway Migrations
echo "Running Flyway migrations..."
kubectl create configmap flyway-sql-config --from-file=db/migration
kubectl apply -f kubernetes/flyway-migration-job.yaml
# Debugging…
# kubectl describe pod -l job-name=flyway-migration

# Step 7: Wait for Flyway Job to Complete
echo "Waiting for Flyway migration job to complete..."
kubectl wait --for=condition=complete job/flyway-migration --timeout=300s
if [ $? -ne 0 ]; then
    echo "Error: Flyway migration job failed. Exiting."
    exit 1
fi

# Step 8: Deploy Application
echo "Deploying the application..."
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml

# Step 9: Wait for Application Pods to Be Ready
echo "Waiting for application pods to be ready..."
kubectl wait --for=condition=ready pod -l app=customer-management-api --timeout=300s
if [ $? -ne 0 ]; then
    echo "Error: Application pods failed to become ready. Exiting."
    exit 1
fi

# Step 10: Get Minikube Service URL
echo "Fetching Minikube service URL..."
minikube service customer-management-api --url

echo "=== Service Started Successfully ==="


# My favorite tmux window commands:
#
# watch kubectl get pods
# watch kubectl get service
# watch 'kubectl logs job/flyway-migration | tail -20'
# kubectl logs -l app=customer-management-api --all-containers --follow
# kubectl logs postgres-0 --follow
# minikube service customer-management-api --url
