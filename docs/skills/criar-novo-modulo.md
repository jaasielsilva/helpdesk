# 🛠️ Skill de Padronização: Criar Novo Módulo (Angular + HP2)

Esta "Skill" serve como gabarito arquitetural e visual para a criação de qualquer novo módulo/funcionalidade no frontend do **Helpdesk Pro**. Ela garante consistência absoluta no código Typescript, marcação HTML (HP2 Design System), estilos CSS e integração com serviços do backend Spring Boot.

---

## 📁 1. Estrutura de Pastas Padrão

Todo novo módulo de negócio deve ser criado dentro do diretório `/src/app/features/` usando componentes autônomos (*Standalone Components*).

```
src/app/features/novo-modulo/
├── novo-modulo.component.ts      # Lógica, reatividade, controle de estado e chamadas à API
├── novo-modulo.component.html    # Marcação estruturada seguindo as classes semânticas HP2
└── novo-modulo.component.css     # Estilos e transições particulares da tela (se houver)
```

---

## ☕ 2. Gabarito de Código: Typescript (`novo-modulo.component.ts`)

A lógica deve gerenciar de forma limpa os estados de carregamento, validações de formulário reativo e exibição de feedbacks visuais de sucesso/erro via `NotificationService`.

```typescript
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NotificationService } from '../../core/services/notification.service';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

// Interfaces locais de dados (conforme os DTOs do backend)
interface ElementoItem {
  id: number;
  titulo: string;
  descricao: string;
  status: string;
  dataCriacao: string;
}

@Component({
  selector: 'app-novo-modulo',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './novo-modulo.component.html',
  styleUrls: ['./novo-modulo.component.css']
})
export class NovoModuloComponent implements OnInit {
  // Controle de estados da UI
  carregando = false;
  salvando = false;
  
  // Estruturas de dados
  elementos: ElementoItem[] = [];
  form!: FormGroup;

  constructor(
    private fb: FormBuilder,
    private notification: NotificationService,
    private http: HttpClient
  ) {}

  ngOnInit(): void {
    this.inicializarFormulario();
    this.carregarDados();
  }

  /**
   * Inicializa o formulário reativo com validações completas
   */
  private inicializarFormulario(): void {
    this.form = this.fb.group({
      titulo: ['', [Validators.required, Validators.minLength(5)]],
      descricao: ['', [Validators.required, Validators.minLength(10)]]
    });
  }

  /**
   * Carrega os registros do backend controlando o estado de carregamento
   */
  async carregarDados(): Promise<void> {
    this.carregando = true;
    try {
      // Ajustar URL para o endpoint correspondente do backend Spring Boot
      const resposta = await firstValueFrom(
        this.http.get<any>('/api/novo-modulo-endpoint')
      );
      this.elementos = resposta.dados || resposta;
    } catch (erro) {
      // O interceptor global trata a maioria dos erros, mas tratamentos específicos vão aqui
      console.error('Erro ao buscar dados:', erro);
    } finally {
      this.carregando = false;
    }
  }

  /**
   * Envia o formulário validando os campos e exibindo Toasts de feedback
   */
  async salvar(): Promise<void> {
    if (this.form.invalid) {
      this.notification.warning('Por favor, preencha todos os campos obrigatórios corretamente.');
      this.form.markAllAsTouched();
      return;
    }

    this.salvando = true;
    try {
      const payload = this.form.value;
      await firstValueFrom(
        this.http.post('/api/novo-modulo-endpoint', payload)
      );

      this.notification.success('Registro criado com sucesso!', '🎉 Sucesso');
      this.form.reset();
      this.carregarDados(); // Recarrega a listagem
    } catch (erro) {
      console.error('Erro ao salvar:', erro);
    } finally {
      this.salvando = false;
    }
  }

  // Helpers auxiliares para validação no HTML
  campoInvalido(nomeCampo: string): boolean {
    const campo = this.form.get(nomeCampo);
    return !!(campo && campo.invalid && (campo.dirty || campo.touched));
  }
}
```

---

## 🎨 3. Gabarito de Código: HTML (`novo-modulo.component.html`)

O HTML deve estruturar a tela usando a arquitetura de **Grades Fluidas**, **Painéis com Sombras Premium**, **Inputs Validados** e **Tabelas Elegantes** baseadas no HP2 Design System.

```html
<!-- Grid do Layout de Negócio (Esquerda: Formulário, Direita: Listagem) -->
<div class="grid grid-cols-1 lg:grid-cols-[380px_1fr] gap-6 items-start">

  <!-- PAINEL ESQUERDO: Formulário de Registro -->
  <section class="panel p-6 rounded-2xl bg-white border border-slate-100/80 shadow-premium">
    <div class="border-b border-slate-100 pb-4 mb-5">
      <h2 class="text-lg font-bold text-slate-800 flex items-center gap-2">
        <span>🚀</span> Novo Registro
      </h2>
      <p class="text-xs text-slate-500 mt-1">Crie um novo item operacional na fila do helpdesk.</p>
    </div>

    <form [formGroup]="form" (ngSubmit)="salvar()" class="space-y-4">
      
      <!-- Campo 1: Título -->
      <div>
        <label for="titulo" class="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">
          Título do Item <span class="text-rose-500">*</span>
        </label>
        <div class="relative">
          <span class="absolute left-4 top-3.5 text-slate-400">📝</span>
          <input 
            id="titulo" 
            type="text" 
            formControlName="titulo"
            class="w-full pl-11 pr-4 py-3 rounded-lg border text-sm transition-all focus:outline-none focus:ring-2 focus:ring-opacity-20"
            [class.border-slate-200]="!campoInvalido('titulo')"
            [class.border-rose-500]="campoInvalido('titulo')"
            [class.focus:ring-brand-500]="!campoInvalido('titulo')"
            [class.focus:ring-rose-500]="campoInvalido('titulo')"
            [class.focus:border-brand-500]="!campoInvalido('titulo')"
            [class.focus:border-rose-500]="campoInvalido('titulo')"
            placeholder="Ex: Instalação de software corporativo"
          />
        </div>
        <!-- Feedback de Erro Dinâmico -->
        <div *ngIf="campoInvalido('titulo')" class="text-xs text-rose-500 mt-1.5 flex items-center gap-1 animate-slideDown">
          <span>⚠️</span> O título é obrigatório (mín. 5 caracteres).
        </div>
      </div>

      <!-- Campo 2: Descrição -->
      <div>
        <label for="descricao" class="block text-xs font-bold text-slate-700 uppercase tracking-wider mb-2">
          Descrição Detalhada <span class="text-rose-500">*</span>
        </label>
        <textarea 
          id="descricao" 
          formControlName="descricao" 
          rows="4"
          class="w-full px-4 py-3 rounded-lg border text-sm transition-all focus:outline-none focus:ring-2 focus:ring-opacity-20"
          [class.border-slate-200]="!campoInvalido('descricao')"
          [class.border-rose-500]="campoInvalido('descricao')"
          [class.focus:ring-brand-500]="!campoInvalido('descricao')"
          [class.focus:ring-rose-500]="campoInvalido('descricao')"
          [class.focus:border-brand-500]="!campoInvalido('descricao')"
          [class.focus:border-rose-500]="campoInvalido('descricao')"
          placeholder="Descreva detalhadamente a necessidade do negócio..."
        ></textarea>
        <!-- Feedback de Erro Dinâmico -->
        <div *ngIf="campoInvalido('descricao')" class="text-xs text-rose-500 mt-1.5 flex items-center gap-1 animate-slideDown">
          <span>⚠️</span> A descrição é obrigatória (mín. 10 caracteres).
        </div>
      </div>

      <!-- Botão de Ação Primário com Estado de Carregamento -->
      <button 
        type="submit" 
        [disabled]="form.invalid || salvando"
        class="w-full btn-primary py-3.5 flex items-center justify-center gap-2 text-sm font-semibold rounded-lg transition-all"
      >
        <span *ngIf="!salvando" class="flex items-center gap-2">
          <span>💾</span> Salvar Registro
        </span>
        <span *ngIf="salvando" class="flex items-center gap-2">
          <span class="loading-spinner w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
          Processando...
        </span>
      </button>
    </form>
  </section>

  <!-- PAINEL DIREITO: Tabela / Visualização dos Dados -->
  <section class="panel p-6 rounded-2xl bg-white border border-slate-100/80 shadow-premium">
    
    <!-- Cabeçalho do Painel -->
    <div class="border-b border-slate-100 pb-4 mb-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4">
      <div>
        <h2 class="text-lg font-bold text-slate-800 flex items-center gap-2">
          <span>📋</span> Fila de Operações
        </h2>
        <p class="text-xs text-slate-500 mt-1">Acompanhamento dos registros criados em tempo real.</p>
      </div>
      <button (click)="carregarDados()" class="btn-secondary px-4 py-2 text-xs flex items-center gap-2 rounded-lg">
        <span>🔄</span> Atualizar
      </button>
    </div>

    <!-- Estado: Carregando -->
    <div *ngIf="carregando" class="py-12 flex flex-col items-center justify-center gap-3 text-slate-400">
      <span class="loading-spinner w-8 h-8 border-3 border-slate-200 border-t-brand-500 rounded-full animate-spin"></span>
      <span class="text-xs font-semibold uppercase tracking-wider">Buscando dados da nuvem...</span>
    </div>

    <!-- Estado: Sem Registros -->
    <div *ngIf="!carregando && elementos.length === 0" class="py-16 text-center text-slate-400">
      <span class="text-4xl block mb-3">📂</span>
      <p class="text-sm font-medium">Nenhum registro encontrado.</p>
      <p class="text-xs mt-1">Utilize o formulário ao lado para cadastrar seu primeiro registro.</p>
    </div>

    <!-- Tabela de Dados Padronizada (Zebra & Hover) -->
    <div *ngIf="!carregando && elementos.length > 0" class="overflow-x-auto">
      <table class="data-table w-full text-left border-collapse text-sm">
        <thead>
          <tr class="border-b border-slate-100 text-xs font-bold text-slate-400 uppercase tracking-wider bg-slate-50/50 rounded-t-lg">
            <th class="py-4 px-4">#</th>
            <th class="py-4 px-4">Título</th>
            <th class="py-4 px-4 text-center">Status</th>
            <th class="py-4 px-4 text-right">Criado em</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-slate-50">
          <tr *ngFor="let item of elementos; trackBy: item.id" class="hover:bg-slate-50/40 transition-colors">
            <!-- ID com ênfase menor -->
            <td class="py-4 px-4 font-bold text-slate-400 text-xs">#{{ item.id }}</td>
            
            <!-- Conteúdo Principal -->
            <td class="py-4 px-4">
              <span class="block font-semibold text-slate-700 text-sm">{{ item.titulo }}</span>
              <span class="block text-xs text-slate-500 mt-0.5 truncate max-w-[280px]">{{ item.descricao }}</span>
            </td>
            
            <!-- Badges Glassmorphic Estilizados -->
            <td class="py-4 px-4 text-center">
              <span 
                class="badge" 
                [class.badge-blue]="item.status === 'ABERTO'"
                [class.badge-yellow]="item.status === 'EM_ATENDIMENTO'"
                [class.badge-green]="item.status === 'RESOLVIDO'"
              >
                {{ item.status }}
              </span>
            </td>
            
            <!-- Data Formatada -->
            <td class="py-4 px-4 text-right text-xs text-slate-400 font-medium">
              {{ item.dataCriacao | date:'dd/MM/yyyy HH:mm' }}
            </td>
          </tr>
        </tbody>
      </table>
    </div>

  </section>
</div>
```

---

## 🔮 4. Gabarito de Código: Estilos Específicos (`novo-modulo.component.css`)

O CSS local do componente deve focar estritamente em animações de transição local ou pequenos layouts de grade.

```css
/* Animação suave para o slide down do erro de validação */
@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.animate-slideDown {
  animation: slideDown 0.2s ease-out forwards;
}

/* Garante o círculo perfeito no spinner */
.loading-spinner {
  display: inline-block;
}
```

---

## 🚀 5. Checklist de Integração de Rotas

Sempre que concluir a criação dos arquivos do módulo, insira-o no arquivo `/src/app/app.routes.ts` definindo o título da página:

1. Importe o componente no topo:
   ```typescript
   import { NovoModuloComponent } from './features/novo-modulo/novo-modulo.component';
   ```
2. Adicione nas rotas filhas do `AppShellComponent`:
   ```typescript
   { 
     path: 'nome-da-rota', 
     component: NovoModuloComponent, 
     data: { title: 'Título que Aparecerá no Topbar' } 
   }
   ```
3. Atualize os links no menu lateral (`app-shell.component.html`) utilizando a classe ativa configurada:
   ```html
   <a routerLink="/nome-da-rota" routerLinkActive="bg-brand-50 text-brand-700 border-l-4 border-brand-purple" ...>
   ```

---

> [!NOTE]  
> Ao ler este arquivo corporativo com a opção `IsSkillFile: true` habilitada, você ou qualquer outro agente de IA podem gerar novas páginas que utilizam **100% da identidade HP2 de forma automatizada e sem erros**.
