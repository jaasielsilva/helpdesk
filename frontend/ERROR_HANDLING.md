# 🚨 Tratamento de Erros - Frontend

## Visão Geral

O frontend agora tem um **sistema profissional de tratamento de erros** com notificações personalizadas (toast) para cada tipo de erro HTTP.

## Arquitetura

```
HTTP Request
    ↓
Backend API
    ↓
HTTP Response (com ApiResponse)
    ↓
errorInterceptor (captura erros)
    ↓
NotificationService (exibe toast)
    ↓
Component (error já foi notificado)
```

## Componentes Implementados

### 1. ErrorInterceptor (`core/interceptors/error.interceptor.ts`)

**Responsabilidade:** Capturar todos os erros HTTP e notificar o usuário

```typescript
export const errorInterceptor: HttpInterceptorFn = (request, next) => {
  const notificationService = inject(NotificationService);
  
  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      handleHttpError(error, notificationService);
      return throwError(() => error);
    })
  );
};
```

**Tratamentos por Status Code:**

| Status | Título | Mensagem | Tempo |
|--------|--------|----------|-------|
| **400** | ❌ Dados Inválidos | Erros de validação | 5s |
| **401** | 🔐 Não Autenticado | "Sessão expirou. Faça login novamente." | 5s |
| **403** | 🚫 Acesso Negado | "Sem permissão para esta ação" | 5s |
| **404** | ❓ Não Encontrado | "Recurso não encontrado" | 3s |
| **409** | ⚠️ Conflito | "Conflito ao processar dados" | 5s |
| **500** | 💥 Erro do Servidor | "Erro interno do servidor" | 5s |
| **502/503/504** | 🔧 Serviço Indisponível | "Serviço temporariamente indisponível" | 5s |
| **0** (Network) | 📡 Erro de Rede | "Erro de conexão. Verifique sua internet." | 5s |

### 2. NotificationService (`core/services/notification.service.ts`)

**Responsabilidade:** Exibir notificações toast personalizadas

```typescript
@Injectable({ providedIn: 'root' })
export class NotificationService {
  success(message: string, title = '✓ Sucesso'): void
  error(message: string, title = '✗ Erro'): void
  info(message: string, title = 'ℹ Informação'): void
  warning(message: string, title = '⚠ Aviso'): void
  show(message, type, title, options): void
  clear(): void
}
```

**Exemplo de Uso:**

```typescript
// Em qualquer componente
private notificationService = inject(NotificationService);

// Sucesso
this.notificationService.success('Dados salvos!', '✓ Sucesso');

// Erro
this.notificationService.error('Falha ao salvar', '✗ Erro');

// Info
this.notificationService.info('Processando...', 'ℹ Aguarde');

// Aviso
this.notificationService.warning('Campo obrigatório', '⚠ Aviso');
```

## Configuração

### main.ts

```typescript
import { provideToastr } from 'ngx-toastr';
import { errorInterceptor } from './app/core/interceptors/error.interceptor';

bootstrapApplication(AppComponent, {
  providers: [
    provideAnimations(),
    provideToastr({
      timeOut: 3000,                    // Duração padrão
      positionClass: 'toast-top-right', // Posição
      preventDuplicates: true,          // Evitar duplicatas
      progressBar: true,                // Barra de progresso
      maxOpened: 4,                     // Máximo de toasts simultâneos
      newestOnTop: true                 // Toast novo no topo
    }),
    provideHttpClient(
      withInterceptors([credentialsInterceptor, errorInterceptor])
    )
  ]
});
```

### angular.json

```json
{
  "styles": [
    "node_modules/ngx-toastr/toastr.css",
    "src/styles.css"
  ]
}
```

### styles.css

Estilos personalizados para cada tipo de toast:

```css
.toast-success {
  border-left: 4px solid #15803d;
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
}

.toast-error {
  border-left: 4px solid #b91c1c;
  background: linear-gradient(135deg, #fef2f2 0%, #fee2e2 100%);
}

.toast-info {
  border-left: 4px solid #2563eb;
  background: linear-gradient(135deg, #eff6ff 0%, #dbeafe 100%);
}

.toast-warning {
  border-left: 4px solid #f59e0b;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
}
```

## Tipos de Erro

### Validação (400)

**Cenário:** Campos obrigatórios vazios ou inválidos

```json
{
  "sucesso": false,
  "mensagem": "Erro de validação",
  "dados": {
    "titulo": "Título deve ter entre 3 e 100 caracteres",
    "descricao": "Descrição deve ter entre 10 e 1000 caracteres"
  }
}
```

**Toast Exibido:**
```
❌ Dados Inválidos
titulo: Título deve ter entre 3 e 100 caracteres; 
descricao: Descrição deve ter entre 10 e 1000 caracteres
```

### Autenticação (401)

**Cenário:** Sessão expirada ou credenciais inválidas

```json
{
  "sucesso": false,
  "mensagem": "Credenciais inválidas"
}
```

**Toast Exibido:**
```
🔐 Não Autenticado
Sua sessão expirou. Faça login novamente.
```

**Ação:** Limpa session e redireciona para `/login`

### Autorização (403)

**Cenário:** Usuário sem permissão para ação

```json
{
  "sucesso": false,
  "mensagem": "Você não tem permissão para executar esta ação"
}
```

**Toast Exibido:**
```
🚫 Acesso Negado
Você não tem permissão para executar esta ação.
```

### Não Encontrado (404)

**Cenário:** Recurso não existe

```json
{
  "sucesso": false,
  "mensagem": "Chamado não encontrado"
}
```

**Toast Exibido:**
```
❓ Não Encontrado
Chamado não encontrado.
```

### Servidor (500)

**Cenário:** Erro interno do servidor

```json
{
  "sucesso": false,
  "mensagem": "Erro interno do servidor: NullPointerException"
}
```

**Toast Exibido:**
```
💥 Erro do Servidor
Erro interno do servidor. Tente novamente mais tarde.
```

## Fluxo Completo

### 1. Fazer Login com Senha Errada

```typescript
// Component
this.authService.login('admin', 'senha-errada').subscribe({
  next: () => { /* sucesso */ },
  error: () => { /* erro tratado pelo interceptor */ }
});
```

**O que acontece:**
1. Request: `POST /api/auth/login`
2. Backend retorna: `401 Unauthorized`
3. ErrorInterceptor captura
4. Extrai mensagem do backend
5. NotificationService exibe toast
6. Redireciona para login

**Toast Exibido:**
```
🔐 Não Autenticado
Usuário ou senha inválidos
```

### 2. Criar Chamado sem Permissão (Usuário comum)

```typescript
// Component
this.chamadoService.criar({
  titulo: '...',
  descricao: '...'
}).subscribe({
  next: () => { /* sucesso */ },
  error: () => { /* erro tratado */ }
});
```

**O que acontece:**
1. Request: `POST /api/chamados`
2. Backend retorna: `403 Forbidden` (se usuário não é ADMIN)
3. ErrorInterceptor captura
4. Extrai mensagem
5. NotificationService exibe toast

**Toast Exibido:**
```
🚫 Acesso Negado
Você não tem permissão para executar esta ação.
```

### 3. Criar Chamado com Título Vazio

```typescript
// Component
this.chamadoService.criar({
  titulo: '',  // Vazio!
  descricao: 'Descrição válida'
}).subscribe({
  next: () => { /* sucesso */ },
  error: () => { /* erro tratado */ }
});
```

**O que acontece:**
1. Request: `POST /api/chamados`
2. Backend valida
3. Retorna: `400 Bad Request`
4. ErrorInterceptor captura
5. Extrai erros de validação
6. NotificationService exibe toast

**Toast Exibido:**
```
❌ Dados Inválidos
titulo: Título é obrigatório
```

## Posição e Tempo dos Toasts

```
┌──────────────────────────┐
│  ✓ Sucesso (3s)         │  ← top-right
├──────────────────────────┤
│  ✗ Erro (5s)            │
├──────────────────────────┤
│  ℹ Info (3s)            │
├──────────────────────────┤
│  ⚠ Aviso (4s)           │
└──────────────────────────┘

Máximo 4 toasts simultâneos
```

## Estilo dos Toasts

### Sucesso
```
✓ Sucesso
Operação realizada com sucesso
[Barra verde com gradiente]
```

### Erro
```
✗ Erro
Falha ao processar requisição
[Barra vermelha com gradiente]
```

### Informação
```
ℹ Informação
Processando sua solicitação
[Barra azul com gradiente]
```

### Aviso
```
⚠ Aviso
Atenção, verifique os dados
[Barra amarela com gradiente]
```

## Componentes Atualizados

### LoginComponent

```typescript
entrar(): void {
  this.authService.login(usuario, senha).subscribe({
    next: () => {
      this.notificationService.success(
        'Bem-vindo! Redirecionando...',
        '🎉 Login Bem-sucedido'
      );
      this.router.navigate(['/dashboard']);
    },
    error: () => {
      // Interceptor exibe erro automaticamente
      this.carregando = false;
    }
  });
}
```

### ChamadosComponent

```typescript
salvar(): void {
  this.chamadoService.criar(dados).subscribe({
    next: (chamado) => {
      this.chamados = [chamado, ...this.chamados];
      this.form.reset();
      this.notificationService.success(
        'Chamado criado com sucesso!',
        '✓ Sucesso'
      );
    },
    error: () => {
      // Interceptor exibe erro automaticamente
    }
  });
}
```

## Testando

### 1. Login com Credenciais Inválidas

```
Usuário: admin
Senha: senha-errada
Resultado: Toast 401 com mensagem de erro
```

### 2. Criar Chamado sem Título

```
Título: (vazio)
Descrição: Descrição válida
Resultado: Toast 400 com erro de validação
```

### 3. Desconectar e Tentar Ação

```
1. Fazer logout
2. Tentar acessar /chamados
3. Resultado: Toast 401 e redireciona para login
```

### 4. Erro de Rede

```
1. Desligar backend
2. Tentar fazer login
3. Resultado: Toast com erro de conexão
```

## Dependências

```json
{
  "ngx-toastr": "^17.0.2",
  "@angular/animations": "^17.3.12"
}
```

## Principais Características

✅ **Automático:** Interceptor captura todos os erros  
✅ **Personalizado:** Toast com ícones e cores por tipo  
✅ **Informativo:** Extrai mensagens do backend  
✅ **Profissional:** Estilos modernos com gradientes  
✅ **User-friendly:** Posição fixa no canto superior direito  
✅ **Sem Duplicatas:** Previne múltiplos toasts iguais  
✅ **Redireciona:** 401 leva para login automaticamente  
✅ **Timeout:** Tempo diferente por tipo de erro

## Melhorias Futuras

- [ ] Localização (i18n) das mensagens
- [ ] Histórico de notificações
- [ ] Som de notificação (opcional)
- [ ] Notificações de background
- [ ] Integration com Sentry para erros críticos
- [ ] Retry automático para erros de rede
- [ ] Customização de temas de cores

