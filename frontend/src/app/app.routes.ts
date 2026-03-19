import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

/**
 * Déclare les routes principales du frontend PMT.
 * Les écrans métiers sont chargés paresseusement pour limiter le poids initial du bundle.
 */
export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'dashboard',
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login-page.component').then((m) => m.LoginPageComponent),
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register-page.component').then((m) => m.RegisterPageComponent),
  },
  {
    path: 'invitation/:token',
    loadComponent: () => import('./features/invitations/invitation-accept-page.component').then((m) => m.InvitationAcceptPageComponent),
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/dashboard-page.component').then((m) => m.DashboardPageComponent),
  },
  {
    path: '**',
    redirectTo: 'dashboard',
  },
];
