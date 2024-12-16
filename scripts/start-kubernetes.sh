#!/bin/bash

echo "=== Starting Kubernetes Service from Scratch ==="
echo "=== (Please run this from the project root and call like this 'scripts/start-kubernetes.sh') ==="
# Ensure the script is always run from the same directory
# shellcheck disable=SC2164
cd "$(dirname "$0")/.."

# Step 1: Start Minikube with Increased Resources
echo "Starting Minikube with increased resources..."
minikube start --memory=6144 --cpus=4

# Step 2: Apply Secrets if Available
echo "Checking for secrets.yaml..."
if [ -f secrets.yaml ]; then
    echo "Applying secrets from secrets.yaml..."
    kubectl apply -f secrets.yaml
else
    echo "No secrets.yaml found. Skipping secrets setup."
fi

# Step 3: Preload Docker Images
echo "Preloading Flyway image into Minikube..."
minikube image load flyway/flyway:latest

# Step 4: Deploy PostgreSQL
echo "Deploying PostgreSQL..."
kubectl apply -f kubernetes/postgres.yaml

# Step 5: Wait for PostgreSQL to Be Ready
echo "Waiting for PostgreSQL to be ready..."
kubectl wait --for=condition=ready pod -l app=postgres --timeout=300s || {
    echo "Error: PostgreSQL pod not ready. Debugging info:"
    kubectl describe pods -l app=postgres
    kubectl logs -l app=postgres
    exit 1
}

# Step 6: Run Flyway Migrations
echo "Running Flyway migrations..."
if [ ! -d "db/migration" ]; then
    echo "Error: Migration directory 'db/migration' does not exist. Exiting."
    exit 1
fi
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

# Step 8: Build Application JAR using Kubernetes Job
echo "Building application JAR using Kubernetes Job..."
kubectl apply -f kubernetes/app-image-build-job.yaml
kubectl wait --for=condition=complete job/app-image-build --timeout=600s || {
    echo "Error: Application JAR Build Job failed. Debugging info:"
    kubectl logs job/app-image-build
    exit 1
}

# Step 9: Build Docker Image and Load into Minikube
echo "Building Docker image..."
docker build -t customer-management-api-app:latest .

echo "Loading Docker image into Minikube..."
minikube image load customer-management-api-app:latest

# Step 10: Deploy Application
echo "Deploying the application..."
kubectl apply -f kubernetes/deployment.yaml
kubectl apply -f kubernetes/service.yaml

# Step 11: Wait for Application Pods to Be Ready
echo "Waiting for application pods to be ready..."
kubectl wait --for=condition=ready pod -l app=customer-management-api --timeout=300s
if [ $? -ne 0 ]; then
    echo "Error: Application pods failed to become ready. Exiting."
    exit 1
fi

# Step 12: Get Minikube Service URL
echo "Fetching Minikube service URL..."
minikube service customer-management-api --url

echo "=== Service Started Successfully ==="


# My favorite tmux window commands:
#
# watch kubectl get pods
# watch kubectl get service
# watch 'kubectl logs job/flyway-migration | tail -20'
# kubectl describe pod -l job-name=flyway-migration
# watch 'kubectl logs job/app-image-build | tail -20'
# kubectl describe pod -l job-name=app-image-build
## kubectl logs -l app=customer-management-api --all-containers --follow
# watch 'kubectl logs service/customer-management-api | tail -20'
# watch 'kubectl get pods -l app=customer-management-api -o name | xargs -r kubectl logs --tail=20'
## kubectl logs postgres-0 --follow
# watch 'kubectl get pods -l app=postgres -o name | xargs -r kubectl logs --tail=20'
# watch "kubectl get events --sort-by='.lastTimestamp' | tail -15"

# minikube service customer-management-api --url
