import { CommonModule } from '@angular/common';
import { Component, input, output } from '@angular/core';

import { Project } from '../../../../core/models/api.models';
import { KanbanColumn } from '../../models/dashboard.models';

@Component({
  selector: 'app-dashboard-kanban-board',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-kanban-board.component.html',
  styleUrl: './dashboard-kanban-board.component.scss',
})
/**
 * Affiche les tâches sous forme de colonnes Kanban filtrées par projet.
 */
export class DashboardKanbanBoardComponent {
  readonly loading = input.required<boolean>();
  readonly projectsCount = input.required<number>();
  readonly selectedProject = input<Project | null>();
  readonly columns = input.required<KanbanColumn[]>();
  readonly detailsEnabled = input(false);

  readonly taskSelected = output<number>();

  /**
   * Stabilise le rendu des colonnes dans les boucles Angular.
   */
  trackByColumn(_: number, column: KanbanColumn): string {
    return column.key;
  }

  /**
   * Convertit la priorité technique en libellé lisible dans les cartes Kanban.
   */
  priorityLabel(priority: string): string {
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
}
