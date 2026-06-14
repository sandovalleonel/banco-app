import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CuentaDto, GeneralResponseDto } from '../models/banco.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class CuentaService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/cuentas`;

  getAll(): Observable<GeneralResponseDto<CuentaDto[]>> {
    return this.http.get<GeneralResponseDto<CuentaDto[]>>(this.apiUrl);
  }

  getByNumero(numeroCuenta: string): Observable<GeneralResponseDto<CuentaDto>> {
    return this.http.get<GeneralResponseDto<CuentaDto>>(`${this.apiUrl}/${numeroCuenta}`);
  }

  create(cuenta: CuentaDto): Observable<GeneralResponseDto<CuentaDto>> {
    return this.http.post<GeneralResponseDto<CuentaDto>>(this.apiUrl, cuenta);
  }

  update(numeroCuenta: string, cuenta: CuentaDto): Observable<GeneralResponseDto<CuentaDto>> {
    return this.http.put<GeneralResponseDto<CuentaDto>>(`${this.apiUrl}/${numeroCuenta}`, cuenta);
  }

  delete(numeroCuenta: string): Observable<GeneralResponseDto<void>> {
    return this.http.delete<GeneralResponseDto<void>>(`${this.apiUrl}/${numeroCuenta}`);
  }
}
