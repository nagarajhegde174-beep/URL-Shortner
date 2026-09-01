#!/bin/bash
set -e

# Colors for output formatting
GREEN='\033[0;32m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== Start Verification Script ===${NC}"

# 1. Verify Backend Tests
echo -e "\n${GREEN}[1/3] Running Backend Tests...${NC}"
cd backend
mvn clean test
cd ..

# 2. Verify Frontend Lint and Build
echo -e "\n${GREEN}[2/3] Verifying Frontend Lint & Build...${NC}"
cd frontend
npm ci
echo "Running Frontend Linter..."
npm run lint
echo "Running Frontend Build..."
npm run build
cd ..

# 3. Verify Docker Compose Configuration
echo -e "\n${GREEN}[3/3] Validating Docker Compose Config...${NC}"
# Ensure environment/.env exists for the compose validation check
if [ ! -f "environment/.env" ]; then
    echo "Creating a temporary dummy .env file for configuration validation..."
    mkdir -p environment
    echo "JWT_SECRET=dummy-key-min-32-chars-long-for-hs256-algo" > environment/.env
    echo "RAZORPAY_KEY_ID=dummy_id" >> environment/.env
    echo "RAZORPAY_KEY_SECRET=dummy_secret" >> environment/.env
    echo "RAZORPAY_WEBHOOK_SECRET=dummy_webhook" >> environment/.env
fi

docker compose config

echo -e "\n${GREEN}=== Verification SUCCESS: All Checks Passed ===${NC}"
