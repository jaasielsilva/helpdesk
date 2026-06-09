import { DatePipe, SlicePipe, UpperCasePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';

import {
  Chamado,
  ChamadoFiltros,
  PageResponse,
  STATUS_CHAMADO_LABELS,
  STATUS_LIST,
  StatusChamado
} from '../../../../core/models/chamado';
import { AuthService } from '../../../../core/services/auth.service';
import { ChamadoService } from '../../../../core/services/chamado.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { PermissionService } from '../../../../core/services/permission.service';
import { ChamadoFiltrosComponent } from '../../components/chamado-filtros/chamado-filtros.component';
import { ChamadoFormComponent } from '../../components/chamado-form/chamado-form.component';
import { ChamadoStatusBadgeComponent } from '../../components/chamado-status-badge/chamado-status-badge.component';

interface TabStatus {
  status: StatusChamado | '';
  label: string;
  count: number;
}

@Component({
  selector: 'app-chamados-lista',
  standalone: true,
  imports: [DatePipe, SlicePipe, UpperCasePipe, ChamadoStatusBadgeComponent, ChamadoFiltrosComponent, ChamadoFormComponent],
  templateUrl: './chamados-lista.component.html',
  styleUrl: './chamados-lista.component.css'
})
export class ChamadosListaComponent implements OnInit {
  private readonly service = inject(ChamadoService);
  private readonly auth = inject(AuthService);
  private readonly notify = inject(NotificationService);
  private readonly perm = inject(PermissionService);
  private readonly router = inject(Router);

  chamados: Chamado[] = [];
  carregando = true;
  mostrarForm = false;
  filtros: ChamadoFiltros = {};

  paginaAtual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  readonly tamanhoPagina = 10;

  readonly tabs: TabStatus[] = [
    { status: '', label: 'Todos', count: 0 },
    ...STATUS_LIST.map((s) => ({ status: s, label: STATUS_CHAMADO_LABELS[s], count: 0 }))
  ];
  tabAtiva: StatusChamado | '' = '';

  get podeCriar(): boolean {
    const perfil = this.auth.usuarioAtual?.perfil ?? 'USER';
    return this.perm.can(perfil, 'CHAMADOS', 'CRIAR');
  }

  get podeEditar(): boolean {
    const perfil = this.auth.usuarioAtual?.perfil ?? 'USER';
    return this.perm.can(perfil, 'CHAMADOS', 'EDITAR');
  }

  get podeAtribuir(): boolean {
    const perfil = this.auth.usuarioAtual?.perfil ?? 'USER';
    return this.perm.can(perfil, 'CHAMADOS', 'ATRIBUIR');
  }

  ngOnInit(): void {
    this.carregar();
    this.carregarContagens();
  }

  selecionarTab(status: StatusChamado | ''): void {
    this.tabAtiva = status;
    this.filtros = { ...this.filtros, status };
    this.carregar(0);
  }

  onFiltrosChange(f: ChamadoFiltros): void {
    this.filtros = { ...f, status: this.tabAtiva };
    this.carregar(0);
  }

  abrirChamado(id: number): void {
    this.router.navigate(['/chamados', id]);
  }

  aoSalvar(c: Chamado): void {
    this.mostrarForm = false;
    this.carregar(0);
    this.carregarContagens();
  }

  irParaPagina(pagina: number): void {
    if (pagina < 0 || pagina >= this.totalPaginas) return;
    this.carregar(pagina);
  }

  get paginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, i) => i);
  }

  private carregar(pagina = 0): void {
    this.carregando = true;
    this.service.listar(pagina, this.tamanhoPagina, this.filtros).subscribe({
      next: (page: PageResponse<Chamado>) => {
        this.chamados = page.content;
        this.paginaAtual = page.page.number;
        this.totalPaginas = page.page.totalPages;
        this.totalElementos = page.page.totalElements;
        this.carregando = false;
      },
      error: () => { this.carregando = false; }
    });
  }

  private carregarContagens(): void {
    // Carrega contagem total para o tab "Todos"
    this.service.listar(0, 1).subscribe({
      next: (page) => {
        const todos = this.tabs.find((t) => t.status === '');
        if (todos) todos.count = page.page.totalElements;
      }
    });
    // Carrega contagem por status
    STATUS_LIST.forEach((status) => {
      this.service.listar(0, 1, { status }).subscribe({
        next: (page) => {
          const tab = this.tabs.find((t) => t.status === status);
          if (tab) tab.count = page.page.totalElements;
        }
      });
    });
  }
}
