package de.bewater.homeradar.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import de.bewater.homeradar.model.ArpEintrag;
import de.bewater.homeradar.model.Ereignis;
import de.bewater.homeradar.model.Geraet;
import de.bewater.homeradar.model.GeraeteStatus;
import de.bewater.homeradar.repository.EreignisRepository;
import de.bewater.homeradar.repository.GeraetRepository;

/**
 * Erkennt moegliche Eindringlinge: unbekannte Geraete, die auftauchen,
 * waehrend kein Bewohner anwesend ist.
 */
@Service
public class IntrusionService {

	private final AnwesenheitsService anwesenheitsService;
	private final GeraetRepository geraetRepository;
	private final EreignisRepository ereignisRepository;

	public IntrusionService(AnwesenheitsService anwesenheitsService, GeraetRepository geraetRepository,
			EreignisRepository ereignisRepository) {
		this.anwesenheitsService = anwesenheitsService;
		this.geraetRepository = geraetRepository;
		this.ereignisRepository = ereignisRepository;
	}

	/**
	 * Prueft die zuletzt gescannten Geraete auf Intrusion. Wird vom
	 * ScanScheduler nach jedem Scan aufgerufen.
	 *
	 * @param aktuelleGeraete die im aktuellen Scan gefundenen ARP-Eintraege
	 */
	public void pruefeAufIntrusion(List<ArpEintrag> aktuelleGeraete) {
		// Ist jemand zu Hause, ist ein unbekanntes Geraet kein Alarmgrund.
		if (!anwesenheitsService.getAnwesendePersonen().isEmpty()) {
			return;
		}

		LocalDateTime jetzt = LocalDateTime.now();
		for (ArpEintrag eintrag : aktuelleGeraete) {
			geraetRepository.findByMac(eintrag.mac())
					.filter(geraet -> geraet.getStatus() == GeraeteStatus.UNBEKANNT)
					// Anti-Spam: nur ein offener Alarm pro Geraet.
					.filter(geraet -> !ereignisRepository.existsByGeraetAndQuittiertFalse(geraet))
					.ifPresent(geraet -> alarmiere(geraet, eintrag.ip(), jetzt));
		}
	}

	/**
	 * Markiert einen Alarm als quittiert.
	 *
	 * @param ereignisId ID des Ereignisses
	 * @return true, wenn das Ereignis existierte und quittiert wurde
	 */
	@Transactional
	public boolean quittiere(Long ereignisId) {
		return ereignisRepository.findById(ereignisId)
				.map(ereignis -> {
					ereignis.setQuittiert(true);
					ereignisRepository.save(ereignis);
					return true;
				})
				.orElse(false);
	}

	private void alarmiere(Geraet geraet, String ip, LocalDateTime zeitpunkt) {
		Ereignis ereignis = new Ereignis();
		ereignis.setGeraet(geraet);
		ereignis.setZeitpunkt(zeitpunkt);
		ereignis.setIpAdresse(ip);
		ereignis.setQuittiert(false);
		ereignisRepository.save(ereignis);
	}
}
