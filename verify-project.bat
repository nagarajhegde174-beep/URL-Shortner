@echo off
echo ===========================================
echo === Start Windows local project check   ===
echo ===========================================

:: 1. Verify Backend Tests
echo.
echo [1/3] Running Backend Tests...
cd backend
call mvn clean test
if %errorlevel% neq 0 (
    echo [ERROR] Backend tests failed!
    exit /b %errorlevel%
)
cd ..

:: 2. Verify Frontend Lint and Build
echo.
echo [2/3] Verifying Frontend Lint & Build...
cd frontend
call npm ci
echo Running Frontend Linter...
call npm run lint
if %errorlevel% neq 0 (
    echo [ERROR] Frontend lint failed!
    exit /b %errorlevel%
)
echo Running Frontend Build...
call npm run build
if %errorlevel% neq 0 (
    echo [ERROR] Frontend build failed!
    exit /b %errorlevel%
)
cd ..

:: 3. Verify Docker Compose Configuration
echo.
echo [3/3] Validating Docker Compose Config...
if not exist "environment\.env" (
    echo Creating temporary dummy .env file for config validation...
    if not exist "environment" mkdir environment
    echo JWT_SECRET=dummy-key-min-32-chars-long-for-hs256-algo> environment\.env
    echo RAZORPAY_KEY_ID=dummy_id>> environment\.env
    echo RAZORPAY_KEY_SECRET=dummy_secret>> environment\.env
    echo RAZORPAY_WEBHOOK_SECRET=dummy_webhook>> environment\.env
)

call docker compose config
if %errorlevel% neq 0 (
    echo [ERROR] Docker compose configuration is invalid!
    exit /b %errorlevel%
)

echo.
echo ===========================================
echo === SUCCESS: All local checks passed!   ===
echo ===========================================
