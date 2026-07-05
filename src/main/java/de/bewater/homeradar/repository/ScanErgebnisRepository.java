package de.bewater.homeradar.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import de.bewater.homeradar.model.Geraet;
import de.bewater.homeradar.model.ScanErgebnis;

public interface ScanErgebnisRepository extends JpaRepository<ScanErgebnis, Long> {

	/** Juengster Scan-Eintrag eines Geraets (fuer die zuletzt gesehene IP). */
	Optional<ScanErgebnis> findTopByGeraetOrderByZeitpunktDesc(Geraet geraet);

	/** Juengster Scan-Eintrag insgesamt (Zeitpunkt des letzten Scans). */
	Optional<ScanErgebnis> findTopByOrderByZeitpunktDesc();
}
