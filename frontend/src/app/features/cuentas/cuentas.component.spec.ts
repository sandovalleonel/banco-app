import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ReactiveFormsModule } from '@angular/forms';
import { CuentasComponent } from './cuentas.component';
import { CuentaService } from '../../core/services/cuenta.service';
import { ClienteService } from '../../core/services/cliente.service';
import { of } from 'rxjs';
import { CuentaDto, ClienteDto } from '../../core/models/banco.models';

describe('CuentasComponent', () => {
  let component: CuentasComponent;
  let fixture: ComponentFixture<CuentasComponent>;
  let mockCuentaService: jasmine.SpyObj<CuentaService>;
  let mockClienteService: jasmine.SpyObj<ClienteService>;

  const mockClientes: ClienteDto[] = [
    {
      id: 1,
      identificacion: '1711111111',
      nombre: 'Jose Lema',
      genero: 'Masculino',
      edad: 30,
      direccion: 'Otavalo',
      telefono: '098254785',
      contrasena: '1234',
      estado: true
    }
  ];

  const mockCuentas: CuentaDto[] = [
    {
      numeroCuenta: '478758',
      tipoCuenta: 'Ahorro',
      saldoInicial: 2000.00,
      saldoActual: 2000.00,
      estado: true,
      clienteId: 1
    }
  ];

  beforeEach(async () => {
    mockCuentaService = jasmine.createSpyObj('CuentaService', ['getAll', 'create', 'update', 'delete']);
    mockClienteService = jasmine.createSpyObj('ClienteService', ['getAll']);

    // Mock initial data load
    mockClienteService.getAll.and.returnValue(of({ success: true, message: 'Clientes cargados', data: mockClientes }));
    mockCuentaService.getAll.and.returnValue(of({ success: true, message: 'Cuentas cargadas', data: mockCuentas }));

    await TestBed.configureTestingModule({
      imports: [CuentasComponent, ReactiveFormsModule],
      providers: [
        { provide: CuentaService, useValue: mockCuentaService },
        { provide: ClienteService, useValue: mockClienteService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CuentasComponent);
    component = fixture.componentInstance;
    fixture.detectChanges(); // Triggers ngOnInit()
  });

  it('debería inicializar el componente y cargar clientes y cuentas', () => {
    expect(component).toBeTruthy();
    expect(mockClienteService.getAll).toHaveBeenCalled();
    expect(mockCuentaService.getAll).toHaveBeenCalled();
    expect(component.cuentas().length).toBe(1);
    expect(component.clientes().length).toBe(1);
  });

  it('debería crear una cuenta nueva exitosamente al enviar el formulario válido', () => {
    // 1. Espiar el alert global del navegador
    spyOn(window, 'alert');

    // 2. Abrir el modal de creación
    component.openAddModal();
    expect(component.isModalOpen).toBeTrue();
    expect(component.isEditMode).toBeFalse();

    // 3. Llenar los valores del formulario
    component.accountForm.setValue({
      numeroCuenta: '123456',
      tipoCuenta: 'Corriente',
      saldoInicial: 500.00,
      clienteId: 1,
      estado: true
    });

    const newAccountResponse: CuentaDto = {
      numeroCuenta: '123456',
      tipoCuenta: 'Corriente',
      saldoInicial: 500.00,
      saldoActual: 500.00,
      estado: true,
      clienteId: 1
    };

    // 4. Configurar el mock para retornar éxito en la creación
    mockCuentaService.create.and.returnValue(of({
      success: true,
      message: 'Cuenta creada exitosamente',
      data: newAccountResponse
    }));

    // 5. Enviar el formulario
    component.onSubmit();

    // 6. Verificar que se llamó al servicio y se mostró la alerta adecuada
    expect(mockCuentaService.create).toHaveBeenCalledWith(jasmine.objectContaining({
      numeroCuenta: '123456',
      tipoCuenta: 'Corriente',
      saldoInicial: 500.00,
      estado: true,
      clienteId: 1
    }));
    expect(window.alert).toHaveBeenCalledWith('Cuenta creada correctamente');
    expect(component.isModalOpen).toBeFalse();
  });

  it('debería actualizar una cuenta existente exitosamente al enviar el formulario de edición válido', () => {
    // 1. Espiar el alert global del navegador
    spyOn(window, 'alert');

    // 2. Seleccionar la cuenta existente y abrir modal de edición
    const cuentaAEditar = mockCuentas[0];
    component.openEditModal(cuentaAEditar);
    expect(component.isModalOpen).toBeTrue();
    expect(component.isEditMode).toBeTrue();
    expect(component.selectedAccountNum).toBe('478758');

    // 3. Modificar algún campo (ej. el tipo de cuenta)
    // El formulario tiene numeroCuenta y saldoInicial deshabilitados en modo edición,
    // pero podemos cambiar tipoCuenta, clienteId y estado.
    component.accountForm.patchValue({
      tipoCuenta: 'Corriente'
    });

    const updatedAccountResponse: CuentaDto = {
      ...cuentaAEditar,
      tipoCuenta: 'Corriente'
    };

    // 4. Configurar el mock para retornar éxito en la actualización
    mockCuentaService.update.and.returnValue(of({
      success: true,
      message: 'Cuenta actualizada exitosamente',
      data: updatedAccountResponse
    }));

    // 5. Enviar el formulario
    component.onSubmit();

    // 6. Verificar que se llamó al servicio con los datos raw correctos
    expect(mockCuentaService.update).toHaveBeenCalledWith('478758', jasmine.objectContaining({
      numeroCuenta: '478758',
      tipoCuenta: 'Corriente',
      saldoInicial: 2000.00,
      estado: true,
      clienteId: 1
    }));
    expect(window.alert).toHaveBeenCalledWith('Cuenta actualizada correctamente');
    expect(component.isModalOpen).toBeFalse();
  });
});
