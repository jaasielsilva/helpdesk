import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';

import { NoMouseFocusDirective } from './core/directives/no-mouse-focus.directive';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet],
  hostDirectives: [NoMouseFocusDirective],
  template: '<router-outlet />'
})
export class AppComponent {}
