import { CommonModule } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';

import { Project, TaskPriority, TaskStatus, User } from '../../../../core/models/api.models';

@Component({
  selector: 'app-dashboard-task-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './dashboard-task-form.component.html',
  styleUrl: './dashboard-task-form.component.scss',
})
export class DashboardTaskFormComponent {
  readonly form = input.required<FormGroup>();
  readonly projects = input.required<Project[]>();
  readonly users = input.required<User[]>();
  readonly priorities = input.required<TaskPriority[]>();
  readonly statuses = input.required<TaskStatus[]>();
  readonly saving = input.required<boolean>();
  readonly disabled = input.required<boolean>();

  readonly formSubmitted = output<void>();

  priorityLabel(priority: TaskPriority | string): string {
    switch (priority) {
      case 'HIGH':
        return 'Haute';
      case 'MEDIUM':
        return 'Moyenne';
      case 'LOW':
        return 'Basse';
      default:
        return priority;
    }
  }

  statusLabel(status: TaskStatus | string): string {
    switch (status) {
      case 'TODO':
        return 'A faire';
      case 'IN_PROGRESS':
        return 'En cours';
      case 'DONE':
        return 'Terminee';
      default:
        return status;
    }
  }
}
