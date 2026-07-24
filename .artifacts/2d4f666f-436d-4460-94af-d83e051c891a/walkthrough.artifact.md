# Walkthrough - GitHub Release & Signed APK Automation

NiseGochi 1.0.0 is now ready for its official debut. I have committed the changes, tagged the version, and set up a GitHub Actions workflow to automate the release process.

## Key Accomplishments

### 1. Version Tagging
- **Commit**: All release preparations have been committed (`chore: release version 1.0.0`).
- **Git Tag**: Created tag `v1.0.0`. Pushing this tag to GitHub will trigger the automated build process.

### 2. Release Automation (GitHub Actions)
- Created [android-release.yml](file:///C:/Users/Oshir/StudioProjects/NiseGochi/.github/workflows/android-release.yml).
- **Triggers**: On every push of a tag starting with `v`.
- **Functionality**:
    - Compiles the Android project.
    - Decodes and applies the signing keystore.
    - Creates a formal GitHub Release.
    - Uploads the signed `app-release.apk` as an asset.

### 3. Signing Support
- Updated `app/build.gradle.kts` to allow signing configurations to be injected via environment variables (`KEYSTORE_PASSWORD`, etc.).
- This allows the build to remain secure (no hardcoded passwords) and portable.

## Verification
- Local build check (`assembleDebug`) was successful.
- Git tag `v1.0.0` points to the latest release commit.

## Next Steps: GitHub Setup

To complete the setup and generate your first signed release, please follow these manual steps:

### 1. Push to GitHub
Run the following command in your terminal to push the code and the tag:
```powershell
git push origin main --tags
```

### 2. Configure GitHub Secrets
Go to your GitHub repository: **Settings > Secrets and variables > Actions** and add the following **Repository secrets**:

| Secret Name | Description |
| :--- | :--- |
| `KEYSTORE_BASE64` | The Base64 encoded string of your `.jks` file. |
| `KEYSTORE_PASSWORD` | Your keystore password. |
| `KEY_ALIAS` | Your key alias. |
| `KEY_PASSWORD` | Your key password. |

> [!TIP]
> To get the `KEYSTORE_BASE64` string on Windows, run:
> `[Convert]::ToBase64String([IO.File]::ReadAllBytes("path\to\your\keystore.jks"))` in PowerShell.

Once these secrets are added, pushing the `v1.0.0` tag will automatically create the release for you!
