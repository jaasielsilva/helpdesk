import { Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../core/services/auth.service';
import { NotificationService } from '../../core/services/notification.service';
import { RememberUserService } from '../../core/services/remember-user.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly notificationService = inject(NotificationService);
  private readonly rememberUserService = inject(RememberUserService);
  private readonly formBuilder = inject(FormBuilder);
  private readonly router = inject(Router);

  erro = '';
  carregando = false;
  mostrarSenha = false;
  exigirEmpresaSlug = false;

  readonly form = this.formBuilder.nonNullable.group({
    usuario: ['', Validators.required],
    senha: ['', Validators.required],
    empresaSlug: [''],
    lembrarMe: [false]
  });

  ngOnInit(): void {
    const usuarioSalvo = this.rememberUserService.getRememberedUsuario();
    if (usuarioSalvo) {
      this.form.patchValue({ usuario: usuarioSalvo, lembrarMe: true });
    }
  }

  entrar(): void {
    const usuario = this.form.controls.usuario.value.trim();
    const senha = this.form.controls.senha.value.trim();
    const empresaSlug = this.form.controls.empresaSlug.value.trim();
    const lembrarMe = this.form.controls.lembrarMe.value;

    this.form.patchValue({ usuario, senha, empresaSlug });

    if (!usuario || !senha || this.carregando) {
      this.notificationService.warning('Preencha todos os campos');
      return;
    }

    if (this.exigirEmpresaSlug && !empresaSlug) {
      this.notificationService.warning('Informe o identificador da empresa');
      return;
    }

    this.erro = '';
    this.carregando = true;

    this.authService.login(usuario, senha, empresaSlug || undefined).subscribe({
      next: () => {
        this.rememberUserService.persistPreference(lembrarMe, usuario);
        this.notificationService.success(
          'Bem-vindo! Redirecionando...',
          '🎉 Login Bem-sucedido'
        );
        this.router.navigate(['/dashboard']);
      },
      error: (err: HttpErrorResponse) => {
        this.carregando = false;
        const mensagem = (err.error as { mensagem?: string })?.mensagem ?? '';
        if (mensagem === 'REQUIRES_EMPRESA_SLUG') {
          this.exigirEmpresaSlug = true;
          this.erro = 'Seu usuário existe em mais de uma empresa. Informe o identificador (slug) da empresa.';
          return;
        }
        this.erro = 'Usuário ou senha inválidos.';
      }
    });
  }
}
