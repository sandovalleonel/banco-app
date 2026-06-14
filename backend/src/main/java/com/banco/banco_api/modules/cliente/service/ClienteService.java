package com.banco.banco_api.modules.cliente.service;

import com.banco.banco_api.modules.cliente.dto.ClienteDto;
import java.util.List;

public interface ClienteService {
    List<ClienteDto> getAllClients();
    ClienteDto getClientById(Long id);
    ClienteDto createClient(ClienteDto clientDto);
    ClienteDto updateClient(Long id, ClienteDto clientDto);
    void deleteClient(Long id);
}
