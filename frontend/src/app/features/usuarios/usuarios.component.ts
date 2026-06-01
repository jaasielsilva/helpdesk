import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { Empresa } from '../../core/models/empresa';
import { UserRole } from '../../core/models/user-role';
import { Usuario } from '../../core/models/usuario';
import { AuthService } from '../../core/services/auth.service';
import { EmpresaService } from '../../core/services/empresa.service';
import { NotificationService } from '../../core/services/notification.service';
import { PermissionService } from '../../core/services/permission.service';
import { UsuarioService } from '../../core/services/usuario.service';

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule],
  templateUrl: './usuarios.component.html',
  styleUrl: './usuarios.component.css'
})
export class UsuariosComponent implements OnInit {
  private readonly usuarioService = inject(UsuarioService);
  private readonly empresaService = inject(EmpresaService);
  private readonly authService = inject(AuthService);
  private readonly notificationService = inject(NotificationService);
  private readonly permissionService = inject(PermissionService);
  private readonly formBuilder = inject(FormBuilder);

  usuarios: Usuario[] = [];
  empresas: Empresa[] = [];
  carregando = true;
  salvando = false;
  mostrarForm = false;
  empresaFiltro: number | null = null;

  paginaAtual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  readonly tamanhoPagina = 10;

  readonly perfisTenant: UserRole[] = ['USER', 'SUPORTE', 'ADMIN'];

  readonly form = this.formBuilder.nonNullable.group({
    usuario: ['', [Validators.required, Validators.minLength(3)]],
    senha: ['', [Validators.required, Validators.minLength(6)]],
    nome: [''],
    perfil: ['USER' as UserRole, Validators.required],
    empresaId: ['' as string | number | null]
  });

  get isSuperAdmin(): boolean {
    return this.authService.usuarioAtual?.perfil === 'SUPER_ADMIN';
  }

  get podeCriar(): boolean {
    const perfil = this.authService.usuarioAtual?.perfil ?? 'USER';
    return this.permissionService.can(perfil, 'USUARIOS', 'CRIAR');
  }

  ngOnInit(): void {
    if (this.isSuperAdmin) {
      this.empresaService.listar(0, 100).subscribe({
        next: (page) => {
          this.empresas = page.content;
        }
      });
    }
    this.carregar();
  }

  abrirForm(): void {
    this.form.reset({ perfil: 'USER', empresaId: this.empresaFiltro });
    this.mostrarForm = true;
  }

  fecharForm(): void {
    this.mostrarForm = false;
  }

  onFiltroEmpresaChange(event: Event): void {
    const value = (event.target as HTMLSelectElement).value;
    this.empresaFiltro = value ? Number(value) : null;
    this.carregar(0);
  }

  salvar(): void {
    if (this.form.invalid || this.salvando) {
      this.notificationService.warning('Preencha todos os campos corretamente');
      return;
    }

    const raw = this.form.getRawValue();
    const empresaId = raw.empresaId === '' || raw.empresaId == null
      ? null
      : Number(raw.empresaId);

    if (this.isSuperAdmin && empresaId == null) {
      this.notificationService.warning('Selecione a empresa do usuário');
      return;
    }

    this.salvando = true;
    this.usuarioService.criar({
      usuario: raw.usuario.trim(),
      senha: raw.senha.trim(),
      nome: raw.nome?.trim() || undefined,
      perfil: raw.perfil,
      empresaId: this.isSuperAdmin ? empresaId : undefined
    }).subscribe({
      next: () => {
        this.salvando = false;
        this.mostrarForm = false;
        this.notificationService.success('Usuário criado com sucesso');
        this.carregar(this.paginaAtual);
      },
      error: () => {
        this.salvando = false;
      }
    });
  }

  desativar(usuario: Usuario): void {
    if (!confirm(`Desativar usuário "${usuario.usuario}"?`)) return;
    this.usuarioService.desativar(usuario.id).subscribe({
      next: () => {
        this.notificationService.success('Usuário desativado');
        this.carregar(this.paginaAtual);
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

  perfilLabel(perfil: string): string {
    const map: Record<string, string> = {
      ADMIN: 'Administrador',
      SUPORTE: 'Suporte',
      USER: 'Usuário'
    };
    return map[perfil] ?? perfil;
  }

  private carregar(pagina = 0): void {
    this.carregando = true;
    this.usuarioService.listar(pagina, this.tamanhoPagina, this.empresaFiltro ?? undefined).subscribe({
      next: (page) => {
        this.usuarios = page.content;
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
}
