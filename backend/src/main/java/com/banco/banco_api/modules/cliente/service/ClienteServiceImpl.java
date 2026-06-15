package com.banco.banco_api.modules.cliente.service;

import com.banco.banco_api.modules.cliente.domain.ClienteEntity;
import com.banco.banco_api.modules.cliente.dto.ClienteDto;
import com.banco.banco_api.modules.cliente.repository.ClienteRepository;
import com.banco.banco_api.modules.cuenta.domain.CuentaEntity;
import com.banco.banco_api.modules.cuenta.repository.CuentaRepository;
import com.banco.banco_api.modules.cuenta.service.CuentaService;
import com.banco.banco_api.modules.shared.exception.BusinessRuleException;
import com.banco.banco_api.modules.shared.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final CuentaService cuentaService;

    @Autowired
    public ClienteServiceImpl(ClienteRepository clienteRepository,
                              CuentaRepository cuentaRepository,
                              CuentaService cuentaService) {
        this.clienteRepository = clienteRepository;
        this.cuentaRepository = cuentaRepository;
        this.cuentaService = cuentaService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClienteDto> getAllClients() {
        return clienteRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ClienteDto getClientById(Long id) {
        ClienteEntity entity = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con el ID: " + id));
        return toDto(entity);
    }

    @Override
    public ClienteDto createClient(ClienteDto clientDto) {
        if (clienteRepository.findByIdentificacion(clientDto.getIdentificacion()).isPresent()) {
            throw new BusinessRuleException("Ya existe una persona registrada con la identificación: " + clientDto.getIdentificacion());
        }
        ClienteEntity entity = toEntity(clientDto);
        ClienteEntity saved = clienteRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public ClienteDto updateClient(Long id, ClienteDto clientDto) {
        ClienteEntity existing = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con el ID: " + id));
        
        existing.setNombre(clientDto.getNombre());
        existing.setGenero(clientDto.getGenero());
        existing.setEdad(clientDto.getEdad());
        existing.setDireccion(clientDto.getDireccion());
        existing.setTelefono(clientDto.getTelefono());
        existing.setContrasena(clientDto.getContrasena());
        existing.setEstado(clientDto.getEstado());

        if (Boolean.FALSE.equals(clientDto.getEstado())) {
            cuentaService.deactivateAccountsByClientId(id);
        }

        ClienteEntity saved = clienteRepository.save(existing);
        return toDto(saved);
    }

    @Override
    public void deleteClient(Long id) {
        ClienteEntity existing = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con el ID: " + id));
        
        // Verificar restricción: No eliminar si existen cuentas con saldo
        List<CuentaEntity> cuentas = cuentaRepository.findByClienteId(id);
        for (CuentaEntity cuenta : cuentas) {
            if (cuenta.getSaldoInicial() != null && cuenta.getSaldoInicial().compareTo(BigDecimal.ZERO) > 0) {
                throw new BusinessRuleException("No se permite la eliminación si existen cuentas asociadas con saldos vigentes.");
            }
        }
        
        for (CuentaEntity cuenta : cuentas) {
            cuentaRepository.delete(cuenta);
        }
        clienteRepository.delete(existing);
    }

    private ClienteDto toDto(ClienteEntity entity) {
        if (entity == null) return null;
        return ClienteDto.builder()
                .id(entity.getId())
                .identificacion(entity.getIdentificacion())
                .nombre(entity.getNombre())
                .genero(entity.getGenero())
                .edad(entity.getEdad())
                .direccion(entity.getDireccion())
                .telefono(entity.getTelefono())
                .contrasena(entity.getContrasena())
                .estado(entity.getEstado())
                .build();
    }

    private ClienteEntity toEntity(ClienteDto dto) {
        if (dto == null) return null;
        return ClienteEntity.builder()
                .id(dto.getId())
                .identificacion(dto.getIdentificacion())
                .nombre(dto.getNombre())
                .genero(dto.getGenero())
                .edad(dto.getEdad())
                .direccion(dto.getDireccion())
                .telefono(dto.getTelefono())
                .contrasena(dto.getContrasena())
                .estado(dto.getEstado())
                .build();
    }
}
