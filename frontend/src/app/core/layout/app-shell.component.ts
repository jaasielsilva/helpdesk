import { AsyncPipe, DatePipe, SlicePipe, UpperCasePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map, startWith } from 'rxjs';

import { AuthService } from '../services/auth.service';
import { MenuService } from '../services/menu.service';
import { NotificationService } from '../services/notification.service';
import { NavIconComponent } from './nav-icon.component';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [AsyncPipe, DatePipe, SlicePipe, UpperCasePipe, RouterLink, RouterLinkActive, RouterOutlet, NavIconComponent],
  templateUrl: './app-shell.component.html',
  styleUrl: './app-shell.component.css'
})
export class AppShellComponent {
  private readonly authService = inject(AuthService);
  private readonly menuService = inject(MenuService);
  private readonly notificationService = inject(NotificationService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  readonly usuario$ = this.authService.usuario$;
  readonly menuGroups$ = this.usuario$.pipe(
    map((user) => this.menuService.getMenuGroupsForUser(user))
  );
  readonly agora = new Date();

  readonly pageTitle$ = this.router.events.pipe(
    filter((event) => event instanceof NavigationEnd),
    startWith(null),
    map(() => this.route.firstChild?.snapshot.data['title'] ?? 'Dashboard')
  );

  logout(): void {
    this.authService.logout().subscribe({
      next: () => {
        this.notificationService.success('Até logo! Sua sessão foi encerrada.', 'Deslogado');
        this.router.navigate(['/login']);
      },
      error: () => {
        this.notificationService.success('Até logo! Sua sessão foi encerrada.', 'Deslogado');
        this.router.navigate(['/login']);
      }
    });
  }
}
