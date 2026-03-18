import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import {
  CreateProjectPayload,
  CreateProjectMemberPayload,
  CreateTaskPayload,
  CreateUserPayload,
  Notification,
  Project,
  ProjectInvitation,
  ProjectMember,
  Task,
  TaskHistory,
  User,
} from '../models/api.models';

@Injectable({
  providedIn: 'root',
})
export class PmtApiService {
  private readonly http = inject(HttpClient);
  private readonly apiBaseUrl = '/api';

  listUsers(): Observable<User[]> {
    return this.list<User>('users').pipe(map((users) => users.map((user) => this.mapUser(user))));
  }

  getUser(id: number): Observable<User> {
    return this.getById<User>('users', id).pipe(map((user) => this.mapUser(user)));
  }

  createUser(payload: CreateUserPayload): Observable<unknown> {
    return this.create<unknown>('users', payload);
  }

  login(payload: { email: string; password: string }): Observable<User> {
    return this.create<User>('auth/login', payload).pipe(map((user) => this.mapUser(user)));
  }

  deleteUser(id: number): Observable<void> {
    return this.delete('users', id);
  }

  listProjects(): Observable<Project[]> {
    return this.list<unknown>('projects').pipe(map((projects) => projects.map((project) => this.mapProject(project))));
  }

  getProject(id: number): Observable<Project> {
    return this.getById<unknown>('projects', id).pipe(map((project) => this.mapProject(project)));
  }

  createProject(payload: CreateProjectPayload): Observable<number> {
    return this.create<number>('projects', payload);
  }

  deleteProject(id: number): Observable<void> {
    return this.delete('projects', id);
  }

  listTasks(): Observable<Task[]> {
    return this.list<unknown>('tasks').pipe(map((tasks) => tasks.map((task) => this.mapTask(task))));
  }

  getTask(id: number): Observable<Task> {
    return this.getById<unknown>('tasks', id).pipe(map((task) => this.mapTask(task)));
  }

  createTask(payload: CreateTaskPayload): Observable<number> {
    return this.create<number>('tasks', payload);
  }

  deleteTask(id: number): Observable<void> {
    return this.delete('tasks', id);
  }

  listProjectMembers(): Observable<ProjectMember[]> {
    return this.list<unknown>('project-members').pipe(map((members) => members.map((member) => this.mapProjectMember(member))));
  }

  getProjectMember(id: number): Observable<ProjectMember> {
    return this.getById<unknown>('project-members', id).pipe(map((member) => this.mapProjectMember(member)));
  }

  createProjectMember(payload: CreateProjectMemberPayload): Observable<number> {
    return this.create<number>('project-members', payload);
  }

  deleteProjectMember(id: number): Observable<void> {
    return this.delete('project-members', id);
  }

  listProjectInvitations(): Observable<ProjectInvitation[]> {
    return this.list<unknown>('project-invitations').pipe(map((invitations) => invitations.map((invitation) => this.mapProjectInvitation(invitation))));
  }

  getProjectInvitation(id: number): Observable<ProjectInvitation> {
    return this.getById<unknown>('project-invitations', id).pipe(map((invitation) => this.mapProjectInvitation(invitation)));
  }

  createProjectInvitation(payload: Record<string, unknown>): Observable<number> {
    return this.create<number>('project-invitations', payload);
  }

  deleteProjectInvitation(id: number): Observable<void> {
    return this.delete('project-invitations', id);
  }

  listNotifications(): Observable<Notification[]> {
    return this.list<unknown>('notifications').pipe(map((notifications) => notifications.map((notification) => this.mapNotification(notification))));
  }

  getNotification(id: number): Observable<Notification> {
    return this.getById<unknown>('notifications', id).pipe(map((notification) => this.mapNotification(notification)));
  }

  createNotification(payload: Record<string, unknown>): Observable<number> {
    return this.create<number>('notifications', payload);
  }

  deleteNotification(id: number): Observable<void> {
    return this.delete('notifications', id);
  }

  listTaskHistories(): Observable<TaskHistory[]> {
    return this.list<unknown>('task-histories').pipe(map((histories) => histories.map((history) => this.mapTaskHistory(history))));
  }

  getTaskHistory(id: number): Observable<TaskHistory> {
    return this.getById<unknown>('task-histories', id).pipe(map((history) => this.mapTaskHistory(history)));
  }

  createTaskHistory(payload: Record<string, unknown>): Observable<number> {
    return this.create<number>('task-histories', payload);
  }

  deleteTaskHistory(id: number): Observable<void> {
    return this.delete('task-histories', id);
  }

  list<T>(path: string): Observable<T[]> {
    return this.http.get<T[]>(this.buildUrl(path));
  }

  getById<T>(path: string, id: number): Observable<T> {
    return this.http.get<T>(this.buildUrl(path, id));
  }

  create<TResponse>(path: string, payload: unknown): Observable<TResponse> {
    return this.http.post<TResponse>(this.buildUrl(path), payload);
  }

  delete(path: string, id: number): Observable<void> {
    return this.http.delete<void>(this.buildUrl(path, id));
  }

  private buildUrl(path: string, id?: number): string {
    return id === undefined ? `${this.apiBaseUrl}/${path}` : `${this.apiBaseUrl}/${path}/${id}`;
  }

  private mapUser(payload: unknown): User {
    const value = payload as {
      id: number;
      username: string;
      email: string;
      createdAt?: string;
    };

    return {
      id: value.id,
      username: value.username,
      email: value.email,
      createdAt: value.createdAt,
    };
  }

  private mapProject(payload: unknown): Project {
    const value = payload as {
      id: number;
      name: string;
      description?: string;
      startDate?: string;
      owner?: { id?: number };
    };

    return {
      id: value.id,
      name: value.name,
      description: value.description,
      startDate: value.startDate,
      ownerId: value.owner?.id,
    };
  }

  private mapTask(payload: unknown): Task {
    const value = payload as {
      id: number;
      title: string;
      description?: string;
      status: string;
      priority: string;
      dueDate?: string;
      endDate?: string;
      project?: { id?: number };
      createdBy?: { id?: number };
      assignedTo?: { id?: number };
    };

    return {
      id: value.id,
      title: value.title,
      description: value.description,
      status: value.status as Task['status'],
      priority: value.priority as Task['priority'],
      dueDate: value.dueDate,
      endDate: value.endDate,
      projectId: value.project?.id ?? 0,
      createdById: value.createdBy?.id ?? 0,
      assignedToId: value.assignedTo?.id,
    };
  }

  private mapProjectMember(payload: unknown): ProjectMember {
    const value = payload as {
      id: number;
      role: string;
      joinedAt?: string;
      project?: { id?: number };
      user?: { id?: number };
    };

    return {
      id: value.id,
      role: value.role,
      joinedAt: value.joinedAt,
      projectId: value.project?.id ?? 0,
      userId: value.user?.id ?? 0,
    };
  }

  private mapProjectInvitation(payload: unknown): ProjectInvitation {
    const value = payload as {
      id: number;
      email: string;
      role?: string;
      status?: string;
      project?: { id?: number };
    };

    return {
      id: value.id,
      email: value.email,
      role: value.role,
      status: value.status,
      projectId: value.project?.id,
    };
  }

  private mapNotification(payload: unknown): Notification {
    const value = payload as {
      id: number;
      type?: string;
      status?: string;
      message?: string;
      createdAt?: string;
      user?: { id?: number };
      task?: { id?: number };
    };

    return {
      id: value.id,
      type: value.type,
      status: value.status,
      message: value.message,
      createdAt: value.createdAt,
      userId: value.user?.id,
      taskId: value.task?.id,
    };
  }

  private mapTaskHistory(payload: unknown): TaskHistory {
    const value = payload as {
      id: number;
      actionType?: string;
      createdAt?: string;
      task?: { id?: number };
      changedBy?: { id?: number };
    };

    return {
      id: value.id,
      actionType: value.actionType,
      createdAt: value.createdAt,
      taskId: value.task?.id,
      changedById: value.changedBy?.id,
    };
  }
}
