#!/bin/bash

echo "=== Kubernetes Logs Viewer ==="

# Ensure the script is always run from the same directory
# shellcheck disable=SC2164
cd "$(dirname "$0")/.."

# Option 1: Logs for All Pods in a Deployment
echo "Fetching logs for all pods in the customer-management-api deployment..."
kubectl logs -l app=customer-management-api --all-containers > logs-deployment.txt
echo "Logs saved to logs-deployment.txt."

# Option 2: Stream Logs for All Pods in Real-Time
read -p "Do you want to stream logs for all pods in real-time? (y/N): " stream_logs
if [[ "$stream_logs" =~ ^[Yy]$ ]]; then
    echo "Streaming logs for all pods in the customer-management-api deployment..."
    kubectl logs -l app=customer-management-api --all-containers --follow
fi

# Option 3: Logs for All Pods Across All Namespaces
read -p "Do you want to fetch logs for all pods across all namespaces? (y/N): " all_ns_logs
if [[ "$all_ns_logs" =~ ^[Yy]$ ]]; then
    echo "Fetching logs for all pods across all namespaces..."
    kubectl logs --all-namespaces > logs-all-namespaces.txt
    echo "Logs saved to logs-all-namespaces.txt."
fi

# Option 4: Stream Logs for All Pods Across All Namespaces
read -p "Do you want to stream logs for all pods across all namespaces in real-time? (y/N): " stream_all_ns_logs
if [[ "$stream_all_ns_logs" =~ ^[Yy]$ ]]; then
    echo "Streaming logs for all pods across all namespaces..."
    kubectl get pods --all-namespaces -o jsonpath='{range .items[*]}{.metadata.namespace}{" "}{.metadata.name}{"\n"}{end}' | while read namespace pod; do
        kubectl logs -n $namespace $pod --follow &
    done
    echo "Streaming logs... Use Ctrl+C to stop."
fi

echo "=== Logs Viewing Completed ==="
