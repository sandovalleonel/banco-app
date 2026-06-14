import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'clientes',
    loadComponent: () => import('./features/clientes/clientes.component').then(m => m.ClientesComponent)
  },
  {
    path: 'cuentas',
    loadComponent: () => import('./features/cuentas/cuentas.component').then(m => m.CuentasComponent)
  },
  {
    path: 'movimientos',
    loadComponent: () => import('./features/movimientos/movimientos.component').then(m => m.MovimientosComponent)
  },
  {
    path: 'reportes',
    loadComponent: () => import('./features/reportes/reportes.component').then(m => m.ReportesComponent)
  },
  {
    path: '',
    redirectTo: 'clientes',
    pathMatch: 'full'
  },
  {
    path: '**',
    redirectTo: 'clientes'
  }
];
