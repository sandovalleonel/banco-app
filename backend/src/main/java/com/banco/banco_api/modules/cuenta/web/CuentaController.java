package com.banco.banco_api.modules.cuenta.web;

import com.banco.banco_api.modules.cuenta.dto.CuentaDto;
import com.banco.banco_api.modules.cuenta.service.CuentaService;
import com.banco.banco_api.modules.shared.dto.GeneralResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/cuentas")
@CrossOrigin(origins = "*")
public class CuentaController {

    private final CuentaService cuentaService;

    @Autowired
    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = cuentaService;
    }

    @GetMapping
    public ResponseEntity<GeneralResponseDto<List<CuentaDto>>> getAllAccounts() {
        List<CuentaDto> accounts = cuentaService.getAllAccounts();
        GeneralResponseDto<List<CuentaDto>> response = GeneralResponseDto.<List<CuentaDto>>builder()
                .success(true)
                .message("Cuentas recuperadas exitosamente")
                .data(accounts)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{numeroCuenta}")
    public ResponseEntity<GeneralResponseDto<CuentaDto>> getAccountByNumber(@PathVariable String numeroCuenta) {
        CuentaDto account = cuentaService.getAccountByNumber(numeroCuenta);
        GeneralResponseDto<CuentaDto> response = GeneralResponseDto.<CuentaDto>builder()
                .success(true)
                .message("Cuenta recuperada exitosamente")
                .data(account)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<GeneralResponseDto<CuentaDto>> createAccount(@Valid @RequestBody CuentaDto accountDto) {
        CuentaDto created = cuentaService.createAccount(accountDto);
        GeneralResponseDto<CuentaDto> response = GeneralResponseDto.<CuentaDto>builder()
                .success(true)
                .message("Cuenta creada exitosamente")
                .data(created)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{numeroCuenta}")
    public ResponseEntity<GeneralResponseDto<CuentaDto>> updateAccount(@PathVariable String numeroCuenta, @Valid @RequestBody CuentaDto accountDto) {
        CuentaDto updated = cuentaService.updateAccount(numeroCuenta, accountDto);
        GeneralResponseDto<CuentaDto> response = GeneralResponseDto.<CuentaDto>builder()
                .success(true)
                .message("Cuenta actualizada exitosamente")
                .data(updated)
                .build();
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{numeroCuenta}")
    public ResponseEntity<GeneralResponseDto<Void>> deleteAccount(@PathVariable String numeroCuenta) {
        cuentaService.deleteAccount(numeroCuenta);
        GeneralResponseDto<Void> response = GeneralResponseDto.<Void>builder()
                .success(true)
                .message("Cuenta eliminada exitosamente")
                .data(null)
                .build();
        return ResponseEntity.ok(response);
    }
}
