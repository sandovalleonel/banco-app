package com.banco.banco_api.modules.movimiento.web;

import com.banco.banco_api.modules.movimiento.dto.MovimientoDto;
import com.banco.banco_api.modules.movimiento.service.MovimientoService;
import com.banco.banco_api.modules.shared.constants.Constants;
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
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MovimientoController.class)
class MovimientoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovimientoService movimientoService;

    @Autowired
    private ObjectMapper objectMapper;

    private MovimientoDto movimientoDto;
    private MovimientoRequest request;

    @BeforeEach
    void setUp() {
        movimientoDto = MovimientoDto.builder()
                .id(1L)
                .fecha(LocalDateTime.now())
                .tipoMovimiento(Constants.TIPO_DEBITO)
                .valor(new BigDecimal("-200.00"))
                .saldoResultante(new BigDecimal("800.00"))
                .numeroCuenta("478758")
                .build();

        request = new MovimientoRequest();
        request.setNumeroCuenta("478758");
        request.setTipoMovimiento(Constants.TIPO_DEBITO);
        request.setValor(new BigDecimal("200.00"));
    }

    @Test
    void testCreateMovement_Success() throws Exception {
        // Arrange
        when(movimientoService.createMovement(eq("478758"), eq(Constants.TIPO_DEBITO), any(BigDecimal.class)))
                .thenReturn(movimientoDto);

        // Act & Assert
        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.data.id", is(1)))
                .andExpect(jsonPath("$.data.valor", is(-200.00)));

        verify(movimientoService, times(1)).createMovement(eq("478758"), eq(Constants.TIPO_DEBITO), any(BigDecimal.class));
    }

    @Test
    void testCreateMovement_BusinessRuleException_InsufficientBalance() throws Exception {
        // Arrange
        when(movimientoService.createMovement(eq("478758"), eq(Constants.TIPO_DEBITO), any(BigDecimal.class)))
                .thenThrow(new BusinessRuleException("Saldo no disponible"));

        // Act & Assert
        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success", is(false)))
                .andExpect(jsonPath("$.message", is("Saldo no disponible")))
                .andExpect(jsonPath("$.data", nullValue()));
    }
}
