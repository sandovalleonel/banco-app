package com.banco.banco_api.modules.cuenta.domain;

import com.banco.banco_api.modules.cliente.domain.ClienteEntity;
import java.math.BigDecimal;
import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "cuenta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CuentaEntity {

    @Id
    @Column(name = "numero_cuenta", nullable = false, length = 50)
    @NotBlank(message = "El número de cuenta es obligatorio")
    private String numeroCuenta;

    @Column(name = "tipo_cuenta", nullable = false, length = 20)
    @NotBlank(message = "El tipo de cuenta es obligatorio")
    private String tipoCuenta; // "Ahorro" o "Corriente"

    @Column(name = "saldo_inicial", nullable = false, precision = 12, scale = 2)
    @NotNull(message = "El saldo inicial es obligatorio")
    private BigDecimal saldoInicial;

    @Column(name = "saldo_actual", nullable = false, precision = 12, scale = 2)
    @NotNull(message = "El saldo actual es obligatorio")
    private BigDecimal saldoActual;

    @Column(nullable = false)
    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    @NotNull(message = "El cliente es obligatorio")
    private ClienteEntity cliente;
}
