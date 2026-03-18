import { Task, TaskStatus } from '../../../core/models/api.models';

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
