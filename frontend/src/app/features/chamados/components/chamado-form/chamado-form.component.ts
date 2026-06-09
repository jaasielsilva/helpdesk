import { Component, EventEmitter, Input, OnChanges, Output, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Chamado, CriarChamadoRequest, AtualizarChamadoRequest, STATUS_CHAMADO_LABELS, STATUS_LIST } from '../../../../core/models/chamado';
import { AuthService } from '../../../../core/services/auth.service';
import { ChamadoService } from '../../../../core/services/chamado.service';
import { NotificationService } from '../../../../core/services/notification.service';

@Component({
  selector: 'app-chamado-form',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './chamado-form.component.html'
})
export class ChamadoFormComponent implements OnChanges {
  @Input() chamado: Chamado | null = null;
  @Output() fechar = new EventEmitter<void>();
  @Output() salvo = new EventEmitter<Chamado>();

  private readonly fb = inject(FormBuilder);
  private readonly service = inject(ChamadoService);
  private readonly auth = inject(AuthService);
  private readonly notify = inject(NotificationService);

  salvando = false;
  readonly statusList = STATUS_LIST;
  readonly statusLabels = STATUS_CHAMADO_LABELS;

  readonly form = this.fb.nonNullable.group({
    titulo: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(100)]],
    descricao: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(1000)]]
  });

  get modoEdicao(): boolean {
    return !!this.chamado;
  }

  ngOnChanges(): void {
    if (this.chamado) {
      this.form.patchValue({
        titulo: this.chamado.titulo,
        descricao: this.chamado.descricao
      });
    } else {
      this.form.reset();
    }
  }

  salvar(): void {
    if (this.form.invalid || this.salvando) {
      this.form.markAllAsTouched();
      return;
    }

    this.salvando = true;
    const raw = this.form.getRawValue();

    if (this.chamado) {
      const payload: AtualizarChamadoRequest = { titulo: raw.titulo, descricao: raw.descricao };
      this.service.atualizar(this.chamado.id, payload).subscribe({
        next: (c) => { this.salvando = false; this.salvo.emit(c); this.notify.success('Chamado atualizado'); },
        error: () => { this.salvando = false; }
      });
    } else {
      const usuario = this.auth.usuarioAtual;
      if (!usuario?.id) { this.notify.error('Sessão inválida.'); this.salvando = false; return; }
      const payload: CriarChamadoRequest = { titulo: raw.titulo, descricao: raw.descricao, usuarioId: usuario.id };
      this.service.criar(payload).subscribe({
        next: (c) => { this.salvando = false; this.salvo.emit(c); this.notify.success('Chamado criado com sucesso!', '✓ Sucesso'); },
        error: () => { this.salvando = false; }
      });
    }
  }

  invalido(campo: string): boolean {
    const c = this.form.get(campo);
    return !!(c && c.invalid && (c.dirty || c.touched));
  }
}
