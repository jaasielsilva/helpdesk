import { Injectable } from '@angular/core';

const STORAGE_KEY = 'helpdesk.access_token';

@Injectable({ providedIn: 'root' })
export class TokenStorageService {
  getToken(): string | null {
    try {
      return localStorage.getItem(STORAGE_KEY);
    } catch {
      return null;
    }
  }

  setToken(token: string): void {
    try {
      localStorage.setItem(STORAGE_KEY, token);
    } catch {
      // ignore
    }
  }

  clearToken(): void {
    try {
      localStorage.removeItem(STORAGE_KEY);
    } catch {
      // ignore
    }
  }

  hasToken(): boolean {
    return !!this.getToken()?.trim();
  }
}
