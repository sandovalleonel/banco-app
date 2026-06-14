package com.banco.banco_api.modules.movimiento.web;

import com.banco.banco_api.modules.movimiento.dto.MovimientoDto;
import com.banco.banco_api.modules.movimiento.service.MovimientoService;
import com.banco.banco_api.modules.shared.dto.GeneralResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/movimientos")
@CrossOrigin(origins = "*")
public class MovimientoController {

    private final MovimientoService movimientoService;

    @Autowired
    public MovimientoController(MovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

    @GetMapping
    public ResponseEntity<GeneralResponseDto<List<MovimientoDto>>> getAllMovements() {
        List<MovimientoDto> movements = movimientoService.getAllMovements();
        GeneralResponseDto<List<MovimientoDto>> response = GeneralResponseDto.<List<MovimientoDto>>builder()
                .success(true)
                .message("Movimientos recuperados exitosamente")
                .data(movements)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<GeneralResponseDto<MovimientoDto>> createMovement(@Valid @RequestBody MovimientoRequest request) {
        MovimientoDto created = movimientoService.createMovement(
                request.getNumeroCuenta(),
                request.getTipoMovimiento(),
                request.getValor()
        );
        GeneralResponseDto<MovimientoDto> response = GeneralResponseDto.<MovimientoDto>builder()
                .success(true)
                .message("Movimiento creado exitosamente")
                .data(created)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
