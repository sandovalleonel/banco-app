import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ClienteDto, GeneralResponseDto } from '../models/banco.models';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ClienteService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/clientes`;

  getAll(): Observable<GeneralResponseDto<ClienteDto[]>> {
    return this.http.get<GeneralResponseDto<ClienteDto[]>>(this.apiUrl);
  }

  getById(id: number): Observable<GeneralResponseDto<ClienteDto>> {
    return this.http.get<GeneralResponseDto<ClienteDto>>(`${this.apiUrl}/${id}`);
  }

  create(cliente: ClienteDto): Observable<GeneralResponseDto<ClienteDto>> {
    return this.http.post<GeneralResponseDto<ClienteDto>>(this.apiUrl, cliente);
  }

  update(id: number, cliente: ClienteDto): Observable<GeneralResponseDto<ClienteDto>> {
    return this.http.put<GeneralResponseDto<ClienteDto>>(`${this.apiUrl}/${id}`, cliente);
  }

  delete(id: number): Observable<GeneralResponseDto<void>> {
    return this.http.delete<GeneralResponseDto<void>>(`${this.apiUrl}/${id}`);
  }
}
