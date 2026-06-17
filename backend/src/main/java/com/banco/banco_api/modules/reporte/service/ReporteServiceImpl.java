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
    private final ReportePdfService reportePdfService;

    @Autowired
    public ReporteServiceImpl(ClienteRepository clienteRepository,
                              CuentaRepository cuentaRepository,
                              MovimientoRepository movimientoRepository,
                              ReportePdfService reportePdfService) {
        this.clienteRepository = clienteRepository;
        this.cuentaRepository = cuentaRepository;
        this.movimientoRepository = movimientoRepository;
        this.reportePdfService = reportePdfService;
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

        // Restructure cuentas: nest matching movements
        List<Map<String, Object>> cuentasList = cuentas.stream().map(c -> {
            Map<String, Object> cMap = new HashMap<>();
            cMap.put("numeroCuenta", c.getNumeroCuenta());
            cMap.put("tipoCuenta", c.getTipoCuenta());
            cMap.put("saldoInicial", c.getSaldoInicial());
            cMap.put("saldoActual", c.getSaldoActual());
            cMap.put("estado", c.getEstado());

            // Filter movements belonging to this account
            List<Map<String, Object>> accountMovements = movimientos.stream()
                .filter(m -> m.getCuenta().getNumeroCuenta().equals(c.getNumeroCuenta()))
                .map(m -> {
                    Map<String, Object> mMap = new HashMap<>();
                    mMap.put("fecha", m.getFecha());
                    mMap.put("tipoMovimiento", m.getTipoMovimiento());
                    mMap.put("valor", m.getValor());
                    mMap.put("saldoResultante", m.getSaldo());
                    return mMap;
                }).collect(Collectors.toList());

            cMap.put("movimientos", accountMovements);
            return cMap;
        }).collect(Collectors.toList());

        report.put("cuentas", cuentasList);

        // Generate PDF and encode to Base64 using injected ReportePdfService
        String pdfBase64 = reportePdfService.generatePdfBase64(cliente, cuentas, movimientos, startDate, endDate);
        report.put("pdfBase64", pdfBase64);

        return report;
    }
}
