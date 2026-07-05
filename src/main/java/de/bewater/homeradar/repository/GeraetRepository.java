package de.bewater.homeradar.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import de.bewater.homeradar.model.Geraet;

public interface GeraetRepository extends JpaRepository<Geraet, Long> {

	Optional<Geraet> findByMac(String mac);
}
