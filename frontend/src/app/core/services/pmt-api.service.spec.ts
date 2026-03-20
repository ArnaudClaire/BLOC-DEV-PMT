import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';

import { PmtApiService } from './pmt-api.service';

describe('PmtApiService', () => {
  let service: PmtApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        PmtApiService,
        provideHttpClient(),
        provideHttpClientTesting(),
      ],
    });

    service = TestBed.inject(PmtApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should map users returned by the auth endpoint', () => {
    let actualUser: unknown;

    service.login({ email: 'alice@example.com', password: 'secret123' }).subscribe((user) => {
      actualUser = user;
    });

    const request = httpMock.expectOne('/api/auth/login');
    expect(request.request.method).toBe('POST');
    request.flush({
      id: 7,
      username: 'alice',
      email: 'alice@example.com',
      createdAt: '2026-03-19T10:00:00.000Z',
    });

    expect(actualUser).toEqual({
      id: 7,
      username: 'alice',
      email: 'alice@example.com',
      createdAt: '2026-03-19T10:00:00.000Z',
    });
  });

  it('should map projects and tasks with nested identifiers', () => {
    let actualProjects: unknown;
    let actualTask: unknown;

    service.listProjects().subscribe((projects) => {
      actualProjects = projects;
    });
    service.getTask(42).subscribe((task) => {
      actualTask = task;
    });

    const projectRequest = httpMock.expectOne('/api/projects');
    expect(projectRequest.request.method).toBe('GET');
    projectRequest.flush([
      {
        id: 1,
        name: 'PMT Launch',
        description: 'Projet',
        startDate: '2026-03-19',
        owner: { id: 9 },
      },
    ]);

    const taskRequest = httpMock.expectOne('/api/tasks/42');
    expect(taskRequest.request.method).toBe('GET');
    taskRequest.flush({
      id: 42,
      title: 'Verifier le mapping',
      description: 'Tache de test',
      status: 'TODO',
      priority: 'HIGH',
      dueDate: '2026-03-20',
      endDate: '2026-03-21',
      project: {},
      createdBy: {},
    });

    expect(actualProjects).toEqual([
      {
        id: 1,
        name: 'PMT Launch',
        description: 'Projet',
        startDate: '2026-03-19',
        ownerId: 9,
      },
    ]);
    expect(actualTask).toEqual({
      id: 42,
      title: 'Verifier le mapping',
      description: 'Tache de test',
      status: 'TODO',
      priority: 'HIGH',
      dueDate: '2026-03-20',
      endDate: '2026-03-21',
      projectId: 0,
      createdById: 0,
      assignedToId: undefined,
    });
  });

  it('should map project members and invitations', () => {
    let actualMembers: unknown;
    let actualInvitation: unknown;
    let actualInvitationDetails: unknown;

    service.listProjectMembers().subscribe((members) => {
      actualMembers = members;
    });
    service.getProjectInvitation(3).subscribe((invitation) => {
      actualInvitation = invitation;
    });
    service.getProjectInvitationByToken('invite-token').subscribe((invitation) => {
      actualInvitationDetails = invitation;
    });

    httpMock.expectOne('/api/project-members').flush([
      {
        id: 11,
        role: 'ADMIN',
        joinedAt: '2026-03-19T09:00:00.000Z',
        project: { id: 5 },
        user: {},
      },
    ]);
    httpMock.expectOne('/api/project-invitations/3').flush({
      id: 3,
      email: 'invitee@example.com',
      token: 'invite-token',
      role: 'MEMBER',
      status: 'PENDING',
      createdAt: '2026-03-19T09:00:00.000Z',
      expiresAt: '2026-03-26T09:00:00.000Z',
      acceptedAt: undefined,
      invitedBy: { id: 1 },
      project: {},
    });
    httpMock.expectOne('/api/project-invitations/token/invite-token').flush({
      id: 3,
      email: 'invitee@example.com',
      role: 'MEMBER',
      status: 'PENDING',
      projectId: 5,
      projectName: 'PMT Launch',
      expiresAt: '2026-03-26T09:00:00.000Z',
    });

    expect(actualMembers).toEqual([
      {
        id: 11,
        role: 'ADMIN',
        joinedAt: '2026-03-19T09:00:00.000Z',
        projectId: 5,
        userId: 0,
      },
    ]);
    expect(actualInvitation).toEqual({
      id: 3,
      email: 'invitee@example.com',
      token: 'invite-token',
      role: 'MEMBER',
      status: 'PENDING',
      createdAt: '2026-03-19T09:00:00.000Z',
      expiresAt: '2026-03-26T09:00:00.000Z',
      acceptedAt: undefined,
      invitedById: 1,
      projectId: undefined,
    });
    expect(actualInvitationDetails).toEqual({
      id: 3,
      email: 'invitee@example.com',
      role: 'MEMBER',
      status: 'PENDING',
      projectId: 5,
      projectName: 'PMT Launch',
      expiresAt: '2026-03-26T09:00:00.000Z',
    });
  });

  it('should map notifications and histories while preserving fallback timestamps', () => {
    let actualNotifications: unknown;
    let actualHistories: unknown;

    service.listNotifications().subscribe((notifications) => {
      actualNotifications = notifications;
    });
    service.listTaskHistories().subscribe((histories) => {
      actualHistories = histories;
    });

    httpMock.expectOne('/api/notifications').flush([
      {
        id: 8,
        type: 'TASK_ASSIGNED',
        status: 'SENT',
        message: 'Une tache vous a ete assignee',
        sentAt: '2026-03-19T10:00:00.000Z',
        createdAt: '2026-03-18T10:00:00.000Z',
        user: { id: 2 },
        task: {},
      },
    ]);
    httpMock.expectOne('/api/task-histories').flush([
      {
        id: 9,
        actionType: 'UPDATED',
        fieldName: 'status',
        oldValue: 'TODO',
        newValue: 'DONE',
        createdAt: '2026-03-19T11:00:00.000Z',
        task: { id: 42 },
        changedBy: {},
      },
    ]);

    expect(actualNotifications).toEqual([
      {
        id: 8,
        type: 'TASK_ASSIGNED',
        status: 'SENT',
        message: 'Une tache vous a ete assignee',
        createdAt: '2026-03-19T10:00:00.000Z',
        userId: 2,
        taskId: undefined,
      },
    ]);
    expect(actualHistories).toEqual([
      {
        id: 9,
        actionType: 'UPDATED',
        fieldName: 'status',
        oldValue: 'TODO',
        newValue: 'DONE',
        createdAt: '2026-03-19T11:00:00.000Z',
        taskId: 42,
        changedById: undefined,
      },
    ]);
  });

  it('should send update, accept and delete requests to the expected endpoints', () => {
    service.updateProjectMember(11, { role: 'OBSERVER', requestedById: 1 }).subscribe();
    service.acceptProjectInvitation('invite-token', { userId: 3 }).subscribe();
    service.deleteNotification(8).subscribe();

    const updateRequest = httpMock.expectOne('/api/project-members/11');
    expect(updateRequest.request.method).toBe('PUT');
    expect(updateRequest.request.body).toEqual({ role: 'OBSERVER', requestedById: 1 });
    updateRequest.flush(null);

    const acceptRequest = httpMock.expectOne('/api/project-invitations/token/invite-token/accept');
    expect(acceptRequest.request.method).toBe('POST');
    expect(acceptRequest.request.body).toEqual({ userId: 3 });
    acceptRequest.flush({ projectId: 5 });

    const deleteRequest = httpMock.expectOne('/api/notifications/8');
    expect(deleteRequest.request.method).toBe('DELETE');
    deleteRequest.flush(null);
  });
});
