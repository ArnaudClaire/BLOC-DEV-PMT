import { Component, input } from '@angular/core';

@Component({
  selector: 'app-dashboard-hero',
  standalone: true,
  templateUrl: './dashboard-hero.component.html',
  styleUrl: './dashboard-hero.component.scss',
})
export class DashboardHeroComponent {
  readonly activeProjects = input.required<number>();
  readonly totalTasks = input.required<number>();
  readonly completedTasks = input.required<number>();
}
