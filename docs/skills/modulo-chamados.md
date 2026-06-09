# 🎫 Base de Conhecimento: Módulo de Chamados (HP2)

Esta skill documenta a arquitetura, componentes, serviços e padrões do módulo **Chamados** do Helpdesk Pro. Use como referência para manutenção, extensão ou integração com outros módulos.

---

## 📁 1. Estrutura de Arquivos

```
src/app/features/chamados/
├── chamados.routes.ts                          # Rotas lazy do módulo
├── chamados.component.ts / .html / .css        # Componente legado (substituído por /pages)
│
├── pages/
│   ├── lista/
│   │   ├── chamados-lista.component.ts         # Listagem com tabs, filtros e paginação
│   │   ├── chamados-lista.component.html
│   │   └── chamados-lista.component.css
│   └── detalhe/
│       ├── chamado-detalhe.component.ts        # Detalhe + stepper de status + ações
│       ├── chamado-detalhe.component.html
│       └── chamado-detalhe.component.css
│
└── components/
    ├── chamado-filtros/
    │   └── chamado-filtros.component.ts        # Filtro reutilizável (busca + status)
    ├── chamado-form/
    │   ├── chamado-form.component.ts           # Formulário de criação/edição
    │   └── chamado-form.component.html
    ├── chamado-status-badge/
    │   └── chamado-status-badge.component.ts  # Badge visual de status (inline template)
    ├── chamado-resolucao-modal/
    │   ├── chamado-resolucao-modal.component.ts
    │   └── chamado-resolucao-modal.component.html
    └── chamado-timeline/
        ├── chamado-timeline.component.ts       # Timeline de comentários + eventos
        ├── chamado-timeline.component.html
        └── chamado-timeline.component.css
```

---

## 🗺️ 2. Rotas

```typescript
// chamados.routes.ts
export const CHAMADOS_ROUTES: Routes = [
  { path: '',    loadComponent: () => import('./pages/lista/chamados-lista.component')... },
  { path: ':id', loadComponent: () => import('./pages/detalhe/chamado-detalhe.component')... }
];
```

| Rota             | Componente                 | Descrição                  |
|------------------|----------------------------|----------------------------|
| `/chamados`      | `ChamadosListaComponent`   | Listagem com filtros e tabs |
| `/chamados/:id`  | `ChamadoDetalheComponent`  | Detalhe + timeline + ações |

---

## 📦 3. Models e Tipos (`core/models/chamado.ts`)

```typescript
export type StatusChamado = 'ABERTO' | 'EM_ATENDIMENTO' | 'RESOLVIDO' | 'FECHADO';

export interface Chamado {
  id: number;
  titulo: string;
  descricao: string;
  status: StatusChamado;
  usuarioId: number;
  usuarioNome: string;
  usuarioAtribuidoId: number | null;
  usuarioAtribuidoNome: string | null;
  dataCriacao: string;
  dataAtualizacao: string;
  dataFechamento: string | null;
}

export interface ChamadoFiltros {
  status?: StatusChamado | '';
  busca?: string;
}

export interface CriarChamadoRequest {
  titulo: string;       // min 3 chars
  descricao: string;    // min 10 chars
  usuarioId: number;
}

export interface AtualizarChamadoRequest {
  titulo?: string;
  descricao?: string;
  status?: StatusChamado;
  usuarioAtribuidoId?: number | null;
}

// Labels PT-BR prontos para uso no template
export const STATUS_CHAMADO_LABELS: Record<StatusChamado, string> = {
  ABERTO: 'Aberto',
  EM_ATENDIMENTO: 'Em Atendimento',
  RESOLVIDO: 'Resolvido',
  FECHADO: 'Fechado'
};
```

---

## 🔌 4. Serviços

### `ChamadoService` (`core/services/chamado.service.ts`)

| Método                              | HTTP                        | Descrição                          |
|-------------------------------------|-----------------------------|------------------------------------|
| `listar(page, size, filtros?)`      | `GET /api/chamados`         | Lista paginada com filtros opcionais |
| `buscarPorId(id)`                   | `GET /api/chamados/:id`     | Retorna um chamado completo         |
| `criar(request)`                    | `POST /api/chamados`        | Cria novo chamado                   |
| `atualizar(id, request)`            | `PUT /api/chamados/:id`     | Atualiza campos ou status           |
| `deletar(id)`                       | `DELETE /api/chamados/:id`  | Soft delete (ADMIN only)            |

**Filtros suportados via query params:**
```
GET /api/chamados?page=0&size=10&sort=dataCriacao,desc&status=ABERTO&busca=erro
```

### `ComentarioService` (`core/services/comentario.service.ts`)

| Método                        | HTTP                                      | Descrição                   |
|-------------------------------|-------------------------------------------|-----------------------------|
| `listar(chamadoId)`           | `GET /api/chamados/:id/comentarios`       | Lista comentários do chamado |
| `adicionar(chamadoId, payload)` | `POST /api/chamados/:id/comentarios`    | Adiciona comentário/evento   |

**Model de comentário:**
```typescript
export interface Comentario {
  id: number;
  chamadoId: number;
  autorId: number | null;
  autorNome: string;
  autorPerfil: string | null;
  conteudo: string;
  tipo: 'COMENTARIO' | 'EVENTO_SISTEMA';
  interno: boolean;          // true = visível só para SUPORTE/ADMIN
  dataCriacao: string;
}
```

---

## 🧩 5. Componentes Reutilizáveis

### `ChamadoStatusBadgeComponent`

Exibe o badge visual de status. Usa inline template.

```html
<app-chamado-status-badge [status]="chamado.status" />
```

Classes aplicadas automaticamente:
| Status           | Classe CSS         |
|------------------|--------------------|
| `ABERTO`         | `badge badge-blue` |
| `EM_ATENDIMENTO` | `badge badge-yellow` |
| `RESOLVIDO`      | `badge badge-green` |
| `FECHADO`        | `badge badge-gray`  |

---

### `ChamadoFiltrosComponent`

Barra de filtros com busca por texto e select de status. Emite `ChamadoFiltros` via Output.

```html
<app-chamado-filtros (filtrosChange)="onFiltrosChange($event)" />
```

```typescript
// No componente pai:
onFiltrosChange(f: ChamadoFiltros): void {
  this.filtros = { ...f, status: this.tabAtiva };
  this.carregar(0);
}
```

---

### `ChamadoFormComponent`

Formulário reativo para criar ou editar chamado. Emite o `Chamado` salvo via `@Output() salvo`.

```html
<!-- Criação -->
<app-chamado-form (salvo)="aoSalvar($event)" (cancelar)="mostrarForm = false" />

<!-- Edição -->
<app-chamado-form [chamado]="chamado" (salvo)="aoSalvarEdicao($event)" />
```

---

### `ChamadoTimelineComponent`

Timeline de comentários e eventos do sistema de um chamado.

```html
<app-chamado-timeline [chamadoId]="chamado.id" [fechado]="chamado.status === 'FECHADO'" />
```

- `[chamadoId]` (required): ID do chamado
- `[fechado]`: Desabilita o formulário de novo comentário quando `true`
- Suporta modo interno (visível apenas para SUPORTE/ADMIN)
- Distingue visualmente `COMENTARIO` de `EVENTO_SISTEMA`

---

### `ChamadoResolucaoModalComponent`

Modal para registrar a resolução com descrição obrigatória.

```html
<app-chamado-resolucao-modal
  [chamadoId]="chamado.id"
  (resolvido)="aoResolvido($event)"
  (cancelar)="mostrarModalResolucao = false"
/>
```

---

## 🔐 6. Permissões (RBAC)

As permissões são verificadas via `PermissionService` com a chave `'CHAMADOS'`.

| Ação        | USER | SUPORTE | ADMIN | SUPER_ADMIN |
|-------------|------|---------|-------|-------------|
| `VISUALIZAR`| ✅   | ✅      | ✅    | ✅          |
| `CRIAR`     | ✅   | ✅      | ✅    | ✅          |
| `EDITAR`    | ❌   | ❌      | ✅    | ✅          |
| `ATENDER`   | ❌   | ✅      | ✅    | ✅          |
| `ATRIBUIR`  | ❌   | ✅      | ✅    | ✅          |
| `EXCLUIR`   | ❌   | ❌      | ✅    | ✅          |

```typescript
// Padrão de uso no componente
get podeEditar(): boolean {
  return this.perm.can(this.auth.usuarioAtual?.perfil ?? 'USER', 'CHAMADOS', 'EDITAR');
}
```

---

## 🔄 7. Fluxo de Status

```
ABERTO ──► EM_ATENDIMENTO ──► RESOLVIDO ──► FECHADO
  ▲                                │
  └────────────────────────────────┘ (reabrir)
```

| Transição                          | Quem pode        | Condição                          |
|------------------------------------|------------------|-----------------------------------|
| `ABERTO → EM_ATENDIMENTO`          | SUPORTE, ADMIN   | Chamado deve estar atribuído a ele |
| `EM_ATENDIMENTO → RESOLVIDO`       | SUPORTE, ADMIN   | Requer descrição da resolução      |
| `RESOLVIDO → FECHADO`              | Proprietário, SUPORTE, ADMIN | Confirmação do solicitante |
| `RESOLVIDO/FECHADO → ABERTO`       | SUPORTE, ADMIN   | Reabertura justificada             |

---

## 🏗️ 8. Padrão da Página de Lista

A `ChamadosListaComponent` combina tabs de contagem + filtro livre + tabela + paginação:

```typescript
// Tabs com contagem por status (carregadas em paralelo via N+1 requests)
readonly tabs: TabStatus[] = [
  { status: '', label: 'Todos', count: 0 },
  ...STATUS_LIST.map(s => ({ status: s, label: STATUS_CHAMADO_LABELS[s], count: 0 }))
];

// Tab ativa controla o filtro de status; filtros livres são compostos:
onFiltrosChange(f: ChamadoFiltros): void {
  this.filtros = { ...f, status: this.tabAtiva };
  this.carregar(0);
}
```

> ⚠️ As contagens de cada tab são carregadas com `size=1` para minimizar payload. Para escalar, substituir por um endpoint `/api/chamados/contagens` no backend.

---

## ✅ 9. Checklist para Estender o Módulo

Ao adicionar uma nova funcionalidade no módulo de chamados:

- [ ] Novo campo no `Chamado` interface? Adicionar também no `AtualizarChamadoRequest` se editável
- [ ] Nova transição de status? Adicionar getter de permissão em `ChamadoDetalheComponent` e linha na tabela de fluxo acima
- [ ] Novo componente reutilizável? Seguir o padrão standalone com `@Input({ required: true })`
- [ ] Novo endpoint de API? Adicionar método no `ChamadoService` e documentar no `API.md`
- [ ] Nova permissão? Atualizar `permissions.config.ts` e a tabela RBAC desta skill

---

> [!NOTE]
> Ao ler este arquivo com `IsSkillFile: true`, qualquer agente de IA pode gerar extensões do módulo Chamados seguindo 100% os padrões HP2 sem desvios arquiteturais.
