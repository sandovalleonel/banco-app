package com.banco.banco_api.modules.cliente.web;

import com.banco.banco_api.modules.cliente.dto.ClienteDto;
import com.banco.banco_api.modules.cliente.service.ClienteService;
import com.banco.banco_api.modules.shared.dto.GeneralResponseDto;
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
public class ClienteController {

    private final ClienteService clienteService;

    @Autowired
    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<GeneralResponseDto<List<ClienteDto>>> getAllClients() {
        List<ClienteDto> clients = clienteService.getAllClients();
        GeneralResponseDto<List<ClienteDto>> response = GeneralResponseDto.<List<ClienteDto>>builder()
                .success(true)
                .message("Clientes recuperados exitosamente")
                .data(clients)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GeneralResponseDto<ClienteDto>> getClientById(@PathVariable Long id) {
        ClienteDto client = clienteService.getClientById(id);
        GeneralResponseDto<ClienteDto> response = GeneralResponseDto.<ClienteDto>builder()
                .success(true)
                .message("Cliente recuperado exitosamente")
                .data(client)
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<GeneralResponseDto<ClienteDto>> createClient(@Valid @RequestBody ClienteDto clientDto) {
        ClienteDto created = clienteService.createClient(clientDto);
        GeneralResponseDto<ClienteDto> response = GeneralResponseDto.<ClienteDto>builder()
                .success(true)
                .message("Cliente creado exitosamente")
                .data(created)
                .build();
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeneralResponseDto<ClienteDto>> updateClient(@PathVariable Long id, @Valid @RequestBody ClienteDto clientDto) {
        ClienteDto updated = clienteService.updateClient(id, clientDto);
        GeneralResponseDto<ClienteDto> response = GeneralResponseDto.<ClienteDto>builder()
                .success(true)
                .message("Cliente actualizado exitosamente")
                .data(updated)
                .build();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GeneralResponseDto<ClienteDto>> patchClient(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
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
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<GeneralResponseDto<Void>> deleteClient(@PathVariable Long id) {
        clienteService.deleteClient(id);
        GeneralResponseDto<Void> response = GeneralResponseDto.<Void>builder()
                .success(true)
                .message("Cliente eliminado exitosamente")
                .data(null)
                .build();
        return ResponseEntity.ok(response);
    }
}
