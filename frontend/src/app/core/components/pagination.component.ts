import { Component, Input, Output, EventEmitter } from '@angular/core';

@Component({
  selector: 'app-pagination',
  standalone: true,
  template: `
    @if (totalPaginas > 1) {
      <nav class="pagination-bar mt-6 flex items-center justify-center gap-2" aria-label="Paginação">

        <!-- Botão Anterior -->
        <button
          class="pagination-btn flex items-center justify-center"
          [disabled]="paginaAtual === 0"
          (click)="irParaPagina.emit(paginaAtual - 1)"
          aria-label="Página anterior"
        >
          <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="15 18 9 12 15 6"/>
          </svg>
        </button>

        <!-- Páginas Numéricas -->
        @for (pagina of paginas; track pagina) {
          <button
            class="pagination-btn"
            [class.active]="pagina === paginaAtual"
            (click)="irParaPagina.emit(pagina)"
            [attr.aria-current]="pagina === paginaAtual ? 'page' : null"
          >
            {{ pagina + 1 }}
          </button>
        }

        <!-- Botão Próximo -->
        <button
          class="pagination-btn flex items-center justify-center"
          [disabled]="paginaAtual === totalPaginas - 1"
          (click)="irParaPagina.emit(paginaAtual + 1)"
          aria-label="Próxima página"
        >
          <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </button>
      </nav>
    }
  `,
  styles: [`
    .pagination-btn {
      width: 36px;
      height: 36px;
      border: 1px solid var(--border);
      background-color: #ffffff;
      color: var(--text);
      font-weight: 600;
      font-size: 13px;
      border-radius: 8px;
      cursor: pointer;
      transition: all 0.2s ease;
    }

    .pagination-btn:hover:not(:disabled) {
      border-color: var(--primary);
      background-color: #f5f3ff;
      color: var(--primary);
    }

    .pagination-btn.active {
      background: var(--brand-gradient);
      border-color: transparent;
      color: #ffffff;
      box-shadow: 0 4px 10px rgba(118, 75, 162, 0.15);
    }

    .pagination-btn:disabled {
      opacity: 0.45;
      cursor: not-allowed;
      background-color: #f8fafc;
    }
  `]
})
export class PaginationComponent {
  @Input({ required: true }) paginaAtual = 0;
  @Input({ required: true }) totalPaginas = 0;
  @Output() irParaPagina = new EventEmitter<number>();

  get paginas(): number[] {
    return Array.from({ length: this.totalPaginas }, (_, i) => i);
  }
}
