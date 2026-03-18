import { signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';

import { AuthService } from '../../core/services/auth.service';
import { PmtApiService } from '../../core/services/pmt-api.service';
import { DashboardPageComponent } from './dashboard-page.component';

describe('DashboardPageComponent', () => {
  let fixture: ComponentFixture<DashboardPageComponent>;
  let component: DashboardPageComponent;
  let apiSpy: jasmine.SpyObj<PmtApiService>;

  beforeEach(async () => {
    apiSpy = jasmine.createSpyObj<PmtApiService>('PmtApiService', [
      'listProjects',
      'listTasks',
      'listUsers',
      'createProject',
      'createTask',
    ]);

    apiSpy.listProjects.and.returnValue(of([
      { id: 1, name: 'Projet A', description: 'Migration' },
    ]));
    apiSpy.listTasks.and.returnValue(of([
      {
        id: 10,
        title: 'Configurer auth',
        description: 'Brancher la session',
        status: 'TODO',
        priority: 'HIGH',
        projectId: 1,
        createdById: 2,
        assignedToId: 2,
        dueDate: '2026-03-20T00:00:00.000Z',
        endDate: '2026-03-20T00:00:00.000Z',
      },
      {
        id: 11,
        title: 'Finir dashboard',
        description: 'Kanban',
        status: 'DONE',
        priority: 'MEDIUM',
        projectId: 1,
        createdById: 2,
        assignedToId: 2,
        dueDate: '2026-03-21T00:00:00.000Z',
        endDate: '2026-03-21T00:00:00.000Z',
      },
    ]));
    apiSpy.listUsers.and.returnValue(of([
      { id: 2, username: 'alice', email: 'alice@example.com' },
    ]));
    apiSpy.createProject.and.returnValue(of(2));
    apiSpy.createTask.and.returnValue(of(12));

    await TestBed.configureTestingModule({
      imports: [DashboardPageComponent],
      providers: [
        { provide: PmtApiService, useValue: apiSpy },
        {
          provide: AuthService,
          useValue: {
            currentUser: signal({ id: 2, username: 'alice', email: 'alice@example.com' }),
            requireUser: () => ({ id: 2, username: 'alice', email: 'alice@example.com' }),
          },
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should group tasks into kanban columns', () => {
    const columns = component.kanbanColumns();

    expect(columns.find((column) => column.key === 'TODO')?.tasks.length).toBe(1);
    expect(columns.find((column) => column.key === 'DONE')?.tasks.length).toBe(1);
    expect(component.totalTasks()).toBe(2);
  });

  it('should create a project and select it', () => {
    component.projectForm.setValue({
      name: 'Projet B',
      description: 'Description tres complete',
      startDate: '2026-03-30',
    });

    component.createProject();

    expect(apiSpy.createProject).toHaveBeenCalled();
    expect(apiSpy.createProject).toHaveBeenCalledWith(jasmine.objectContaining({
      ownerId: 2,
    }));
    expect(component.projects().some((project) => project.id === 2)).toBeTrue();
    expect(component.selectedProjectId()).toBe(2);
  });

  it('should reset task form without keeping validation errors after task creation', () => {
    component.taskForm.markAllAsTouched();
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
    expect(component.taskForm.controls.title.value).toBe('');
    expect(component.taskForm.controls.description.value).toBe('');
    expect(component.taskForm.controls.dueDate.value).toBe('');
    expect(component.taskForm.controls.status.value).toBe('TODO');
    expect(component.taskForm.controls.priority.value).toBe('MEDIUM');
    expect(component.taskForm.controls.projectId.value).toBe(1);
    expect(component.taskForm.controls.assignedToId.value).toBe(2);
    expect(component.taskForm.untouched).toBeTrue();
    expect(component.taskForm.pristine).toBeTrue();
    expect(component.taskForm.controls.title.touched).toBeFalse();
    expect(component.taskForm.controls.description.touched).toBeFalse();
    expect(component.taskForm.controls.dueDate.touched).toBeFalse();
  });
});
