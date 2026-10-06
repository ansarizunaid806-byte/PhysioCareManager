#!/bin/bash
# ============================================================
# PhysioCare Manager - Build Release APK
# ============================================================
# One-command build script.
#
# Usage:
#   ./build-release.sh          → Builds signed release APK
#   ./build-release.sh debug    → Builds debug APK (no signing)
# ============================================================

set -e

echo ""
echo "╔══════════════════════════════════════════════════╗"
echo "║   PhysioCare Manager - APK Builder               ║"
echo "╚══════════════════════════════════════════════════╝"
echo ""

BUILD_TYPE="${1:-release}"

# -----------------------------------------------------------
# Preflight checks
# -----------------------------------------------------------

# Check JDK
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Install JDK 17 from https://adoptium.net/"
    exit 1
fi

JAVA_VER=$(java -version 2>&1 | head -1 | cut -d'"' -f2 | cut -d'.' -f1)
echo "📌 Java version: $JAVA_VER"

# Check Android SDK
if [ -z "$ANDROID_HOME" ] && [ -z "$ANDROID_SDK_ROOT" ]; then
    # Try common locations
    if [ -d "$HOME/Android/Sdk" ]; then
        export ANDROID_HOME="$HOME/Android/Sdk"
    elif [ -d "$HOME/Library/Android/sdk" ]; then
        export ANDROID_HOME="$HOME/Library/Android/sdk"
    elif [ -d "/usr/local/lib/android/sdk" ]; then
        export ANDROID_HOME="/usr/local/lib/android/sdk"
    else
        echo "❌ Android SDK not found."
        echo ""
        echo "Set ANDROID_HOME environment variable:"
        echo "  export ANDROID_HOME=\$HOME/Android/Sdk"
        echo ""
        echo "Or create local.properties:"
        echo "  echo 'sdk.dir=/path/to/android/sdk' > local.properties"
        exit 1
    fi
fi

echo "📌 Android SDK: ${ANDROID_HOME:-$ANDROID_SDK_ROOT}"

# Check local.properties
if [ ! -f "local.properties" ]; then
    SDK_DIR="${ANDROID_HOME:-$ANDROID_SDK_ROOT}"
    echo "sdk.dir=${SDK_DIR}" > local.properties
    echo "📝 Created local.properties"
fi

# -----------------------------------------------------------
# Build
# -----------------------------------------------------------

if [ "$BUILD_TYPE" = "debug" ]; then
    echo ""
    echo "🔨 Building DEBUG APK..."
    echo ""

    chmod +x gradlew 2>/dev/null || true
    ./gradlew assembleDebug --no-daemon

    APK_PATH="app/build/outputs/apk/debug/app-debug.apk"

    if [ -f "$APK_PATH" ]; then
        APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
        echo ""
        echo "╔══════════════════════════════════════════════════╗"
        echo "║   ✅ BUILD SUCCESSFUL                            ║"
        echo "╚══════════════════════════════════════════════════╝"
        echo ""
        echo "📦 Debug APK:  $APK_PATH"
        echo "📏 Size:       $APK_SIZE"
        echo ""
        echo "Install on phone:"
        echo "  adb install $APK_PATH"
        echo ""
    else
        echo "❌ Build failed — APK not found."
        exit 1
    fi

else
    # Release build
    if [ ! -f "keystore.properties" ] || ! grep -q "storeFile=app/release-keystore.jks" keystore.properties 2>/dev/null; then
        echo ""
        echo "⚠️  No signing keystore found."
        echo ""
        read -p "Generate one now? [Y/n] " GEN_KEYSTORE
        if [[ "$GEN_KEYSTORE" != "n" && "$GEN_KEYSTORE" != "N" ]]; then
            chmod +x generate-keystore.sh
            bash generate-keystore.sh
        else
            echo ""
            echo "Skipping signing. Building unsigned release..."
            echo "(You can manually sign later with apksigner)"
            echo ""
        fi
    fi

    echo ""
    echo "🔨 Building RELEASE APK..."
    echo ""

    chmod +x gradlew 2>/dev/null || true
    ./gradlew assembleRelease --no-daemon

    APK_PATH="app/build/outputs/apk/release/app-release-unsigned.apk"
    if [ ! -f "$APK_PATH" ]; then
        APK_PATH="app/build/outputs/apk/release/app-release.apk"
    fi

    if [ -f "$APK_PATH" ]; then
        APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
        echo ""
        echo "╔══════════════════════════════════════════════════╗"
        echo "║   ✅ BUILD SUCCESSFUL                            ║"
        echo "╚══════════════════════════════════════════════════╝"
        echo ""
        echo "📦 Release APK:  $APK_PATH"
        echo "📏 Size:         $APK_SIZE"
        echo ""
        echo "Install on phone:"
        echo "  adb install $APK_PATH"
        echo ""
        echo "Or transfer APK to phone and tap to install."
        echo ""
    else
        echo "❌ Build failed — APK not found."
        exit 1
    fi
fi
