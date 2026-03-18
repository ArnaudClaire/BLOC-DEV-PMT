import { CommonModule } from '@angular/common';
import { Component, input, output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-dashboard-project-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './dashboard-project-form.component.html',
  styleUrl: './dashboard-project-form.component.scss',
})
export class DashboardProjectFormComponent {
  readonly form = input.required<FormGroup>();
  readonly saving = input.required<boolean>();

  readonly formSubmitted = output<void>();
}
