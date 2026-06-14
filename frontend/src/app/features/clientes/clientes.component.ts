import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ClienteService } from '../../core/services/cliente.service';
import { ClienteDto } from '../../core/models/banco.models';

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './clientes.component.html',
  styleUrl: './clientes.component.css'
})
export class ClientesComponent implements OnInit {
  private clienteService = inject(ClienteService);
  private fb = inject(FormBuilder);

  // State
  clientes = signal<ClienteDto[]>([]);
  filteredClientes = signal<ClienteDto[]>([]);
  searchQuery = signal<string>('');
  
  isModalOpen = false;
  isEditMode = false;
  selectedClientId?: number;
  
  clientForm!: FormGroup;
  isSubmitted = false;

  ngOnInit(): void {
    this.initForm();
    this.loadClientes();
  }

  initForm(): void {
    this.clientForm = this.fb.group({
      nombre: ['', [Validators.required, Validators.minLength(3)]],
      identificacion: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(20)]],
      genero: ['', [Validators.required]],
      edad: [null, [Validators.required, Validators.min(1), Validators.max(120)]],
      direccion: ['', [Validators.required]],
      telefono: ['', [Validators.required]],
      contrasena: ['', [Validators.required, Validators.minLength(4)]],
      estado: [true, [Validators.required]]
    });
  }

  loadClientes(): void {
    this.clienteService.getAll().subscribe({
      next: (res) => {
        if (res.success) {
          this.clientes.set(res.data);
          this.filterClientes();
        }
      },
      error: (err) => {
        alert('Error al cargar clientes: ' + err.message);
      }
    });
  }

  onSearch(event: Event): void {
    const query = (event.target as HTMLInputElement).value;
    this.searchQuery.set(query);
    this.filterClientes();
  }

  filterClientes(): void {
    const query = this.searchQuery().toLowerCase().trim();
    if (!query) {
      this.filteredClientes.set(this.clientes());
      return;
    }

    const filtered = this.clientes().filter(c => 
      c.nombre.toLowerCase().includes(query) || 
      c.identificacion.toLowerCase().includes(query)
    );
    this.filteredClientes.set(filtered);
  }

  openAddModal(): void {
    this.isEditMode = false;
    this.isSubmitted = false;
    this.selectedClientId = undefined;
    this.clientForm.reset({ estado: true, genero: '' });
    this.isModalOpen = true;
  }

  openEditModal(cliente: ClienteDto): void {
    this.isEditMode = true;
    this.isSubmitted = false;
    this.selectedClientId = cliente.id;
    this.clientForm.patchValue({
      nombre: cliente.nombre,
      identificacion: cliente.identificacion,
      genero: cliente.genero,
      edad: cliente.edad,
      direccion: cliente.direccion,
      telefono: cliente.telefono,
      contrasena: cliente.contrasena,
      estado: cliente.estado
    });
    this.isModalOpen = true;
  }

  closeModal(): void {
    this.isModalOpen = false;
  }

  onSubmit(): void {
    this.isSubmitted = true;
    if (this.clientForm.invalid) {
      return;
    }

    const clientData: ClienteDto = this.clientForm.value;

    if (this.isEditMode && this.selectedClientId !== undefined) {
      this.clienteService.update(this.selectedClientId, clientData).subscribe({
        next: (res) => {
          if (res.success) {
            alert('Cliente actualizado correctamente');
            this.loadClientes();
            this.closeModal();
          }
        },
        error: (err) => {
          alert('Error al actualizar cliente: ' + err.message);
        }
      });
    } else {
      this.clienteService.create(clientData).subscribe({
        next: (res) => {
          if (res.success) {
            alert('Cliente creado correctamente');
            this.loadClientes();
            this.closeModal();
          }
        },
        error: (err) => {
          alert('Error al crear cliente: ' + err.message);
        }
      });
    }
  }

  deleteCliente(id: number): void {
    if (confirm('¿Está seguro de que desea eliminar este cliente?')) {
      this.clienteService.delete(id).subscribe({
        next: (res) => {
          if (res.success) {
            alert('Cliente eliminado correctamente');
            this.loadClientes();
          }
        },
        error: (err) => {
          alert('Error al eliminar cliente: ' + err.message);
        }
      });
    }
  }
}
