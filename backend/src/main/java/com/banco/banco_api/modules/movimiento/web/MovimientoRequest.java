package com.banco.banco_api.modules.movimiento.web;

import java.math.BigDecimal;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MovimientoRequest {

    @NotBlank(message = "El número de cuenta es obligatorio")
    private String numeroCuenta;

    @NotBlank(message = "El tipo de movimiento es obligatorio (Débito o Crédito)")
    private String tipoMovimiento;

    @NotNull(message = "El valor es obligatorio")
    private BigDecimal valor;
}
