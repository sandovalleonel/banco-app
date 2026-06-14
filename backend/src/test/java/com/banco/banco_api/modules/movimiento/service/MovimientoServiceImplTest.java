package com.banco.banco_api.modules.movimiento.service;

import com.banco.banco_api.modules.cliente.domain.ClienteEntity;
import com.banco.banco_api.modules.configuracion.repository.ConfiguracionRepository;
import com.banco.banco_api.modules.cuenta.domain.CuentaEntity;
import com.banco.banco_api.modules.cuenta.repository.CuentaRepository;
import com.banco.banco_api.modules.movimiento.domain.MovimientoEntity;
import com.banco.banco_api.modules.movimiento.dto.MovimientoDto;
import com.banco.banco_api.modules.movimiento.repository.MovimientoRepository;
import com.banco.banco_api.modules.shared.constants.Constants;
import com.banco.banco_api.modules.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovimientoServiceImplTest {

    @Mock
    private MovimientoRepository movimientoRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private ConfiguracionRepository configuracionRepository;

    @InjectMocks
    private MovimientoServiceImpl movimientoService;

    private ClienteEntity cliente;
    private CuentaEntity cuenta;

    @BeforeEach
    void setUp() {
        cliente = ClienteEntity.builder()
                .id(1L)
                .nombre("Jose Lema")
                .estado(true)
                .build();

        cuenta = CuentaEntity.builder()
                .numeroCuenta("478758")
                .saldoInicial(new BigDecimal("1000.00"))
                .estado(true)
                .cliente(cliente)
                .build();
    }

    @Test
    void testCreateMovement_Success_Debit() {
        // Arrange
        when(cuentaRepository.findById("478758")).thenReturn(Optional.of(cuenta));
        when(configuracionRepository.findById("LIMITE_DIARIO_RETIRO")).thenReturn(Optional.empty());
        when(movimientoRepository.sumDebitsByClienteAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);

        MovimientoEntity savedEntity = MovimientoEntity.builder()
                .id(100L)
                .tipoMovimiento(Constants.TIPO_DEBITO)
                .valor(new BigDecimal("-200.00"))
                .saldo(new BigDecimal("800.00"))
                .cuenta(cuenta)
                .build();
        when(movimientoRepository.save(any(MovimientoEntity.class))).thenReturn(savedEntity);

        // Act
        MovimientoDto result = movimientoService.createMovement("478758", Constants.TIPO_DEBITO, new BigDecimal("200.00"));

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("-200.00"), result.getValor());
        assertEquals(new BigDecimal("800.00"), result.getSaldoResultante());
        verify(cuentaRepository, times(1)).save(cuenta);
    }

    @Test
    void testCreateMovement_InsufficientBalance() {
        // Arrange
        when(cuentaRepository.findById("478758")).thenReturn(Optional.of(cuenta));

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            movimientoService.createMovement("478758", Constants.TIPO_DEBITO, new BigDecimal("1200.00"));
        });

        assertEquals("Saldo no disponible", exception.getMessage());
        verify(cuentaRepository, never()).save(any(CuentaEntity.class));
    }

    @Test
    void testCreateMovement_DailyLimitExceeded() {
        // Arrange
        when(cuentaRepository.findById("478758")).thenReturn(Optional.of(cuenta));
        when(configuracionRepository.findById("LIMITE_DIARIO_RETIRO")).thenReturn(Optional.empty());
        // sumDebitsByClienteAndDateRange returns today's debits as -$800.00
        when(movimientoRepository.sumDebitsByClienteAndDateRange(anyLong(), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("-800.00"));

        // Act & Assert (Debit of $300 + $800 = $1100, which exceeds $1000 limit)
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            movimientoService.createMovement("478758", Constants.TIPO_DEBITO, new BigDecimal("300.00"));
        });

        assertEquals("Cupo diario Excedido", exception.getMessage());
        verify(cuentaRepository, never()).save(any(CuentaEntity.class));
    }
}
