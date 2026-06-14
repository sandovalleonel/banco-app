package com.banco.banco_api.modules.movimiento.web;

import com.banco.banco_api.modules.movimiento.dto.MovimientoDto;
import com.banco.banco_api.modules.movimiento.service.MovimientoService;
import com.banco.banco_api.modules.shared.dto.GeneralResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/movimientos")
@CrossOrigin(origins = "*")
@Slf4j
public class MovimientoController {

    private final MovimientoService movimientoService;

    @Autowired
    public MovimientoController(MovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

    @GetMapping
    public ResponseEntity<GeneralResponseDto<List<MovimientoDto>>> getAllMovements() {
        log.info("Request: Get all movements");
        List<MovimientoDto> movements = movimientoService.getAllMovements();
        GeneralResponseDto<List<MovimientoDto>> response = GeneralResponseDto.<List<MovimientoDto>>builder()
                .success(true)
                .message("Movimientos recuperados exitosamente")
                .data(movements)
                .build();
        log.info("Response: Get all movements -> {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<GeneralResponseDto<MovimientoDto>> createMovement(@Valid @RequestBody MovimientoRequest request) {
        log.info("Request: Create movement -> {}", request);
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
        log.info("Response: Create movement -> {}", response);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponseDto<MovimientoDto>> updateMovement(@PathVariable Long id, @Valid @RequestBody MovimientoRequest request) {
        log.info("Request: Update movement with id: {} -> {}", id, request);
        MovimientoDto updated = movimientoService.updateMovement(id, request);
        GeneralResponseDto<MovimientoDto> response = GeneralResponseDto.<MovimientoDto>builder()
                .success(true)
                .message("Movimiento actualizado exitosamente")
                .data(updated)
                .build();
        log.info("Response: Update movement with id: {} -> {}", id, response);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponseDto<Void>> deleteMovement(@PathVariable Long id) {
        log.info("Request: Delete movement with id: {}", id);
        movimientoService.deleteMovement(id);
        GeneralResponseDto<Void> response = GeneralResponseDto.<Void>builder()
                .success(true)
                .message("Movimiento eliminado exitosamente")
                .data(null)
                .build();
        log.info("Response: Delete movement with id: {} -> {}", id, response);
        return ResponseEntity.ok(response);
    }
}
