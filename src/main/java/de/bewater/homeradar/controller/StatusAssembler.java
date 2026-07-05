package de.bewater.homeradar.controller;

import java.util.List;

import org.springframework.stereotype.Component;

import de.bewater.homeradar.controller.dto.AnwesendePersonDto;
import de.bewater.homeradar.controller.dto.EreignisDto;
import de.bewater.homeradar.controller.dto.GeraetStatusDto;
import de.bewater.homeradar.controller.dto.PersonStatusDto;
import de.bewater.homeradar.model.Ereignis;
import de.bewater.homeradar.model.Geraet;
import de.bewater.homeradar.model.Person;
import de.bewater.homeradar.repository.EreignisRepository;
import de.bewater.homeradar.repository.GeraetRepository;
import de.bewater.homeradar.repository.PersonRepository;
import de.bewater.homeradar.repository.ScanErgebnisRepository;
import de.bewater.homeradar.service.AnwesenheitsService;
import de.bewater.homeradar.service.OuiLookupService;

/**
 * Baut die DTO-Sichten fuer API und Dashboard auf, damit die Controller
 * keine Entities serialisieren und die Mapping-Logik nicht doppelt haben.
 *
 * <p>Die Methoden greifen auf Lazy-Collections zu und muessen daher innerhalb
 * einer Transaktion des aufrufenden Controllers laufen.
 */
@Component
public class StatusAssembler {

	private final AnwesenheitsService anwesenheitsService;
	private final OuiLookupService ouiLookupService;
	private final PersonRepository personRepository;
	private final GeraetRepository geraetRepository;
	private final ScanErgebnisRepository scanErgebnisRepository;
	private final EreignisRepository ereignisRepository;

	public StatusAssembler(AnwesenheitsService anwesenheitsService, OuiLookupService ouiLookupService,
			PersonRepository personRepository, GeraetRepository geraetRepository,
			ScanErgebnisRepository scanErgebnisRepository, EreignisRepository ereignisRepository) {
		this.anwesenheitsService = anwesenheitsService;
		this.ouiLookupService = ouiLookupService;
		this.personRepository = personRepository;
		this.geraetRepository = geraetRepository;
		this.scanErgebnisRepository = scanErgebnisRepository;
		this.ereignisRepository = ereignisRepository;
	}

	/** Alle aktuell anwesenden Personen als kompaktes DTO. */
	public List<AnwesendePersonDto> anwesendePersonen() {
		return anwesenheitsService.getAnwesendePersonen().stream()
				.map(p -> new AnwesendePersonDto(p.getName(), zaehleOnlineGeraete(p)))
				.toList();
	}

	/** Alle Bewohner mit Anwesenheit und Anzahl online-Geraete. */
	public List<PersonStatusDto> alleBewohnerMitStatus() {
		return personRepository.findAll().stream()
				.map(p -> new PersonStatusDto(
						p.getName(),
						anwesenheitsService.istPersonAnwesend(p),
						zaehleOnlineGeraete(p)))
				.toList();
	}

	/** Alle Geraete mit Online-Status, letzter IP und zugeordneter Person. */
	public List<GeraetStatusDto> alleGeraeteMitStatus() {
		return geraetRepository.findAll().stream()
				.map(this::toGeraetStatus)
				.toList();
	}

	/** Alle offenen (nicht quittierten) Alarme, neueste zuerst. */
	public List<EreignisDto> offeneAlarme() {
		return ereignisRepository.findByQuittiertFalseOrderByZeitpunktDesc().stream()
				.map(this::toEreignisDto)
				.toList();
	}

	/** Anzahl offener Alarme (fuer den Badge). */
	public long offeneAlarmeAnzahl() {
		return ereignisRepository.countByQuittiertFalse();
	}

	private long zaehleOnlineGeraete(Person person) {
		return person.getGeraete().stream()
				.filter(anwesenheitsService::istGeraetOnline)
				.count();
	}

	private GeraetStatusDto toGeraetStatus(Geraet g) {
		String ipZuletzt = scanErgebnisRepository.findTopByGeraetOrderByZeitpunktDesc(g)
				.map(se -> se.getIpAdresse())
				.orElse(null);
		Long personId = g.getPerson() != null ? g.getPerson().getId() : null;
		String personName = g.getPerson() != null ? g.getPerson().getName() : null;
		return new GeraetStatusDto(
				g.getId(),
				g.getMac(),
				g.getName(),
				ouiLookupService.anzeigeName(g.getName(), g.getMac()),
				ipZuletzt,
				g.getZuletztGesehen(),
				anwesenheitsService.istGeraetOnline(g),
				personId,
				personName,
				g.getStatus());
	}

	private EreignisDto toEreignisDto(Ereignis e) {
		Geraet geraet = e.getGeraet();
		return new EreignisDto(
				e.getId(),
				geraet != null ? geraet.getMac() : null,
				geraet != null ? geraet.getName() : null,
				e.getIpAdresse(),
				e.getZeitpunkt());
	}
}
