# ZapPay Build & Release Guide

## Prerequisites
- Java 17: `/opt/homebrew/opt/openjdk@17`
- Android SDK: `/opt/homebrew/share/android-commandlinetools`
- Gradle (wrapped via `gradlew`)

## Build Debug APK
```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17
cd android
./gradlew assembleDebug
```

APK location: `android/app/build/outputs/apk/debug/app-debug.apk`

## Install on Device (adb)
```bash
adb install -r android/app/build/outputs/apk/debug/app-debug.apk
```

## Environment
- Backend: `https://zappay-6sof.onrender.com/api/v1/`
- Device: OnePlus CPH2413 (serial `1a8b1ed7`)
- minSdk: 26, targetSdk: 35

## Release Build (TODO when ready)
1. Generate keystore
2. Update `android/app/build.gradle.kts` signing config
3. Run `./gradlew bundleRelease`
4. Upload to Play Console

## Key Commands
- Build APK: `./gradlew assembleDebug`
- Install: `/opt/homebrew/share/android-commandlinetools/platform-tools/adb install -r app-debug.apk`
- Logcat: `/opt/homebrew/share/android-commandlinetools/platform-tools/adb logcat -s ZapPay`
- Devices: `/opt/homebrew/share/android-commandlinetools/platform-tools/adb devices`
- Python (backend) syntax check: `python3 -m py_compile <file>`
