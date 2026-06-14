package com.banco.banco_api.modules.cliente.domain;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "cliente")
@PrimaryKeyJoinColumn(name = "cliente_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ClienteEntity extends PersonaEntity {

    @Column(name = "contrasena", nullable = false)
    @NotBlank(message = "La contraseña es obligatoria")
    private String contrasena;

    @Column(nullable = false)
    @NotNull(message = "El estado es obligatorio")
    private Boolean estado;
}
