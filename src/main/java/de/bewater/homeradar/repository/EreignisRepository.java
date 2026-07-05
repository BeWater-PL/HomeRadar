package de.bewater.homeradar.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import de.bewater.homeradar.model.Ereignis;
import de.bewater.homeradar.model.Geraet;

public interface EreignisRepository extends JpaRepository<Ereignis, Long> {

	/** Offene (nicht quittierte) Alarme, neueste zuerst. */
	List<Ereignis> findByQuittiertFalseOrderByZeitpunktDesc();

	/** Anzahl offener Alarme (fuer den Badge). */
	long countByQuittiertFalse();

	/** Ob fuer ein Geraet bereits ein offener Alarm existiert (Anti-Spam). */
	boolean existsByGeraetAndQuittiertFalse(Geraet geraet);
}
