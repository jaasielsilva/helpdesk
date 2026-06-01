import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import { Chamado, CriarChamadoRequest, PageResponse } from '../models/chamado';

@Injectable({ providedIn: 'root' })
export class ChamadoService {
  private readonly http = inject(HttpClient);

  listar(page = 0, size = 10): Observable<PageResponse<Chamado>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'dataCriacao,desc');

    return this.http.get<{ dados: PageResponse<Chamado> }>('/api/chamados', { params }).pipe(
      map(res => res.dados)
    );
  }

  criar(chamado: CriarChamadoRequest): Observable<Chamado> {
    return this.http.post<{ dados: Chamado }>('/api/chamados', chamado).pipe(
      map(res => res.dados)
    );
  }
}
