package com.banco.banco_api.modules.cliente.service;

import com.banco.banco_api.modules.cliente.domain.ClienteEntity;
import com.banco.banco_api.modules.cliente.dto.ClienteDto;
import com.banco.banco_api.modules.cliente.repository.ClienteRepository;
import com.banco.banco_api.modules.cuenta.service.CuentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CuentaService cuentaService;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private ClienteEntity cliente;
    private ClienteDto clienteDto;

    @BeforeEach
    void setUp() {
        cliente = ClienteEntity.builder()
                .id(1L)
                .nombre("Jose Lema")
                .estado(true)
                .build();

        clienteDto = ClienteDto.builder()
                .id(1L)
                .nombre("Jose Lema")
                .estado(false) // Deactivated
                .build();
    }

    @Test
    void testUpdateClient_DeactivateAccountsOnDeactivation() {
        // Arrange
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(any(ClienteEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        ClienteDto result = clienteService.updateClient(1L, clienteDto);

        // Assert
        assertNotNull(result);
        assertFalse(result.getEstado());
        verify(cuentaService, times(1)).deactivateAccountsByClientId(1L);
        verify(clienteRepository, times(1)).save(any(ClienteEntity.class));
    }
}
