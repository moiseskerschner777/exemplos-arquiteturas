import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AdicionarProcedimentoDtoRequest, ProcedimentoAdicionadoDtoResponse } from './procedimento.model';

@Injectable({ providedIn: 'root' })
export class ProcedimentoService {
  private readonly http = inject(HttpClient);

  adicionar(atendimentoId: number, pedido: AdicionarProcedimentoDtoRequest): Observable<ProcedimentoAdicionadoDtoResponse> {
    return this.http.post<ProcedimentoAdicionadoDtoResponse>(`/attendances/${atendimentoId}/procedures`, pedido);
  }
}
