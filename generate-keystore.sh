#!/bin/bash
# ============================================================
# PhysioCare Manager - Generate Release Keystore
# ============================================================
# Run this ONCE before building the release APK.
# It creates a signing keystore used to sign your APK.
# ============================================================

set -e

echo ""
echo "╔══════════════════════════════════════════════════╗"
echo "║   PhysioCare Manager - Keystore Generator        ║"
echo "╚══════════════════════════════════════════════════╝"
echo ""

# Check if keytool is available
if ! command -v keytool &> /dev/null; then
    echo "❌ ERROR: 'keytool' not found."
    echo "   Make sure JDK 17 is installed and on your PATH."
    echo "   Download: https://adoptium.net/"
    exit 1
fi

# Prompt for passwords
echo "Set a keystore password (min 6 characters):"
read -s -p "> " STORE_PASS
echo ""

if [ ${#STORE_PASS} -lt 6 ]; then
    echo "❌ Password must be at least 6 characters."
    exit 1
fi

echo "Confirm password:"
read -s -p "> " STORE_PASS_CONFIRM
echo ""

if [ "$STORE_PASS" != "$STORE_PASS_CONFIRM" ]; then
    echo "❌ Passwords do not match."
    exit 1
fi

# Generate the keystore
echo ""
echo "🔐 Generating keystore..."

keytool -genkeypair \
    -v \
    -keystore app/release-keystore.jks \
    -alias physiocare \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -storepass "$STORE_PASS" \
    -keypass "$STORE_PASS" \
    -dname "CN=PhysioCare Manager, OU=Dev, O=PhysioCare, L=Bengaluru, ST=Karnataka, C=IN"

# Update keystore.properties
cat > keystore.properties << EOF
storeFile=app/release-keystore.jks
storePassword=${STORE_PASS}
keyAlias=physiocare
keyPassword=${STORE_PASS}
EOF

echo ""
echo "✅ Keystore generated successfully!"
echo ""
echo "📄 Files created:"
echo "   • app/release-keystore.jks    (signing key — KEEP SAFE)"
echo "   • keystore.properties          (credentials)"
echo ""
echo "⚠️  IMPORTANT:"
echo "   • Back up release-keystore.jks — you need it for future updates"
echo "   • Never commit release-keystore.jks to Git"
echo "   • keystore.properties is already in .gitignore"
echo ""
echo "👉 Now run:  ./build-release.sh"
echo ""
