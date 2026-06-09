import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import {
  AtualizarChamadoRequest,
  Chamado,
  ChamadoFiltros,
  CriarChamadoRequest,
  PageResponse
} from '../models/chamado';

@Injectable({ providedIn: 'root' })
export class ChamadoService {
  private readonly http = inject(HttpClient);

  listar(page = 0, size = 10, filtros?: ChamadoFiltros): Observable<PageResponse<Chamado>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'dataCriacao,desc');

    if (filtros?.status) params = params.set('status', filtros.status);
    if (filtros?.busca?.trim()) params = params.set('busca', filtros.busca.trim());

    return this.http
      .get<{ dados: PageResponse<Chamado> }>('/api/chamados', { params })
      .pipe(map((res) => res.dados));
  }

  buscarPorId(id: number): Observable<Chamado> {
    return this.http
      .get<{ dados: Chamado }>(`/api/chamados/${id}`)
      .pipe(map((res) => res.dados));
  }

  criar(chamado: CriarChamadoRequest): Observable<Chamado> {
    return this.http
      .post<{ dados: Chamado }>('/api/chamados', chamado)
      .pipe(map((res) => res.dados));
  }

  atualizar(id: number, payload: AtualizarChamadoRequest): Observable<Chamado> {
    return this.http
      .put<{ dados: Chamado }>(`/api/chamados/${id}`, payload)
      .pipe(map((res) => res.dados));
  }

  deletar(id: number): Observable<void> {
    return this.http
      .delete<{ dados: void }>(`/api/chamados/${id}`)
      .pipe(map(() => undefined));
  }
}
