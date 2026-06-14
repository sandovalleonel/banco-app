package com.banco.banco_api.modules.cuenta.service;

import com.banco.banco_api.modules.cliente.domain.ClienteEntity;
import com.banco.banco_api.modules.cliente.repository.ClienteRepository;
import com.banco.banco_api.modules.cuenta.domain.CuentaEntity;
import com.banco.banco_api.modules.cuenta.dto.CuentaDto;
import com.banco.banco_api.modules.cuenta.repository.CuentaRepository;
import com.banco.banco_api.modules.movimiento.domain.MovimientoEntity;
import com.banco.banco_api.modules.movimiento.repository.MovimientoRepository;
import com.banco.banco_api.modules.shared.exception.BusinessRuleException;
import com.banco.banco_api.modules.shared.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;
    private final ClienteRepository clienteRepository;

    @Autowired
    public CuentaServiceImpl(CuentaRepository cuentaRepository,
                             MovimientoRepository movimientoRepository,
                             ClienteRepository clienteRepository) {
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaDto> getAllAccounts() {
        return cuentaRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaDto getAccountByNumber(String accountNumber) {
        CuentaEntity entity = cuentaRepository.findById(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con el número: " + accountNumber));
        return toDto(entity);
    }

    @Override
    public CuentaDto createAccount(CuentaDto accountDto) {
        if (cuentaRepository.existsById(accountDto.getNumeroCuenta())) {
            throw new BusinessRuleException("Ya existe una cuenta con el número: " + accountDto.getNumeroCuenta());
        }

        ClienteEntity cliente = clienteRepository.findById(accountDto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con el ID: " + accountDto.getClienteId()));

        if (Boolean.FALSE.equals(cliente.getEstado())) {
            throw new BusinessRuleException("El cliente está inactivo y no se pueden realizar operaciones de creación o edición.");
        }

        CuentaEntity entity = CuentaEntity.builder()
                .numeroCuenta(accountDto.getNumeroCuenta())
                .tipoCuenta(accountDto.getTipoCuenta())
                .saldoInicial(accountDto.getSaldoInicial())
                .estado(accountDto.getEstado())
                .cliente(cliente)
                .build();

        CuentaEntity saved = cuentaRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public CuentaDto updateAccount(String accountNumber, CuentaDto accountDto) {
        CuentaEntity existing = cuentaRepository.findById(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con el número: " + accountNumber));

        if (existing.getCliente() != null && !existing.getCliente().getId().equals(accountDto.getClienteId())) {
            throw new BusinessRuleException("No se puede modificar el cliente de una cuenta.");
        }

        ClienteEntity cliente = clienteRepository.findById(accountDto.getClienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con el ID: " + accountDto.getClienteId()));

        if (Boolean.FALSE.equals(cliente.getEstado())) {
            throw new BusinessRuleException("El cliente está inactivo y no se pueden realizar operaciones de creación o edición.");
        }

        existing.setTipoCuenta(accountDto.getTipoCuenta());
        existing.setSaldoInicial(accountDto.getSaldoInicial());
        existing.setEstado(accountDto.getEstado());
        existing.setCliente(cliente);

        CuentaEntity saved = cuentaRepository.save(existing);
        return toDto(saved);
    }

    @Override
    public void deleteAccount(String accountNumber) {
        CuentaEntity existing = cuentaRepository.findById(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con el número: " + accountNumber));

        List<MovimientoEntity> movimientos = movimientoRepository.findByCuentaNumeroCuentaOrderByFechaDesc(accountNumber);
        if (!movimientos.isEmpty()) {
            throw new BusinessRuleException("No se permite eliminar la cuenta porque registra movimientos previos que alteran la auditoría contable.");
        }

        cuentaRepository.delete(existing);
    }

    private CuentaDto toDto(CuentaEntity entity) {
        if (entity == null) return null;
        return CuentaDto.builder()
                .numeroCuenta(entity.getNumeroCuenta())
                .tipoCuenta(entity.getTipoCuenta())
                .saldoInicial(entity.getSaldoInicial())
                .estado(entity.getEstado())
                .clienteId(entity.getCliente() != null ? entity.getCliente().getId() : null)
                .build();
    }
}
