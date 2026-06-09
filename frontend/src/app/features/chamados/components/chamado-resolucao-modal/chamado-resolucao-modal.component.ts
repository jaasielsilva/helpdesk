import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { Chamado, AtualizarChamadoRequest } from '../../../../core/models/chamado';
import { ChamadoService } from '../../../../core/services/chamado.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-chamado-resolucao-modal',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './chamado-resolucao-modal.component.html'
})
export class ChamadoResolucaoModalComponent {
  @Input({ required: true }) chamado!: Chamado;
  @Output() fechar = new EventEmitter<void>();
  @Output() resolvido = new EventEmitter<Chamado>();

  private readonly service = inject(ChamadoService);
  private readonly notify = inject(NotificationService);
  private readonly fb = inject(FormBuilder);

  salvando = false;

  readonly form = this.fb.nonNullable.group({
    solucao: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(1000)]]
  });

  confirmar(): void {
    if (this.form.invalid || this.salvando) {
      this.form.markAllAsTouched();
      return;
    }
    this.salvando = true;

    // Appenda a solução na descrição original com separador visual
    const descricaoAtualizada =
      this.chamado.descricao +
      '\n\n---\n✅ Solução aplicada:\n' +
      this.form.getRawValue().solucao.trim();

    const payload: AtualizarChamadoRequest = {
      status: 'RESOLVIDO',
      descricao: descricaoAtualizada
    };

    this.service.atualizar(this.chamado.id, payload).subscribe({
      next: (c) => {
        this.salvando = false;
        this.notify.success('Chamado marcado como resolvido', '✓ Resolvido');
        this.resolvido.emit(c);
      },
      error: () => { this.salvando = false; }
    });
  }

  invalido(campo: string): boolean {
    const c = this.form.get(campo);
    return !!(c && c.invalid && (c.dirty || c.touched));
  }
}
