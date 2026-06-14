package com.banco.banco_api.modules.cliente.domain;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "persona", indexes = {
    @Index(name = "idx_persona_identificacion", columnList = "identificacion", unique = true)
})
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PersonaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "La identificación es obligatoria")
    @Size(min = 5, max = 10, message = "La identificación debe tener entre 5 y 10 caracteres")
    @Pattern(  regexp = "^\\S+$",  message = "La identificación no puede contener espacios en blanco en ningún lado")
    private String identificacion;

    @Column(nullable = false)
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String genero;

    private Integer edad;

    private String direccion;

    private String telefono;
}
