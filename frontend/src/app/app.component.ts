import { CommonModule } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
/**
 * Composant racine de l'interface.
 * Il expose l'état de session et la navigation globale affichée dans le shell applicatif.
 */
export class AppComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly currentUser = this.auth.currentUser;
  readonly isAuthenticated = this.auth.isAuthenticated;
  readonly title = computed(() => this.isAuthenticated() ? 'Workspace PMT' : 'PMT Frontend');

  /**
   * Ferme la session locale puis renvoie l'utilisateur vers l'écran de connexion.
   */
  logout(): void {
    this.auth.logout();
    void this.router.navigate(['/login']);
  }
}
