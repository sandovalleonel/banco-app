package com.banco.banco_api.modules.cuenta.repository;

import com.banco.banco_api.modules.cuenta.domain.CuentaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuentaRepository extends JpaRepository<CuentaEntity, String> {
    List<CuentaEntity> findByClienteId(Long clienteId);
}
