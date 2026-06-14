package com.banco.banco_api.modules.cuenta.service;

import com.banco.banco_api.modules.cliente.domain.ClienteEntity;
import com.banco.banco_api.modules.cliente.repository.ClienteRepository;
import com.banco.banco_api.modules.cuenta.domain.CuentaEntity;
import com.banco.banco_api.modules.cuenta.dto.CuentaDto;
import com.banco.banco_api.modules.cuenta.repository.CuentaRepository;
import com.banco.banco_api.modules.shared.exception.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaServiceImplTest {

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private CuentaServiceImpl cuentaService;

    private ClienteEntity cliente;
    private CuentaDto cuentaDto;
    private CuentaEntity cuentaEntity;

    @BeforeEach
    void setUp() {
        cliente = ClienteEntity.builder()
                .id(1L)
                .nombre("Jose Lema")
                .estado(true)
                .build();

        cuentaDto = CuentaDto.builder()
                .numeroCuenta("478758")
                .tipoCuenta("Ahorro")
                .saldoInicial(new BigDecimal("2000.00"))
                .estado(true)
                .clienteId(1L)
                .build();

        cuentaEntity = CuentaEntity.builder()
                .numeroCuenta("478758")
                .tipoCuenta("Ahorro")
                .saldoInicial(new BigDecimal("2000.00"))
                .estado(true)
                .cliente(cliente)
                .build();
    }

    @Test
    void testCreateAccount_Success() {
        // Arrange
        when(cuentaRepository.existsById("478758")).thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(cuentaRepository.save(any(CuentaEntity.class))).thenReturn(cuentaEntity);

        // Act
        CuentaDto result = cuentaService.createAccount(cuentaDto);

        // Assert
        assertNotNull(result);
        assertEquals("478758", result.getNumeroCuenta());
        assertEquals(1L, result.getClienteId());
        verify(cuentaRepository, times(1)).save(any(CuentaEntity.class));
    }

    @Test
    void testCreateAccount_AlreadyExists() {
        // Arrange
        when(cuentaRepository.existsById("478758")).thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            cuentaService.createAccount(cuentaDto);
        });

        assertEquals("Ya existe una cuenta con el número: 478758", exception.getMessage());
        verify(cuentaRepository, never()).save(any(CuentaEntity.class));
    }
}
