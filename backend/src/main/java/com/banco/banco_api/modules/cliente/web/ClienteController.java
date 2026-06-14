package com.banco.banco_api.modules.cliente.web;

import com.banco.banco_api.modules.cliente.dto.ClienteDto;
import com.banco.banco_api.modules.cliente.service.ClienteService;
import com.banco.banco_api.modules.shared.dto.GeneralResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/clientes")
@CrossOrigin(origins = "*")
@Slf4j
public class ClienteController {

    private final ClienteService clienteService;

    @Autowired
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<GeneralResponseDto<List<ClienteDto>>> getAllClients() {
        log.info("Request: Get all clients");
        List<ClienteDto> clients = clienteService.getAllClients();
        GeneralResponseDto<List<ClienteDto>> response = GeneralResponseDto.<List<ClienteDto>>builder()
                .success(true)
                .message("Clientes recuperados exitosamente")
                .data(clients)
                .build();
        log.info("Response: Get all clients -> {}", response);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponseDto<ClienteDto>> getClientById(@PathVariable Long id) {
        log.info("Request: Get client by id: {}", id);
        ClienteDto client = clienteService.getClientById(id);
        GeneralResponseDto<ClienteDto> response = GeneralResponseDto.<ClienteDto>builder()
                .success(true)
                .message("Cliente recuperado exitosamente")
                .data(client)
                .build();
        log.info("Response: Get client by id: {} -> {}", id, response);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<GeneralResponseDto<ClienteDto>> createClient(@Valid @RequestBody ClienteDto clientDto) {
        log.info("Request: Create client -> {}", clientDto);
        ClienteDto created = clienteService.createClient(clientDto);
        GeneralResponseDto<ClienteDto> response = GeneralResponseDto.<ClienteDto>builder()
                .success(true)
                .message("Cliente creado exitosamente")
                .data(created)
                .build();
        log.info("Response: Create client -> {}", response);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponseDto<ClienteDto>> updateClient(@PathVariable Long id, @Valid @RequestBody ClienteDto clientDto) {
        log.info("Request: Update client with id: {} -> {}", id, clientDto);
        ClienteDto updated = clienteService.updateClient(id, clientDto);
        GeneralResponseDto<ClienteDto> response = GeneralResponseDto.<ClienteDto>builder()
                .success(true)
                .message("Cliente actualizado exitosamente")
                .data(updated)
                .build();
        log.info("Response: Update client with id: {} -> {}", id, response);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GeneralResponseDto<ClienteDto>> patchClient(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        log.info("Request: Patch client with id: {} -> {}", id, updates);
        ClienteDto existing = clienteService.getClientById(id);

        if (updates.containsKey("nombre")) {
            existing.setNombre((String) updates.get("nombre"));
        }
        if (updates.containsKey("genero")) {
            existing.setGenero((String) updates.get("genero"));
        }
        if (updates.containsKey("edad")) {
            existing.setEdad((Integer) updates.get("edad"));
        }
        if (updates.containsKey("direccion")) {
            existing.setDireccion((String) updates.get("direccion"));
        }
        if (updates.containsKey("telefono")) {
            existing.setTelefono((String) updates.get("telefono"));
        }
        if (updates.containsKey("contrasena")) {
            existing.setContrasena((String) updates.get("contrasena"));
        }
        if (updates.containsKey("estado")) {
            existing.setEstado((Boolean) updates.get("estado"));
        }

        ClienteDto updated = clienteService.updateClient(id, existing);
        GeneralResponseDto<ClienteDto> response = GeneralResponseDto.<ClienteDto>builder()
                .success(true)
                .message("Cliente modificado exitosamente")
                .data(updated)
                .build();
        log.info("Response: Patch client with id: {} -> {}", id, response);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponseDto<Void>> deleteClient(@PathVariable Long id) {
        log.info("Request: Delete client with id: {}", id);
        clienteService.deleteClient(id);
        GeneralResponseDto<Void> response = GeneralResponseDto.<Void>builder()
                .success(true)
                .message("Cliente eliminado exitosamente")
                .data(null)
                .build();
        log.info("Response: Delete client with id: {} -> {}", id, response);
        return ResponseEntity.ok(response);
    }
}
