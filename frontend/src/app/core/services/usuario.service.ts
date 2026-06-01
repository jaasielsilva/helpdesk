import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import {
  CreateUsuarioRequest,
  UpdateUsuarioRequest,
  Usuario
} from '../models/usuario';
import { PageResponse } from '../models/page-response';

@Injectable({ providedIn: 'root' })
export class UsuarioService {
  private readonly http = inject(HttpClient);

  listar(page = 0, size = 10, empresaId?: number | null): Observable<PageResponse<Usuario>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'dataCriacao,desc');

    if (empresaId != null) {
      params = params.set('empresaId', empresaId);
    }

    return this.http.get<{ dados: PageResponse<Usuario> }>('/api/usuarios', { params }).pipe(
      map((res) => res.dados)
    );
  }

  criar(request: CreateUsuarioRequest): Observable<Usuario> {
    return this.http.post<{ dados: Usuario }>('/api/usuarios', request).pipe(
      map((res) => res.dados)
    );
  }

  atualizar(id: number, request: UpdateUsuarioRequest): Observable<Usuario> {
    return this.http.put<{ dados: Usuario }>(`/api/usuarios/${id}`, request).pipe(
      map((res) => res.dados)
    );
  }

  desativar(id: number): Observable<void> {
    return this.http.delete<{ dados: void }>(`/api/usuarios/${id}`).pipe(
      map(() => undefined)
    );
  }
}
