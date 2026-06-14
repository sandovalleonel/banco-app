package com.banco.banco_api.modules.movimiento.service;

import com.banco.banco_api.modules.movimiento.dto.MovimientoDto;
import java.math.BigDecimal;
import java.util.List;

public interface MovimientoService {
    List<MovimientoDto> getAllMovements();
    MovimientoDto createMovement(String accountNumber, String type, BigDecimal value);
}
