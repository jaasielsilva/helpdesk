# Release artifacts

Versioned deployable JARs live here. Each release folder contains:

- `helpdesk-<version>.jar` — executable Spring Boot fat JAR
- `helpdesk-<version>.jar.sha256` — integrity checksum

## Run

```bash
java -jar helpdesk-1.0.0.jar --spring.profiles.active=prod
```

Set `JWT_SECRET` and database variables via `.env` or environment before starting in production.

## Rebuild

```powershell
powershell -ExecutionPolicy Bypass -File scripts/build-release.ps1
```

```bash
./scripts/build-release.sh
```
