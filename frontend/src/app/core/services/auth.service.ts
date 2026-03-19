import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, map, tap } from 'rxjs';

import { CreateUserPayload, User } from '../models/api.models';
import { PmtApiService } from './pmt-api.service';

const SESSION_KEY = 'mpmt.current-user';

@Injectable({
  providedIn: 'root',
})
/**
 * Gère l'authentification côté frontend ainsi que la persistance de la session dans le navigateur.
 */
export class AuthService {
  private readonly api = inject(PmtApiService);
  readonly currentUser = signal<User | null>(this.readStoredUser());
  readonly isAuthenticated = computed(() => this.currentUser() !== null);

  /**
   * Crée un compte puis ouvre immédiatement une session locale avec l'utilisateur créé.
   */
  register(payload: CreateUserPayload): Observable<User> {
    return this.api.createUser(payload).pipe(
      map((response) => this.normalizeRegisteredUser(response, payload)),
      tap((user) => this.setSession(user)),
    );
  }

  /**
   * Authentifie l'utilisateur auprès du backend puis mémorise la session.
   */
  login(email: string, password: string): Observable<User> {
    return this.api.login({ email, password }).pipe(tap((user) => this.setSession(user)));
  }

  /**
   * Supprime la session locale courante.
   */
  logout(): void {
    this.currentUser.set(null);
    localStorage.removeItem(SESSION_KEY);
  }

  /**
   * Retourne l'utilisateur courant ou lève une erreur quand aucune session n'est ouverte.
   */
  requireUser(): User {
    const user = this.currentUser();

    if (!user) {
      throw new Error('Session utilisateur absente.');
    }

    return user;
  }

  /**
   * Enregistre la session en mémoire et dans le `localStorage`.
   */
  private setSession(user: User): void {
    this.currentUser.set(user);
    localStorage.setItem(SESSION_KEY, JSON.stringify(user));
  }

  /**
   * Tolère plusieurs formats de réponse backend pour l'inscription afin de garder le front robuste.
   */
  private normalizeRegisteredUser(response: unknown, payload: CreateUserPayload): User {
    if (typeof response === 'number') {
      return {
        id: response,
        username: payload.username,
        email: payload.email,
      };
    }

    if (typeof response === 'string' && !Number.isNaN(Number(response))) {
      return {
        id: Number(response),
        username: payload.username,
        email: payload.email,
      };
    }

    if (this.isUserShape(response)) {
      return {
        id: response.id,
        username: response.username ?? payload.username,
        email: response.email ?? payload.email,
      };
    }

    if (this.hasNumericId(response)) {
      return {
        id: response.id,
        username: payload.username,
        email: payload.email,
      };
    }

    throw new Error("Reponse d'inscription backend non prise en charge.");
  }

  private hasNumericId(value: unknown): value is { id: number } {
    return typeof value === 'object' && value !== null && typeof (value as { id?: unknown }).id === 'number';
  }

  private isUserShape(value: unknown): value is User {
    return typeof value === 'object'
      && value !== null
      && typeof (value as User).id === 'number'
      && typeof (value as Partial<User>).email === 'string';
  }

  /**
   * Recharge la session persistée dans le navigateur au démarrage du service.
   */
  private readStoredUser(): User | null {
    try {
      const rawValue = localStorage.getItem(SESSION_KEY);
      return rawValue ? (JSON.parse(rawValue) as User) : null;
    } catch {
      return null;
    }
  }
}
