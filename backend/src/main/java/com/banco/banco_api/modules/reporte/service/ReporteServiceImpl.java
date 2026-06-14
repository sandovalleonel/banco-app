package com.banco.banco_api.modules.reporte.service;

import com.banco.banco_api.modules.cliente.domain.ClienteEntity;
import com.banco.banco_api.modules.cliente.repository.ClienteRepository;
import com.banco.banco_api.modules.cuenta.domain.CuentaEntity;
import com.banco.banco_api.modules.cuenta.repository.CuentaRepository;
import com.banco.banco_api.modules.movimiento.domain.MovimientoEntity;
import com.banco.banco_api.modules.movimiento.repository.MovimientoRepository;
import com.banco.banco_api.modules.shared.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private final ClienteRepository clienteRepository;
    private final CuentaRepository cuentaRepository;
    private final MovimientoRepository movimientoRepository;

    @Autowired
    public ReporteServiceImpl(ClienteRepository clienteRepository,
                              CuentaRepository cuentaRepository,
                              MovimientoRepository movimientoRepository) {
        this.clienteRepository = clienteRepository;
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Override
    public Map<String, Object> generateAccountStatement(Long clientId, LocalDate startDate, LocalDate endDate) {
        // Validar que el cliente existe
        ClienteEntity cliente = clienteRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con el ID: " + clientId));

        // Obtener cuentas asociadas
        List<CuentaEntity> cuentas = cuentaRepository.findByClienteId(clientId);

        // Convertir fechas a fechaHora de inicio y fin de día
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        // Buscar todos los movimientos asociados a las cuentas del cliente
        List<MovimientoEntity> movimientos = movimientoRepository.findByClienteAndDateRange(clientId, startDateTime, endDateTime);

        // Construir reporte consolidado
        Map<String, Object> report = new HashMap<>();
        report.put("cliente", cliente.getNombre());
        report.put("identificacion", cliente.getIdentificacion());
        report.put("cuentas", cuentas.stream().map(c -> {
            Map<String, Object> cMap = new HashMap<>();
            cMap.put("numeroCuenta", c.getNumeroCuenta());
            cMap.put("tipoCuenta", c.getTipoCuenta());
            cMap.put("saldoActual", c.getSaldoInicial());
            cMap.put("estado", c.getEstado());
            return cMap;
        }).collect(Collectors.toList()));
        
        report.put("movimientos", movimientos.stream().map(m -> {
            Map<String, Object> mMap = new HashMap<>();
            mMap.put("fecha", m.getFecha());
            mMap.put("numeroCuenta", m.getCuenta().getNumeroCuenta());
            mMap.put("tipoMovimiento", m.getTipoMovimiento());
            mMap.put("valor", m.getValor());
            mMap.put("saldoResultante", m.getSaldo());
            return mMap;
        }).collect(Collectors.toList()));

        return report;
    }
}
