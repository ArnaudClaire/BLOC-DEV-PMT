import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { Component, DestroyRef, computed, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { forkJoin } from 'rxjs';

import {
  CreateProjectPayload,
  CreateTaskPayload,
  Project,
  Task,
  TaskPriority,
  TaskStatus,
  User,
} from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { PmtApiService } from '../../core/services/pmt-api.service';
import { DashboardHeroComponent } from './components/dashboard-hero/dashboard-hero.component';
import { DashboardKanbanBoardComponent } from './components/dashboard-kanban-board/dashboard-kanban-board.component';
import { DashboardProjectFilterComponent } from './components/dashboard-project-filter/dashboard-project-filter.component';
import { DashboardProjectFormComponent } from './components/dashboard-project-form/dashboard-project-form.component';
import { DashboardTaskFormComponent } from './components/dashboard-task-form/dashboard-task-form.component';
import { KanbanColumn } from './models/dashboard.models';

@Component({
  selector: 'app-dashboard-page',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    DashboardHeroComponent,
    DashboardProjectFilterComponent,
    DashboardProjectFormComponent,
    DashboardTaskFormComponent,
    DashboardKanbanBoardComponent,
  ],
  templateUrl: './dashboard-page.component.html',
  styleUrl: './dashboard-page.component.scss',
})
export class DashboardPageComponent {
  private readonly api = inject(PmtApiService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  private readonly destroyRef = inject(DestroyRef);

  readonly currentUser = this.auth.currentUser;
  readonly loading = signal(true);
  readonly savingProject = signal(false);
  readonly savingTask = signal(false);
  readonly errorMessage = signal('');
  readonly projects = signal<Project[]>([]);
  readonly tasks = signal<Task[]>([]);
  readonly users = signal<User[]>([]);
  readonly selectedProjectId = signal<number | 'all'>('all');

  readonly statuses: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE'];
  readonly priorities: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH'];

  readonly selectedProject = computed(() => {
    const selectedId = this.selectedProjectId();
    return selectedId === 'all' ? null : this.projects().find((project) => project.id === selectedId) ?? null;
  });

  readonly visibleTasks = computed(() => {
    const selectedId = this.selectedProjectId();
    const tasks = this.tasks();

    if (selectedId === 'all') {
      return tasks;
    }

    return tasks.filter((task) => task.projectId === selectedId);
  });

  readonly kanbanColumns = computed<KanbanColumn[]>(() => {
    const projectsById = new Map(this.projects().map((project) => [project.id, this.projectLabel(project)]));
    const usersById = new Map(this.users().map((user) => [user.id, user.username]));
    const baseColumns: KanbanColumn[] = [
      { key: 'TODO', label: 'A preparer', tone: 'todo', tasks: [] },
      { key: 'IN_PROGRESS', label: 'En cours', tone: 'progress', tasks: [] },
      { key: 'DONE', label: 'Termine', tone: 'done', tasks: [] },
    ];

    return baseColumns.map((column) => ({
      ...column,
      tasks: this.visibleTasks()
        .filter((task) => this.normalizeStatus(task.status) === column.key)
        .map((task) => ({
          ...task,
          projectName: projectsById.get(task.projectId) ?? `Projet #${task.projectId}`,
          assigneeName: task.assignedToId ? (usersById.get(task.assignedToId) ?? 'Non assigne') : 'Non assigne',
          dueDateLabel: task.dueDate ? new Date(task.dueDate).toLocaleDateString('fr-FR') : 'Sans date',
        })),
    }));
  });

  readonly totalTasks = computed(() => this.visibleTasks().length);
  readonly completedTasks = computed(() => this.visibleTasks().filter((task) => this.normalizeStatus(task.status) === 'DONE').length);
  readonly activeProjects = computed(() => this.projects().length);

  readonly projectForm = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(3)]],
    description: ['', [Validators.required, Validators.minLength(10)]],
    startDate: ['', [Validators.required]],
  });

  readonly taskForm = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.minLength(3)]],
    description: ['', [Validators.required, Validators.minLength(10)]],
    projectId: [0, [Validators.required, Validators.min(1)]],
    priority: ['MEDIUM' as TaskPriority, [Validators.required]],
    status: ['TODO' as TaskStatus, [Validators.required]],
    dueDate: ['', [Validators.required]],
    assignedToId: [0, [Validators.required, Validators.min(1)]],
  });

  constructor() {
    this.loadData();
  }

  loadData(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    forkJoin({
      projects: this.api.listProjects(),
      tasks: this.api.listTasks(),
      users: this.api.listUsers(),
    })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: ({ projects, tasks, users }) => {
          this.projects.set(projects);
          this.tasks.set(tasks);
          this.users.set(users);

          if (projects.length > 0 && this.taskForm.controls.projectId.value === 0) {
            this.taskForm.patchValue({ projectId: projects[0].id });
          }

          const currentUserId = this.currentUser()?.id ?? users[0]?.id ?? 0;
          if (currentUserId !== 0 && this.taskForm.controls.assignedToId.value === 0) {
            this.taskForm.patchValue({ assignedToId: currentUserId });
          }

          this.loading.set(false);
        },
        error: () => {
          this.errorMessage.set("Impossible de charger les donnees depuis le backend. Verifie que l'API tourne sur http://localhost:8081.");
          this.loading.set(false);
        },
      });
  }

  selectProject(projectId: number | 'all'): void {
    this.selectedProjectId.set(projectId);
  }

  createProject(): void {
    if (this.projectForm.invalid) {
      this.projectForm.markAllAsTouched();
      this.errorMessage.set("Le formulaire projet est incomplet. Renseigne un nom d'au moins 3 caracteres, une description d'au moins 10 caracteres et une date de demarrage.");
      return;
    }

    const value = this.projectForm.getRawValue();
    const currentUser = this.auth.requireUser();
    const payload: CreateProjectPayload = {
      name: value.name,
      description: value.description,
      startDate: value.startDate,
      ownerId: currentUser.id,
    };

    this.savingProject.set(true);
    this.errorMessage.set('');

    this.api.createProject(payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (projectId) => {
          const project: Project = { id: projectId, ...payload };
          this.projects.update((projects) => [...projects, project]);
          this.selectedProjectId.set(projectId);
          this.taskForm.patchValue({ projectId });
          this.projectForm.reset({
            name: '',
            description: '',
            startDate: '',
          });
          this.savingProject.set(false);
        },
        error: (error: unknown) => {
          this.errorMessage.set(this.describeMutationError('projet', 'POST /projects', error));
          this.savingProject.set(false);
        },
      });
  }

  createTask(): void {
    if (this.taskForm.invalid) {
      this.taskForm.markAllAsTouched();
      this.errorMessage.set("Le formulaire tache est incomplet. Verifie le titre, la description, le projet, l'echeance et l'utilisateur assigne.");
      return;
    }

    const currentUser = this.auth.requireUser();
    const value = this.taskForm.getRawValue();
    const payload: CreateTaskPayload = {
      title: value.title,
      description: value.description,
      status: value.status,
      priority: value.priority,
      dueDate: value.dueDate,
      endDate: value.dueDate,
      projectId: value.projectId,
      createdById: currentUser.id,
      assignedToId: value.assignedToId,
    };

    this.savingTask.set(true);
    this.errorMessage.set('');

    this.api.createTask(payload)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (taskId) => {
          this.tasks.update((tasks) => [...tasks, { id: taskId, ...payload }]);
          this.resetTaskForm();
          this.savingTask.set(false);
        },
        error: (error: unknown) => {
          this.errorMessage.set(this.describeMutationError('tache', 'POST /tasks', error));
          this.savingTask.set(false);
        },
      });
  }

  projectLabel(project: Project): string {
    return project.name || `Projet #${project.id}`;
  }

  private normalizeStatus(status: string): TaskStatus {
    if (status === 'IN_PROGRESS' || status === 'DONE') {
      return status;
    }

    return 'TODO';
  }

  private resetTaskForm(): void {
    const selectedProjectId = this.selectedProjectId();
    const currentProjectId = selectedProjectId === 'all'
      ? (this.projects()[0]?.id ?? 0)
      : selectedProjectId;
    const currentAssigneeId = this.currentUser()?.id ?? this.users()[0]?.id ?? 0;

    this.taskForm.reset({
      title: '',
      description: '',
      projectId: currentProjectId,
      priority: 'MEDIUM',
      status: 'TODO',
      dueDate: '',
      assignedToId: currentAssigneeId,
    });
    this.taskForm.markAsPristine();
    this.taskForm.markAsUntouched();
  }

  private describeMutationError(resourceLabel: string, endpoint: string, error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      const details = this.extractBackendMessage(error);
      return details
        ? `La creation du ${resourceLabel} a echoue via ${endpoint}: ${details}`
        : `La creation du ${resourceLabel} a echoue. Le backend a repondu avec le statut ${error.status || 0}.`;
    }

    return `La creation du ${resourceLabel} a echoue. Verifie le schema attendu par ${endpoint}.`;
  }

  private extractBackendMessage(error: HttpErrorResponse): string {
    if (typeof error.error === 'string' && error.error.trim()) {
      return error.error.replace(/<[^>]+>/g, ' ').replace(/\s+/g, ' ').trim();
    }

    if (error.error && typeof error.error.message === 'string') {
      return error.error.message;
    }

    return '';
  }
}
