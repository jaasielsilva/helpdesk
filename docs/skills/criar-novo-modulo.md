# Skill de Padronizacao: Criar Novo Modulo (Angular + Spring Boot)

Este guia define o padrao arquitetural para criacao de qualquer novo modulo no **Helpdesk Pro**.
O modulo **Chamados** e a referencia oficial -- siga a mesma estrutura, nomenclatura e padroes.

---

## 1. Estrutura de Arquivos

### Backend (Spring Boot)

```
src/main/java/com/jaasielsilva/helpdesk/
├── controller/NovoController.java        # REST controller com @PreAuthorize
├── dto/novo/
│   ├── NovoCreateRequest.java            # Java record para criacao
│   ├── NovoUpdateRequest.java            # Java record para edicao
│   └── NovoResponse.java                 # Java record com static from(Model)
├── model/Novo.java                       # Entidade JPA
├── repository/NovoRepository.java        # Spring Data JPA
├── service/
│   ├── NovoService.java                  # Interface
│   └── NovoServiceImpl.java             # Implementacao com @Transactional
```

### Frontend (Angular Standalone)

```
frontend/src/app/
├── core/
│   ├── models/novo.ts                    # Interfaces + tipos + labels
│   └── services/novo.service.ts          # Service com HttpClient + Observable
├── features/novo/
│   ├── novo.component.ts                 # Component standalone com inject()
│   ├── novo.component.html               # Template com @if/@for (novo control flow)
│   └── novo.component.css                # Estilos locais (minimo)
```

---

## 2. Backend: Padroes

### DTO Response com static from()

Todo DTO de resposta deve ter um factory method `from()` para conversao da entidade:

```java
public record NovoResponse(
    Long id,
    String titulo,
    String descricao,
    LocalDateTime dataCriacao
) {
    public static NovoResponse from(Novo novo) {
        return new NovoResponse(
            novo.getId(),
            novo.getTitulo(),
            novo.getDescricao(),
            novo.getDataCriacao()
        );
    }
}
```

### Controller com PreAuthorize

```java
@RestController
@RequestMapping("/api/novos")
public class NovoController {

    private final NovoService service;

    public NovoController(NovoService service) {
        this.service = service;
    }

    @PostMapping
    @PreAuthorize("@permService.can(authentication, T(...ModuloSistema).NOVO, T(...PermissaoAcao).CRIAR)")
    public ResponseEntity<ApiResponse<NovoResponse>> criar(
            @Valid @RequestBody NovoCreateRequest request, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.sucesso("Criado com sucesso", service.criar(request, auth)));
    }

    @GetMapping
    @PreAuthorize("@permService.can(authentication, T(...ModuloSistema).NOVO, T(...PermissaoAcao).VISUALIZAR)")
    public ResponseEntity<ApiResponse<Page<NovoResponse>>> listar(Pageable pageable, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.sucesso("Listados", service.listar(pageable, auth)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permService.can(authentication, T(...ModuloSistema).NOVO, T(...PermissaoAcao).EDITAR)")
    public ResponseEntity<ApiResponse<NovoResponse>> atualizar(
            @PathVariable Long id, @Valid @RequestBody NovoUpdateRequest request, Authentication auth) {
        return ResponseEntity.ok(ApiResponse.sucesso("Atualizado", service.atualizar(id, request, auth)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permService.can(authentication, T(...ModuloSistema).NOVO, T(...PermissaoAcao).EXCLUIR)")
    public ResponseEntity<ApiResponse<Void>> deletar(@PathVariable Long id, Authentication auth) {
        service.deletar(id, auth);
        return ResponseEntity.ok(ApiResponse.sucesso("Deletado"));
    }
}
```

### Service com Tenant Isolation

```java
@Service
@Transactional
public class NovoServiceImpl implements NovoService {

    // Constructor injection (sem @Autowired)
    private final NovoRepository repository;
    private final TenantAccessService tenantAccessService;

    public NovoServiceImpl(NovoRepository repository, TenantAccessService tenantAccessService) {
        this.repository = repository;
        this.tenantAccessService = tenantAccessService;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NovoResponse> listar(Pageable pageable, Authentication auth) {
        UsuarioAutenticado autenticado = UsuarioDetailsService.requireUsuarioAutenticado(auth);
        if (autenticado.isSuperAdmin()) {
            return repository.findAllActive(pageable).map(NovoResponse::from);
        }
        Long empresaId = tenantAccessService.requireEmpresaId();
        return repository.findAllActiveByEmpresa(empresaId, pageable).map(NovoResponse::from);
    }
}
```

---

## 3. Frontend: Model (`core/models/novo.ts`)

```typescript
export interface Novo {
  id: number;
  titulo: string;
  descricao: string;
  dataCriacao: string;
}

export interface CriarNovoRequest {
  titulo: string;
  descricao: string;
}

export interface AtualizarNovoRequest {
  titulo?: string;
  descricao?: string;
}
```

**Importante:** `PageResponse` e compartilhado -- importe de `core/models/page-response`:

```typescript
import { PageResponse } from '../models/page-response';
```

---

## 4. Frontend: Service (`core/services/novo.service.ts`)

Padrao: `inject()` para DI, `Observable` + `map` para extrair `dados` do `ApiResponse`:

```typescript
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import { AtualizarNovoRequest, CriarNovoRequest, Novo } from '../models/novo';
import { PageResponse } from '../models/page-response';

@Injectable({ providedIn: 'root' })
export class NovoService {
  private readonly http = inject(HttpClient);

  listar(page = 0, size = 10): Observable<PageResponse<Novo>> {
    const params = new HttpParams()
      .set('page', page).set('size', size).set('sort', 'dataCriacao,desc');
    return this.http.get<{ dados: PageResponse<Novo> }>('/api/novos', { params }).pipe(
      map(res => res.dados)
    );
  }

  criar(request: CriarNovoRequest): Observable<Novo> {
    return this.http.post<{ dados: Novo }>('/api/novos', request).pipe(map(res => res.dados));
  }

  atualizar(id: number, request: AtualizarNovoRequest): Observable<Novo> {
    return this.http.put<{ dados: Novo }>(`/api/novos/${id}`, request).pipe(map(res => res.dados));
  }

  deletar(id: number): Observable<void> {
    return this.http.delete<{ dados: void }>(`/api/novos/${id}`).pipe(map(() => undefined));
  }
}
```

---

## 5. Frontend: Component (`features/novo/novo.component.ts`)

Padrao: `inject()`, `formBuilder.nonNullable.group()`, componentes compartilhados:

```typescript
import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { EmptyStateComponent } from '../../core/components/empty-state.component';
import { PaginationComponent } from '../../core/components/pagination.component';
import { Novo } from '../../core/models/novo';
import { PageResponse } from '../../core/models/page-response';
import { AuthService } from '../../core/services/auth.service';
import { NovoService } from '../../core/services/novo.service';
import { NotificationService } from '../../core/services/notification.service';
import { PermissionService } from '../../core/services/permission.service';

@Component({
  selector: 'app-novo',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule, PaginationComponent, EmptyStateComponent],
  templateUrl: './novo.component.html',
  styleUrl: './novo.component.css'
})
export class NovoComponent implements OnInit {
  private readonly novoService = inject(NovoService);
  private readonly authService = inject(AuthService);
  private readonly notificationService = inject(NotificationService);
  private readonly permissionService = inject(PermissionService);
  private readonly formBuilder = inject(FormBuilder);

  itens: Novo[] = [];
  carregando = true;
  salvando = false;

  paginaAtual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  readonly tamanhoPagina = 10;

  get podeCriar(): boolean {
    const perfil = this.authService.usuarioAtual?.perfil ?? 'USER';
    return this.permissionService.can(perfil, 'NOVO_MODULO', 'CRIAR');
  }

  readonly form = this.formBuilder.nonNullable.group({
    titulo: ['', [Validators.required, Validators.minLength(3)]],
    descricao: ['', [Validators.required, Validators.minLength(10)]]
  });

  ngOnInit(): void {
    this.carregar();
  }

  salvar(): void {
    if (this.form.invalid || this.salvando) {
      this.notificationService.warning('Preencha todos os campos corretamente');
      return;
    }
    this.salvando = true;
    this.novoService.criar(this.form.getRawValue()).subscribe({
      next: () => {
        this.form.reset();
        this.salvando = false;
        this.notificationService.success('Registro criado com sucesso!');
        this.carregar(0);
      },
      error: () => { this.salvando = false; }
    });
  }

  carregar(pagina = 0): void {
    this.carregando = true;
    this.novoService.listar(pagina, this.tamanhoPagina).subscribe({
      next: (page: PageResponse<Novo>) => {
        this.itens = page.content;
        this.paginaAtual = page.page.number;
        this.totalPaginas = page.page.totalPages;
        this.totalElementos = page.page.totalElements;
        this.carregando = false;
      },
      error: () => { this.carregando = false; }
    });
  }

  campoInvalido(nomeCampo: string): boolean {
    const campo = this.form.get(nomeCampo);
    return !!(campo && campo.invalid && (campo.dirty || campo.touched));
  }
}
```

---

## 6. Frontend: Template (`features/novo/novo.component.html`)

Usar **novo control flow** (`@if`, `@for`) e componentes compartilhados (`<app-pagination>`, `<app-empty-state>`):

```html
<div class="grid grid-cols-1 lg:grid-cols-[380px_1fr] gap-6 items-start animate-fade-in">

  <!-- PAINEL ESQUERDO: Formulario -->
  <section class="panel bg-white border border-slate-100 rounded-2xl p-6 shadow-premium">
    <!-- ...cabecalho + form (ver Chamados como referencia)... -->
  </section>

  <!-- PAINEL DIREITO: Listagem -->
  <section class="panel bg-white border border-slate-100 rounded-2xl p-6 shadow-premium">
    <!-- ...cabecalho... -->

    @if (carregando) {
      <div class="py-16 flex flex-col items-center justify-center gap-3 text-slate-400">
        <span class="loading-spinner w-8 h-8 border-3 border-slate-200 border-t-brand-500 rounded-full animate-spin"></span>
        <span class="text-xs font-semibold uppercase tracking-wider">Carregando...</span>
      </div>
    } @else if (itens.length === 0) {
      <app-empty-state
        icone="📂"
        titulo="Nenhum registro encontrado."
        descricao="Use o formulario ao lado para criar o primeiro registro."
      />
    } @else {
      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr class="bg-slate-50/50">
              <th>ID</th>
              <th>Titulo</th>
              <th class="text-right">Criado em</th>
            </tr>
          </thead>
          <tbody class="divide-y divide-slate-50">
            @for (item of itens; track item.id) {
              <tr class="hover:bg-slate-50/30 transition-colors duration-150">
                <td class="font-bold text-slate-400 text-xs w-16">#{{ item.id }}</td>
                <td>
                  <strong class="text-slate-700 font-semibold block text-sm">{{ item.titulo }}</strong>
                  <p class="text-xs text-slate-400 mt-0.5 max-w-[420px] truncate block">{{ item.descricao }}</p>
                </td>
                <td class="text-right text-xs text-slate-400 font-medium w-40">
                  {{ item.dataCriacao | date:'dd/MM/yyyy HH:mm' }}
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>

      <app-pagination
        [paginaAtual]="paginaAtual"
        [totalPaginas]="totalPaginas"
        (irParaPagina)="carregar($event)"
      />
    }
  </section>
</div>
```

---

## 7. Componentes Compartilhados

### PaginationComponent (`core/components/pagination.component.ts`)

Inputs: `paginaAtual`, `totalPaginas`. Output: `irParaPagina` (EventEmitter).
Ja inclui CSS premium inline.

### EmptyStateComponent (`core/components/empty-state.component.ts`)

Inputs: `icone`, `titulo`, `descricao`.

### PageResponse (`core/models/page-response.ts`)

Tipo generico compartilhado para respostas paginadas do Spring Boot.

---

## 8. Checklist de Integracao

1. Criar os arquivos backend + frontend seguindo os padroes acima
2. Adicionar `NovoResponse.from()` como factory method no DTO
3. Registrar a rota em `app.routes.ts` dentro do children do AppShell
4. Adicionar o modulo em `ModuloSistema.java` (enum backend)
5. Adicionar permissoes em `MatrizPermissoes.java` (PERMISSION_MATRIX)
6. Adicionar o item de menu em `menu.config.ts` e `route-permissions.config.ts`
7. Adicionar a permissao no `permissions.config.ts` frontend

---

## 9. Modulo de Referencia

O modulo **Chamados** e o modelo definitivo. Arquivos de referencia:

| Camada | Arquivo |
|--------|---------|
| Controller | `controller/ChamadoController.java` |
| Service Impl | `service/ChamadoServiceImpl.java` |
| DTO Response | `dto/chamado/ChamadoResponse.java` |
| Frontend Model | `core/models/chamado.ts` |
| Frontend Service | `core/services/chamado.service.ts` |
| Frontend Component | `features/chamados/chamados.component.ts` |
| Frontend Template | `features/chamados/chamados.component.html` |
