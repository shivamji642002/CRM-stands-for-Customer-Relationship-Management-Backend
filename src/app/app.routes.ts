// src/app/app.routes.ts
import { Routes } from '@angular/router';
import { HomeComponent } from './home.component';
import { TicketCrudComponent } from './ticket-crud/ticket-crud.component';

export const appRoutes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'ticket', component: TicketCrudComponent },
  {
    path: 'ticket/:id',
    loadComponent: () =>
      import('./ticket-panel/ticket-panel.component').then(m => m.TicketPanelComponent)
  },
  {
    path: 'subtask/:id',
    loadComponent: () =>
      import('./subtask/subtask.component').then(m => m.SubtaskComponent)
  }
];
