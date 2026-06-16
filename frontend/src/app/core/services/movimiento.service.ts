import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { MovimientoDto, MovimientoRequest, GeneralResponseDto } from '../models/banco.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class MovimientoService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/movimientos`;

  getAll(): Observable<GeneralResponseDto<MovimientoDto[]>> {
    return this.http.get<GeneralResponseDto<MovimientoDto[]>>(this.apiUrl);
  }

  create(movimiento: MovimientoRequest): Observable<GeneralResponseDto<MovimientoDto>> {
    return this.http.post<GeneralResponseDto<MovimientoDto>>(this.apiUrl, movimiento);
  }

  delete(id: number): Observable<GeneralResponseDto<void>> {
    return this.http.delete<GeneralResponseDto<void>>(`${this.apiUrl}/${id}`);
  }
}
