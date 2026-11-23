#!/bin/bash
# Сборка Android APK МузСтудии — по рецепту студии (см. android/README.md)
set -e
ROOT=/home/z/my-project/github/music-studio
TOOLS=/home/z/my-project/.tools
BT=$TOOLS/android-14
AJ=$TOOLS/android-34/android.jar
export JAVA_HOME=$TOOLS/jdk-21.0.13.1+2
export PATH=$JAVA_HOME/bin:$PATH

cd $ROOT
rm -rf android-build
mkdir -p android-build/assets android-build/obj android-build/dex

cp index.html icon-192.png icon-512.png android-build/assets/

$BT/aapt2 compile --dir android/res -o android-build/res.zip
$BT/aapt2 link -o android-build/base-unsigned.apk -I $AJ \
  --manifest android/AndroidManifest.xml \
  -A android-build/assets --min-sdk-version 24 --target-sdk-version 34 \
  --version-code 1 --version-name 0.1.0 --auto-add-overlay android-build/res.zip

javac -nowarn -source 11 -target 11 -cp $AJ \
  -d android-build/obj $(find android/java -name '*.java')

$BT/d8 --release --lib $AJ --min-api 24 \
  --output android-build/dex $(find android-build/obj -name '*.class')

python3 -c "import zipfile; z=zipfile.ZipFile('android-build/base-unsigned.apk','a'); z.write('android-build/dex/classes.dex','classes.dex',compress_type=zipfile.ZIP_DEFLATED); z.close()"

$BT/zipalign -f 4 android-build/base-unsigned.apk android-build/base-aligned.apk

$BT/apksigner sign \
  --ks android/keystore/muzstudio-release.jks --ks-key-alias muzstudio \
  --ks-pass pass:muzstudio2026 --key-pass pass:muzstudio2026 \
  --out android-build/MuzStudio-0.1.0.apk android-build/base-aligned.apk

$BT/apksigner verify --print-certs android-build/MuzStudio-0.1.0.apk
ls -la android-build/MuzStudio-0.1.0.apk
echo "APK BUILD OK"
