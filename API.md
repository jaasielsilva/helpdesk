# 📡 API Documentation

## Base URL

```
Development:  http://localhost:8080/api
Production:   https://api.helpdesk.com/api
```

## Autenticação

Todas as requisições, exceto `/auth/login`, requerem uma sessão válida (cookie `JSESSIONID`).

```bash
# 1. Fazer login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usuario":"admin","senha":"admin@123"}'

# 2. Usar cookie automaticamente em requisições subsequentes
curl -X GET http://localhost:8080/api/chamados \
  -H "Cookie: JSESSIONID=xxxxx"
```

---

## Endpoints

### 🔐 Autenticação

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "usuario": "admin",
  "senha": "admin@123"
}
```

**Response 200:**
```json
{
  "sucesso": true,
  "mensagem": "Login realizado com sucesso",
  "dados": {
    "usuario": "admin",
    "nome": "Administrador",
    "perfil": "ADMIN"
  },
  "timestamp": "2026-06-01T14:08:00"
}
```

**Response 401:**
```json
{
  "sucesso": false,
  "mensagem": "Credenciais inválidas",
  "dados": null,
  "timestamp": "2026-06-01T14:08:00"
}
```

#### Usuário Atual
```http
GET /auth/me
Cookie: JSESSIONID=xxxxx
```

**Response 200:**
```json
{
  "sucesso": true,
  "mensagem": "Usuário carregado com sucesso",
  "dados": {
    "usuario": "admin",
    "nome": "Administrador",
    "perfil": "ADMIN"
  },
  "timestamp": "2026-06-01T14:08:00"
}
```

**Response 401:**
```json
{
  "sucesso": false,
  "mensagem": "Não autenticado",
  "dados": null,
  "timestamp": "2026-06-01T14:08:00"
}
```

#### Logout
```http
POST /auth/logout
Cookie: JSESSIONID=xxxxx
```

**Response 200:**
```json
{
  "sucesso": true,
  "mensagem": "Logout realizado com sucesso",
  "dados": null,
  "timestamp": "2026-06-01T14:08:00"
}
```

---

### 🎫 Chamados

#### Listar Chamados (com Paginação)

```http
GET /chamados?page=0&size=10&sort=dataCriacao,desc
Cookie: JSESSIONID=xxxxx
```

**Parâmetros Query:**

| Parâmetro | Tipo | Padrão | Descrição |
|-----------|------|--------|-----------|
| `page` | Integer | 0 | Número da página (começa em 0) |
| `size` | Integer | 10 | Quantidade de registros |
| `sort` | String | `dataCriacao,desc` | Campo,direção (asc/desc) |

**Campos para ordenação:**
- `id`
- `titulo`
- `descricao`
- `status`
- `dataCriacao`
- `dataAtualizacao`
- `dataFechamento`

**Response 200:**
```json
{
  "sucesso": true,
  "mensagem": "Chamados carregados com sucesso",
  "dados": {
    "content": [
      {
        "id": 1,
        "titulo": "Erro ao fazer login",
        "descricao": "Usuário não consegue fazer login no sistema",
        "status": "ABERTO",
        "usuarioId": 1,
        "usuarioNome": "admin",
        "usuarioAtribuidoId": 2,
        "usuarioAtribuidoNome": "suporte@email.com",
        "dataCriacao": "2026-06-01T14:00:00",
        "dataAtualizacao": "2026-06-01T14:05:00",
        "dataFechamento": null
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "offset": 0,
      "paged": true,
      "unpaged": false
    },
    "totalPages": 5,
    "totalElements": 50,
    "numberOfElements": 10,
    "first": true,
    "last": false,
    "empty": false
  },
  "timestamp": "2026-06-01T14:08:00"
}
```

**Exemplos de Requisições:**

```bash
# Primeira página, 20 itens
GET /chamados?page=0&size=20

# Ordenar por título, ascendente
GET /chamados?sort=titulo,asc

# Múltiplos ordenamentos
GET /chamados?sort=status,asc&sort=dataCriacao,desc

# Página 2, 15 itens, ordenado por data
GET /chamados?page=2&size=15&sort=dataCriacao,desc
```

**Status Codes:**
- `200` - Sucesso
- `401` - Não autenticado
- `403` - Acesso negado
- `500` - Erro servidor

---

#### Criar Chamado

```http
POST /chamados
Cookie: JSESSIONID=xxxxx
Content-Type: application/json

{
  "titulo": "Sistema travado",
  "descricao": "O sistema está travado ao tentar acessar relatórios",
  "usuarioId": 1
}
```

**Body Fields:**

| Campo | Tipo | Obrigatório | Validação |
|-------|------|-------------|-----------|
| `titulo` | String | Sim | 3-100 caracteres |
| `descricao` | String | Sim | 10-1000 caracteres |
| `usuarioId` | Long | Sim | ID válido de usuário |

**Response 201:**
```json
{
  "sucesso": true,
  "mensagem": "Chamado criado com sucesso",
  "dados": {
    "id": 1,
    "titulo": "Sistema travado",
    "descricao": "O sistema está travado ao tentar acessar relatórios",
    "status": "ABERTO",
    "usuarioId": 1,
    "usuarioNome": "admin",
    "usuarioAtribuidoId": null,
    "usuarioAtribuidoNome": null,
    "dataCriacao": "2026-06-01T14:08:00",
    "dataAtualizacao": "2026-06-01T14:08:00",
    "dataFechamento": null
  },
  "timestamp": "2026-06-01T14:08:00"
}
```

**Response 400 (Validação):**
```json
{
  "sucesso": false,
  "mensagem": "Erro de validação",
  "dados": {
    "titulo": "Título deve ter entre 3 e 100 caracteres",
    "descricao": "Descrição deve ter entre 10 e 1000 caracteres"
  },
  "timestamp": "2026-06-01T14:08:00"
}
```

**Response 401/403:**
```json
{
  "sucesso": false,
  "mensagem": "Acesso negado",
  "dados": null,
  "timestamp": "2026-06-01T14:08:00"
}
```

---

#### Atualizar Chamado (ADMIN only)

```http
PUT /chamados/1
Cookie: JSESSIONID=xxxxx
Content-Type: application/json

{
  "titulo": "Sistema travado - RESOLVIDO",
  "descricao": "Problema foi resolvido com atualização",
  "status": "RESOLVIDO",
  "usuarioAtribuidoId": 2
}
```

**Body Fields (todos opcionais):**

| Campo | Tipo | Validação |
|-------|------|-----------|
| `titulo` | String | 3-100 caracteres |
| `descricao` | String | 10-1000 caracteres |
| `status` | String | ABERTO, EM_ATENDIMENTO, RESOLVIDO, FECHADO |
| `usuarioAtribuidoId` | Long | ID válido ou null |

**Response 200:**
```json
{
  "sucesso": true,
  "mensagem": "Chamado atualizado com sucesso",
  "dados": {
    "id": 1,
    "titulo": "Sistema travado - RESOLVIDO",
    "descricao": "Problema foi resolvido com atualização",
    "status": "RESOLVIDO",
    "usuarioId": 1,
    "usuarioNome": "admin",
    "usuarioAtribuidoId": 2,
    "usuarioAtribuidoNome": "suporte",
    "dataCriacao": "2026-06-01T14:00:00",
    "dataAtualizacao": "2026-06-01T14:10:00",
    "dataFechamento": null
  },
  "timestamp": "2026-06-01T14:10:00"
}
```

**Response 404:**
```json
{
  "sucesso": false,
  "mensagem": "Chamado não encontrado",
  "dados": null,
  "timestamp": "2026-06-01T14:10:00"
}
```

---

#### Deletar Chamado (Soft Delete, ADMIN only)

```http
DELETE /chamados/1
Cookie: JSESSIONID=xxxxx
```

**Response 204:**
```
(sem body)
```

**Response 404:**
```json
{
  "sucesso": false,
  "mensagem": "Chamado não encontrado",
  "dados": null,
  "timestamp": "2026-06-01T14:10:00"
}
```

---

## 📊 Estrutura de Dados

### ChamadoResponse

```json
{
  "id": 1,
  "titulo": "string",
  "descricao": "string",
  "status": "ABERTO|EM_ATENDIMENTO|RESOLVIDO|FECHADO",
  "usuarioId": 1,
  "usuarioNome": "string",
  "usuarioAtribuidoId": 2,
  "usuarioAtribuidoNome": "string",
  "dataCriacao": "2026-06-01T14:00:00",
  "dataAtualizacao": "2026-06-01T14:05:00",
  "dataFechamento": "2026-06-01T15:00:00"
}
```

### UsuarioLogadoResponse

```json
{
  "usuario": "admin",
  "nome": "Administrador",
  "perfil": "ADMIN|USER"
}
```

### Page Response

```json
{
  "content": [],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "totalPages": 5,
  "totalElements": 50,
  "numberOfElements": 10,
  "first": true,
  "last": false,
  "empty": false
}
```

---

## 🔄 Status HTTP

| Código | Significado |
|--------|-------------|
| `200` | OK - Sucesso |
| `201` | Created - Recurso criado |
| `204` | No Content - Sucesso sem conteúdo |
| `400` | Bad Request - Erro de validação |
| `401` | Unauthorized - Não autenticado |
| `403` | Forbidden - Sem permissão |
| `404` | Not Found - Recurso não encontrado |
| `500` | Internal Server Error - Erro do servidor |

---

## 🧪 Exemplos com cURL

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usuario":"admin","senha":"admin@123"}' \
  -c cookies.txt
```

### Listar Chamados
```bash
curl -X GET "http://localhost:8080/api/chamados?page=0&size=10" \
  -H "Cookie: JSESSIONID=xxxxx"
```

### Criar Chamado
```bash
curl -X POST http://localhost:8080/api/chamados \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=xxxxx" \
  -d '{
    "titulo": "Bug no relatório",
    "descricao": "O relatório de vendas não está calculando corretamente",
    "usuarioId": 1
  }'
```

### Atualizar Chamado
```bash
curl -X PUT http://localhost:8080/api/chamados/1 \
  -H "Content-Type: application/json" \
  -H "Cookie: JSESSIONID=xxxxx" \
  -d '{
    "status": "EM_ATENDIMENTO",
    "usuarioAtribuidoId": 2
  }'
```

### Deletar Chamado
```bash
curl -X DELETE http://localhost:8080/api/chamados/1 \
  -H "Cookie: JSESSIONID=xxxxx"
```

---

## 🧪 Exemplos com Postman

1. **Environment Variables:**
   ```
   base_url: http://localhost:8080/api
   jsessionid: {{Cookie}}
   ```

2. **Login (Pre-request):**
   ```javascript
   // Fazer login e salvar cookie
   pm.sendRequest({
     url: pm.environment.get("base_url") + "/auth/login",
     method: "POST",
     body: {
       mode: "raw",
       raw: JSON.stringify({
         usuario: "admin",
         senha: "admin@123"
       })
     }
   }, function(err, response) {
     if(!err) {
       pm.environment.set("jsessionid", response.headers.get("Set-Cookie"));
     }
   });
   ```

3. **Headers em requisições:**
   ```
   Cookie: {{jsessionid}}
   Content-Type: application/json
   ```

---

## 🚨 Tratamento de Erros

Todos os erros retornam no formato:

```json
{
  "sucesso": false,
  "mensagem": "Descrição do erro",
  "dados": null,
  "timestamp": "2026-06-01T14:08:00"
}
```

### Validação (400)
```json
{
  "sucesso": false,
  "mensagem": "Erro de validação",
  "dados": {
    "titulo": "Título é obrigatório",
    "descricao": "Descrição deve ter no mínimo 10 caracteres"
  },
  "timestamp": "2026-06-01T14:08:00"
}
```

### Autenticação (401)
```json
{
  "sucesso": false,
  "mensagem": "Credenciais inválidas",
  "dados": null,
  "timestamp": "2026-06-01T14:08:00"
}
```

### Autorização (403)
```json
{
  "sucesso": false,
  "mensagem": "Você não tem permissão para executar esta ação",
  "dados": null,
  "timestamp": "2026-06-01T14:08:00"
}
```

---

## 📝 Notas Importantes

1. **Cookies**: Sessões são mantidas via cookies `JSESSIONID`
2. **Soft Delete**: Deletados não são removidos, apenas marcados
3. **Paginação**: Obrigatória em listagens para performance
4. **Validação**: Feita no controller antes de chegar ao serviço
5. **Timestamps**: Em timezone `America/Sao_Paulo`

