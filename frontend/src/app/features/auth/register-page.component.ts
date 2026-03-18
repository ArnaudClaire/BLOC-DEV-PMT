import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { TimeoutError, timeout } from 'rxjs';

import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register-page',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register-page.component.html',
})
export class RegisterPageComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  readonly errorMessage = signal('');
  readonly successMessage = signal('');
  readonly isSubmitting = signal(false);
  readonly backendStatus = signal('');
  readonly form = this.fb.nonNullable.group({
    username: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.errorMessage.set('');
    this.successMessage.set('');
    this.backendStatus.set("Envoi de la requete d'inscription au backend...");
    this.isSubmitting.set(true);

    this.auth.register(this.form.getRawValue())
      .pipe(timeout(8000))
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: () => {
          this.successMessage.set('Compte cree, redirection vers le tableau de bord...');
          this.backendStatus.set('Le backend a repondu correctement.');
          this.isSubmitting.set(false);
          void this.router.navigate(['/dashboard']);
        },
        error: (error: unknown) => {
          this.errorMessage.set(this.describeError(error));
          this.isSubmitting.set(false);
        },
      });
  }

  hasFieldError(fieldName: 'username' | 'email' | 'password'): boolean {
    const control = this.form.controls[fieldName];
    return control.invalid && control.touched;
  }

  private describeError(error: unknown): string {
    if (error instanceof TimeoutError) {
      this.backendStatus.set("Aucune reponse du backend apres 8 secondes.");
      return "Le backend ne repond pas. Verifie que l'API tourne bien sur http://localhost:8081 et que POST /users termine correctement.";
    }

    if (error instanceof HttpErrorResponse) {
      this.backendStatus.set(`Erreur HTTP ${error.status || 0} recue depuis le backend.`);

      if (typeof error.error === 'string' && error.error.trim()) {
        return `Inscription impossible: ${error.error}`;
      }

      if (error.error && typeof error.error.message === 'string') {
        return `Inscription impossible: ${error.error.message}`;
      }

      return `Inscription impossible. Le backend a repondu avec le statut ${error.status || 0}.`;
    }

    this.backendStatus.set("Le front a recu une erreur non HTTP pendant l'inscription.");
    return "Inscription impossible. Verifie la reponse du backend et la console navigateur.";
  }
}
