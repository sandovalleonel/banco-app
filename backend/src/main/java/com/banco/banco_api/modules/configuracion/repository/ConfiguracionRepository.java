package com.banco.banco_api.modules.configuracion.repository;

import com.banco.banco_api.modules.configuracion.domain.ConfiguracionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracionRepository extends JpaRepository<ConfiguracionEntity, String> {
}
