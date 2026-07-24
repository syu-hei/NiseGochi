# Implementation Plan - GitHub Release & Signed APK Setup

This plan covers the steps to commit the current changes, tag the release, set up automation via GitHub Actions, and configure the project for signing.

## User Review Required

> [!IMPORTANT]
> **Keystore Security**: To generate a **Signed APK** for release, you need a Keystore file (.jks). **NEVER** commit this file or its passwords to GitHub. I will set up the GitHub Action to use "Secrets" to keep these safe.
> **Git Remote**: Please ensure you have a remote repository set up on GitHub (e.g., `git remote add origin ...`).

## Proposed Changes

### 1. Git Operations
- Commit all current modifications with a message: `chore: release version 1.0.0`
- Create a Git tag: `v1.0.0`

### 2. GitHub Actions Setup
#### [NEW] [android-release.yml](file:///C:/Users/Oshir/StudioProjects/NiseGochi/.github/workflows/android-release.yml)
- Create a workflow that triggers on tags (e.g., `v*`).
- The workflow will:
    - Set up JDK 17.
    - Build the Release APK.
    - Sign the APK using GitHub Secrets.
    - Create a GitHub Release and upload the APK.

### 3. Build Configuration
#### [MODIFY] [build.gradle.kts](file:///C:/Users/Oshir/StudioProjects/NiseGochi/app/build.gradle.kts)
- Add a `signingConfigs` block that pulls values from environment variables. This allows the same build logic to work locally (with a `local.properties` or similar) and on GitHub Actions (with Secrets).

## Verification Plan

### Automated Verification
- Verify that the GitHub Action configuration is syntactically correct.
- Run a dry-run Gradle build for the release variant.

### Manual Verification
1.  **Push to GitHub**: You will need to run `git push origin main --tags`.
2.  **Configure Secrets**: On GitHub, go to **Settings > Secrets and variables > Actions** and add:
    - `KEYSTORE_BASE64`: Base64 encoded content of your .jks file.
    - `KEYSTORE_PASSWORD`: Your keystore password.
    - `KEY_ALIAS`: Your key alias.
    - `KEY_PASSWORD`: Your key password.
