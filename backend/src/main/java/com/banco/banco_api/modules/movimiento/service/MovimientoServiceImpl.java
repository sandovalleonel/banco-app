package com.banco.banco_api.modules.movimiento.service;

import com.banco.banco_api.modules.cuenta.domain.CuentaEntity;
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

        if (value == null) {
            throw new BusinessRuleException("El valor del movimiento no puede ser nulo.");
        }

        // 2. Determinar valor ajustado según tipo de movimiento (Débito negativo, Crédito positivo)
        BigDecimal processValue;
        if ("Débito".equalsIgnoreCase(type)) {
            processValue = value.compareTo(BigDecimal.ZERO) > 0 ? value.negate() : value;
        } else if ("Crédito".equalsIgnoreCase(type)) {
            processValue = value.compareTo(BigDecimal.ZERO) < 0 ? value.negate() : value;
        } else {
            throw new BusinessRuleException("Tipo de movimiento no válido. Debe ser 'Débito' o 'Crédito'.");
        }
        processValue = processValue.setScale(2, RoundingMode.HALF_UP);

        BigDecimal currentBalance = cuenta.getSaldoInicial() != null ? cuenta.getSaldoInicial() : BigDecimal.ZERO;
        BigDecimal newBalance = currentBalance.add(processValue).setScale(2, RoundingMode.HALF_UP);

        // 3. Reglas de negocio para Débitos
        if ("Débito".equalsIgnoreCase(type)) {
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
        cuenta.setSaldoInicial(newBalance);
        cuentaRepository.save(cuenta);

        // 5. Persistir Movimiento
        MovimientoEntity movimiento = MovimientoEntity.builder()
                .fecha(LocalDateTime.now())
                .tipoMovimiento("Débito".equalsIgnoreCase(type) ? "Débito" : "Crédito")
                .valor(processValue)
                .saldo(newBalance)
                .cuenta(cuenta)
                .build();

        MovimientoEntity saved = movimientoRepository.save(movimiento);
        return toDto(saved);
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
