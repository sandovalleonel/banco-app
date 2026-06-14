import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CuentaService } from '../../core/services/cuenta.service';
import { ClienteService } from '../../core/services/cliente.service';
import { CuentaDto, ClienteDto } from '../../core/models/banco.models';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-cuentas',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './cuentas.component.html',
  styleUrl: './cuentas.component.css'
})
export class CuentasComponent implements OnInit {
  private cuentaService = inject(CuentaService);
  private clienteService = inject(ClienteService);
  private fb = inject(FormBuilder);

  // State
  cuentas = signal<CuentaDto[]>([]);
  filteredCuentas = signal<CuentaDto[]>([]);
  clientes = signal<ClienteDto[]>([]);
  searchQuery = signal<string>('');
  
  // Client mapping helper
  clienteMap = new Map<number, string>();

  isModalOpen = false;
  isEditMode = false;
  selectedAccountNum?: string;
  
  accountForm!: FormGroup;
  isSubmitted = false;

  ngOnInit(): void {
    this.initForm();
    this.loadData();
  }

  initForm(): void {
    this.accountForm = this.fb.group({
      numeroCuenta: ['', [Validators.required, Validators.pattern('^[0-9]+$'), Validators.minLength(4), Validators.maxLength(12)]],
      tipoCuenta: ['', [Validators.required]],
      saldoInicial: [0, [Validators.required, Validators.min(0)]],
      clienteId: ['', [Validators.required]],
      estado: [true, [Validators.required]]
    });
  }

  loadData(): void {
    forkJoin({
      clientesRes: this.clienteService.getAll(),
      cuentasRes: this.cuentaService.getAll()
    }).subscribe({
      next: (res) => {
        if (res.clientesRes.success) {
          this.clientes.set(res.clientesRes.data);
          
          this.clienteMap.clear();
          res.clientesRes.data.forEach(c => {
            if (c.id !== undefined) {
              this.clienteMap.set(c.id, c.nombre);
            }
          });
        }
        
        if (res.cuentasRes.success) {
          this.cuentas.set(res.cuentasRes.data);
          this.filterCuentas();
        }
      },
      error: (err) => {
        alert('Error al cargar datos: ' + err.message);
      }
    });
  }

  getClienteNombre(id: number): string {
    return this.clienteMap.get(id) || `Cliente #${id}`;
  }

  onSearch(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    this.searchQuery.set(query);
    this.filterCuentas();
  }

  filterCuentas(): void {
    const query = this.searchQuery().toLowerCase().trim();
    if (!query) {
      this.filteredCuentas.set(this.cuentas());
      return;
    }

    const filtered = this.cuentas().filter(c => {
      const ownerName = this.getClienteNombre(c.clienteId).toLowerCase();
      return c.numeroCuenta.toLowerCase().includes(query) || ownerName.includes(query);
    });
    this.filteredCuentas.set(filtered);
  }

  openAddModal(): void {
    this.isEditMode = false;
    this.isSubmitted = false;
    this.selectedAccountNum = undefined;
    this.accountForm.reset({ estado: true, tipoCuenta: '', clienteId: '' });
    this.accountForm.get('numeroCuenta')?.enable();
    this.accountForm.get('saldoInicial')?.enable();
    this.isModalOpen = true;
  }

  openEditModal(cuenta: CuentaDto): void {
    this.isEditMode = true;
    this.isSubmitted = false;
    this.selectedAccountNum = cuenta.numeroCuenta;
    
    this.accountForm.patchValue({
      numeroCuenta: cuenta.numeroCuenta,
      tipoCuenta: cuenta.tipoCuenta,
      saldoInicial: cuenta.saldoInicial,
      clienteId: cuenta.clienteId,
      estado: cuenta.estado
    });

    this.accountForm.get('numeroCuenta')?.disable();
    this.accountForm.get('saldoInicial')?.disable();
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
  }

  onSubmit(): void {
    this.isSubmitted = true;
    if (this.accountForm.invalid) {
      return;
    }

    const accountData: CuentaDto = this.accountForm.getRawValue();

    if (this.isEditMode && this.selectedAccountNum !== undefined) {
      this.cuentaService.update(this.selectedAccountNum, accountData).subscribe({
        next: (res) => {
          if (res.success) {
            alert('Cuenta actualizada correctamente');
            this.loadData();
            this.closeModal();
          }
        },
        error: (err) => {
          alert('Error al actualizar cuenta: ' + err.message);
        }
      });
    } else {
      this.cuentaService.create(accountData).subscribe({
        next: (res) => {
          if (res.success) {
            alert('Cuenta creada correctamente');
            this.loadData();
            this.closeModal();
          }
        },
        error: (err) => {
          alert('Error al crear cuenta: ' + err.message);
        }
      });
    }
  }

  deleteCuenta(numeroCuenta: string): void {
    if (confirm(`¿Está seguro de que desea eliminar la cuenta N° ${numeroCuenta}?`)) {
      this.cuentaService.delete(numeroCuenta).subscribe({
        next: (res) => {
          if (res.success) {
            alert('Cuenta eliminada correctamente');
            this.loadData();
          }
        },
        error: (err) => {
          alert('Error al eliminar cuenta: ' + err.message);
        }
      });
    }
  }
}
