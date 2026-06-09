import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import { Comentario, CriarComentarioRequest } from '../models/comentario';

@Injectable({ providedIn: 'root' })
export class ComentarioService {
  private readonly http = inject(HttpClient);

  listar(chamadoId: number): Observable<Comentario[]> {
    return this.http
      .get<{ dados: Comentario[] }>(`/api/chamados/${chamadoId}/comentarios`)
      .pipe(map((res) => res.dados));
  }

  adicionar(chamadoId: number, payload: CriarComentarioRequest): Observable<Comentario> {
    return this.http
      .post<{ dados: Comentario }>(`/api/chamados/${chamadoId}/comentarios`, payload)
      .pipe(map((res) => res.dados));
  }
}
