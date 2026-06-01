# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2026-06-01

### Added

- Multi-tenant SaaS MVP with row-level isolation (`Empresa` + `TenantContext`)
- JWT authentication (stateless) with RBAC: `SUPER_ADMIN`, `ADMIN`, `SUPORTE`, `USER`
- Tenant provisioning wizard (`POST /api/empresas`) — creates company + initial ADMIN user
- User management API and UI (`/api/usuarios`, `/usuarios`) scoped by tenant
- Companies management UI (`/empresas`) for SUPER_ADMIN
- Multi-tenant login with optional `empresaSlug` when username is ambiguous
- Inactive tenant blocks login and invalidates JWT sessions
- Integration tests (`SaasIntegrationTest`) for tenant creation and login flows
- Production JWT secret generator scripts (`scripts/generate-jwt-secret.*`)
- Release artifact pipeline (`scripts/build-release.*`) with SHA-256 checksum

### Changed

- Dashboard shows tenant context or platform-wide view for SUPER_ADMIN
- Login page supports conditional company slug field
- Sidebar keeps future modules visible as "Em breve" placeholders

### Security

- `JWT_SECRET` required in production profile (minimum 256 bits)
- Rate limiting on login endpoint

[1.0.0]: https://github.com/jaasielsilva/helpdesk/releases/tag/v1.0.0
