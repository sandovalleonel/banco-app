package com.banco.banco_api.modules.movimiento.service;

import com.banco.banco_api.modules.cuenta.domain.CuentaEntity;
import com.banco.banco_api.modules.shared.constants.Constants;
import com.banco.banco_api.modules.movimiento.web.MovimientoRequest;
import com.banco.banco_api.modules.cuenta.repository.CuentaRepository;
import com.banco.banco_api.modules.movimiento.domain.MovimientoEntity;
import com.banco.banco_api.modules.movimiento.dto.MovimientoDto;
import com.banco.banco_api.modules.movimiento.repository.MovimientoRepository;
import com.banco.banco_api.modules.configuracion.domain.ConfiguracionEntity;
import com.banco.banco_api.modules.configuracion.repository.ConfiguracionRepository;
import com.banco.banco_api.modules.shared.exception.BusinessRuleException;
import com.banco.banco_api.modules.shared.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MovimientoServiceImpl implements MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;
    private final ConfiguracionRepository configuracionRepository;

    @Autowired
    public MovimientoServiceImpl(MovimientoRepository movimientoRepository,
                                 CuentaRepository cuentaRepository,
                                 ConfiguracionRepository configuracionRepository) {
        this.movimientoRepository = movimientoRepository;
        this.cuentaRepository = cuentaRepository;
        this.configuracionRepository = configuracionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoDto> getAllMovements() {
        return movimientoRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public MovimientoDto createMovement(String accountNumber, String type, BigDecimal value) {
        // 1. Validar Existencia de Cuenta
        CuentaEntity cuenta = cuentaRepository.findById(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con el número: " + accountNumber));

        if (Boolean.FALSE.equals(cuenta.getEstado())) {
            throw new BusinessRuleException("La cuenta está inactiva y no se pueden realizar movimientos.");
        }

        if (value == null) {
            throw new BusinessRuleException("El valor del movimiento no puede ser nulo.");
        }

        // 2. Determinar valor ajustado según tipo de movimiento (Débito negativo, Crédito positivo)
        BigDecimal processValue;
        if (Constants.TIPO_DEBITO.equalsIgnoreCase(type)) {
            processValue = value.compareTo(BigDecimal.ZERO) > 0 ? value.negate() : value;
        } else if (Constants.TIPO_CREDITO.equalsIgnoreCase(type)) {
            processValue = value.compareTo(BigDecimal.ZERO) < 0 ? value.negate() : value;
        } else {
            throw new BusinessRuleException("Tipo de movimiento no válido. Debe ser '" + Constants.TIPO_DEBITO + "' o '" + Constants.TIPO_CREDITO + "'.");
        }
        processValue = processValue.setScale(2, RoundingMode.HALF_UP);

        BigDecimal currentBalance = cuenta.getSaldoActual() != null ? cuenta.getSaldoActual() : BigDecimal.ZERO;
        BigDecimal newBalance = currentBalance.add(processValue).setScale(2, RoundingMode.HALF_UP);

        // 3. Reglas de negocio para Débitos
        if (Constants.TIPO_DEBITO.equalsIgnoreCase(type)) {
            // A. Control de Fondos Disponibles
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new BusinessRuleException("Saldo no disponible");
            }

            // B. Control de Cupos Acumulados Diarios ($1000 base)
            LocalDateTime startOfDay = LocalDateTime.now().with(LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.now().with(LocalTime.MAX);
            
            BigDecimal sumDebitsToday = movimientoRepository.sumDebitsByClienteAndDateRange(
                    cuenta.getCliente().getId(),
                    startOfDay,
                    endOfDay
            );
            
            // La suma devuelta es negativa (o cero), convertimos a absoluto
            BigDecimal absSumDebitsToday = sumDebitsToday.abs();
            BigDecimal absNewDebit = processValue.abs();

            BigDecimal limit = BigDecimal.valueOf(1000.00);
            Optional<ConfiguracionEntity> configOpt = configuracionRepository.findById("LIMITE_DIARIO_RETIRO");
            if (configOpt.isPresent()) {
                try {
                    limit = new BigDecimal(configOpt.get().getValor());
                } catch (NumberFormatException e) {
                    // Mantener el límite por defecto
                }
            }

            if (absSumDebitsToday.add(absNewDebit).compareTo(limit) > 0) {
                throw new BusinessRuleException("Cupo diario Excedido");
            }
        }

        // 4. Actualizar saldo de cuenta
        cuenta.setSaldoActual(newBalance);
        cuentaRepository.save(cuenta);

        // 5. Persistir Movimiento
        MovimientoEntity movimiento = MovimientoEntity.builder()
                .fecha(LocalDateTime.now())
                .tipoMovimiento(Constants.TIPO_DEBITO.equalsIgnoreCase(type) ? Constants.TIPO_DEBITO : Constants.TIPO_CREDITO)
                .valor(processValue)
                .saldo(newBalance)
                .cuenta(cuenta)
                .build();

        MovimientoEntity saved = movimientoRepository.save(movimiento);
        return toDto(saved);
    }

    @Override
    public MovimientoDto updateMovement(Long id, MovimientoRequest request) {
        MovimientoEntity existing = movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con el ID: " + id));

        CuentaEntity cuenta = existing.getCuenta();
        if (cuenta == null) {
            throw new ResourceNotFoundException("Cuenta no encontrada para este movimiento.");
        }

        if (Boolean.FALSE.equals(cuenta.getEstado())) {
            throw new BusinessRuleException("La cuenta está inactiva y no se pueden realizar operaciones sobre sus movimientos.");
        }

        // Revertir el valor del movimiento anterior
        BigDecimal currentBalance = cuenta.getSaldoActual() != null ? cuenta.getSaldoActual() : BigDecimal.ZERO;
        BigDecimal revertedBalance = currentBalance.subtract(existing.getValor());

        BigDecimal value = request.getValor();
        if (value == null) {
            throw new BusinessRuleException("El valor del movimiento no puede ser nulo.");
        }

        String type = request.getTipoMovimiento();

        // Determinar valor ajustado según tipo de movimiento (Débito negativo, Crédito positivo)
        BigDecimal processValue;
        if (Constants.TIPO_DEBITO.equalsIgnoreCase(type)) {
            processValue = value.compareTo(BigDecimal.ZERO) > 0 ? value.negate() : value;
        } else if (Constants.TIPO_CREDITO.equalsIgnoreCase(type)) {
            processValue = value.compareTo(BigDecimal.ZERO) < 0 ? value.negate() : value;
        } else {
            throw new BusinessRuleException("Tipo de movimiento no válido. Debe ser '" + Constants.TIPO_DEBITO + "' o '" + Constants.TIPO_CREDITO + "'.");
        }
        processValue = processValue.setScale(2, RoundingMode.HALF_UP);

        BigDecimal newBalance = revertedBalance.add(processValue).setScale(2, RoundingMode.HALF_UP);

        // Control de saldo no disponible (tanto si el nuevo saldo es negativo, como si es débito)
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Saldo no disponible");
        }

        // Reglas de negocio para Débitos
        if (Constants.TIPO_DEBITO.equalsIgnoreCase(type)) {
            // Control de Cupos Acumulados Diarios ($1000 base)
            LocalDateTime startOfDay = existing.getFecha().with(LocalTime.MIN);
            LocalDateTime endOfDay = existing.getFecha().with(LocalTime.MAX);
            
            BigDecimal sumDebitsToday = movimientoRepository.sumDebitsByClienteAndDateRange(
                    cuenta.getCliente().getId(),
                    startOfDay,
                    endOfDay
            );
            
            // Restamos el valor del movimiento anterior de la suma diaria (si era un débito) para recalcular correctamente
            BigDecimal adjustedSumDebitsToday = sumDebitsToday;
            if (Constants.TIPO_DEBITO.equalsIgnoreCase(existing.getTipoMovimiento())) {
                adjustedSumDebitsToday = sumDebitsToday.subtract(existing.getValor());
            }

            BigDecimal absSumDebitsToday = adjustedSumDebitsToday.abs();
            BigDecimal absNewDebit = processValue.abs();

            BigDecimal limit = BigDecimal.valueOf(1000.00);
            Optional<ConfiguracionEntity> configOpt = configuracionRepository.findById("LIMITE_DIARIO_RETIRO");
            if (configOpt.isPresent()) {
                try {
                    limit = new BigDecimal(configOpt.get().getValor());
                } catch (NumberFormatException e) {
                    // Mantener el límite por defecto
                }
            }

            if (absSumDebitsToday.add(absNewDebit).compareTo(limit) > 0) {
                throw new BusinessRuleException("Cupo diario Excedido");
            }
        }

        // Actualizar saldo de cuenta
        cuenta.setSaldoActual(newBalance);
        cuentaRepository.save(cuenta);

        // Actualizar datos del movimiento (mantenemos la fecha original)
        existing.setTipoMovimiento(Constants.TIPO_DEBITO.equalsIgnoreCase(type) ? Constants.TIPO_DEBITO : Constants.TIPO_CREDITO);
        existing.setValor(processValue);
        existing.setSaldo(newBalance);

        MovimientoEntity saved = movimientoRepository.save(existing);
        return toDto(saved);
    }

    @Override
    public void deleteMovement(Long id) {
        MovimientoEntity existing = movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con el ID: " + id));

        CuentaEntity cuenta = existing.getCuenta();
        if (cuenta == null) {
            throw new ResourceNotFoundException("Cuenta no encontrada para este movimiento.");
        }



        // Revertir el valor del movimiento
        BigDecimal currentBalance = cuenta.getSaldoActual() != null ? cuenta.getSaldoActual() : BigDecimal.ZERO;
        BigDecimal newBalance = currentBalance.subtract(existing.getValor()).setScale(2, RoundingMode.HALF_UP);

  

        // Actualizar saldo de cuenta
        cuenta.setSaldoActual(newBalance);
        cuentaRepository.save(cuenta);

        // Eliminar el movimiento
        movimientoRepository.delete(existing);
    }

    private MovimientoDto toDto(MovimientoEntity entity) {
        if (entity == null) return null;
        return MovimientoDto.builder()
                .id(entity.getId())
                .fecha(entity.getFecha())
                .tipoMovimiento(entity.getTipoMovimiento())
                .valor(entity.getValor())
                .saldoResultante(entity.getSaldo())
                .numeroCuenta(entity.getCuenta() != null ? entity.getCuenta().getNumeroCuenta() : null)
                .build();
    }
}
