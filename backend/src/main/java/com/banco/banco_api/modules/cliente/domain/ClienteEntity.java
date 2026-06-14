package com.banco.banco_api.modules.cliente.domain;

import javax.persistence.*;
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
    @javax.validation.constraints.NotBlank(message = "La contraseña es obligatoria")
    private String contrasena;

    @Column(nullable = false)
    @javax.validation.constraints.NotNull(message = "El estado es obligatorio")
    private Boolean estado;
}
