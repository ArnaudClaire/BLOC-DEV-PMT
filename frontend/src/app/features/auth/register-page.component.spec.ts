import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { of, throwError } from 'rxjs';

import { AuthService } from '../../core/services/auth.service';
import { PmtApiService } from '../../core/services/pmt-api.service';
import { RegisterPageComponent } from './register-page.component';

describe('RegisterPageComponent', () => {
  let fixture: ComponentFixture<RegisterPageComponent>;
  let component: RegisterPageComponent;
  let authServiceSpy: jasmine.SpyObj<AuthService>;
  let apiSpy: jasmine.SpyObj<PmtApiService>;
  let routerSpy: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    authServiceSpy = jasmine.createSpyObj<AuthService>('AuthService', ['register'], {
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
      imports: [RegisterPageComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authServiceSpy },
        { provide: PmtApiService, useValue: apiSpy },
      ],
    }).compileComponents();

    routerSpy = TestBed.inject(Router) as jasmine.SpyObj<Router>;
    spyOn(routerSpy, 'navigate').and.returnValue(Promise.resolve(true));

    fixture = TestBed.createComponent(RegisterPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should mark fields as touched when form is invalid', () => {
    component.submit();

    expect(component.form.controls.username.touched).toBeTrue();
    expect(authServiceSpy.register).not.toHaveBeenCalled();
  });

  it('should navigate to dashboard on successful registration', () => {
    authServiceSpy.register.and.returnValue(of({
      id: 1,
      username: 'alice',
      email: 'alice@example.com',
    }));
    component.form.setValue({
      username: 'alice',
      email: 'alice@example.com',
      password: 'secret123',
    });

    component.submit();

    expect(authServiceSpy.register).toHaveBeenCalled();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/dashboard']);
    expect(component.successMessage()).toContain('Compte cree');
  });

  it('should expose an error message on failed registration', () => {
    authServiceSpy.register.and.returnValue(throwError(() => new Error('backend error')));
    component.form.setValue({
      username: 'alice',
      email: 'alice@example.com',
      password: 'secret123',
    });

    component.submit();

    expect(component.errorMessage()).toContain('Inscription impossible');
    expect(component.isSubmitting()).toBeFalse();
  });
});
