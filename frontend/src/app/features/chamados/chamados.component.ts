import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

import { Chamado, ChamadoFiltros, PageResponse, STATUS_CHAMADO_LABELS, StatusChamado } from '../../core/models/chamado';
import { AuthService } from '../../core/services/auth.service';
import { ChamadoService } from '../../core/services/chamado.service';
import { NotificationService } from '../../core/services/notification.service';

@Component({
  selector: 'app-chamados',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './chamados.component.html',
  styleUrl: './chamados.component.css'
})
export class ChamadosComponent implements OnInit {
  private readonly chamadoService = inject(ChamadoService);
  private readonly authService = inject(AuthService);
  private readonly notificationService = inject(NotificationService);
  private readonly formBuilder = inject(FormBuilder);

  chamados: Chamado[] = [];
  carregando = true;
  salvando = false;

  paginaAtual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  readonly tamanhoPagina = 10;

  filtroStatus: StatusChamado | '' = '';
  filtroBusca = '';
  readonly statusLabels = STATUS_CHAMADO_LABELS;
  readonly statusOpcoes: Array<{ valor: StatusChamado | ''; label: string }> = [
    { valor: '', label: 'Todos' },
    { valor: 'ABERTO', label: 'Aberto' },
    { valor: 'EM_ATENDIMENTO', label: 'Em Atendimento' },
    { valor: 'RESOLVIDO', label: 'Resolvido' },
    { valor: 'FECHADO', label: 'Fechado' },
  ];

  readonly form = this.formBuilder.nonNullable.group({
    titulo: ['', [Validators.required, Validators.minLength(3)]],
    descricao: ['', [Validators.required, Validators.minLength(10)]]
  });

  readonly buscaControl = this.formBuilder.nonNullable.control('');

  ngOnInit(): void {
    this.carregar();
    this.buscaControl.valueChanges.pipe(
      debounceTime(400),
      distinctUntilChanged()
    ).subscribe(valor => {
      this.filtroBusca = valor;
      this.carregar(0);
    });
  }

  filtrarPorStatus(status: StatusChamado | ''): void {
    this.filtroStatus = status;
    this.carregar(0);
  }

  limparFiltros(): void {
    this.filtroStatus = '';
    this.filtroBusca = '';
    this.buscaControl.setValue('', { emitEvent: false });
    this.carregar(0);
  }

  get temFiltroAtivo(): boolean {
    return !!this.filtroStatus || !!this.filtroBusca;
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
    this.chamadoService.criar({ ...this.form.getRawValue(), usuarioId: usuario.id }).subscribe({
      next: () => {
        this.form.reset();
        this.salvando = false;
        this.notificationService.success('Chamado criado com sucesso!', '✓ Sucesso');
        this.carregar(0);
      },
      error: () => {
        this.salvando = false;
      }
    });
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
    const filtros: ChamadoFiltros = {
      status: this.filtroStatus,
      busca: this.filtroBusca
    };
    this.chamadoService.listar(pagina, this.tamanhoPagina, filtros).subscribe({
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

  campoInvalido(nomeCampo: string): boolean {
    const campo = this.form.get(nomeCampo);
    return !!(campo && campo.invalid && (campo.dirty || campo.touched));
  }
}
