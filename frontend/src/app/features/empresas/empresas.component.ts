import { DatePipe } from '@angular/common';
import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { EmptyStateComponent } from '../../core/components/empty-state.component';
import { PaginationComponent } from '../../core/components/pagination.component';
import { Empresa } from '../../core/models/empresa';
import { EmpresaService } from '../../core/services/empresa.service';
import { NotificationService } from '../../core/services/notification.service';
import { PermissionService } from '../../core/services/permission.service';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-empresas',
  standalone: true,
  imports: [DatePipe, ReactiveFormsModule, PaginationComponent, EmptyStateComponent],
  templateUrl: './empresas.component.html',
  styleUrl: './empresas.component.css'
})
export class EmpresasComponent implements OnInit {
  private readonly empresaService = inject(EmpresaService);
  private readonly notificationService = inject(NotificationService);
  private readonly permissionService = inject(PermissionService);
  private readonly authService = inject(AuthService);
  private readonly formBuilder = inject(FormBuilder);

  empresas: Empresa[] = [];
  carregando = true;
  salvando = false;
  mostrarWizard = false;

  paginaAtual = 0;
  totalPaginas = 0;
  totalElementos = 0;
  readonly tamanhoPagina = 10;

  readonly form = this.formBuilder.nonNullable.group({
    slug: ['', [Validators.required, Validators.pattern(/^[a-z0-9]+(?:-[a-z0-9]+)*$/)]],
    nome: ['', [Validators.required, Validators.maxLength(150)]],
    adminUsuario: ['', [Validators.required, Validators.minLength(3)]],
    adminSenha: ['', [Validators.required, Validators.minLength(6)]],
    adminNome: ['']
  });

  get podeCriar(): boolean {
    const perfil = this.authService.usuarioAtual?.perfil ?? 'USER';
    return this.permissionService.can(perfil, 'EMPRESAS', 'CRIAR');
  }

  ngOnInit(): void {
    this.carregar();
  }

  abrirWizard(): void {
    this.form.reset();
    this.mostrarWizard = true;
  }

  fecharWizard(): void {
    this.mostrarWizard = false;
  }

  salvar(): void {
    if (this.form.invalid || this.salvando) {
      this.notificationService.warning('Preencha todos os campos corretamente');
      return;
    }

    this.salvando = true;
    const raw = this.form.getRawValue();
    this.empresaService.criar({
      slug: raw.slug.trim().toLowerCase(),
      nome: raw.nome.trim(),
      adminUsuario: raw.adminUsuario.trim(),
      adminSenha: raw.adminSenha.trim(),
      adminNome: raw.adminNome?.trim() || undefined
    }).subscribe({
      next: (res) => {
        this.salvando = false;
        this.mostrarWizard = false;
        this.notificationService.success(
          `Tenant "${res.empresa.nome}" criado. Admin: ${res.adminUsuario}`,
          'Tenant provisionado'
        );
        this.carregar(0);
      },
      error: () => {
        this.salvando = false;
      }
    });
  }

  alternarStatus(empresa: Empresa): void {
    this.empresaService.atualizarStatus(empresa.id, !empresa.ativo).subscribe({
      next: (atualizada) => {
        empresa.ativo = atualizada.ativo;
        this.notificationService.success(
          atualizada.ativo ? 'Empresa ativada' : 'Empresa desativada'
        );
      }
    });
  }

  carregar(pagina = 0): void {
    this.carregando = true;
    this.empresaService.listar(pagina, this.tamanhoPagina).subscribe({
      next: (page) => {
        this.empresas = page.content;
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
