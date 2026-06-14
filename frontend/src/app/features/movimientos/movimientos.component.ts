import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MovimientoService } from '../../core/services/movimiento.service';
import { CuentaService } from '../../core/services/cuenta.service';
import { ClienteService } from '../../core/services/cliente.service';
import { MovimientoDto, CuentaDto, ClienteDto, MovimientoRequest } from '../../core/models/banco.models';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-movimientos',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './movimientos.component.html',
  styleUrl: './movimientos.component.css'
})
export class MovimientosComponent implements OnInit {
  private movimientoService = inject(MovimientoService);
  private cuentaService = inject(CuentaService);
  private clienteService = inject(ClienteService);
  private fb = inject(FormBuilder);

  // State
  movimientos = signal<MovimientoDto[]>([]);
  filteredMovimientos = signal<MovimientoDto[]>([]);
  cuentas = signal<CuentaDto[]>([]);
  
  // Lookup helpers
  clienteMap = new Map<number, string>();
  cuentaMap = new Map<string, CuentaDto>();

  searchQuery = signal<string>('');
  
  isModalOpen = false;
  transactionForm!: FormGroup;
  isSubmitted = false;
  backendErrorMsg = '';

  ngOnInit(): void {
    this.initForm();
    this.loadData();
  }

  initForm(): void {
    this.transactionForm = this.fb.group({
      numeroCuenta: ['', [Validators.required]],
      tipoMovimiento: ['', [Validators.required]], // 'Deposito' | 'Retiro'
      valor: [null, [Validators.required, Validators.min(0.01)]]
    });
  }

  loadData(): void {
    forkJoin({
      clientesRes: this.clienteService.getAll(),
      cuentasRes: this.cuentaService.getAll(),
      movsRes: this.movimientoService.getAll()
    }).subscribe({
      next: (res) => {
        if (res.clientesRes.success) {
          res.clientesRes.data.forEach(c => {
            if (c.id !== undefined) {
              this.clienteMap.set(c.id, c.nombre);
            }
          });
        }

        if (res.cuentasRes.success) {
          const activeCuentas = res.cuentasRes.data.filter(c => c.estado);
          this.cuentas.set(activeCuentas);
          
          res.cuentasRes.data.forEach(c => {
            this.cuentaMap.set(c.numeroCuenta, c);
          });
        }

        if (res.movsRes.success) {
          const sorted = res.movsRes.data.sort((a, b) => 
            new Date(b.fecha).getTime() - new Date(a.fecha).getTime()
          );
          this.movimientos.set(sorted);
          this.filterMovimientos();
        }
      },
      error: (err) => {
        alert('Error al cargar datos: ' + err.message);
      }
    });
  }

  getOwnerName(numeroCuenta: string): string {
    const cuenta = this.cuentaMap.get(numeroCuenta);
    if (cuenta) {
      return this.clienteMap.get(cuenta.clienteId) || 'Cliente Desconocido';
    }
    return 'Cuenta Desconocida';
  }

  onSearch(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    this.searchQuery.set(query);
    this.filterMovimientos();
  }

  filterMovimientos(): void {
    const query = this.searchQuery().toLowerCase().trim();
    if (!query) {
      this.filteredMovimientos.set(this.movimientos());
      return;
    }

    const filtered = this.movimientos().filter(m => {
      const owner = this.getOwnerName(m.numeroCuenta).toLowerCase();
      return m.numeroCuenta.toLowerCase().includes(query) || owner.includes(query);
    });
    this.filteredMovimientos.set(filtered);
  }

  openAddModal(): void {
    this.isSubmitted = false;
    this.backendErrorMsg = '';
    this.transactionForm.reset({ numeroCuenta: '', tipoMovimiento: '' });
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
  }

  onSubmit(): void {
    this.isSubmitted = true;
    this.backendErrorMsg = '';

    if (this.transactionForm.invalid) {
      return;
    }

    const { numeroCuenta, tipoMovimiento, valor } = this.transactionForm.value;
    const finalValue = tipoMovimiento === 'Retiro' ? -Math.abs(valor) : Math.abs(valor);

    const request: MovimientoRequest = {
      numeroCuenta,
      tipoMovimiento,
      valor: finalValue
    };

    this.movimientoService.create(request).subscribe({
      next: (res) => {
        if (res.success) {
          alert('Transacción ejecutada con éxito');
          this.loadData();
          this.closeModal();
        }
      },
      error: (err) => {
        this.backendErrorMsg = err.message;
        alert('Error de transacción: ' + err.message);
      }
    });
  }
}
