# Deploy the update API

The Android client checks:

```text
GET /api/app/update
```

If that endpoint returns HTTP 404, deploy the current `server/app.py` and restart
the service:

```bash
scp server/app.py ubuntu@49.232.149.194:/home/ubuntu/daohen-server/app.py
ssh ubuntu@49.232.149.194 'sudo systemctl restart daohen.service'
curl http://49.232.149.194:5001/api/app/update
```

Before publishing a real update, upload the signed APK and configure a version
code greater than the installed app. See `releases/README.md`.
