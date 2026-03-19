import { CommonModule } from '@angular/common';
import { Component, input, output } from '@angular/core';

import { Project } from '../../../../core/models/api.models';

@Component({
  selector: 'app-dashboard-project-filter',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard-project-filter.component.html',
  styleUrl: './dashboard-project-filter.component.scss',
})
/**
 * Gère le sélecteur de projet et le bouton de rafraîchissement du dashboard.
 */
export class DashboardProjectFilterComponent {
  readonly projects = input.required<Project[]>();
  readonly selectedProjectId = input.required<number | 'all'>();

  readonly projectSelected = output<number | 'all'>();
  readonly refreshRequested = output<void>();

  /**
   * Produit un libellé d'affichage robuste même si le nom du projet est absent.
   */
  projectLabel(project: Project): string {
    return project.name || `Projet #${project.id}`;
  }
}
