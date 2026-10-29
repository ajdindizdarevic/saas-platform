import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { SidebarComponent } from '../sidebar/sidebar.component';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [RouterOutlet, NavbarComponent, SidebarComponent],
  template: `
    <div class="shell">
      <app-navbar (sidebarToggle)="sidebarOpen.set(!sidebarOpen())" />
      <div class="body">
        <app-sidebar [open]="sidebarOpen()" />
        <main class="content">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
  styles: [`
    .shell { display: flex; flex-direction: column; height: 100vh; overflow: hidden; }
    .body  { display: flex; flex: 1; overflow: hidden; }
    .content { flex: 1; overflow-y: auto; background: #f8f8f9; }
  `]
})
export class ShellComponent {
  sidebarOpen = signal(false);
}
