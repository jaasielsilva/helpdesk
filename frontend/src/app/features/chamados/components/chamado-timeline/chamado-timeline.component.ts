import { DatePipe, SlicePipe, UpperCasePipe } from '@angular/common';
import { Component, Input, OnChanges, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { Comentario } from '../../../../core/models/comentario';
import { AuthService } from '../../../../core/services/auth.service';
import { ComentarioService } from '../../../../core/services/comentario.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { PermissionService } from '../../../../core/services/permission.service';

@Component({
  selector: 'app-chamado-timeline',
  standalone: true,
  imports: [DatePipe, SlicePipe, UpperCasePipe, ReactiveFormsModule],
  templateUrl: './chamado-timeline.component.html',
  styleUrl: './chamado-timeline.component.css'
})
export class ChamadoTimelineComponent implements OnChanges {
  @Input({ required: true }) chamadoId!: number;
  @Input() fechado = false;

  private readonly service = inject(ComentarioService);
  protected readonly auth = inject(AuthService);
  private readonly notify = inject(NotificationService);
  private readonly perm = inject(PermissionService);
  private readonly fb = inject(FormBuilder);

  comentarios: Comentario[] = [];
  carregando = true;
  enviando = false;
  modoInterno = false;

  readonly form = this.fb.nonNullable.group({
    conteudo: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(2000)]]
  });

  get isTenantStaff(): boolean {
    const p = this.auth.usuarioAtual?.perfil ?? 'USER';
    return p === 'SUPORTE' || p === 'ADMIN' || p === 'SUPER_ADMIN';
  }

  get podeComentarPublico(): boolean {
    return this.perm.can(this.auth.usuarioAtual?.perfil ?? 'USER', 'CHAMADOS', 'VISUALIZAR');
  }

  ngOnChanges(): void {
    if (this.chamadoId) this.carregar();
  }

  enviar(): void {
    if (this.form.invalid || this.enviando) return;
    this.enviando = true;
    this.service.adicionar(this.chamadoId, {
      conteudo: this.form.getRawValue().conteudo.trim(),
      interno: this.modoInterno
    }).subscribe({
      next: (c) => {
        this.comentarios.push(c);
        this.form.reset();
        this.modoInterno = false;
        this.enviando = false;
      },
      error: () => { this.enviando = false; }
    });
  }

  isEvento(c: Comentario): boolean {
    return c.tipo === 'EVENTO_SISTEMA';
  }

  isProprioUsuario(c: Comentario): boolean {
    return c.autorId === this.auth.usuarioAtual?.id;
  }

  avatarLabel(nome: string): string {
    return nome?.charAt(0)?.toUpperCase() ?? '?';
  }

  private carregar(): void {
    this.carregando = true;
    this.service.listar(this.chamadoId).subscribe({
      next: (lista) => { this.comentarios = lista; this.carregando = false; },
      error: () => { this.carregando = false; }
    });
  }
}
