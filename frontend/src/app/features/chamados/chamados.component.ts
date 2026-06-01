import { DatePipe } from '@angular/common';
import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Subject, debounceTime, distinctUntilChanged, takeUntil } from 'rxjs';

import { EmptyStateComponent } from '../../core/components/empty-state.component';
import { PaginationComponent } from '../../core/components/pagination.component';
import {
  Chamado,
  ChamadoStats,
  PRIORIDADE_COLOR,
  PRIORIDADE_LABEL,
  PrioridadeChamado,
  STATUS_CHAMADO_COLOR,
  STATUS_CHAMADO_LABEL,
  StatusChamado
} from '../../core/models/chamado';
import { PageResponse } from '../../core/models/page-response';
import { AuthService } from '../../core/services/auth.service';
import { ChamadoService } from '../../core/services/chamado.service';
import { NotificationService } from '../../core/services/notification.service';
import { PermissionService } from '../../core/services/permission.service';

@Component({
  selector: 'app-chamados',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule, PaginationComponent, EmptyStateComponent],
  templateUrl: './chamados.component.html',
  styleUrl: './chamados.component.css'
})
export class ChamadosComponent implements OnInit, OnDestroy {
  private readonly chamadoService = inject(ChamadoService);
  private readonly authService = inject(AuthService);
  private readonly notificationService = inject(NotificationService);
  private readonly permissionService = inject(PermissionService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly destroy$ = new Subject<void>();

  chamados: Chamado[] = [];
  carregando = true;
  salvando = false;

  paginaAtual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  readonly tamanhoPagina = 10;

  readonly statusOptions: StatusChamado[] = ['ABERTO', 'EM_ATENDIMENTO', 'RESOLVIDO', 'FECHADO'];
  readonly prioridadeOptions: PrioridadeChamado[] = ['BAIXA', 'MEDIA', 'ALTA', 'URGENTE'];
  filtroStatus: StatusChamado | '' = '';
  termoBusca = '';

  stats: ChamadoStats = { total: 0, abertos: 0, emAtendimento: 0, resolvidos: 0, fechados: 0 };

  // Slide-over
  chamadoSelecionado: Chamado | null = null;
  slideOverAberto = false;

  private readonly buscaSubject = new Subject<string>();

  get podeCriar(): boolean {
    const perfil = this.authService.usuarioAtual?.perfil ?? 'USER';
    return this.permissionService.can(perfil, 'CHAMADOS', 'CRIAR');
  }

  get podeEditar(): boolean {
    const perfil = this.authService.usuarioAtual?.perfil ?? 'USER';
    return this.permissionService.can(perfil, 'CHAMADOS', 'EDITAR');
  }

  readonly form = this.formBuilder.nonNullable.group({
    titulo: ['', [Validators.required, Validators.minLength(3)]],
    descricao: ['', [Validators.required, Validators.minLength(10)]],
    prioridade: ['MEDIA' as PrioridadeChamado]
  });

  ngOnInit(): void {
    this.carregar();
    this.carregarStats();

    this.buscaSubject.pipe(
      debounceTime(400),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(termo => {
      this.termoBusca = termo;
      this.carregar(0);
    });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  onBuscaInput(event: Event): void {
    const valor = (event.target as HTMLInputElement).value;
    this.buscaSubject.next(valor);
  }

  filtrarPorStatus(status: StatusChamado | ''): void {
    this.filtroStatus = status;
    this.carregar(0);
  }

  salvar(): void {
    if (this.form.invalid || this.salvando) {
      this.notificationService.warning('Preencha todos os campos corretamente');
      return;
    }

    const usuario = this.authService.usuarioAtual;
    if (!usuario?.id) {
      this.notificationService.error('Sessão inválida. Faça login novamente.');
      return;
    }

    this.salvando = true;
    const valor = this.form.getRawValue();
    this.chamadoService.criar({
      titulo: valor.titulo,
      descricao: valor.descricao,
      usuarioId: usuario.id,
      prioridade: valor.prioridade as PrioridadeChamado
    }).subscribe({
      next: () => {
        this.form.reset({ titulo: '', descricao: '', prioridade: 'MEDIA' });
        this.salvando = false;
        this.notificationService.success('Chamado criado com sucesso!', '✓ Sucesso');
        this.carregar(0);
        this.carregarStats();
      },
      error: () => {
        this.salvando = false;
      }
    });
  }

  carregar(pagina = 0): void {
    this.carregando = true;
    const status = this.filtroStatus || null;
    const busca = this.termoBusca || null;
    this.chamadoService.listar(pagina, this.tamanhoPagina, status, busca).subscribe({
      next: (page: PageResponse<Chamado>) => {
        this.chamados = page.content;
        this.paginaAtual = page.page.number;
        this.totalPaginas = page.page.totalPages;
        this.totalElementos = page.page.totalElements;
        this.carregando = false;
      },
      error: () => {
        this.carregando = false;
      }
    });
  }

  carregarStats(): void {
    this.chamadoService.estatisticas().subscribe({
      next: (stats) => this.stats = stats,
      error: () => {}
    });
  }

  abrirDetalhe(chamado: Chamado): void {
    this.chamadoSelecionado = chamado;
    this.slideOverAberto = true;
  }

  fecharDetalhe(): void {
    this.slideOverAberto = false;
    setTimeout(() => this.chamadoSelecionado = null, 300);
  }

  campoInvalido(nomeCampo: string): boolean {
    const campo = this.form.get(nomeCampo);
    return !!(campo && campo.invalid && (campo.dirty || campo.touched));
  }

  statusLabel(status: StatusChamado): string {
    return STATUS_CHAMADO_LABEL[status] ?? status;
  }

  statusColor(status: StatusChamado): string {
    return STATUS_CHAMADO_COLOR[status] ?? '#64748b';
  }

  prioridadeLabel(p: PrioridadeChamado): string {
    return PRIORIDADE_LABEL[p] ?? p;
  }

  prioridadeColor(p: PrioridadeChamado): string {
    return PRIORIDADE_COLOR[p] ?? '#64748b';
  }

  tempoRelativo(dataIso: string): string {
    const diff = Date.now() - new Date(dataIso).getTime();
    const min = Math.floor(diff / 60000);
    if (min < 1) return 'agora';
    if (min < 60) return `há ${min} min`;
    const horas = Math.floor(min / 60);
    if (horas < 24) return `há ${horas}h`;
    const dias = Math.floor(horas / 24);
    return `há ${dias}d`;
  }

  atualizarStatus(chamado: Chamado, novoStatus: StatusChamado): void {
    this.chamadoService.atualizar(chamado.id, { status: novoStatus }).subscribe({
      next: (atualizado) => {
        chamado.status = atualizado.status;
        if (this.chamadoSelecionado?.id === chamado.id) {
          this.chamadoSelecionado = { ...atualizado };
        }
        this.notificationService.success(`Chamado #${chamado.id} → "${this.statusLabel(novoStatus)}"`);
        this.carregarStats();
      }
    });
  }

  deletar(chamado: Chamado): void {
    if (!confirm(`Deletar chamado #${chamado.id} - "${chamado.titulo}"?`)) return;
    this.chamadoService.deletar(chamado.id).subscribe({
      next: () => {
        this.notificationService.success(`Chamado #${chamado.id} deletado`);
        if (this.slideOverAberto) this.fecharDetalhe();
        this.carregar(this.paginaAtual);
        this.carregarStats();
      }
    });
  }
}
