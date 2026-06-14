package com.banco.banco_api.modules.cuenta.web;

import com.banco.banco_api.modules.cuenta.dto.CuentaDto;
import com.banco.banco_api.modules.cuenta.service.CuentaService;
import com.banco.banco_api.modules.shared.exception.BusinessRuleException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CuentaController.class)
class CuentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CuentaService cuentaService;

    @Autowired
    private ObjectMapper objectMapper;

    private CuentaDto cuentaDto;

    @BeforeEach
    void setUp() {
        cuentaDto = CuentaDto.builder()
                .numeroCuenta("478758")
                .tipoCuenta("Ahorro")
                .saldoInicial(new BigDecimal("2000.00"))
                .estado(true)
                .clienteId(1L)
                .build();
    }

    @Test
    void testCreateAccount_Success() throws Exception {
        // Arrange
        when(cuentaService.createAccount(any(CuentaDto.class))).thenReturn(cuentaDto);

        // Act & Assert
        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cuentaDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.numeroCuenta", is("478758")));

        verify(cuentaService, times(1)).createAccount(any(CuentaDto.class));
    }

    @Test
    void testCreateAccount_Duplicate() throws Exception {
        // Arrange
        when(cuentaService.createAccount(any(CuentaDto.class)))
                .thenThrow(new BusinessRuleException("Ya existe una cuenta con el número: 478758"));

        // Act & Assert
        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cuentaDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", containsString("Ya existe una cuenta")));
    }
}
