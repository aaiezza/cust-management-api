#!/bin/bash

echo "=== Shutting Down and Purging Kubernetes Environment ==="

# Ensure the script is always run from the same directory
# shellcheck disable=SC2164
cd "$(dirname "$0")/.."

# Step 1: Delete Kubernetes Resources
echo "Deleting Kubernetes resources..."
kubectl delete -f kubernetes/service.yaml
kubectl delete -f kubernetes/deployment.yaml
kubectl delete -f kubernetes/flyway-migration-job.yaml
kubectl delete -f kubernetes/postgres.yaml

# Step 2: Stop and Delete Minikube
echo "Stopping Minikube..."
minikube stop

echo "Deleting Minikube cluster..."
minikube delete

# Step 3: Purge Database Remnants (Optional)
# Uncomment the following lines to remove persistent volumes and data
# echo "Purging database remnants..."
# kubectl delete pvc postgres-pvc
# kubectl delete pv $(kubectl get pv -o jsonpath='{.items[?(@.spec.claimRef.name=="postgres-pvc")].metadata.name}')
# echo "Database remnants purged."

# Step 4: Remove Docker Images (Optional)
# shellcheck disable=SC2162
read -p "Do you want to delete the local Docker images? [y/N]: " delete_images
if [[ "$delete_images" =~ ^[Yy]$ ]]; then
    echo "Deleting Docker images..."
    docker rmi customer-management-api-app:latest flyway/flyway:latest
fi

echo "=== Environment Purged ==="
