import { Injectable } from '@angular/core';

const STORAGE_KEY = 'helpdesk.remembered-usuario';

@Injectable({ providedIn: 'root' })
export class RememberUserService {
  getRememberedUsuario(): string | null {
    try {
      const usuario = localStorage.getItem(STORAGE_KEY)?.trim();
      return usuario || null;
    } catch {
      return null;
    }
  }

  persistPreference(lembrar: boolean, usuario: string): void {
    try {
      if (lembrar && usuario.trim()) {
        localStorage.setItem(STORAGE_KEY, usuario.trim());
        return;
      }

      localStorage.removeItem(STORAGE_KEY);
    } catch {
      // Ignora indisponibilidade de localStorage (modo privado, etc.)
    }
  }
}
