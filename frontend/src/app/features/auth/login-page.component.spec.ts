/**
 * Couvre les scénarios principaux de l'écran de connexion.
 */
import { HttpErrorResponse } from '@angular/common/http';
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { TimeoutError, of, throwError } from 'rxjs';

import { AuthService } from '../../core/services/auth.service';
import { PmtApiService } from '../../core/services/pmt-api.service';
import { LoginPageComponent } from './login-page.component';

describe('LoginPageComponent', () => {
  let fixture: ComponentFixture<LoginPageComponent>;
  let component: LoginPageComponent;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let apiSpy: jasmine.SpyObj<PmtApiService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', ['login'], {
      currentUser: signal(null),
      isAuthenticated: signal(false),
    } as never);
    apiSpy = jasmine.createSpyObj<PmtApiService>('PmtApiService', ['getProjectInvitationByToken']);
    apiSpy.getProjectInvitationByToken.and.returnValue(of({
      id: 1,
      email: 'alice@example.com',
      role: 'MEMBER',
      status: 'PENDING',
      projectId: 1,
      projectName: 'PMT Launch',
      expiresAt: '2026-03-25T09:00:00.000Z',
    }));

    await TestBed.configureTestingModule({
      imports: [LoginPageComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceSpy },
        { provide: PmtApiService, useValue: apiSpy },
      ],
    }).compileComponents();

    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    spyOn(routerSpy, 'navigate').and.returnValue(Promise.resolve(true));

    fixture = TestBed.createComponent(LoginPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should mark fields as touched when form is invalid', () => {
    component.submit();

    expect(component.form.controls.email.touched).toBeTrue();
    expect(component.form.controls.password.touched).toBeTrue();
    expect(authServiceSpy.login).not.toHaveBeenCalled();
  });

  it('should navigate to dashboard on successful login', () => {
    authServiceSpy.login.and.returnValue(of({
      id: 1,
      username: 'alice',
      email: 'alice@example.com',
    }));
    component.form.setValue({
      email: 'alice@example.com',
      password: 'secret123',
    });

    component.submit();

    expect(authServiceSpy.login).toHaveBeenCalledWith('alice@example.com', 'secret123');
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/dashboard']);
  });

  it('should show invalid credentials message when backend rejects login', () => {
    authServiceSpy.login.and.returnValue(throwError(() => new HttpErrorResponse({
      status: 401,
      error: { message: 'Email ou mot de passe invalide.' },
    })));
    component.form.setValue({
      email: 'alice@example.com',
      password: 'badpass',
    });

    component.submit();

    expect(component.errorMessage()).toBe('Identifiant ou mot de passe incorrect.');
  });

  it('should expose a timeout message when the backend does not reply', () => {
    authServiceSpy.login.and.returnValue(throwError(() => new TimeoutError()));
    component.form.setValue({
      email: 'alice@example.com',
      password: 'secret123',
    });

    component.submit();

    expect(component.backendStatus()).toContain('8 secondes');
    expect(component.errorMessage()).toContain('backend ne repond pas');
  });

  it('should display backend string errors for unexpected HTTP statuses', () => {
    authServiceSpy.login.and.returnValue(throwError(() => new HttpErrorResponse({
      status: 500,
      error: 'database offline',
    })));
    component.form.setValue({
      email: 'alice@example.com',
      password: 'secret123',
    });

    component.submit();

    expect(component.errorMessage()).toBe('Connexion impossible: database offline');
    expect(component.backendStatus()).toContain('Erreur HTTP 500');
  });

  it('should map credential-like runtime errors to the invalid credentials message', () => {
    authServiceSpy.login.and.returnValue(throwError(() => new Error('identifiant inconnu')));
    component.form.setValue({
      email: 'alice@example.com',
      password: 'secret123',
    });

    component.submit();

    expect(component.errorMessage()).toBe('Identifiant ou mot de passe incorrect.');
    expect(component.backendStatus()).toContain('identifiants');
  });
});
