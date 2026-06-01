import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  template: `
    <div class="py-20 text-center text-slate-400 font-medium">
      <span class="text-4xl block mb-3">{{ icone }}</span>
      <p class="text-sm">{{ titulo }}</p>
      @if (descricao) {
        <p class="text-xs text-slate-400 mt-1">{{ descricao }}</p>
      }
    </div>
  `
})
export class EmptyStateComponent {
  @Input({ required: true }) icone = '📂';
  @Input({ required: true }) titulo = 'Nenhum registro encontrado.';
  @Input() descricao = '';
}
