package com.banco.banco_api.modules.cuenta.service;

import com.banco.banco_api.modules.cuenta.dto.CuentaDto;
import java.util.List;

public interface CuentaService {
    List<CuentaDto> getAllAccounts();
    CuentaDto getAccountByNumber(String accountNumber);
    CuentaDto createAccount(CuentaDto accountDto);
    CuentaDto updateAccount(String accountNumber, CuentaDto accountDto);
    void deleteAccount(String accountNumber);
    void deactivateAccountsByClientId(Long clientId);
}
