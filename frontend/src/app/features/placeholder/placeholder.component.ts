import { Component, inject } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-placeholder',
  standalone: true,
  template: `
    <section class="panel">
      <div class="panel-header">
        <div>
          <h2>{{ titulo }}</h2>
          <p>Modulo reservado para a proxima etapa do sistema.</p>
        </div>
      </div>
      <div class="empty-state">Estrutura pronta para crescer.</div>
    </section>
  `
})
export class PlaceholderComponent {
  private readonly route = inject(ActivatedRoute);

  readonly titulo = this.route.snapshot.data['title'] ?? 'Modulo';
}
