import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { BehaviorSubject, Observable, catchError, map, of, tap } from 'rxjs';

import { AuthUser } from '../models/auth-user';
import { TokenStorageService } from './token-storage.service';

interface ApiResponse<T> {
  dados: T;
}

interface LoginResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
  usuario: AuthUser;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly usuarioSubject = new BehaviorSubject<AuthUser | null>(null);

  readonly usuario$ = this.usuarioSubject.asObservable();

  get usuarioAtual(): AuthUser | null {
    return this.usuarioSubject.getValue();
  }

  login(usuario: string, senha: string, empresaSlug?: string): Observable<AuthUser> {
    const body: { usuario: string; senha: string; empresaSlug?: string } = { usuario, senha };
    if (empresaSlug?.trim()) {
      body.empresaSlug = empresaSlug.trim();
    }
    return this.http.post<ApiResponse<LoginResponse>>('/api/auth/login', body).pipe(
      tap((res) => {
        this.tokenStorage.setToken(res.dados.accessToken);
        this.usuarioSubject.next(res.dados.usuario);
      }),
      map((res) => res.dados.usuario)
    );
  }

  me(): Observable<AuthUser> {
    return this.http.get<ApiResponse<AuthUser>>('/api/auth/me').pipe(
      map((res) => res.dados),
      tap((user) => this.usuarioSubject.next(user)),
      catchError((error) => {
        this.clearSession();
        throw error;
      })
    );
  }

  logout(): Observable<void> {
    return this.http.post<ApiResponse<void>>('/api/auth/logout', {}).pipe(
      map(() => undefined),
      catchError(() => of(undefined)),
      tap(() => this.clearSession())
    );
  }

  clearSession(): void {
    this.tokenStorage.clearToken();
    this.usuarioSubject.next(null);
  }

  isAuthenticated(): boolean {
    return this.tokenStorage.hasToken();
  }
}
