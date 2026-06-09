import { Type } from '@angular/core';
import { Routes } from '@angular/router';

import { authGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { AppShellComponent } from './core/layout/app-shell.component';
import { AuditoriaComponent } from './features/auditoria/auditoria.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { EmpresasComponent } from './features/empresas/empresas.component';
import { LoginComponent } from './features/login/login.component';
import { PlaceholderComponent } from './features/placeholder/placeholder.component';
import { UsuariosComponent } from './features/usuarios/usuarios.component';

function protectedRoute(path: string, component: Type<unknown>, title: string) {
  return {
    path,
    component,
    canActivate: [roleGuard],
    data: { title }
  };
}

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  {
    path: '',
    component: AppShellComponent,
    canActivate: [authGuard],
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      protectedRoute('dashboard', DashboardComponent, 'Dashboard'),
      {
        path: 'chamados',
        canActivate: [roleGuard],
        data: { title: 'Chamados' },
        loadChildren: () =>
          import('./features/chamados/chamados.routes').then((m) => m.CHAMADOS_ROUTES)
      },
      protectedRoute('categorias', PlaceholderComponent, 'Categorias'),
      protectedRoute('sla', PlaceholderComponent, 'SLA'),
      protectedRoute('equipes', PlaceholderComponent, 'Equipes'),
      protectedRoute('usuarios', UsuariosComponent, 'Usuários'),
      protectedRoute('base-conhecimento', PlaceholderComponent, 'Base de Conhecimento'),
      protectedRoute('automacao', PlaceholderComponent, 'Automação'),
      protectedRoute('analytics', PlaceholderComponent, 'Analytics'),
      protectedRoute('relatorios', PlaceholderComponent, 'Relatórios'),
      protectedRoute('notificacoes', PlaceholderComponent, 'Notificações'),
      protectedRoute('mensagens', PlaceholderComponent, 'Mensagens'),
      protectedRoute('auditoria', AuditoriaComponent, 'Auditoria'),
      protectedRoute('integracoes', PlaceholderComponent, 'Integrações'),
      protectedRoute('empresas', EmpresasComponent, 'Empresas'),
      protectedRoute('assinatura', PlaceholderComponent, 'Assinatura')
    ]
  },
  { path: '**', redirectTo: 'dashboard' }
];
