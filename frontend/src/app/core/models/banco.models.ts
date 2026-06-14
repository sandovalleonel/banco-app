export interface ClienteDto {
  id?: number;
  identificacion: string;
  nombre: string;
  genero: string;
  edad: number;
  direccion: string;
  telefono: string;
  contrasena: string;
  estado: boolean;
}

export interface CuentaDto {
  numeroCuenta: string;
  tipoCuenta: string;
  saldoInicial: number;
  estado: boolean;
  clienteId: number;
}

export interface MovimientoRequest {
  numeroCuenta: string;
  tipoMovimiento: string; // "Retiro" or "Deposito" (or positive/negative values)
  valor: number;
}

export interface MovimientoDto {
  id?: number;
  fecha: string;
  tipoMovimiento: string;
  valor: number;
  saldoResultante: number;
  numeroCuenta: string;
}

export interface GeneralResponseDto<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface ReporteItemDto {
  Fecha: string;
  Cliente: string;
  "Numero Cuenta": string;
  Tipo: string;
  "Saldo Inicial": number;
  Estado: boolean;
  Movimiento: number;
  "Saldo Disponible": number;
}
