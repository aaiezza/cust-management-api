#!/bin/bash

# Name of the zip file
ZIP_FILE="customer-management-api-with-history.zip"

# Directories and files to exclude
EXCLUDES=(
    "node_modules/*"
    "*.log"
    ".idea/*"
    "target/*"
    ".DS_Store"
)

# Ensure the script is always run from the same directory
# shellcheck disable=SC2164
cd "$(dirname "$0")/.."

# Base directory (the current working directory by default)
BASE_DIR=$(pwd)

# Create the zip file
echo "Zipping repository at $BASE_DIR, including Git history..."
zip -r $ZIP_FILE . -x "${EXCLUDES[@]}"

if [ $? -eq 0 ]; then
    echo "Repository successfully zipped with Git history as $ZIP_FILE."
else
    echo "Error occurred while creating zip file."
fi
