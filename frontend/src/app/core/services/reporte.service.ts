import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { GeneralResponseDto } from '../models/banco.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ReporteService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/reportes`;

  getAccountStatement(fechaInicio: string, fechaFin: string, clienteId: number): Observable<GeneralResponseDto<any>> {
    const params = new HttpParams()
      .set('fechaInicio', fechaInicio)
      .set('fechaFin', fechaFin)
      .set('cliente', clienteId.toString());

    return this.http.get<GeneralResponseDto<any>>(this.apiUrl, { params });
  }
}
