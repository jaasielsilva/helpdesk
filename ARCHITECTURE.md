# 🏗️ Arquitetura do Helpdesk

## Visão Geral

O Helpdesk é um sistema profissional de gestão de chamados construído com **Spring Boot 3.5.14** no backend e **Angular** no frontend, seguindo padrões de arquitetura em camadas com responsabilidades bem definidas.

```
┌─────────────────────────────────────────────────┐
│             Angular Frontend                     │
│         (Standalone Components)                  │
└─────────────────────┬───────────────────────────┘
                      │ HTTP/HTTPS
                      ▼
┌─────────────────────────────────────────────────┐
│       Spring Boot Backend (REST API)             │
├─────────────────────────────────────────────────┤
│  Controllers │ Services │ Repositories │ Models │
└─────────────────────────────────────────────────┘
                      │
                      ▼
        ┌─────────────────────────────┐
        │   MySQL 8.0 Database        │
        │  (Hibernate ORM)            │
        └─────────────────────────────┘
```

## Camadas da Aplicação

### 1️⃣ **Camada de Apresentação (Controllers)**

**Responsabilidade:** Receber requisições HTTP, validar entrada e retornar respostas.

**Principais Controllers:**

- **AuthController** (`/api/auth/*`)
  - `/login` - Autenticação (POST)
  - `/me` - Usuário atual (GET)
  - `/logout` - Logout (POST)

- **ChamadoController** (`/api/chamados/*`)
  - `GET /` - Listar com paginação
  - `POST /` - Criar novo
  - `PUT /{id}` - Atualizar (ADMIN only)
  - `DELETE /{id}` - Soft delete (ADMIN only)

**Padrão de Resposta:**
```json
{
  "sucesso": true,
  "mensagem": "Descrição",
  "dados": {},
  "timestamp": "2026-06-01T14:00:00"
}
```

### 2️⃣ **Camada de Serviço (Business Logic)**

**Responsabilidade:** Implementar regras de negócio, validações e orquestração.

**Padrão Utilizado:** Interface + Implementação

```
IAuthService ◄── AuthService
IChamadoService ◄── ChamadoServiceImpl
```

**Características:**
- Transações gerenciadas (`@Transactional`)
- Conversão de DTOs
- Validações de negócio
- Tratamento de exceções

### 3️⃣ **Camada de Dados (Repositories)**

**Responsabilidade:** Acesso aos dados com queries otimizadas.

**Repositories:**

- **UsuarioRepository** - Queries para usuários
- **ChamadoRepository** - Queries para chamados

**Queries Customizadas:**
```java
@Query("SELECT c FROM Chamado c WHERE c.deletedAt IS NULL")
Page<Chamado> findAllActive(Pageable pageable);

@Query("SELECT u FROM Usuario u WHERE u.usuario = :usuario AND u.deletedAt IS NULL")
Optional<Usuario> findByUsuarioIgnoreCase(String usuario);
```

### 4️⃣ **Camada de Modelos (Entities)**

**Responsabilidade:** Representar dados do banco com mapeamento JPA.

**Modelos Principais:**

```java
@Entity
public class Usuario {
    @Id @GeneratedValue
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private PerfilUsuario perfil;
    
    @CreationTimestamp
    private LocalDateTime dataCriacao;
    
    private LocalDateTime deletedAt; // Soft delete
}

@Entity
public class Chamado {
    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;
    
    @ManyToOne
    @JoinColumn(name = "usuario_atribuido_id")
    private Usuario usuarioAtribuido;
    
    @Enumerated(EnumType.STRING)
    private StatusChamado status;
    
    private LocalDateTime deletedAt;
}
```

### 5️⃣ **Data Transfer Objects (DTOs)**

**Responsabilidade:** Transferir dados entre camadas e cliente.

**Tipos:**

- **Request DTOs**: `ChamadoCreateRequest`, `ChamadoUpdateRequest`, `LoginRequest`
- **Response DTOs**: `ChamadoResponse`, `UsuarioLogadoResponse`
- **Wrapper**: `ApiResponse<T>` para todas as respostas

## 🔒 Segurança

### Spring Security

```
Requisição HTTP
      ↓
Security Filter Chain
  ├── CORS Filter
  ├── Authentication Filter
  └── Authorization Filter
      ↓
      ├─ Público: /api/auth/login
      └─ Protegido: @PreAuthorize("hasRole('ADMIN')")
```

### Autenticação

- **Tipo**: Session-based (IF_REQUIRED)
- **Encoding**: BCrypt com salt aleatório
- **Provider**: DaoAuthenticationProvider

```java
UsernamePasswordAuthenticationToken token = 
    new UsernamePasswordAuthenticationToken(user, password);
Authentication authenticated = authenticationManager.authenticate(token);
SecurityContextHolder.setContext(new SecurityContextImpl(authenticated));
```

### Autorização

**Roles:**
- `ADMIN` - Acesso total
- `USER` - Acesso limitado

**Exemplo:**
```java
@PostMapping
@PreAuthorize("hasAnyRole('ADMIN', 'USER')")
public ResponseEntity<ApiResponse<ChamadoResponse>> criar(...) {}

@DeleteMapping("/{id}")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<ApiResponse<Void>> deletar(...) {}
```

## 📋 Enumerações

### StatusChamado
- `ABERTO` - Novo chamado
- `EM_ATENDIMENTO` - Sendo resolvido
- `RESOLVIDO` - Solução encontrada
- `FECHADO` - Finalizado

### PerfilUsuario
- `ADMIN` - Administrador do sistema
- `USER` - Usuário comum

## 🔄 Fluxo de Dados

### Criar Chamado

```
Frontend (Form)
    ↓ POST /api/chamados
    ↓ ChamadoCreateRequest (validação)
    ↓ ChamadoController.criar()
    ↓ ChamadoService.criar()
    ├─ Validar usuario
    ├─ Criar entidade
    └─ Salvar no BD
    ↓ converter para ChamadoResponse
    ↓ ApiResponse<ChamadoResponse>
    ↓ Frontend (JSON)
```

### Autenticar Usuário

```
Frontend (Login Form)
    ↓ POST /api/auth/login
    ↓ LoginRequest
    ↓ AuthController.autenticar()
    ↓ AuthService.autenticar()
    ├─ UsernamePasswordAuthenticationToken
    ├─ authenticationManager.authenticate()
    └─ Spring Security Session
    ↓ UsuarioLogadoResponse
    ↓ Frontend (Cookie + JSON)
```

## 🗄️ Banco de Dados

### Diagrama de Relacionamentos

```
┌─────────────┐
│  USUARIO    │
├─────────────┤
│ id (PK)     │
│ usuario     │
│ senha       │
│ perfil      │
│ ativo       │
│ data_criacao│
│ deleted_at  │
└─────────────┘
       ▲
       │ 1
       │
      ┌└──────────┐
      │           │
      │ N         │ N
   ┌──┴────────┐
   │ CHAMADO   │
   ├───────────┤
   │ id (PK)   │
   │ titulo    │
   │ descricao │
   │ status    │
   │ usuario_id (FK)
   │ usuario_atribuido_id (FK)
   │ data_criacao
   │ deleted_at
   └───────────┘
```

### Soft Delete

Todos os deletes são "soft" (lógicos):

```sql
-- DELETE físico (nunca acontece)
DELETE FROM chamado WHERE id = 1;

-- DELETE lógico (real)
UPDATE chamado SET deleted_at = NOW() WHERE id = 1;

-- SELECT filtra automaticamente
SELECT * FROM chamado WHERE deleted_at IS NULL;
```

## 📊 Paginação

Implementada com Spring Data `Page<T>`:

```java
// Request
GET /api/chamados?page=0&size=10&sort=dataCriacao,desc

// Response
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {"empty": false}
  },
  "totalPages": 5,
  "totalElements": 50,
  "numberOfElements": 10,
  "empty": false
}
```

## 🌍 Tratamento Global de Erros

**GlobalExceptionHandler** intercepta exceções:

| Exceção | Status | Mensagem |
|---------|--------|----------|
| `EntityNotFoundException` | 404 | Recurso não encontrado |
| `BadCredentialsException` | 401 | Credenciais inválidas |
| `AccessDeniedException` | 403 | Acesso negado |
| `MethodArgumentNotValidException` | 400 | Erro de validação |
| Genérica | 500 | Erro interno |

**Resposta de Erro:**
```json
{
  "sucesso": false,
  "mensagem": "Chamado não encontrado",
  "dados": null,
  "timestamp": "2026-06-01T14:00:00"
}
```

## 🔧 Configuração por Ambiente

### Development (DEV)

```properties
spring.profiles.active=dev
spring.jpa.show-sql=true
logging.level.root=DEBUG
spring.jpa.hibernate.ddl-auto=update
```

### Production (PROD)

```properties
spring.profiles.active=prod
spring.jpa.show-sql=false
logging.level.root=WARN
spring.jpa.hibernate.ddl-auto=validate
server.servlet.http2.enabled=true
server.compression.enabled=true
```

### Testing (TEST)

```properties
spring.profiles.active=test
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
logging.level.root=WARN
```

## 📦 Dependências Principais

```xml
<!-- Spring Boot -->
<spring-boot-starter-web/>        <!-- REST, Tomcat -->
<spring-boot-starter-security/>   <!-- Autenticação -->
<spring-boot-starter-data-jpa/>   <!-- ORM, Repositories -->
<spring-boot-starter-validation/> <!-- Validação -->

<!-- Banco de Dados -->
<mysql-connector-j/>              <!-- Driver MySQL -->
<spring-boot-starter-data-jpa/>   <!-- Hibernate -->

<!-- Utilidades -->
<lombok/>                          <!-- Getters/Setters automáticos -->
<h2/> (test)                      <!-- BD em memória para testes -->
```

## 🚀 Fluxo de Deploy

```
1. Git Push
   ↓
2. Maven Build (mvn clean install)
   ├─ Compile
   ├─ Test (unit tests)
   └─ Package (JAR)
   ↓
3. Upload Artefato
   ↓
4. Spring Boot Runtime
   ├─ Load Properties
   ├─ DataInitializer (criar admin)
   └─ Start Server
   ↓
5. ✅ API Pronta
```

## 📈 Performance

### Otimizações Implementadas

1. **Lazy Loading** para relacionamentos
2. **Indexed Queries** com `@Query` customizadas
3. **Paginação** obrigatória em listagens
4. **Compressão** de respostas (prod)
5. **HTTP/2** habilitado
6. **Connection Pool** (HikariCP)

## 🔍 Monitoramento

### Métricas Disponíveis

- Logs estruturados por ambiente
- Timestamps em todas as operações
- Auditoria via `dataCriacao`, `dataAtualizacao`, `deletedAt`

### Endpoints de Health (Prod)

```
GET /actuator/health
GET /actuator/info
```

---

**Padrão de Projeto:** Clean Architecture + Layered Architecture
**Padrão de API:** RESTful + ApiResponse Wrapper
**Padrão de Dados:** Repository Pattern + JPA
**Padrão de Segurança:** Spring Security + Role-based Access Control

