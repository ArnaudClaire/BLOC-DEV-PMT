import { MemberRole, Notification, ProjectInvitation, ProjectMember, Task, TaskHistory, TaskStatus } from '../../../core/models/api.models';

/**
 * Modèles de vue spécifiques au dashboard.
 * Ils enrichissent les données API brutes avec des libellés et des informations calculées pour l'interface.
 */
export type TaskCardView = Task & {
  projectName: string;
  assigneeName: string;
  dueDateLabel: string;
};

export type KanbanColumn = {
  key: TaskStatus;
  label: string;
  tone: string;
  tasks: TaskCardView[];
};

export type ProjectMemberView = ProjectMember & {
  displayId: string;
  username: string;
  email: string;
  role: MemberRole;
  isOwner: boolean;
};

export type NotificationView = Notification & {
  projectId?: number;
  projectName: string;
  taskTitle: string;
  createdAtLabel: string;
};

export type ProjectInvitationView = ProjectInvitation & {
  projectName: string;
  invitedByName: string;
  createdAtLabel: string;
  expiresAtLabel: string;
};

export type TaskHistoryView = TaskHistory & {
  projectId?: number;
  projectName: string;
  taskTitle: string;
  changedByName: string;
  createdAtLabel: string;
};
