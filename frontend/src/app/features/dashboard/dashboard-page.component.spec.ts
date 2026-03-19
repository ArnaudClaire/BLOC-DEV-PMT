/**
 * Vérifie les interactions clés du dashboard et de ses formulaires métiers.
 */
import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { AuthService } from '../../core/services/auth.service';
import { PmtApiService } from '../../core/services/pmt-api.service';
import { DashboardPageComponent } from './dashboard-page.component';

describe('DashboardPageComponent', () => {
  let apiSpy: jasmine.SpyObj<PmtApiService>;

  const users = [
    { id: 1, username: 'a.admin', email: 'alice.admin@pmt.local' },
    { id: 2, username: 'b.member', email: 'bob.member@pmt.local' },
    { id: 3, username: 'c.observer', email: 'claire.observer@pmt.local' },
  ];

  const projects = [
    { id: 1, name: 'PMT Launch', description: 'Projet admin/member', ownerId: 1 },
    { id: 2, name: 'Mobile Refresh', description: 'Projet observer', ownerId: 2 },
  ];

  const tasks = [
    {
      id: 10,
      title: 'Configurer auth',
      description: 'Brancher la session',
      status: 'TODO',
      priority: 'HIGH',
      projectId: 1,
      createdById: 1,
      assignedToId: 2,
      dueDate: '2026-03-20T00:00:00.000Z',
      endDate: '2026-03-20T00:00:00.000Z',
    },
    {
      id: 11,
      title: 'Revoir dashboard mobile',
      description: 'Lecture seule',
      status: 'DONE',
      priority: 'LOW',
      projectId: 2,
      createdById: 2,
      assignedToId: 3,
      dueDate: '2026-03-21T00:00:00.000Z',
      endDate: '2026-03-22T00:00:00.000Z',
    },
  ];

  const projectMembers = [
    { id: 101, role: 'ADMIN', projectId: 1, userId: 1, joinedAt: '2026-03-01T09:00:00.000Z' },
    { id: 102, role: 'MEMBER', projectId: 1, userId: 2, joinedAt: '2026-03-02T09:00:00.000Z' },
    { id: 201, role: 'OBSERVER', projectId: 2, userId: 3, joinedAt: '2026-03-03T09:00:00.000Z' },
  ];

  const projectInvitations = [
    {
      id: 701,
      email: 'new.member@example.com',
      role: 'MEMBER',
      status: 'PENDING',
      projectId: 1,
      invitedById: 1,
      createdAt: '2026-03-18T09:00:00.000Z',
      expiresAt: '2026-03-25T09:00:00.000Z',
    },
  ];

  const notifications = [
    {
      id: 301,
      type: 'TASK_ASSIGNED',
      status: 'SENT',
      message: 'Configurer auth vous a ete assignee.',
      taskId: 10,
      userId: 2,
      createdAt: '2026-03-19T10:00:00.000Z',
    },
    {
      id: 302,
      type: 'INVITATION_SENT',
      status: 'READ',
      message: 'Invitation PMT Launch',
      userId: 3,
      createdAt: '2026-03-18T08:00:00.000Z',
    },
  ];

  const taskHistories = [
    {
      id: 401,
      actionType: 'STATUS_CHANGED',
      fieldName: 'status',
      oldValue: 'TODO',
      newValue: 'IN_PROGRESS',
      taskId: 10,
      changedById: 1,
      createdAt: '2026-03-19T11:00:00.000Z',
    },
    {
      id: 402,
      actionType: 'COMPLETED',
      fieldName: 'status',
      oldValue: 'IN_PROGRESS',
      newValue: 'DONE',
      taskId: 11,
      changedById: 2,
      createdAt: '2026-03-18T11:00:00.000Z',
    },
  ];

  beforeEach(() => {
    apiSpy = jasmine.createSpyObj<PmtApiService>('PmtApiService', [
      'listProjects',
      'listTasks',
      'listUsers',
      'listProjectMembers',
      'listNotifications',
      'listTaskHistories',
      'listProjectInvitations',
      'createProject',
      'createTask',
      'createProjectMember',
      'createProjectInvitation',
      'cancelProjectInvitation',
      'resendProjectInvitation',
      'createNotification',
      'createTaskHistory',
      'updateTask',
    ]);

    apiSpy.listProjects.and.returnValue(of(projects));
    apiSpy.listTasks.and.returnValue(of(tasks));
    apiSpy.listUsers.and.returnValue(of(users));
    apiSpy.listProjectMembers.and.returnValue(of(projectMembers));
    apiSpy.listProjectInvitations.and.returnValue(of(projectInvitations));
    apiSpy.listNotifications.and.returnValue(of(notifications));
    apiSpy.listTaskHistories.and.returnValue(of(taskHistories));
    apiSpy.createProject.and.returnValue(of(3));
    apiSpy.createTask.and.returnValue(of(12));
    apiSpy.createProjectMember.and.returnValue(of(202));
    apiSpy.createProjectInvitation.and.returnValue(of(203));
    apiSpy.cancelProjectInvitation.and.returnValue(of(void 0));
    apiSpy.resendProjectInvitation.and.returnValue(of(void 0));
    apiSpy.createNotification.and.returnValue(of(501));
    apiSpy.createTaskHistory.and.returnValue(of(601));
    apiSpy.updateTask.and.returnValue(of(void 0));
  });

  async function createComponentFor(userId: number): Promise<{
    fixture: ComponentFixture<DashboardPageComponent>;
    component: DashboardPageComponent;
  }> {
    const currentUser = users.find((user) => user.id === userId)!;

    await TestBed.configureTestingModule({
      imports: [DashboardPageComponent],
      providers: [
        { provide: PmtApiService, useValue: apiSpy },
        {
          provide: AuthService,
          useValue: {
            currentUser: signal(currentUser),
            requireUser: () => currentUser,
          },
        },
      ],
    }).compileComponents();

    const fixture = TestBed.createComponent(DashboardPageComponent);
    fixture.detectChanges();

    return {
      fixture,
      component: fixture.componentInstance,
    };
  }

  it('should expose admin capabilities on an admin project', async () => {
    const { component } = await createComponentFor(1);

    expect(component.selectedProject()?.id).toBe(1);
    expect(component.currentProjectRole()).toBe('ADMIN');
    expect(component.canManageMembers()).toBeTrue();
    expect(component.canManageTasks()).toBeTrue();
    expect(component.totalTasks()).toBe(1);
  });

  it('should allow a member to manage tasks but not project members', async () => {
    const { component } = await createComponentFor(2);

    component.selectProject(1);

    expect(component.currentProjectRole()).toBe('MEMBER');
    expect(component.canManageMembers()).toBeFalse();
    expect(component.canManageTasks()).toBeTrue();
  });

  it('should restrict an observer to dashboard, notifications and history', async () => {
    const { component } = await createComponentFor(3);

    expect(component.selectedProject()?.id).toBe(2);
    expect(component.currentProjectRole()).toBe('OBSERVER');
    expect(component.canManageMembers()).toBeFalse();
    expect(component.canManageTasks()).toBeFalse();
    expect(component.canViewTaskDetails()).toBeFalse();
    expect(component.notifications().length).toBe(1);
    expect(component.taskHistories().length).toBe(1);
  });

  it('should create a task and record notification plus history', async () => {
    const { component } = await createComponentFor(1);

    component.taskForm.setValue({
      title: 'Nouvelle tache',
      description: 'Description suffisamment longue',
      projectId: 1,
      priority: 'HIGH',
      status: 'TODO',
      dueDate: '2026-03-30',
      assignedToId: 2,
    });

    component.createTask();

    expect(apiSpy.createTask).toHaveBeenCalled();
    expect(apiSpy.createTaskHistory).toHaveBeenCalledTimes(2);
    expect(apiSpy.createNotification).toHaveBeenCalled();
  });

  it('should open and close task details from a kanban click context', async () => {
    const { component } = await createComponentFor(1);

    component.openTaskDetails(10);

    expect(component.selectedTask()?.id).toBe(10);
    expect(component.selectedTaskView()?.projectName).toBe('PMT Launch');
    expect(component.selectedTaskMembers().map((member) => member.userId)).toEqual([1, 2]);

    component.closeTaskDetails();

    expect(component.selectedTask()).toBeNull();
  });

  it('should close task details after a successful task update', async () => {
    const { component } = await createComponentFor(1);

    component.openTaskDetails(10);
    component.taskDetailForm.patchValue({
      title: 'Configurer auth v2',
      description: 'Brancher la session avec plus de details',
      priority: 'HIGH',
      status: 'IN_PROGRESS',
      dueDate: '2026-03-25',
      endDate: '2026-03-26',
      assignedToId: 2,
    });

    component.updateSelectedTask();

    expect(apiSpy.updateTask).toHaveBeenCalled();
    expect(component.selectedTask()).toBeNull();
  });

  it('should create a project invitation for admins', async () => {
    const { component } = await createComponentFor(1);

    component.invitationForm.setValue({
      email: 'invitee@example.com',
      role: 'MEMBER',
    });

    component.createProjectInvitation();

    expect(apiSpy.createProjectInvitation).toHaveBeenCalledWith({
      email: 'invitee@example.com',
      role: 'MEMBER',
      projectId: 1,
      invitedById: 1,
    });
  });
});
