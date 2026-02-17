#!/bin/bash

# Ensure we are in the script's directory
cd "$(dirname "$0")"

echo "Starting Easy Recon Go Demo..."

# Run the Go demo with CGO disabled to avoid linker issues on some environments
CGO_ENABLED=0 go run main.go
