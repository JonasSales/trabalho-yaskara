package com.br.projetoyaskara.repository;

import com.br.projetoyaskara.model.Reservas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReservasRepository extends JpaRepository<Reservas, Long> {

    List<Reservas> findReservasByClientId(UUID clienteId);

    List<Reservas> findReservasByDataReservaBeforeAndClient_Id(LocalDateTime dataReservaBefore, UUID clientId);

}
