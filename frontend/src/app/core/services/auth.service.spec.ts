import { TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { CreateUserPayload } from '../models/api.models';
import { PmtApiService } from './pmt-api.service';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let apiSpy: jasmine.SpyObj<PmtApiService>;

  const registerPayload: CreateUserPayload = {
    username: 'alice',
    email: 'alice@example.com',
    password: 'secret123',
  };

  beforeEach(() => {
    localStorage.clear();

    apiSpy = jasmine.createSpyObj<PmtApiService>('PmtApiService', ['createUser', 'login']);

    TestBed.configureTestingModule({
      providers: [
        AuthService,
        { provide: PmtApiService, useValue: apiSpy },
      ],
    });

    service = TestBed.inject(AuthService);
  });

  it('should register from a numeric id response', (done) => {
    apiSpy.createUser.and.returnValue(of(42));

    service.register(registerPayload).subscribe((user) => {
      expect(user).toEqual({
        id: 42,
        username: 'alice',
        email: 'alice@example.com',
      });
      expect(service.currentUser()).toEqual(user);
      done();
    });
  });

  it('should register from a full user response', (done) => {
    apiSpy.createUser.and.returnValue(of({
      id: 7,
      username: 'backend-name',
      email: 'backend@example.com',
    }));

    service.register(registerPayload).subscribe((user) => {
      expect(user).toEqual({
        id: 7,
        username: 'backend-name',
        email: 'backend@example.com',
      });
      done();
    });
  });

  it('should throw when register response shape is unsupported', (done) => {
    apiSpy.createUser.and.returnValue(of({ ok: true }));

    service.register(registerPayload).subscribe({
      next: () => fail('expected an error'),
      error: (error: Error) => {
        expect(error.message).toContain('Reponse');
        done();
      },
    });
  });

  it('should log in with backend authentication', (done) => {
    apiSpy.login.and.returnValue(of(
      { id: 3, username: 'bob', email: 'bob@example.com' },
    ));

    service.login('bob@example.com', 'secret123').subscribe((user) => {
      expect(user.id).toBe(3);
      expect(service.isAuthenticated()).toBeTrue();
      done();
    });
  });

  it('should bubble up backend login errors', (done) => {
    apiSpy.login.and.returnValue(throwError(() => new Error('Email ou mot de passe invalide.')));

    service.login('missing@example.com', 'bad-password').subscribe({
      next: () => fail('expected login error'),
      error: (error: Error) => {
        expect(error.message).toContain('invalide');
        done();
      },
    });
  });
});
