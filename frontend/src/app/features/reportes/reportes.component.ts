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

    const formatDate = (date: Date) => {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, '0');
      const day = String(date.getDate()).padStart(2, '0');
      return `${year}-${month}-${day}`;
    };

    this.filterForm = this.fb.group({
      clienteId: ['', [Validators.required]],
      fechaInicio: [formatDate(thirtyDaysAgo), [Validators.required]],
      fechaFin: [formatDate(today), [Validators.required]]
    }, { validators: this.dateRangeValidator });
  }

  dateRangeValidator = (group: FormGroup): {[key: string]: any} | null => {
    const inicio = group.get('fechaInicio')?.value;
    const fin = group.get('fechaFin')?.value;
    if (inicio && fin && new Date(inicio) > new Date(fin)) {
      return { rangeInvalid: true };
    }
    return null;
  };

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
          this.calculateMetrics(data.cuentas || []);
          alert('Reporte generado correctamente');
        }
      },
      error: (err) => {
        alert('Error al generar reporte: ' + err.message);
      }
    });
  }

  calculateMetrics(cuentas: any[]): void {
    let dep = 0;
    let ret = 0;

    cuentas.forEach(cuenta => {
      if (cuenta.movimientos) {
        cuenta.movimientos.forEach((mov: any) => {
          const val = Number(mov.valor);
          const tipo = (mov.tipoMovimiento || '').toLowerCase();
          const isDebit = tipo.includes('deb') || tipo.includes('ret');
          if (isDebit) {
            ret += Math.abs(val);
          } else {
            dep += Math.abs(val);
          }
        });
      }
    });

    this.totalDepositos.set(dep);
    this.totalRetiros.set(ret);
    this.balanceNeto.set(dep - ret);
  }

  calculateSaldoInicial(cuenta: any): number {
    if (!cuenta.movimientos || cuenta.movimientos.length === 0) {
      return cuenta.saldoActual;
    }
    // Sort movements by date ascending to find the oldest one
    const sorted = [...cuenta.movimientos].sort((a, b) => new Date(a.fecha).getTime() - new Date(b.fecha).getTime());
    const oldest = sorted[0];
    const tipo = (oldest.tipoMovimiento || '').toLowerCase();
    const isDebit = tipo.includes('deb') || tipo.includes('ret');
    if (isDebit) {
      return oldest.saldoResultante + Math.abs(oldest.valor);
    } else {
      return oldest.saldoResultante - Math.abs(oldest.valor);
    }
  }

  calculateSaldoActual(cuenta: any): number {
    if (!cuenta.movimientos || cuenta.movimientos.length === 0) {
      return cuenta.saldoActual;
    }
    // Sort movements by date ascending to find the newest one
    const sorted = [...cuenta.movimientos].sort((a, b) => new Date(a.fecha).getTime() - new Date(b.fecha).getTime());
    const newest = sorted[sorted.length - 1];
    return newest.saldoResultante;
  }

  descargarPdf(): void {
    const data = this.reportData();
    if (!data || !data.pdfBase64) {
      alert('No hay archivo PDF disponible para este reporte');
      return;
    }

    try {
      const base64Pdf = data.pdfBase64;
      const linkSource = `data:application/pdf;base64,${base64Pdf}`;
      const downloadLink = document.createElement('a');
      const fileName = `Estado_Cuenta_${(data.cliente || 'Reporte').replace(/\s+/g, '_')}_${this.filterForm.value.fechaInicio}_a_${this.filterForm.value.fechaFin}.pdf`;
      
      downloadLink.href = linkSource;
      downloadLink.download = fileName;
      downloadLink.click();
    } catch (e) {
      alert('Error al descargar el PDF: ' + (e as Error).message);
    }
  }
}
