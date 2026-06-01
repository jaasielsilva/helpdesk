# Helpdesk Pro — Sistema SaaS de Gestão de Chamados

> Plataforma multi-tenant com JWT, RBAC e provisionamento de assinantes (tenants).

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.14-green)
![Java](https://img.shields.io/badge/Java-17-blue)
![Angular](https://img.shields.io/badge/Angular-17-red)
![Version](https://img.shields.io/badge/Version-1.0.0-blueviolet)

## V1.0.0 — O que está pronto

| Módulo | Status |
|--------|--------|
| Login JWT + RBAC (4 roles) | ✅ |
| Provisionamento de tenant (SUPER_ADMIN) | ✅ |
| Gestão de usuários por tenant | ✅ |
| Chamados com isolamento multi-tenant | ✅ |
| Dashboard (KPIs tenant-aware) | ✅ |
| Demais módulos do menu | Placeholder "Em breve" |

## Credenciais de demonstração (seed)

| Usuário | Senha | Perfil | Escopo |
|---------|-------|--------|--------|
| `superadmin` | `super@123` | SUPER_ADMIN | Plataforma |
| `admin` | `admin@123` | ADMIN | Tenant `demo` |
| `suporte` | `suporte@123` | SUPORTE | Tenant `demo` |
| `user` | `user@123` | USER | Tenant `demo` |

## Início rápido

### Backend

```bash
mvn spring-boot:run
# http://localhost:8080
```

### Frontend

```bash
cd frontend
npm install
npm start
# http://localhost:4200
```

### Produção — JWT secret

```powershell
powershell -ExecutionPolicy Bypass -File scripts/generate-jwt-secret.ps1
# Depois: profile prod com JWT_SECRET no .env
```

## Fluxo SUPER_ADMIN — criar tenant

1. Login como `superadmin`
2. Menu **Empresas** → **Novo tenant**
3. Preencher slug, nome e credenciais do ADMIN inicial
4. O admin do novo tenant faz login (informe o slug se o usuário existir em mais de uma empresa)

## API — endpoints principais

### Autenticação

```http
POST /api/auth/login
{ "usuario": "admin", "senha": "admin@123", "empresaSlug": "demo" }
```

`empresaSlug` é obrigatório quando o mesmo usuário existe em múltiplos tenants. Resposta `REQUIRES_EMPRESA_SLUG` indica que o campo deve ser informado.

### Empresas (SUPER_ADMIN)

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/api/empresas` | Wizard: empresa + admin |
| GET | `/api/empresas` | Listar (paginado) |
| GET | `/api/empresas/{id}` | Detalhe |
| PATCH | `/api/empresas/{id}/status` | Ativar/desativar |

### Usuários

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/api/usuarios?empresaId=` | Listar (ADMIN: próprio tenant) |
| POST | `/api/usuarios` | Criar USER/SUPORTE |
| PUT | `/api/usuarios/{id}` | Atualizar |
| DELETE | `/api/usuarios/{id}` | Desativar (soft) |

### Chamados

```http
GET /api/chamados?page=0&size=10
Authorization: Bearer <token>
```

Isolamento: usuários de tenant A não veem chamados de tenant B. SUPER_ADMIN vê todos.

## Roles e permissões

- **SUPER_ADMIN** — plataforma, CRUD empresas, usuários cross-tenant
- **ADMIN** — gestão do tenant (usuários SUPORTE/USER, chamados)
- **SUPORTE** — atendimento de chamados
- **USER** — abertura de chamados próprios

## Testes

```bash
mvn test -Dtest=SaasIntegrationTest
```

## Release (v1.0.0+)

```powershell
# Gera JAR + checksum em release-artifacts/v<versao>/
powershell -ExecutionPolicy Bypass -File scripts/build-release.ps1 -SkipTests

# Executar artefato
java -jar release-artifacts/v1.0.0/helpdesk-1.0.0.jar --spring.profiles.active=prod
```

Artefatos versionados ficam em `release-artifacts/` (JAR + `.sha256`). Tags Git: `v1.0.0`, `v1.1.0`, etc.

## Estrutura

```
helpdesk/
├── src/main/java/.../controller/   Auth, Chamado, Empresa, Usuario
├── src/main/java/.../tenant/       TenantContext, TenantAccessService
├── frontend/src/app/features/
│   ├── empresas/                   Wizard SUPER_ADMIN
│   ├── usuarios/                   CRUD tenant-aware
│   ├── chamados/                   Tickets
│   └── placeholder/                Módulos futuros
└── scripts/generate-jwt-secret.*   Secret produção (256 bits)
```

## Próximas versões (pós V1.0.0)

Base de Conhecimento, SLA, Analytics, Assinatura/billing, self-service signup, Flyway migrations.

---

**Jaasiel Silva** — Helpdesk Pro v1.0.0
