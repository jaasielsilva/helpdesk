import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';

import {
  CreateEmpresaRequest,
  Empresa,
  EmpresaCreateResponse
} from '../models/empresa';
import { PageResponse } from '../models/page-response';

@Injectable({ providedIn: 'root' })
export class EmpresaService {
  private readonly http = inject(HttpClient);

  listar(page = 0, size = 10): Observable<PageResponse<Empresa>> {
    const params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sort', 'dataCriacao,desc');

    return this.http.get<{ dados: PageResponse<Empresa> }>('/api/empresas', { params }).pipe(
      map((res) => res.dados)
    );
  }

  criar(request: CreateEmpresaRequest): Observable<EmpresaCreateResponse> {
    return this.http.post<{ dados: EmpresaCreateResponse }>('/api/empresas', request).pipe(
      map((res) => res.dados)
    );
  }

  atualizarStatus(id: number, ativo: boolean): Observable<Empresa> {
    return this.http.patch<{ dados: Empresa }>(`/api/empresas/${id}/status`, { ativo }).pipe(
      map((res) => res.dados)
    );
  }
}
