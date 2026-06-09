## Helpdesk Pro v1.0.0 — SaaS MVP

Primeira release de produção: plataforma multi-tenant de chamados com Spring Boot 3, Angular 17, JWT e RBAC.

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

### Credenciais de demonstração (seed)

| Usuário | Senha | Perfil |
|---------|-------|--------|
| `superadmin` | `super@123` | SUPER_ADMIN |
| `admin` | `admin@123` | ADMIN (tenant `demo`) |
| `suporte` | `suporte@123` | SUPORTE |
| `user` | `user@123` | USER |

### Executar o JAR

```bash
java -jar helpdesk-1.0.0.jar --spring.profiles.active=prod
```

Configure `JWT_SECRET` e banco de dados antes de subir em produção. Verifique a integridade com o arquivo `.sha256` incluído nesta release.

**Full changelog:** [CHANGELOG.md](https://github.com/jaasielsilva/helpdesk/blob/main/CHANGELOG.md)
