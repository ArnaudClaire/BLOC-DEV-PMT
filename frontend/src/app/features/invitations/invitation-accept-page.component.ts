import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs/operators';

import { AuthService } from '../../core/services/auth.service';
import { ProjectInvitationPublicDetails } from '../../core/models/api.models';
import { PmtApiService } from '../../core/services/pmt-api.service';

@Component({
  selector: 'app-invitation-accept-page',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './invitation-accept-page.component.html',
})
/**
 * Gère l'affichage et l'acceptation d'une invitation de projet résolue depuis son token.
 */
export class InvitationAcceptPageComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly api = inject(PmtApiService);
  private readonly auth = inject(AuthService);
  private readonly destroyRef = inject(DestroyRef);

  readonly token = this.route.snapshot.paramMap.get('token') ?? '';
  readonly loading = signal(true);
  readonly accepting = signal(false);
  readonly errorMessage = signal('');
  readonly invitation = signal<ProjectInvitationPublicDetails | null>(null);
  readonly currentUser = this.auth.currentUser;
  readonly isAuthenticated = this.auth.isAuthenticated;

  readonly canAcceptInvitation = computed(() => {
    const invitation = this.invitation();
    const currentUser = this.currentUser();

    return !!invitation
      && invitation.status === 'PENDING'
      && !!currentUser
      && currentUser.email.toLowerCase() === invitation.email.toLowerCase();
  });

  constructor() {
    this.loadInvitation();
  }

  /**
   * Retourne le libellé lisible du rôle reçu depuis l'API.
   */
  roleLabel(role: string | undefined): string {
    switch (role) {
      case 'ADMIN':
        return 'Administrateur';
      case 'MEMBER':
        return 'Membre';
      case 'OBSERVER':
        return 'Observateur';
      default:
        return role ?? '';
    }
  }

  /**
   * Retourne un libellé utilisateur pour l'état courant de l'invitation.
   */
  statusLabel(status: string | undefined): string {
    switch (status) {
      case 'PENDING':
        return 'Invitation en attente';
      case 'ACCEPTED':
        return 'Invitation deja acceptee';
      case 'DECLINED':
        return 'Invitation refusee';
      case 'EXPIRED':
        return 'Invitation expiree';
      case 'CANCELED':
        return 'Invitation annulee';
      default:
        return status ?? '';
    }
  }

  loginQueryParams(): { invitation: string } {
    return { invitation: this.token };
  }

  registerQueryParams(): { invitation: string } {
    return { invitation: this.token };
  }

  /**
   * Tente d'accepter l'invitation pour l'utilisateur actuellement connecté.
   */
  acceptInvitation(): void {
    const currentUser = this.currentUser();
    if (!currentUser || !this.canAcceptInvitation()) {
      return;
    }

    this.accepting.set(true);
    this.errorMessage.set('');

    this.api.acceptProjectInvitation(this.token, { userId: currentUser.id }).pipe(
      finalize(() => this.accepting.set(false)),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: () => {
        void this.router.navigate(['/dashboard']);
      },
      error: (error: unknown) => {
        this.errorMessage.set(this.describeError(error));
      },
    });
  }

  /**
   * Force une reconnexion lorsque la session ouverte ne correspond pas à l'email invité.
   */
  switchAccount(): void {
    this.auth.logout();
    void this.router.navigate(['/login'], {
      queryParams: { invitation: this.token },
    });
  }

  /**
   * Charge les métadonnées publiques de l'invitation depuis le token présent dans l'URL.
   */
  private loadInvitation(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.api.getProjectInvitationByToken(this.token).pipe(
      finalize(() => this.loading.set(false)),
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (invitation) => this.invitation.set(invitation),
      error: (error: unknown) => this.errorMessage.set(this.describeError(error)),
    });
  }

  /**
   * Normalise les erreurs réseau ou métier en texte affichable dans la page.
   */
  private describeError(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      if (typeof error.error === 'string' && error.error.trim()) {
        return error.error;
      }

      if (error.error && typeof error.error.message === 'string') {
        return error.error.message;
      }

      if (error.status === 404) {
        return "Cette invitation est introuvable.";
      }
    }

    if (error instanceof Error && error.message) {
      return error.message;
    }

    return "Impossible de charger ou d'accepter cette invitation.";
  }
}
