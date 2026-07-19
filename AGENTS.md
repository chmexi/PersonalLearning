# PersonalLearning Project Instructions

## Production server

The SSH connection is already configured in the user's system SSH config.
Always use the alias below instead of asking the user to paste host, user, port,
or identity-file settings again:

```text
ssh chmexi
```

Server layout:

```text
Host alias: chmexi
Application directory: /home/ubuntu/daohen-server
Application entry point: /home/ubuntu/daohen-server/app.py
Python: /home/ubuntu/daohen-server/venv/bin/python
Systemd service: daohen.service
Android release APK: /home/ubuntu/daohen-server/releases/app-release.apk
Public API base URL: http://49.232.149.194:5001
```

Do not copy private keys or SSH identity-file paths into this repository.

## Deployment procedure

Treat this as a production deployment. Unless the user explicitly requests a
different operation:

1. Inspect the remote directory and `daohen.service` status read-only.
2. Upload new files to a temporary `.codex-new` path.
3. Validate the uploaded checksum and Python syntax before replacement.
4. Back up the current file with a timestamp, for example
   `app.py.bak.YYYYMMDDHHMMSS`.
5. Replace the target only after validation succeeds.
6. Restart with `sudo systemctl restart daohen.service`.
7. Verify `systemctl is-active daohen.service`.
8. Verify both endpoints return HTTP 200:
   - `/api/app/update`
   - `/api/daohen/range`
9. Inspect recent service logs for worker startup or runtime errors.

If validation fails, leave the current production file and service untouched.
Never modify or delete the production SQLite database as part of application
deployment.

## Publishing Android updates

The update endpoint is implemented in `server/app.py`. A release is offered only
when `APP_VERSION_CODE` is greater than the installed app version.

Before publishing:

1. Build and sign the production APK.
2. Upload it to
   `/home/ubuntu/daohen-server/releases/app-release.apk`, or configure
   `APP_APK_PATH` / `APP_APK_URL`.
3. Configure `APP_VERSION_CODE`, `APP_VERSION_NAME`, `APP_RELEASE_NOTES`, and
   optionally `APP_PUBLISHED_AT` in the service environment.
4. Restart the service and verify `/api/app/update`.

Do not publish debug or unsigned APKs as production updates.
