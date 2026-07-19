# Android releases

Place the signed production APK at `server/releases/app-release.apk`.

Set these environment variables before restarting the service:

```text
APP_VERSION_CODE=3
APP_VERSION_NAME=1.2
APP_RELEASE_NOTES=Release notes shown in Settings
APP_PUBLISHED_AT=2026-07-16
```

Optional overrides:

```text
APP_APK_PATH=/absolute/path/to/app-release.apk
APP_APK_URL=https://cdn.example.com/app-release.apk
```

`APP_VERSION_CODE` must be greater than the installed Android app's version code
for the client to offer the update.
