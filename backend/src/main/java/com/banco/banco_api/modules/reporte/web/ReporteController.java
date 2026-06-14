package com.banco.banco_api.modules.reporte.web;

import com.banco.banco_api.modules.reporte.service.ReporteService;
import com.banco.banco_api.modules.shared.dto.GeneralResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/reportes")
@CrossOrigin(origins = "*")
public class ReporteController {

    private final ReporteService reporteService;

    @Autowired
    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping
    public ResponseEntity<GeneralResponseDto<Map<String, Object>>> getAccountStatement(
            @RequestParam("fechaInicio") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam("fechaFin") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam("cliente") Long clienteId) {
        
        Map<String, Object> report = reporteService.generateAccountStatement(clienteId, fechaInicio, fechaFin);
        GeneralResponseDto<Map<String, Object>> response = GeneralResponseDto.<Map<String, Object>>builder()
                .success(true)
                .message("Reporte de estado de cuenta generado exitosamente")
                .data(report)
                .build();
        return ResponseEntity.ok(response);
    }
}
