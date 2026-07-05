package de.bewater.homeradar.scanner;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import de.bewater.homeradar.config.ScanConfig;
import de.bewater.homeradar.model.ArpEintrag;
import de.bewater.homeradar.model.Geraet;
import de.bewater.homeradar.model.GeraeteStatus;
import de.bewater.homeradar.model.ScanErgebnis;
import de.bewater.homeradar.repository.GeraetRepository;
import de.bewater.homeradar.repository.ScanErgebnisRepository;
import de.bewater.homeradar.service.IntrusionService;

/**
 * Fuehrt periodisch einen Netzwerk-Scan durch und persistiert die
 * gefundenen Geraete sowie deren Scan-Historie.
 */
@Component
public class ScanScheduler {

	private final ScanConfig scanConfig;
	private final PingSweeper pingSweeper;
	private final NetworkScanner networkScanner;
	private final GeraetRepository geraetRepository;
	private final ScanErgebnisRepository scanErgebnisRepository;
	private final IntrusionService intrusionService;

	public ScanScheduler(ScanConfig scanConfig, PingSweeper pingSweeper, NetworkScanner networkScanner,
			GeraetRepository geraetRepository, ScanErgebnisRepository scanErgebnisRepository,
			IntrusionService intrusionService) {
		this.scanConfig = scanConfig;
		this.pingSweeper = pingSweeper;
		this.networkScanner = networkScanner;
		this.geraetRepository = geraetRepository;
		this.scanErgebnisRepository = scanErgebnisRepository;
		this.intrusionService = intrusionService;
	}

	/**
	 * Scannt das Netzwerk und aktualisiert die Persistenz. Zuerst wird per
	 * Ping-Sweep die ARP-Tabelle des Betriebssystems aufgefrischt, danach
	 * wird diese ausgelesen und ausgewertet.
	 */
	@Scheduled(fixedRate = 60000)
	@Transactional
	public void scan() {
		// Ping-Sweep aktualisiert die ARP-Tabelle des Betriebssystems.
		pingSweeper.sweep(scanConfig.getSubnetz());

		List<ArpEintrag> eintraege = networkScanner.scan();
		LocalDateTime jetzt = LocalDateTime.now();

		for (ArpEintrag eintrag : eintraege) {
			Geraet geraet = geraetRepository.findByMac(eintrag.mac())
					.orElseGet(() -> {
						Geraet neu = new Geraet();
						neu.setMac(eintrag.mac());
						neu.setErstmalsGesehen(jetzt);
						// Neue Geraete sind zunaechst unbekannt.
						neu.setStatus(GeraeteStatus.UNBEKANNT);
						return neu;
					});
			// Bei bestehenden Geraeten bleibt der Status unangetastet.
			geraet.setZuletztGesehen(jetzt);
			geraet = geraetRepository.save(geraet);

			ScanErgebnis ergebnis = new ScanErgebnis();
			ergebnis.setGeraet(geraet);
			ergebnis.setZeitpunkt(jetzt);
			ergebnis.setIpAdresse(eintrag.ip());
			scanErgebnisRepository.save(ergebnis);
		}

		// Nach dem Persistieren auf unbekannte Geraete bei Abwesenheit pruefen.
		intrusionService.pruefeAufIntrusion(eintraege);
	}
}
