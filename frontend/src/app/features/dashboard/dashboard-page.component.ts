import { CommonModule } from '@angular/common';
import { Component, HostListener, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { forkJoin, of } from 'rxjs';
import { catchError, finalize, map, switchMap } from 'rxjs/operators';

import {
  CreateProjectInvitationPayload,
  CreateNotificationPayload,
  CreateProjectMemberPayload,
  CreateTaskHistoryPayload,
  CreateTaskPayload,
  MemberRole,
  NotificationStatus,
  NotificationType,
  Project,
  Task,
  TaskPriority,
  TaskStatus,
  UpdateTaskPayload,
  User,
} from '../../core/models/api.models';
import { AuthService } from '../../core/services/auth.service';
import { PmtApiService } from '../../core/services/pmt-api.service';
import { DashboardHeroComponent } from './components/dashboard-hero/dashboard-hero.component';
import { DashboardKanbanBoardComponent } from './components/dashboard-kanban-board/dashboard-kanban-board.component';
import { DashboardProjectFilterComponent } from './components/dashboard-project-filter/dashboard-project-filter.component';
import { DashboardProjectFormComponent } from './components/dashboard-project-form/dashboard-project-form.component';
import { DashboardTaskFormComponent } from './components/dashboard-task-form/dashboard-task-form.component';
import {
  KanbanColumn,
  NotificationView,
  ProjectInvitationView,
  ProjectMemberView,
  TaskCardView,
  TaskHistoryView,
} from './models/dashboard.models';

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
  private readonly formBuilder = inject(FormBuilder);
  private readonly currentUserSnapshot = this.auth.requireUser();
  private pendingProjectSelection: number | 'all' | null = null;
  private pendingTaskSelection: number | null = null;

  readonly priorities: TaskPriority[] = ['LOW', 'MEDIUM', 'HIGH'];
  readonly statuses: TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE'];
  readonly memberRoles: MemberRole[] = ['ADMIN', 'MEMBER', 'OBSERVER'];

  readonly loading = signal(true);
  readonly savingProject = signal(false);
  readonly savingTask = signal(false);
  readonly savingInvitation = signal(false);
  readonly savingTaskUpdate = signal(false);
  readonly invitationActionId = signal<number | null>(null);
  readonly errorMessage = signal<string | null>(null);

  readonly selectedProjectId = signal<number | 'all'>('all');
  readonly selectedTaskId = signal<number | null>(null);

  private readonly allProjects = signal<Project[]>([]);
  private readonly allTasks = signal<Task[]>([]);
  private readonly allUsers = signal<User[]>([]);
  private readonly allProjectMembers = signal<ProjectMemberView[]>([]);
  private readonly rawProjectMembers = signal<CreateProjectMemberPayload[]>([]);
  private readonly allProjectInvitations = signal<ProjectInvitationView[]>([]);
  private readonly allNotifications = signal<NotificationView[]>([]);
  private readonly allTaskHistories = signal<TaskHistoryView[]>([]);

  readonly projectForm = this.formBuilder.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(3)]],
    description: ['', [Validators.required, Validators.minLength(10)]],
    startDate: ['', Validators.required],
  });

  readonly taskForm = this.formBuilder.nonNullable.group({
    title: ['', [Validators.required, Validators.minLength(3)]],
    description: ['', [Validators.required, Validators.minLength(10)]],
    projectId: [0, [Validators.min(1)]],
    priority: ['MEDIUM' as TaskPriority],
    status: ['TODO' as TaskStatus],
    dueDate: ['', Validators.required],
    assignedToId: [0, [Validators.min(1)]],
  });

  readonly invitationForm = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    role: ['MEMBER' as MemberRole],
  });

  readonly taskDetailForm = this.formBuilder.nonNullable.group({
    title: ['', [Validators.required, Validators.minLength(3)]],
    description: ['', [Validators.required, Validators.minLength(10)]],
    priority: ['MEDIUM' as TaskPriority],
    status: ['TODO' as TaskStatus],
    dueDate: ['', Validators.required],
    endDate: [''],
    assignedToId: [0],
  });

  readonly currentUser = computed(() => this.auth.currentUser() ?? this.currentUserSnapshot);
  readonly users = computed(() => this.allUsers());

  readonly projects = computed(() => {
    const allowedIds = this.visibleProjectIds();
    return this.allProjects().filter((project) => allowedIds.has(project.id));
  });

  readonly selectedProject = computed(() => {
    const selectedProjectId = this.selectedProjectId();
    if (selectedProjectId === 'all') {
      return null;
    }

    return this.projects().find((project) => project.id === selectedProjectId) ?? null;
  });

  readonly currentProjectRole = computed<MemberRole | null>(() => {
    const project = this.selectedProject();
    return project ? this.resolveRoleForProject(project.id) : null;
  });

  readonly canCreateProjects = computed(() => {
    const visibleProjects = this.projects();
    if (visibleProjects.length === 0) {
      return true;
    }

    return visibleProjects.some((project) => {
      const role = this.resolveRoleForProject(project.id);
      return role === 'ADMIN' || role === 'MEMBER';
    });
  });

  readonly manageableProjects = computed(() =>
    this.projects().filter((project) => {
      const role = this.resolveRoleForProject(project.id);
      return role === 'ADMIN' || role === 'MEMBER';
    }),
  );

  readonly canManageMembers = computed(() => this.currentProjectRole() === 'ADMIN');

  readonly canManageTasks = computed(() => {
    const role = this.currentProjectRole();
    return role === 'ADMIN' || role === 'MEMBER';
  });

  readonly canViewTaskDetails = computed(() => this.canManageTasks());

  readonly totalTasks = computed(() => this.filteredTasks().length);
  readonly completedTasks = computed(() => this.filteredTasks().filter((task) => task.status === 'DONE').length);
  readonly activeProjects = computed(() => this.projects().length);

  readonly currentProjectMembers = computed(() => {
    const project = this.selectedProject();
    if (!project) {
      return [];
    }

    return this.projectMembersFor(project.id);
  });

  readonly currentProjectInvitations = computed(() => {
    const project = this.selectedProject();
    if (!project) {
      return [];
    }

    return this.allProjectInvitations().filter((invitation) => invitation.projectId === project.id);
  });

  readonly assignableUsers = computed(() => {
    const selectedProject = this.selectedProject();
    if (selectedProject) {
      return this.currentProjectMembers().map((member) => ({
        id: member.userId,
        username: member.username,
        email: member.email,
      }));
    }

    const usersById = new Map<number, User>();
    this.manageableProjects().forEach((project) => {
      this.usersForProject(project.id).forEach((user) => usersById.set(user.id, user));
    });
    return Array.from(usersById.values()).sort((left, right) => left.username.localeCompare(right.username, 'fr'));
  });

  readonly filteredTasks = computed(() => {
    const selectedProject = this.selectedProject();
    const projectIds = this.visibleProjectIds();
    return this.allTasks().filter((task) => {
      if (!projectIds.has(task.projectId)) {
        return false;
      }

      return selectedProject ? task.projectId === selectedProject.id : true;
    });
  });

  readonly kanbanColumns = computed<KanbanColumn[]>(() => {
    const taskViews = this.filteredTasks().map((task) => this.toTaskCardView(task));
    return [
      { key: 'TODO', label: 'À faire', tone: 'slate', tasks: taskViews.filter((task) => task.status === 'TODO') },
      { key: 'IN_PROGRESS', label: 'En cours', tone: 'amber', tasks: taskViews.filter((task) => task.status === 'IN_PROGRESS') },
      { key: 'DONE', label: 'Terminées', tone: 'green', tasks: taskViews.filter((task) => task.status === 'DONE') },
    ];
  });

  readonly selectedTask = computed(() => {
    const selectedTaskId = this.selectedTaskId();
    if (selectedTaskId === null) {
      return null;
    }

    return this.filteredTasks().find((task) => task.id === selectedTaskId) ?? null;
  });

  readonly selectedTaskView = computed<TaskCardView | null>(() => {
    const task = this.selectedTask();
    return task ? this.toTaskCardView(task) : null;
  });

  readonly selectedTaskMembers = computed(() => {
    const task = this.selectedTask();
    return task ? this.projectMembersFor(task.projectId) : [];
  });

  readonly canEditSelectedTask = computed(() => {
    const task = this.selectedTask();
    return task ? this.canManageTasksForProject(task.projectId) : false;
  });

  readonly notifications = computed<NotificationView[]>(() => {
    const selectedProject = this.selectedProject();
    return this.allNotifications().filter((notification) => {
      if (notification.userId !== this.currentUser().id) {
        return false;
      }

      if (!selectedProject) {
        return true;
      }

      return notification.projectId === undefined || notification.projectId === selectedProject.id;
    });
  });

  readonly taskHistories = computed<TaskHistoryView[]>(() => {
    const selectedProject = this.selectedProject();
    return this.allTaskHistories().filter((history) => {
      if (!selectedProject) {
        return true;
      }

      return history.projectId === selectedProject.id;
    });
  });

  readonly roleCapabilities = computed(() => {
    const role = this.currentProjectRole();

    if (role === 'ADMIN') {
      return [
        'Inviter un membre par email et définir son rôle',
        'Créer, assigner et mettre à jour une tâche',
        'Ouvrir le détail d’une tâche',
        'Consulter le tableau de bord, les notifications et l’historique',
      ];
    }

    if (role === 'MEMBER') {
      return [
        'Créer, assigner et mettre à jour une tâche',
        'Ouvrir le détail d’une tâche',
        'Consulter le tableau de bord, les notifications et l’historique',
      ];
    }

    return [
      'Consulter le tableau de bord',
      'Voir les notifications personnelles',
      'Voir l’historique des modifications',
    ];
  });

  roleLabel(role: MemberRole | null | string | undefined): string {
    switch (role) {
      case 'ADMIN':
        return 'Administrateur';
      case 'MEMBER':
        return 'Membre';
      case 'OBSERVER':
        return 'Observateur';
      default:
        return 'Aucun role';
    }
  }

  priorityLabel(priority: TaskPriority | string | undefined): string {
    switch (priority) {
      case 'HIGH':
        return 'Haute';
      case 'MEDIUM':
        return 'Moyenne';
      case 'LOW':
        return 'Basse';
      default:
        return priority ?? '';
    }
  }

  statusLabel(status: TaskStatus | string | undefined): string {
    switch (status) {
      case 'TODO':
        return 'A faire';
      case 'IN_PROGRESS':
        return 'En cours';
      case 'DONE':
        return 'Terminee';
      default:
        return status ?? '';
    }
  }

  invitationStatusLabel(status: string | undefined): string {
    switch (status) {
      case 'PENDING':
        return 'En attente';
      case 'ACCEPTED':
        return 'Acceptée';
      case 'DECLINED':
        return 'Refusée';
      case 'EXPIRED':
        return 'Expirée';
      case 'CANCELED':
        return 'Annulée';
      default:
        return status ?? '';
    }
  }

  historyActionLabel(actionType: string | undefined): string {
    switch (actionType) {
      case 'CREATED':
        return 'Creation';
      case 'UPDATED':
        return 'Mise a jour';
      case 'ASSIGNED':
        return 'Assignation';
      case 'STATUS_CHANGED':
        return 'Statut modifie';
      case 'COMPLETED':
        return 'Cloture';
      default:
        return actionType ?? '';
    }
  }

  historyFieldLabel(fieldName: string | undefined): string {
    switch (fieldName) {
      case 'title':
        return 'titre';
      case 'description':
        return 'description';
      case 'priority':
        return 'priorite';
      case 'dueDate':
        return 'echeance';
      case 'endDate':
        return 'date de fin';
      case 'status':
        return 'statut';
      case 'assignedTo':
        return 'assignation';
      case 'task':
        return 'tache';
      default:
        return fieldName ?? 'champ global';
    }
  }

  constructor() {
    this.loadData();
  }

  selectProject(projectId: number | 'all'): void {
    this.selectedProjectId.set(projectId);
    this.selectedTaskId.set(null);
    this.syncFormsWithSelection();
  }

  closeTaskDetails(): void {
    this.selectedTaskId.set(null);
    this.patchTaskDetailForm();
  }

  openTaskDetails(taskId: number): void {
    if (!this.canViewTaskDetails()) {
      return;
    }

    this.selectedTaskId.set(taskId);
    this.patchTaskDetailForm();
  }

  @HostListener('document:keydown.escape')
  handleEscapeKey(): void {
    if (this.selectedTask()) {
      this.closeTaskDetails();
    }
  }

  createProject(): void {
    if (!this.canCreateProjects()) {
      this.errorMessage.set('Votre rôle actuel ne permet pas de créer un projet.');
      return;
    }

    if (this.projectForm.invalid) {
      this.projectForm.markAllAsTouched();
      return;
    }

    this.savingProject.set(true);
    this.errorMessage.set(null);

    this.api.createProject({
      ...this.projectForm.getRawValue(),
      ownerId: this.currentUser().id,
    }).pipe(
      finalize(() => this.savingProject.set(false)),
    ).subscribe({
      next: (projectId) => {
        this.pendingProjectSelection = projectId;
        this.projectForm.reset({
          name: '',
          description: '',
          startDate: '',
        });
        this.loadData();
      },
      error: (error) => this.errorMessage.set(this.formatError(error)),
    });
  }

  createTask(): void {
    if (this.taskForm.invalid) {
      this.taskForm.markAllAsTouched();
      return;
    }

    const formValue = this.taskForm.getRawValue();
    if (!this.canManageTasksForProject(formValue.projectId)) {
      this.errorMessage.set('Seuls les admins et membres peuvent créer des tâches sur ce projet.');
      return;
    }

    const payload: CreateTaskPayload = {
      title: formValue.title,
      description: formValue.description,
      status: formValue.status,
      priority: formValue.priority,
      dueDate: formValue.dueDate,
      endDate: formValue.dueDate,
      projectId: formValue.projectId,
      createdById: this.currentUser().id,
      assignedToId: formValue.assignedToId,
    };

    this.savingTask.set(true);
    this.errorMessage.set(null);

    this.api.createTask(payload).pipe(
      switchMap((taskId) =>
        this.runPostTaskOperations(taskId, [
          this.buildHistoryPayload('CREATED', 'task', null, payload.title, taskId),
          this.buildHistoryPayload('ASSIGNED', 'assignedTo', null, this.userLabel(payload.assignedToId), taskId),
        ], payload.assignedToId, `La tâche "${payload.title}" vous a été assignée.`).pipe(map(() => taskId)),
      ),
      finalize(() => this.savingTask.set(false)),
    ).subscribe({
      next: (taskId) => {
        this.pendingProjectSelection = payload.projectId;
        this.pendingTaskSelection = taskId;
        this.resetTaskForm(payload.projectId);
        this.loadData();
      },
      error: (error) => this.errorMessage.set(this.formatError(error)),
    });
  }

  createProjectInvitation(): void {
    const selectedProject = this.selectedProject();
    if (!selectedProject || !this.canManageMembers()) {
      this.errorMessage.set("Seul un admin du projet peut inviter un membre.");
      return;
    }

    if (this.invitationForm.invalid) {
      this.invitationForm.markAllAsTouched();
      return;
    }

    const formValue = this.invitationForm.getRawValue();
    const normalizedEmail = formValue.email.trim().toLowerCase();
    const duplicateMember = this.currentProjectMembers().some((member) => member.email.toLowerCase() === normalizedEmail);

    if (duplicateMember) {
      this.errorMessage.set('Cette adresse email fait déjà partie du projet sélectionné.');
      return;
    }

    const duplicateInvitation = this.currentProjectInvitations().some((invitation) =>
      invitation.email.toLowerCase() === normalizedEmail && invitation.status === 'PENDING',
    );

    if (duplicateInvitation) {
      this.errorMessage.set('Une invitation active existe déjà pour cette adresse email.');
      return;
    }

    const payload: CreateProjectInvitationPayload = {
      email: normalizedEmail,
      role: formValue.role,
      projectId: selectedProject.id,
      invitedById: this.currentUser().id,
    };

    this.savingInvitation.set(true);
    this.errorMessage.set(null);

    this.api.createProjectInvitation(payload).pipe(
      finalize(() => this.savingInvitation.set(false)),
    ).subscribe({
      next: () => {
        this.invitationForm.reset({
          email: '',
          role: 'MEMBER',
        });
        this.loadData();
      },
      error: (error) => this.errorMessage.set(this.formatError(error)),
    });
  }

  resendProjectInvitation(invitationId: number): void {
    if (!this.canManageMembers()) {
      return;
    }

    this.invitationActionId.set(invitationId);
    this.errorMessage.set(null);

    this.api.resendProjectInvitation(invitationId, {
      requestedById: this.currentUser().id,
    }).pipe(
      finalize(() => this.invitationActionId.set(null)),
    ).subscribe({
      next: () => this.loadData(),
      error: (error) => this.errorMessage.set(this.formatError(error)),
    });
  }

  cancelProjectInvitation(invitationId: number): void {
    if (!this.canManageMembers()) {
      return;
    }

    this.invitationActionId.set(invitationId);
    this.errorMessage.set(null);

    this.api.cancelProjectInvitation(invitationId, {
      requestedById: this.currentUser().id,
    }).pipe(
      finalize(() => this.invitationActionId.set(null)),
    ).subscribe({
      next: () => this.loadData(),
      error: (error) => this.errorMessage.set(this.formatError(error)),
    });
  }

  updateSelectedTask(): void {
    const selectedTask = this.selectedTask();
    if (!selectedTask) {
      this.errorMessage.set('Sélectionnez une tâche avant de la mettre à jour.');
      return;
    }

    if (!this.canManageTasksForProject(selectedTask.projectId)) {
      this.errorMessage.set('Votre rôle ne permet pas de modifier cette tâche.');
      return;
    }

    if (this.taskDetailForm.invalid) {
      this.taskDetailForm.markAllAsTouched();
      return;
    }

    const formValue = this.taskDetailForm.getRawValue();
    const payload: UpdateTaskPayload = {
      title: formValue.title,
      description: formValue.description,
      status: formValue.status,
      priority: formValue.priority,
      dueDate: formValue.dueDate,
      endDate: formValue.endDate || formValue.dueDate,
      assignedToId: formValue.assignedToId > 0 ? formValue.assignedToId : undefined,
    };

    this.savingTaskUpdate.set(true);
    this.errorMessage.set(null);

    this.api.updateTask(selectedTask.id, payload).pipe(
      switchMap(() => {
        const historyPayloads = this.buildUpdateHistoryPayloads(selectedTask, payload);
        const assignmentChanged = (selectedTask.assignedToId ?? null) !== (payload.assignedToId ?? null);
        const notificationMessage = assignmentChanged && payload.assignedToId
          ? `La tâche "${payload.title}" vous a été assignée ou réassignée.`
          : null;

        return this.runPostTaskOperations(
          selectedTask.id,
          historyPayloads,
          assignmentChanged ? payload.assignedToId : undefined,
          notificationMessage,
        );
      }),
      finalize(() => this.savingTaskUpdate.set(false)),
    ).subscribe({
      next: () => {
        this.pendingProjectSelection = selectedTask.projectId;
        this.pendingTaskSelection = null;
        this.closeTaskDetails();
        this.loadData();
      },
      error: (error) => this.errorMessage.set(this.formatError(error)),
    });
  }

  loadData(): void {
    this.loading.set(true);
    this.errorMessage.set(null);

    forkJoin({
      projects: this.api.listProjects(),
      tasks: this.api.listTasks(),
      users: this.api.listUsers(),
      projectMembers: this.api.listProjectMembers(),
      projectInvitations: this.api.listProjectInvitations(),
      notifications: this.api.listNotifications(),
      taskHistories: this.api.listTaskHistories(),
    }).pipe(
      catchError((error: unknown) => {
        this.errorMessage.set(this.formatError(error));
        return of(null);
      }),
      finalize(() => this.loading.set(false)),
    ).subscribe((bundle) => {
      if (!bundle) {
        return;
      }

      this.allProjects.set(bundle.projects);
      this.allTasks.set(bundle.tasks);
      this.allUsers.set(bundle.users);
      this.rawProjectMembers.set(bundle.projectMembers.map((member) => ({
        role: member.role as MemberRole,
        joinedAt: member.joinedAt ?? new Date().toISOString(),
        projectId: member.projectId,
        userId: member.userId,
      })));
      this.allProjectMembers.set(bundle.projectMembers.map((member) => {
        const user = bundle.users.find((candidate) => candidate.id === member.userId);
        return {
          ...member,
          role: member.role as MemberRole,
          displayId: String(member.id),
          username: user?.username ?? `Utilisateur #${member.userId}`,
          email: user?.email ?? 'email inconnu',
          isOwner: false,
        };
      }));
      this.allProjectInvitations.set(bundle.projectInvitations.map((invitation) => {
        const project = bundle.projects.find((candidate) => candidate.id === invitation.projectId);
        const invitedBy = bundle.users.find((candidate) => candidate.id === invitation.invitedById);
        return {
          ...invitation,
          projectName: project?.name ?? 'Projet non relié',
          invitedByName: invitedBy?.username ?? 'Administrateur inconnu',
          createdAtLabel: this.formatDateTime(invitation.createdAt),
          expiresAtLabel: this.formatDateTime(invitation.expiresAt),
        };
      }).sort((left, right) => this.compareDates(right.createdAt, left.createdAt)));
      this.allNotifications.set(bundle.notifications.map((notification) => {
        const task = bundle.tasks.find((candidate) => candidate.id === notification.taskId);
        const project = bundle.projects.find((candidate) => candidate.id === task?.projectId);
        return {
          ...notification,
          projectId: task?.projectId,
          projectName: project?.name ?? 'Projet non relié',
          taskTitle: task?.title ?? 'Notification générale',
          createdAtLabel: this.formatDateTime(notification.createdAt),
        };
      }).sort((left, right) => this.compareDates(right.createdAt, left.createdAt)));
      this.allTaskHistories.set(bundle.taskHistories.map((history) => {
        const task = bundle.tasks.find((candidate) => candidate.id === history.taskId);
        const project = bundle.projects.find((candidate) => candidate.id === task?.projectId);
        const changedBy = bundle.users.find((candidate) => candidate.id === history.changedById);
        return {
          ...history,
          projectId: task?.projectId,
          projectName: project?.name ?? 'Projet non relié',
          taskTitle: task?.title ?? 'Tâche supprimée',
          changedByName: changedBy?.username ?? 'Utilisateur inconnu',
          createdAtLabel: this.formatDateTime(history.createdAt),
        };
      }).sort((left, right) => this.compareDates(right.createdAt, left.createdAt)));

      this.ensureSelections();
    });
  }

  private visibleProjectIds(): Set<number> {
    const userId = this.currentUser().id;
    const visibleProjectIds = new Set<number>();

    this.allProjects().forEach((project) => {
      if (project.ownerId === userId) {
        visibleProjectIds.add(project.id);
      }
    });

    this.rawProjectMembers().forEach((member) => {
      if (member.userId === userId) {
        visibleProjectIds.add(member.projectId);
      }
    });

    return visibleProjectIds;
  }

  private ensureSelections(): void {
    const projects = this.projects();
    const selectedProjectId = this.selectedProjectId();

    if (this.pendingProjectSelection !== null) {
      if (this.pendingProjectSelection === 'all') {
        this.selectedProjectId.set('all');
      } else if (projects.some((project) => project.id === this.pendingProjectSelection)) {
        this.selectedProjectId.set(this.pendingProjectSelection);
      }
      this.pendingProjectSelection = null;
    } else if (selectedProjectId !== 'all' && !projects.some((project) => project.id === selectedProjectId)) {
      this.selectedProjectId.set(projects.length === 1 ? projects[0].id : 'all');
    } else if (selectedProjectId === 'all' && projects.length === 1) {
      this.selectedProjectId.set(projects[0].id);
    }

    if (this.pendingTaskSelection !== null) {
      const hasPendingTask = this.filteredTasks().some((task) => task.id === this.pendingTaskSelection);
      this.selectedTaskId.set(hasPendingTask ? this.pendingTaskSelection : null);
      this.pendingTaskSelection = null;
    } else if (this.selectedTaskId() !== null && !this.filteredTasks().some((task) => task.id === this.selectedTaskId())) {
      this.selectedTaskId.set(null);
    }

    this.syncFormsWithSelection();
    this.patchTaskDetailForm();
  }

  private syncFormsWithSelection(): void {
    const selectedProject = this.selectedProject();
    const manageableProject = selectedProject && this.canManageTasksForProject(selectedProject.id)
      ? selectedProject
      : this.manageableProjects()[0] ?? null;

    this.taskForm.patchValue({
      projectId: manageableProject?.id ?? 0,
      assignedToId: this.userIsAssignableToProject(manageableProject?.id, this.taskForm.controls.assignedToId.value)
        ? this.taskForm.controls.assignedToId.value
        : 0,
    }, { emitEvent: false });
  }

  private patchTaskDetailForm(): void {
    const task = this.selectedTask();
    if (!task) {
      this.taskDetailForm.reset({
        title: '',
        description: '',
        priority: 'MEDIUM',
        status: 'TODO',
        dueDate: '',
        endDate: '',
        assignedToId: 0,
      }, { emitEvent: false });
      return;
    }

    this.taskDetailForm.reset({
      title: task.title,
      description: task.description ?? '',
      priority: task.priority as TaskPriority,
      status: task.status as TaskStatus,
      dueDate: this.normalizeDate(task.dueDate),
      endDate: this.normalizeDate(task.endDate),
      assignedToId: task.assignedToId ?? 0,
    }, { emitEvent: false });
  }

  private runPostTaskOperations(
    taskId: number,
    historyPayloads: Array<CreateTaskHistoryPayload | null>,
    assignedUserId?: number,
    notificationMessage?: string | null,
  ) {
    const operations = historyPayloads
      .filter((payload): payload is CreateTaskHistoryPayload => payload !== null)
      .map((payload) => this.api.createTaskHistory(payload));

    if (assignedUserId && notificationMessage) {
      operations.push(this.api.createNotification(
        this.buildNotificationPayload(assignedUserId, taskId, notificationMessage, 'TASK_ASSIGNED'),
      ));
    }

    return operations.length > 0 ? forkJoin(operations) : of([]);
  }

  private buildUpdateHistoryPayloads(task: Task, payload: UpdateTaskPayload): CreateTaskHistoryPayload[] {
    const historyPayloads: Array<CreateTaskHistoryPayload | null> = [
      this.buildChangedHistory(task.title, payload.title, 'UPDATED', 'title', task.id),
      this.buildChangedHistory(task.description ?? '', payload.description, 'UPDATED', 'description', task.id),
      this.buildChangedHistory(task.priority, payload.priority, 'UPDATED', 'priority', task.id),
      this.buildChangedHistory(task.dueDate ?? '', payload.dueDate, 'UPDATED', 'dueDate', task.id),
      this.buildChangedHistory(task.endDate ?? '', payload.endDate ?? '', 'UPDATED', 'endDate', task.id),
      this.buildChangedHistory(
        task.status,
        payload.status,
        payload.status === 'DONE' ? 'COMPLETED' : 'STATUS_CHANGED',
        'status',
        task.id,
      ),
      this.buildChangedHistory(
        this.userLabel(task.assignedToId),
        this.userLabel(payload.assignedToId),
        'ASSIGNED',
        'assignedTo',
        task.id,
      ),
    ];

    if (!historyPayloads.some((payloadItem) => payloadItem !== null)) {
      historyPayloads.push(this.buildHistoryPayload('UPDATED', 'task', null, 'Mise à jour sans changement structurel', task.id));
    }

    return historyPayloads.filter((payloadItem): payloadItem is CreateTaskHistoryPayload => payloadItem !== null);
  }

  private buildChangedHistory(
    previousValue: string | undefined | null,
    nextValue: string | undefined | null,
    actionType: CreateTaskHistoryPayload['actionType'],
    fieldName: string,
    taskId: number,
  ): CreateTaskHistoryPayload | null {
    const normalizedPreviousValue = previousValue ?? '';
    const normalizedNextValue = nextValue ?? '';

    if (normalizedPreviousValue === normalizedNextValue) {
      return null;
    }

    return this.buildHistoryPayload(
      actionType,
      fieldName,
      normalizedPreviousValue || null,
      normalizedNextValue || null,
      taskId,
    );
  }

  private buildHistoryPayload(
    actionType: CreateTaskHistoryPayload['actionType'],
    fieldName: string,
    oldValue: string | null,
    newValue: string | null,
    taskId: number,
  ): CreateTaskHistoryPayload {
    return {
      actionType,
      fieldName,
      oldValue,
      newValue,
      taskId,
      changedById: this.currentUser().id,
    };
  }

  private buildNotificationPayload(
    userId: number,
    taskId: number | undefined,
    message: string,
    type: NotificationType,
  ): CreateNotificationPayload {
    return {
      type,
      status: 'SENT' as NotificationStatus,
      message,
      sentAt: new Date().toISOString(),
      userId,
      taskId,
    };
  }

  private toTaskCardView(task: Task): TaskCardView {
    const project = this.projects().find((candidate) => candidate.id === task.projectId);
    const assignee = task.assignedToId ? this.allUsers().find((candidate) => candidate.id === task.assignedToId) : null;

    return {
      ...task,
      projectName: project?.name ?? `Projet #${task.projectId}`,
      assigneeName: assignee?.username ?? 'Non assignée',
      dueDateLabel: this.formatDate(task.dueDate),
    };
  }

  private resolveRoleForProject(projectId: number): MemberRole | null {
    const project = this.allProjects().find((candidate) => candidate.id === projectId);
    if (project?.ownerId === this.currentUser().id) {
      return 'ADMIN';
    }

    const projectMember = this.allProjectMembers().find((member) =>
      member.projectId === projectId && member.userId === this.currentUser().id,
    );

    return (projectMember?.role as MemberRole | undefined) ?? null;
  }

  private canManageTasksForProject(projectId: number): boolean {
    const role = this.resolveRoleForProject(projectId);
    return role === 'ADMIN' || role === 'MEMBER';
  }

  private usersForProject(projectId: number): User[] {
    const userIds = new Set<number>();
    const project = this.allProjects().find((candidate) => candidate.id === projectId);

    if (project?.ownerId) {
      userIds.add(project.ownerId);
    }

    this.allProjectMembers()
      .filter((member) => member.projectId === projectId)
      .forEach((member) => userIds.add(member.userId));

    return this.allUsers().filter((user) => userIds.has(user.id));
  }

  private userIsAssignableToProject(projectId: number | undefined, userId: number): boolean {
    if (!projectId || userId <= 0) {
      return false;
    }

    return this.usersForProject(projectId).some((user) => user.id === userId);
  }

  private userLabel(userId: number | undefined): string | null {
    if (!userId) {
      return null;
    }

    return this.allUsers().find((user) => user.id === userId)?.username ?? `Utilisateur #${userId}`;
  }

  private projectMembersFor(projectId: number): ProjectMemberView[] {
    const membersByUser = new Map<number, ProjectMemberView>();
    const project = this.allProjects().find((candidate) => candidate.id === projectId);
    const owner = project ? this.allUsers().find((user) => user.id === project.ownerId) : null;

    if (project && owner) {
      membersByUser.set(owner.id, {
        id: -project.id,
        role: 'ADMIN',
        joinedAt: undefined,
        projectId: project.id,
        userId: owner.id,
        displayId: `owner-${project.id}`,
        username: owner.username,
        email: owner.email,
        isOwner: true,
      });
    }

    this.allProjectMembers()
      .filter((member) => member.projectId === projectId)
      .forEach((member) => {
        const isOwner = member.userId === project?.ownerId;
        membersByUser.set(member.userId, {
          ...member,
          isOwner,
          role: isOwner ? 'ADMIN' : member.role,
        });
      });

    return Array.from(membersByUser.values()).sort((left, right) => {
      if (left.isOwner && !right.isOwner) {
        return -1;
      }
      if (!left.isOwner && right.isOwner) {
        return 1;
      }
      return left.username.localeCompare(right.username, 'fr');
    });
  }

  private normalizeDate(value?: string): string {
    return value ? value.slice(0, 10) : '';
  }

  private formatDate(value?: string): string {
    if (!value) {
      return 'Aucune date';
    }

    return new Intl.DateTimeFormat('fr-FR', { dateStyle: 'medium' }).format(new Date(value));
  }

  private formatDateTime(value?: string): string {
    if (!value) {
      return 'À l’instant';
    }

    return new Intl.DateTimeFormat('fr-FR', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(value));
  }

  private compareDates(left?: string, right?: string): number {
    const leftTime = left ? new Date(left).getTime() : 0;
    const rightTime = right ? new Date(right).getTime() : 0;
    return leftTime - rightTime;
  }

  private formatError(error: unknown): string {
    if (error instanceof HttpErrorResponse) {
      return error.error?.message ?? `Le backend a répondu avec le statut ${error.status}.`;
    }

    if (error instanceof Error) {
      return error.message;
    }

    return 'Une erreur inattendue est survenue.';
  }

  private resetTaskForm(projectId: number): void {
    this.taskForm.reset({
      title: '',
      description: '',
      projectId,
      priority: 'MEDIUM',
      status: 'TODO',
      dueDate: '',
      assignedToId: 0,
    });
  }
}
