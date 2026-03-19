/**
 * Types partagés entre le frontend et les réponses/payloads exposés par l'API PMT.
 */
export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';
export type MemberRole = 'ADMIN' | 'MEMBER' | 'OBSERVER';
export type NotificationType = 'TASK_ASSIGNED' | 'INVITATION_SENT';
export type NotificationStatus = 'SENT' | 'READ';
export type TaskHistoryAction = 'CREATED' | 'UPDATED' | 'ASSIGNED' | 'STATUS_CHANGED' | 'COMPLETED';

/**
 * Représente un utilisateur côté frontend.
 */
export interface User {
  id: number;
  username: string;
  email: string;
  createdAt?: string;
}

export interface CreateUserPayload {
  username: string;
  email: string;
  password: string;
}

/**
 * Représente un projet enrichi des champs utiles à l'interface.
 */
export interface Project {
  id: number;
  name: string;
  description?: string;
  startDate?: string;
  ownerId?: number;
}

export interface CreateProjectPayload {
  name: string;
  description: string;
  startDate: string;
  ownerId: number;
}

/**
 * Représente une tâche telle qu'elle est manipulée dans l'UI.
 */
export interface Task {
  id: number;
  title: string;
  description?: string;
  status: TaskStatus | string;
  priority: TaskPriority | string;
  dueDate?: string;
  endDate?: string;
  projectId: number;
  createdById: number;
  assignedToId?: number;
}

export interface CreateTaskPayload {
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: string;
  endDate: string;
  projectId: number;
  createdById: number;
  assignedToId: number;
}

export interface UpdateTaskPayload {
  title: string;
  description: string;
  status: TaskStatus;
  priority: TaskPriority;
  dueDate: string;
  endDate: string;
  assignedToId?: number;
}

/**
 * Représente l'appartenance d'un utilisateur à un projet.
 */
export interface ProjectMember {
  id: number;
  role: MemberRole | string;
  joinedAt?: string;
  projectId: number;
  userId: number;
}

export interface CreateProjectMemberPayload {
  role: MemberRole;
  joinedAt: string;
  projectId: number;
  userId: number;
}

/**
 * Représente une invitation de projet avec son état métier.
 */
export interface ProjectInvitation {
  id: number;
  email: string;
  role?: string;
  projectId?: number;
  status?: string;
  token?: string;
  createdAt?: string;
  expiresAt?: string;
  acceptedAt?: string;
  invitedById?: number;
  projectName?: string;
}

export interface CreateProjectInvitationPayload {
  email: string;
  role: MemberRole;
  projectId: number;
  invitedById: number;
}

export interface ProjectInvitationActionPayload {
  requestedById: number;
}

export interface ProjectInvitationPublicDetails {
  id: number;
  email: string;
  role?: string;
  status?: string;
  projectId: number;
  projectName: string;
  expiresAt?: string;
}

export interface ProjectInvitationAcceptPayload {
  userId: number;
}

export interface ProjectInvitationAcceptResponse {
  projectId: number;
}

/**
 * Notification utilisateur historisée et affichable dans le dashboard.
 */
export interface Notification {
  id: number;
  type?: NotificationType | string;
  message?: string;
  status?: NotificationStatus | string;
  userId?: number;
  taskId?: number;
  createdAt?: string;
}

export interface CreateNotificationPayload {
  type: NotificationType;
  status: NotificationStatus;
  message: string;
  sentAt: string;
  userId: number;
  taskId?: number;
}

/**
 * Entrée d'historique décrivant une modification apportée à une tâche.
 */
export interface TaskHistory {
  id: number;
  actionType?: TaskHistoryAction | string;
  fieldName?: string;
  oldValue?: string | null;
  newValue?: string | null;
  taskId?: number;
  changedById?: number;
  createdAt?: string;
}

export interface CreateTaskHistoryPayload {
  actionType: TaskHistoryAction;
  fieldName: string;
  oldValue?: string | null;
  newValue?: string | null;
  taskId: number;
  changedById: number;
}
