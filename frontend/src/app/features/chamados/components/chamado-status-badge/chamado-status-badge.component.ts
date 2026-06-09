import { Component, Input } from '@angular/core';
import { StatusChamado, STATUS_CHAMADO_LABELS } from '../../../../core/models/chamado';

@Component({
  selector: 'app-chamado-status-badge',
  standalone: true,
  template: `
    <span class="badge" [class]="badgeClass">{{ label }}</span>
  `
})
export class ChamadoStatusBadgeComponent {
  @Input({ required: true }) status!: StatusChamado;

  get label(): string {
    return STATUS_CHAMADO_LABELS[this.status] ?? this.status;
  }

  get badgeClass(): string {
    const map: Record<StatusChamado, string> = {
      ABERTO: 'badge badge-blue',
      EM_ATENDIMENTO: 'badge badge-yellow',
      RESOLVIDO: 'badge badge-green',
      FECHADO: 'badge badge-gray'
    };
    return map[this.status] ?? 'badge badge-gray';
  }
}
