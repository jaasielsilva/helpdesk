import { DatePipe, SlicePipe, UpperCasePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import {
  AtualizarChamadoRequest,
  Chamado,
  STATUS_CHAMADO_LABELS,
  StatusChamado
} from '../../../../core/models/chamado';
import { AuthService } from '../../../../core/services/auth.service';
import { ChamadoService } from '../../../../core/services/chamado.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { PermissionService } from '../../../../core/services/permission.service';
import { ChamadoFormComponent } from '../../components/chamado-form/chamado-form.component';
import { ChamadoResolucaoModalComponent } from '../../components/chamado-resolucao-modal/chamado-resolucao-modal.component';
import { ChamadoStatusBadgeComponent } from '../../components/chamado-status-badge/chamado-status-badge.component';
import { ChamadoTimelineComponent } from '../../components/chamado-timeline/chamado-timeline.component';

@Component({
  selector: 'app-chamado-detalhe',
  standalone: true,
  imports: [
    DatePipe, SlicePipe, UpperCasePipe, FormsModule, RouterLink,
    ChamadoStatusBadgeComponent, ChamadoFormComponent, ChamadoResolucaoModalComponent,
    ChamadoTimelineComponent
  ],
  templateUrl: './chamado-detalhe.component.html',
  styleUrl: './chamado-detalhe.component.css'
})
export class ChamadoDetalheComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(ChamadoService);
  private readonly auth = inject(AuthService);
  private readonly notify = inject(NotificationService);
  private readonly perm = inject(PermissionService);

  chamado: Chamado | null = null;
  carregando = true;
  processando = false;
  mostrarFormEdicao = false;
  mostrarModalResolucao = false;
  confirmandoDelete = false;
  confirmandoFechar = false;
  confirmandoReabrir = false;

  readonly statusLabels = STATUS_CHAMADO_LABELS;

  // ── Permissões ────────────────────────────────────────────────
  private get perfil() { return this.auth.usuarioAtual?.perfil ?? 'USER'; }

  get podeEditar(): boolean   { return this.perm.can(this.perfil, 'CHAMADOS', 'EDITAR'); }
  get podeAtender(): boolean  { return this.perm.can(this.perfil, 'CHAMADOS', 'ATENDER'); }
  get podeAtribuir(): boolean { return this.perm.can(this.perfil, 'CHAMADOS', 'ATRIBUIR'); }
  get podeExcluir(): boolean  { return this.perm.can(this.perfil, 'CHAMADOS', 'EXCLUIR'); }

  get isProprietario(): boolean {
    return this.chamado?.usuarioId === this.auth.usuarioAtual?.id;
  }

  // ── Transições de estado possíveis ───────────────────────────

  /** SUPORTE/ADMIN pode iniciar atendimento quando está ABERTO e atribuído a ele */
  get podeIniciarAtendimento(): boolean {
    if (!this.chamado || !this.podeAtender) return false;
    const estaAtribuido = this.chamado.usuarioAtribuidoId === this.auth.usuarioAtual?.id;
    return this.chamado.status === 'ABERTO' && estaAtribuido;
  }

  /** SUPORTE/ADMIN pode resolver quando está EM_ATENDIMENTO */
  get podeResolver(): boolean {
    if (!this.chamado || !this.podeAtender) return false;
    return this.chamado.status === 'EM_ATENDIMENTO';
  }

  /** Solicitante confirma resolução (fecha) ou ADMIN fecha direto */
  get podeFechar(): boolean {
    if (!this.chamado) return false;
    const statusOk = this.chamado.status === 'RESOLVIDO';
    return statusOk && (this.isProprietario || this.podeAtender);
  }

  /** Reabrir: qualquer um com ATENDER quando está RESOLVIDO ou FECHADO */
  get podeReabrir(): boolean {
    if (!this.chamado || !this.podeAtender) return false;
    return this.chamado.status === 'RESOLVIDO' || this.chamado.status === 'FECHADO';
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!id) { this.router.navigate(['/chamados']); return; }
    this.carregar(id);
  }

  // ── Stepper helper ──────────────────────────────────────────

  isStepDone(statusAtual: StatusChamado, stepStatus: string): boolean {
    const order: StatusChamado[] = ['ABERTO', 'EM_ATENDIMENTO', 'RESOLVIDO', 'FECHADO'];
    return order.indexOf(statusAtual) > order.indexOf(stepStatus as StatusChamado);
  }

  // ── Ações de fluxo ───────────────────────────────────────────

  iniciarAtendimento(): void {
    this.transicionar('EM_ATENDIMENTO', 'Atendimento iniciado');
  }

  aoResolvido(c: Chamado): void {
    this.chamado = c;
    this.mostrarModalResolucao = false;
  }

  fecharChamado(): void {
    this.confirmandoFechar = false;
    this.transicionar('FECHADO', '✓ Chamado fechado com sucesso');
  }

  reabrir(): void {
    this.confirmandoReabrir = false;
    this.transicionar('ABERTO', 'Chamado reaberto');
  }

  // ── Outras ações ─────────────────────────────────────────────

  atribuirAMim(): void {
    if (!this.chamado) return;
    const usuario = this.auth.usuarioAtual;
    if (!usuario?.id) return;
    this.executar({ usuarioAtribuidoId: usuario.id }, 'Chamado atribuído a você');
  }

  aoSalvarEdicao(c: Chamado): void {
    this.chamado = c;
    this.mostrarFormEdicao = false;
  }

  deletar(): void {
    if (!this.chamado) return;
    this.service.deletar(this.chamado.id).subscribe({
      next: () => {
        this.notify.success('Chamado removido');
        this.router.navigate(['/chamados']);
      }
    });
  }

  // ── Helpers ──────────────────────────────────────────────────

  private transicionar(status: StatusChamado, mensagem: string): void {
    this.executar({ status }, mensagem);
  }

  private executar(payload: AtualizarChamadoRequest, mensagem: string): void {
    if (!this.chamado || this.processando) return;
    this.processando = true;
    this.service.atualizar(this.chamado.id, payload).subscribe({
      next: (c) => {
        this.chamado = c;
        this.processando = false;
        this.notify.success(mensagem);
      },
      error: () => { this.processando = false; }
    });
  }

  private carregar(id: number): void {
    this.carregando = true;
    this.service.buscarPorId(id).subscribe({
      next: (c) => { this.chamado = c; this.carregando = false; },
      error: () => {
        this.carregando = false;
        this.notify.error('Chamado não encontrado.');
        this.router.navigate(['/chamados']);
      }
    });
  }
}
