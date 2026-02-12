#!/bin/bash

# Verification script for Linkzly React Native SDK Example App Integration
# This script verifies both local and GitHub remote repository integration

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SDK_DIR="$SCRIPT_DIR"
EXAMPLE_DIR="$SDK_DIR/example"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Linkzly SDK Example App Verification${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""

# Function to print status
print_status() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓${NC} $2"
    else
        echo -e "${RED}✗${NC} $2"
    fi
}

# Function to print info
print_info() {
    echo -e "${YELLOW}ℹ${NC} $1"
}

# Track overall status
OVERALL_STATUS=0

# ============================================
# 1. Verify SDK Build Status
# ============================================
echo -e "${BLUE}[1/7] Verifying SDK Build Status${NC}"

cd "$SDK_DIR"

if [ -d "lib" ]; then
    if [ -f "lib/commonjs/index.js" ] && [ -f "lib/module/index.js" ] && [ -f "lib/typescript/src/index.d.ts" ]; then
        print_status 0 "SDK build artifacts exist"
    else
        print_status 1 "SDK build artifacts incomplete"
        print_info "Run 'npm run build' in the SDK directory"
        OVERALL_STATUS=1
    fi
else
    print_status 1 "SDK lib folder missing"
    print_info "Run 'npm run build' in the SDK directory"
    OVERALL_STATUS=1
fi

# ============================================
# 2. Verify SDK TypeScript Compilation
# ============================================
echo -e "${BLUE}[2/7] Verifying SDK TypeScript Compilation${NC}"

if npm run typecheck > /dev/null 2>&1; then
    print_status 0 "SDK TypeScript compilation passes"
else
    print_status 1 "SDK TypeScript compilation has errors"
    print_info "Run 'npm run typecheck' to see errors"
    OVERALL_STATUS=1
fi

# ============================================
# 3. Verify Example App Dependencies
# ============================================
echo -e "${BLUE}[3/7] Verifying Example App Dependencies${NC}"

cd "$EXAMPLE_DIR"

if [ -d "node_modules" ]; then
    if [ -d "node_modules/@linkzly/react-native-sdk" ]; then
        print_status 0 "SDK dependency linked in example app"
        
        # Check if it's using file:.. dependency
        if grep -q '"file:\.\.' package.json; then
            print_info "Using local file dependency (file:..)"
        fi
    else
        print_status 1 "SDK dependency not found in example app"
        print_info "Run 'npm install' in the example directory"
        OVERALL_STATUS=1
    fi
else
    print_status 1 "Example app node_modules missing"
    print_info "Run 'npm install' in the example directory"
    OVERALL_STATUS=1
fi

# ============================================
# 4. Verify Example App Can Import SDK
# ============================================
echo -e "${BLUE}[4/7] Verifying Example App Can Import SDK${NC}"

if [ -f "App.tsx" ]; then
    if grep -q "from '@linkzly/react-native-sdk'" App.tsx; then
        print_status 0 "Example app imports SDK correctly"
    else
        print_status 1 "Example app doesn't import SDK"
        OVERALL_STATUS=1
    fi
else
    print_status 1 "Example app App.tsx not found"
    OVERALL_STATUS=1
fi

# ============================================
# 5. Verify Native Dependencies (iOS)
# ============================================
echo -e "${BLUE}[5/7] Verifying iOS Native Dependencies${NC}"

if [ -d "ios" ]; then
    if [ -f "ios/Podfile" ]; then
        if [ -d "ios/Pods" ]; then
            print_status 0 "iOS Pods installed"
        else
            print_status 1 "iOS Pods not installed"
            print_info "Run 'cd ios && bundle exec pod install'"
            OVERALL_STATUS=1
        fi
    else
        print_status 1 "iOS Podfile not found"
        OVERALL_STATUS=1
    fi
else
    print_info "iOS directory not found (skipping iOS verification)"
fi

# ============================================
# 6. Verify Native Dependencies (Android)
# ============================================
echo -e "${BLUE}[6/7] Verifying Android Native Dependencies${NC}"

if [ -d "android" ]; then
    if [ -f "android/build.gradle" ] && [ -f "android/settings.gradle" ]; then
        print_status 0 "Android build files exist"
        
        # Check if SDK is referenced in settings.gradle
        if grep -q "linkzly-react-native-sdk" android/settings.gradle 2>/dev/null; then
            print_status 0 "SDK referenced in Android settings.gradle"
        else
            print_info "SDK may use autolinking (check react-native.config.js)"
        fi
    else
        print_status 1 "Android build files missing"
        OVERALL_STATUS=1
    fi
else
    print_info "Android directory not found (skipping Android verification)"
fi

# ============================================
# 7. Verify GitHub Remote Repository
# ============================================
echo -e "${BLUE}[7/7] Verifying GitHub Remote Repository${NC}"

cd "$SDK_DIR"

GIT_REMOTE=$(git remote get-url origin 2>/dev/null || echo "")
if [ -n "$GIT_REMOTE" ]; then
    print_status 0 "Git remote configured: $GIT_REMOTE"
    
    # Check if it's a GitHub URL
    if echo "$GIT_REMOTE" | grep -q "github.com"; then
        print_status 0 "GitHub remote detected"
        
        # Extract repo info
        REPO_NAME=$(echo "$GIT_REMOTE" | sed -E 's/.*github\.com[\/:]([^\/]+)\/([^\/]+)(\.git)?$/\1\/\2/')
        print_info "Repository: $REPO_NAME"
        
        # Check if we can fetch (optional, may require auth)
        if git fetch --dry-run > /dev/null 2>&1; then
            print_status 0 "Can access GitHub repository"
        else
            print_info "GitHub access check skipped (may require authentication)"
        fi
    else
        print_info "Not a GitHub remote (or using different format)"
    fi
else
    print_status 1 "Git remote not configured"
    OVERALL_STATUS=1
fi

# ============================================
# Summary
# ============================================
echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Verification Summary${NC}"
echo -e "${BLUE}========================================${NC}"

if [ $OVERALL_STATUS -eq 0 ]; then
    echo -e "${GREEN}✓ All checks passed!${NC}"
    echo ""
    echo -e "${GREEN}The example app is ready to use.${NC}"
    echo ""
    echo "To run the example app:"
    echo "  cd example"
    echo "  npm start          # Start Metro bundler"
    echo "  npm run ios        # Run on iOS"
    echo "  npm run android    # Run on Android"
    exit 0
else
    echo -e "${RED}✗ Some checks failed${NC}"
    echo ""
    echo "Please fix the issues above before proceeding."
    echo ""
    echo "Common fixes:"
    echo "  1. Build SDK: cd linkzly-react-native-sdk && npm run build"
    echo "  2. Install example deps: cd example && npm install"
    echo "  3. Install iOS pods: cd example/ios && bundle exec pod install"
    exit 1
fi

