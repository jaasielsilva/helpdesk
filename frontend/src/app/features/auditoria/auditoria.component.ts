import { DatePipe, NgForOf, NgIf } from '@angular/common';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { firstValueFrom } from 'rxjs';

import {
  PermissionAction,
  SystemModule,
} from '../../core/config/permissions.config';
import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { PermissionService } from '../../core/services/permission.service';

// ─── Tipos locais ──────────────────────────────────────────────────────────────

export interface AuditoriaLog {
  id: number;
  usuario: string;
  modulo: string;
  acao: string;
  descricao: string;
  dataHora: string;
}

export interface AuditoriaFiltros {
  usuario?: string;
  modulo?: string;
  acao?: string;
  dataInicio?: string;
  dataFim?: string;
}

interface ApiResponse<T> {
  dados: T;
}

interface PageResponse<T> {
  content: T[];
  page: {
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
  };
}

const MODULOS_SISTEMA: { valor: string; label: string }[] = [
  { valor: '', label: 'Todos os módulos' },
  { valor: 'DASHBOARD', label: 'Dashboard' },
  { valor: 'CHAMADOS', label: 'Chamados' },
  { valor: 'BASE_CONHECIMENTO', label: 'Base de Conhecimento' },
  { valor: 'MENSAGENS', label: 'Mensagens' },
  { valor: 'NOTIFICACOES', label: 'Notificações' },
  { valor: 'EQUIPES', label: 'Equipes' },
  { valor: 'CATEGORIAS', label: 'Categorias' },
  { valor: 'SLA', label: 'SLA' },
  { valor: 'ANALYTICS', label: 'Analytics' },
  { valor: 'RELATORIOS', label: 'Relatórios' },
  { valor: 'AUTOMACAO', label: 'Automação' },
  { valor: 'INTEGRACOES', label: 'Integrações' },
  { valor: 'USUARIOS', label: 'Usuários' },
  { valor: 'AUDITORIA', label: 'Auditoria' },
  { valor: 'EMPRESAS', label: 'Empresas' },
  { valor: 'ASSINATURA', label: 'Assinatura' },
];

const ACOES_SISTEMA: { valor: string; label: string }[] = [
  { valor: '', label: 'Todas as ações' },
  { valor: 'VISUALIZAR', label: 'Visualizar' },
  { valor: 'CRIAR', label: 'Criar' },
  { valor: 'EDITAR', label: 'Editar' },
  { valor: 'EXCLUIR', label: 'Excluir' },
  { valor: 'ATENDER', label: 'Atender' },
  { valor: 'ATRIBUIR', label: 'Atribuir' },
  { valor: 'GERENCIAR', label: 'Gerenciar' },
];

// ─── Componente ────────────────────────────────────────────────────────────────

@Component({
  selector: 'app-auditoria',
  standalone: true,
  imports: [NgIf, NgForOf, DatePipe, ReactiveFormsModule],
  templateUrl: './auditoria.component.html',
  styleUrls: ['./auditoria.component.css'],
})
export class AuditoriaComponent implements OnInit {
  private readonly http = inject(HttpClient);
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly permissionService = inject(PermissionService);
  private readonly notification = inject(NotificationService);

  // ── Estado da UI ────────────────────────────────────────────────────────────
  carregando = false;
  buscando = false;
  semPermissao = false;

  // ── Dados ───────────────────────────────────────────────────────────────────
  logs: AuditoriaLog[] = [];

  // ── Paginação ────────────────────────────────────────────────────────────────
  paginaAtual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  readonly tamanhoPagina = 20;

  // ── Formulário de filtros ───────────────────────────────────────────────────
  form!: FormGroup;

  // ── Listas de opções ────────────────────────────────────────────────────────
  readonly modulos = MODULOS_SISTEMA;
  readonly acoes = ACOES_SISTEMA;

  // ─────────────────────────────────────────────────────────────────────────────

  ngOnInit(): void {
    this.inicializarFormulario();
    this.verificarPermissaoECarregar();
  }

  /** Helper de permissão — pronto para uso com Route Guard. */
  can(modulo: SystemModule, acao: PermissionAction): boolean {
    const perfil = this.auth.usuarioAtual?.perfil ?? 'USER';
    return this.permissionService.can(perfil, modulo, acao);
  }

  /** Busca a partir da página 0 sempre que o formulário de filtros é submetido. */
  async buscar(): Promise<void> {
    this.buscando = true;
    try {
      await this.carregarLogs(0);
    } finally {
      this.buscando = false;
    }
  }

  /** Limpa os filtros e recarrega. */
  async limparFiltros(): Promise<void> {
    this.form.reset({ usuario: '', modulo: '', acao: '', dataInicio: '', dataFim: '' });
    await this.buscar();
  }

  /** Navega para uma página específica sem redefinir os filtros. */
  irParaPagina(pagina: number): void {
    if (pagina < 0 || pagina >= this.totalPaginas || pagina === this.paginaAtual) return;
    this.buscando = true;
    this.carregarLogs(pagina).finally(() => (this.buscando = false));
  }

  get paginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, i) => i);
  }

  // ── Privados ─────────────────────────────────────────────────────────────────

  private inicializarFormulario(): void {
    this.form = this.fb.group({
      usuario: [''],
      modulo: [''],
      acao: [''],
      dataInicio: [''],
      dataFim: [''],
    });
  }

  private async verificarPermissaoECarregar(): Promise<void> {
    if (!this.can('AUDITORIA', 'VISUALIZAR')) {
      this.semPermissao = true;
      return;
    }
    this.carregando = true;
    try {
      await this.carregarLogs(0);
    } finally {
      this.carregando = false;
    }
  }

  private async carregarLogs(pagina: number): Promise<void> {
    const filtros = this.obterFiltrosAtivos();
    let params = new HttpParams()
      .set('page', pagina)
      .set('size', this.tamanhoPagina);

    if (filtros.usuario) params = params.set('usuario', filtros.usuario);
    if (filtros.modulo)  params = params.set('modulo', filtros.modulo);
    if (filtros.acao)    params = params.set('acao', filtros.acao);
    if (filtros.dataInicio) params = params.set('dataInicio', filtros.dataInicio);
    if (filtros.dataFim)    params = params.set('dataFim', filtros.dataFim);

    try {
      const resposta = await firstValueFrom(
        this.http.get<ApiResponse<PageResponse<AuditoriaLog>>>('/api/auditoria', { params })
      );
      const dados = resposta?.dados;
      this.logs           = dados?.content ?? [];
      this.paginaAtual    = dados?.page?.number ?? 0;
      this.totalPaginas   = dados?.page?.totalPages ?? 0;
      this.totalElementos = dados?.page?.totalElements ?? 0;
    } catch (erro) {
      console.error('[AuditoriaComponent] Erro ao buscar logs:', erro);
      this.logs = [];
    }
  }

  private obterFiltrosAtivos(): AuditoriaFiltros {
    const v = this.form.value;
    return {
      usuario:    v.usuario?.trim()    || undefined,
      modulo:     v.modulo             || undefined,
      acao:       v.acao               || undefined,
      dataInicio: v.dataInicio         || undefined,
      dataFim:    v.dataFim            || undefined,
    };
  }

  // ── Helpers de template ──────────────────────────────────────────────────────

  get nenhumResultado(): boolean {
    return !this.carregando && !this.buscando && this.logs.length === 0;
  }

  get mostrarTabela(): boolean {
    return !this.carregando && !this.buscando && this.logs.length > 0;
  }

  trackById(_: number, item: AuditoriaLog): number {
    return item.id;
  }

  labelAcao(acao: string): string {
    return ACOES_SISTEMA.find((a) => a.valor === acao)?.label ?? acao;
  }

  labelModulo(modulo: string): string {
    return MODULOS_SISTEMA.find((m) => m.valor === modulo)?.label ?? modulo;
  }
}
