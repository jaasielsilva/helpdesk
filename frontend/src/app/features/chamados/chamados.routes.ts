import { Routes } from '@angular/router';

export const CHAMADOS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/lista/chamados-lista.component').then((m) => m.ChamadosListaComponent)
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/detalhe/chamado-detalhe.component').then((m) => m.ChamadoDetalheComponent)
  }
];
