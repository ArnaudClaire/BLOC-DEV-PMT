import { HttpErrorResponse } from '@angular/common/http';
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { AuthService } from '../../core/services/auth.service';
import { LoginPageComponent } from './login-page.component';

describe('LoginPageComponent', () => {
  let fixture: ComponentFixture<LoginPageComponent>;
  let component: LoginPageComponent;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', ['login'], {
      currentUser: signal(null),
      isAuthenticated: signal(false),
    } as never);

    await TestBed.configureTestingModule({
      imports: [LoginPageComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceSpy },
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
});
