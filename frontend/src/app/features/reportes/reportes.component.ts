import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReporteService } from '../../core/services/reporte.service';
import { ClienteService } from '../../core/services/cliente.service';
import { ClienteDto, ReporteItemDto } from '../../core/models/banco.models';

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './reportes.component.html',
  styleUrl: './reportes.component.css'
})
export class ReportesComponent implements OnInit {
  private reporteService = inject(ReporteService);
  private clienteService = inject(ClienteService);
  private fb = inject(FormBuilder);

  // Filters state
  clientes = signal<ClienteDto[]>([]);
  filterForm!: FormGroup;
  isSubmitted = false;

  // Report results state
  reportData = signal<any | null>(null);
  detalles = signal<ReporteItemDto[]>([]);
  
  // Method to get current date for template
  newDate(): Date {
    return new Date();
  }
  
  // Aggregate Metrics
  totalDepositos = signal<number>(0);
  totalRetiros = signal<number>(0);
  balanceNeto = signal<number>(0);

  ngOnInit(): void {
    this.initForm();
    this.loadClientes();
  }

  initForm(): void {
    const today = new Date();
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(today.getDate() - 30);

    const formatDate = (date: Date) => date.toISOString().substring(0, 10);

    this.filterForm = this.fb.group({
      clienteId: ['', [Validators.required]],
      fechaInicio: [formatDate(thirtyDaysAgo), [Validators.required]],
      fechaFin: [formatDate(today), [Validators.required]]
    });
  }

  loadClientes(): void {
    this.clienteService.getAll().subscribe({
      next: (res) => {
        if (res.success) {
          this.clientes.set(res.data);
        }
      },
      error: (err) => {
        alert('Error al cargar clientes: ' + err.message);
      }
    });
  }

  generarReporte(): void {
    this.isSubmitted = true;
    if (this.filterForm.invalid) {
      return;
    }

    const { clienteId, fechaInicio, fechaFin } = this.filterForm.value;

    this.reporteService.getAccountStatement(fechaInicio, fechaFin, Number(clienteId)).subscribe({
      next: (res) => {
        if (res.success) {
          const data = res.data;
          this.reportData.set(data);
          
          const items: ReporteItemDto[] = data.detalles || [];
          this.detalles.set(items);
          this.calculateMetrics(items);
          
          alert('Reporte generado correctamente');
        }
      },
      error: (err) => {
        alert('Error al generar reporte: ' + err.message);
      }
    });
  }

  calculateMetrics(items: ReporteItemDto[]): void {
    let dep = 0;
    let ret = 0;

    items.forEach(item => {
      const val = item.Movimiento;
      if (val >= 0) {
        dep += val;
      } else {
        ret += Math.abs(val);
      }
    });

    this.totalDepositos.set(dep);
    this.totalRetiros.set(ret);
    this.balanceNeto.set(dep - ret);
  }

  descargarPdf(): void {
    if (!this.reportData()) {
      alert('Genere un reporte primero antes de descargarlo');
      return;
    }

    const base64Pdf = this.reportData().pdfBase64 || this.reportData().pdf;

    if (base64Pdf) {
      try {
        const linkSource = `data:application/pdf;base64,${base64Pdf}`;
        const downloadLink = document.createElement('a');
        const fileName = `Estado_Cuenta_${this.reportData().cliente}_${this.filterForm.value.fechaInicio}_a_${this.filterForm.value.fechaFin}.pdf`;
        
        downloadLink.href = linkSource;
        downloadLink.download = fileName;
        downloadLink.click();
        alert('PDF descargado con éxito');
      } catch (e) {
        alert('Error al decodificar el PDF del servidor.');
      }
    } else {
      alert('Abriendo asistente de impresión para guardar como PDF...');
      setTimeout(() => {
        window.print();
      }, 500);
    }
  }
}
