import { DOCUMENT } from '@angular/common';
import { Directive, DestroyRef, inject, OnInit } from '@angular/core';

const INTERACTIVE_SELECTOR =
  'a, button, summary, [role="button"], li, [tabindex]:not([tabindex="-1"])';

const FORM_CONTROL_SELECTOR = 'input, textarea, select, [contenteditable="true"]';

/**
 * Evita que cliques com mouse deixem outline/caret "preso" em links, botões etc.
 * Navegação por teclado (Tab) continua recebendo foco normalmente.
 */
@Directive({ selector: '[appNoMouseFocus]', standalone: true })
export class NoMouseFocusDirective implements OnInit {
  private readonly document = inject(DOCUMENT);
  private readonly destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    const handler = (event: MouseEvent) => this.onMouseDown(event);
    this.document.addEventListener('mousedown', handler, true);
    this.destroyRef.onDestroy(() =>
      this.document.removeEventListener('mousedown', handler, true)
    );
  }

  private onMouseDown(event: MouseEvent): void {
    if (event.button !== 0 || event.defaultPrevented) return;

    const target = event.target;
    if (!(target instanceof HTMLElement)) return;

    if (target.closest(FORM_CONTROL_SELECTOR)) return;

    const interactive = target.closest(INTERACTIVE_SELECTOR);
    if (!interactive) return;

    event.preventDefault();
  }
}
