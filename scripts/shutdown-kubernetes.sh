#!/bin/bash

echo "=== Shutting Down and Purging Kubernetes Environment ==="

# Ensure the script is always run from the same directory
cd "$(dirname "$0")/.."

# Step 1: Save Secrets (Optional)
read -p "Do you want to save Kubernetes secrets before shutdown? [y/N]: " save_secrets
if [[ "$save_secrets" =~ ^[Yy]$ ]]; then
    echo "Saving secrets to secrets/ directory..."
    mkdir -p secrets
    kubectl get secret git-credentials -o yaml > secrets/git-credentials.yaml
    echo "Secrets saved."
fi

# Step 2: Delete Kubernetes Resources
echo "Deleting Kubernetes resources..."
kubectl delete -f kubernetes/service.yaml
kubectl delete -f kubernetes/deployment.yaml
kubectl delete -f kubernetes/flyway-migration-job.yaml
kubectl delete -f kubernetes/app-image-build-job.yaml
kubectl delete -f kubernetes/postgres.yaml

# Step 3: Stop and Delete Minikube
echo "Stopping Minikube..."
minikube stop

echo "Deleting Minikube cluster..."
minikube delete

# Step 4: Purge Database Remnants (Optional)
read -p "Do you want to delete persistent volumes and data? [y/N]: " purge_data
if [[ "$purge_data" =~ ^[Yy]$ ]]; then
    echo "Purging database remnants..."
    kubectl delete pvc postgres-pvc
    kubectl delete pv "$(kubectl get pv -o jsonpath='{.items[?(@.spec.claimRef.name=="postgres-pvc")].metadata.name}')"
    echo "Database remnants purged."
fi

# Step 5: Remove Docker Images (Optional)
read -p "Do you want to delete the local Docker images? [y/N]: " delete_images
if [[ "$delete_images" =~ ^[Yy]$ ]]; then
    echo "Deleting Docker images..."
    docker rmi customer-management-api-app:latest flyway/flyway:latest
fi

echo "=== Environment Purged ==="
