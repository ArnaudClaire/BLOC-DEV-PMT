import { CommonModule } from '@angular/common';
import { Component, input } from '@angular/core';

import { Project } from '../../../../core/models/api.models';
import { KanbanColumn } from '../../models/dashboard.models';

@Component({
  selector: 'app-dashboard-kanban-board',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-kanban-board.component.html',
  styleUrl: './dashboard-kanban-board.component.scss',
})
export class DashboardKanbanBoardComponent {
  readonly loading = input.required<boolean>();
  readonly projectsCount = input.required<number>();
  readonly selectedProject = input<Project | null>();
  readonly columns = input.required<KanbanColumn[]>();

  trackByColumn(_: number, column: KanbanColumn): string {
    return column.key;
  }
}
