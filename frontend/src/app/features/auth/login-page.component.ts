import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TimeoutError, timeout } from 'rxjs';

import { AuthService } from '../../core/services/auth.service';
import { PmtApiService } from '../../core/services/pmt-api.service';

@Component({
  selector: 'app-login-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login-page.component.html',
})
export class LoginPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly api = inject(PmtApiService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  readonly errorMessage = signal('');
  readonly backendStatus = signal('');
  readonly invitationToken = signal<string | null>(this.route.snapshot.queryParamMap.get('invitation'));
  readonly invitationProjectName = signal('');
  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  constructor() {
    this.prefillInvitationContext();
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { email, password } = this.form.getRawValue();
    this.errorMessage.set('');
    this.backendStatus.set("Verification de l'utilisateur sur le backend...");

    this.auth.login(email, password)
      .pipe(timeout(8000))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.backendStatus.set('Le backend a repondu correctement.');
          void this.navigateAfterLogin();
        },
        error: (error: unknown) => this.errorMessage.set(this.describeError(error)),
      });
  }

  hasFieldError(fieldName: 'email' | 'password'): boolean {
    const control = this.form.controls[fieldName];
    return control.invalid && control.touched;
  }

  invitationQueryParams(): { invitation: string } | null {
    const invitationToken = this.invitationToken();
    return invitationToken ? { invitation: invitationToken } : null;
  }

  private navigateAfterLogin(): Promise<boolean> {
    const invitationToken = this.invitationToken();
    return invitationToken
      ? this.router.navigate(['/invitation', invitationToken])
      : this.router.navigate(['/dashboard']);
  }

  private prefillInvitationContext(): void {
    const invitationToken = this.invitationToken();
    if (!invitationToken) {
      return;
    }

    this.api.getProjectInvitationByToken(invitationToken)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (invitation) => {
          this.invitationProjectName.set(invitation.projectName);
          this.form.controls.email.setValue(invitation.email);
        },
        error: () => {
          this.backendStatus.set("Invitation introuvable ou impossible a charger.");
        },
      });
  }

  private describeError(error: unknown): string {
    if (error instanceof TimeoutError) {
      this.backendStatus.set('Aucune reponse du backend apres 8 secondes.');
      return "Connexion impossible. Le backend ne repond pas sur http://localhost:8081.";
    }

    if (error instanceof HttpErrorResponse) {
      this.backendStatus.set(`Erreur HTTP ${error.status || 0} recue depuis le backend.`);

      if (error.status === 401 || error.status === 403 || error.status === 404) {
        return 'Identifiant ou mot de passe incorrect.';
      }

      if (typeof error.error === 'string' && error.error.trim()) {
        return `Connexion impossible: ${error.error}`;
      }

      if (error.error && typeof error.error.message === 'string') {
        return `Connexion impossible: ${error.error.message}`;
      }

      return `Connexion impossible. Le backend a repondu avec le statut ${error.status || 0}.`;
    }

    if (error instanceof Error && /mot de passe|identifiant|email/i.test(error.message)) {
      this.backendStatus.set('Le backend a refuse les identifiants de connexion.');
      return 'Identifiant ou mot de passe incorrect.';
    }

    this.backendStatus.set('Le front a recu une erreur non HTTP pendant la connexion.');
    return 'Connexion impossible. Verifie les informations saisies et la disponibilite du backend.';
  }
}
