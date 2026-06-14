package com.banco.banco_api.modules.reporte.service;

import java.time.LocalDate;
import java.util.Map;

public interface ReporteService {
    Map<String, Object> generateAccountStatement(Long clientId, LocalDate startDate, LocalDate endDate);
}
