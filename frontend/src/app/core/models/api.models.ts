export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';
export type MemberRole = 'ADMIN' | 'MEMBER' | 'OBSERVER';

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

export interface ProjectInvitation {
  id: number;
  email: string;
  role?: string;
  projectId?: number;
  status?: string;
}

export interface Notification {
  id: number;
  type?: string;
  message?: string;
  status?: string;
  userId?: number;
  taskId?: number;
  createdAt?: string;
}

export interface TaskHistory {
  id: number;
  actionType?: string;
  taskId?: number;
  changedById?: number;
  createdAt?: string;
}
