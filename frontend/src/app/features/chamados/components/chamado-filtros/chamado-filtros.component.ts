import { Component, EventEmitter, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ChamadoFiltros, StatusChamado, STATUS_CHAMADO_LABELS, STATUS_LIST } from '../../../../core/models/chamado';

@Component({
  selector: 'app-chamado-filtros',
  standalone: true,
  imports: [FormsModule],
  template: `
    <div class="flex flex-wrap gap-3 items-center">
      <div class="relative flex-1 min-w-[200px]">
        <span class="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 text-sm">🔍</span>
        <input
          type="text"
          [(ngModel)]="busca"
          (ngModelChange)="emitir()"
          placeholder="Buscar por título..."
          class="input-field pl-9 py-2 text-sm"
        />
      </div>
      <select
        [(ngModel)]="status"
        (ngModelChange)="emitir()"
        class="input-field w-auto py-2 text-sm"
      >
        <option value="">Todos os status</option>
        @for (s of statusList; track s) {
          <option [value]="s">{{ labels[s] }}</option>
        }
      </select>
    </div>
  `
})
export class ChamadoFiltrosComponent {
  @Output() filtrosChange = new EventEmitter<ChamadoFiltros>();

  busca = '';
  status: StatusChamado | '' = '';
  readonly statusList = STATUS_LIST;
  readonly labels = STATUS_CHAMADO_LABELS;

  emitir(): void {
    this.filtrosChange.emit({ busca: this.busca, status: this.status });
  }
}
