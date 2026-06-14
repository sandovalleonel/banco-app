package com.banco.banco_api.modules.movimiento.repository;

import com.banco.banco_api.modules.movimiento.domain.MovimientoEntity;
import com.banco.banco_api.modules.shared.constants.Constants;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MovimientoRepository extends JpaRepository<MovimientoEntity, Long> {

    List<MovimientoEntity> findByCuentaNumeroCuentaOrderByFechaDesc(String numeroCuenta);

    @Query("SELECT COALESCE(SUM(m.valor), 0) FROM MovimientoEntity m " +
           "WHERE m.cuenta.cliente.id = :clienteId " +
           "AND m.tipoMovimiento = '" + Constants.TIPO_DEBITO + "' " +
           "AND m.fecha BETWEEN :startOfDay AND :endOfDay")
    BigDecimal sumDebitsByClienteAndDateRange(
            @Param("clienteId") Long clienteId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay
    );

    @Query("SELECT m FROM MovimientoEntity m " +
           "WHERE m.cuenta.cliente.id = :clienteId " +
           "AND m.fecha BETWEEN :startDate AND :endDate " +
           "ORDER BY m.fecha DESC")
    List<MovimientoEntity> findByClienteAndDateRange(
            @Param("clienteId") Long clienteId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
