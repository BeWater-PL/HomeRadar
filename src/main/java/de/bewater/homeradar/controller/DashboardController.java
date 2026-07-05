package de.bewater.homeradar.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import de.bewater.homeradar.controller.dto.EreignisDto;
import de.bewater.homeradar.controller.dto.GeraetStatusDto;
import de.bewater.homeradar.controller.dto.PersonOptionDto;
import de.bewater.homeradar.controller.dto.PersonStatusDto;
import de.bewater.homeradar.model.GeraeteStatus;
import de.bewater.homeradar.model.Person;
import de.bewater.homeradar.model.ScanErgebnis;
import de.bewater.homeradar.repository.GeraetRepository;
import de.bewater.homeradar.repository.PersonRepository;
import de.bewater.homeradar.repository.ScanErgebnisRepository;
import de.bewater.homeradar.service.IntrusionService;

/**
 * Liefert das Thymeleaf-Dashboard. Die Methode laeuft in einer Transaktion,
 * damit Lazy-Collections beim Rendern nachgeladen werden koennen.
 */
@Controller
public class DashboardController {

	private final StatusAssembler statusAssembler;
	private final ScanErgebnisRepository scanErgebnisRepository;
	private final IntrusionService intrusionService;
	private final GeraetRepository geraetRepository;
	private final PersonRepository personRepository;

	public DashboardController(StatusAssembler statusAssembler, ScanErgebnisRepository scanErgebnisRepository,
			IntrusionService intrusionService, GeraetRepository geraetRepository,
			PersonRepository personRepository) {
		this.statusAssembler = statusAssembler;
		this.scanErgebnisRepository = scanErgebnisRepository;
		this.intrusionService = intrusionService;
		this.geraetRepository = geraetRepository;
		this.personRepository = personRepository;
	}

	@GetMapping("/")
	@Transactional(readOnly = true)
	public String dashboard(Model model) {
		List<PersonStatusDto> bewohner = statusAssembler.alleBewohnerMitStatus();
		List<GeraetStatusDto> geraete = statusAssembler.alleGeraeteMitStatus();
		List<EreignisDto> offeneAlarme = statusAssembler.offeneAlarme();
		long anzahlAnwesend = bewohner.stream().filter(PersonStatusDto::anwesend).count();
		LocalDateTime letzterScan = scanErgebnisRepository.findTopByOrderByZeitpunktDesc()
				.map(ScanErgebnis::getZeitpunkt)
				.orElse(null);
		List<PersonOptionDto> personen = personRepository.findAll().stream()
				.map(p -> new PersonOptionDto(p.getId(), p.getName()))
				.toList();

		model.addAttribute("bewohner", bewohner);
		model.addAttribute("geraete", geraete);
		model.addAttribute("offeneAlarme", offeneAlarme);
		model.addAttribute("anzahlOffeneAlarme", offeneAlarme.size());
		model.addAttribute("anzahlAnwesend", anzahlAnwesend);
		model.addAttribute("gesamtGeraete", geraete.size());
		model.addAttribute("letzterScan", letzterScan);
		model.addAttribute("statusWerte", GeraeteStatus.values());
		model.addAttribute("personen", personen);
		return "dashboard";
	}

	/** Quittiert einen Alarm vom Dashboard und laedt es danach neu. */
	@PostMapping("/alarme/{id}/quittieren")
	public String quittiere(@PathVariable Long id) {
		intrusionService.quittiere(id);
		return "redirect:/";
	}

	/**
	 * Setzt Name, Klasse und Personenzuordnung eines Geraets und laedt das
	 * Dashboard danach neu (Redirect-after-POST). Unbekannte IDs werden
	 * stillschweigend ignoriert.
	 *
	 * @param id       ID des Geraets
	 * @param name     neuer Geraetename; leer -> null (Hersteller-Fallback greift)
	 * @param status   neue Klassifizierung; null -> unveraendert
	 * @param personId zuzuordnende Person; leer/null -> keine Zuordnung
	 */
	@PostMapping("/geraet/{id}/klassifizieren")
	@Transactional
	public String klassifiziere(@PathVariable Long id,
			@RequestParam(required = false) String name,
			@RequestParam(required = false) GeraeteStatus status,
			@RequestParam(required = false) String personId) {
		geraetRepository.findById(id).ifPresent(geraet -> {
			geraet.setName(name != null && !name.isBlank() ? name.trim() : null);
			if (status != null) {
				geraet.setStatus(status);
			}
			geraet.setPerson(loesePerson(personId));
			geraetRepository.save(geraet);
		});
		return "redirect:/";
	}

	/** Ermittelt die zuzuordnende Person; null bei leerer/unbekannter Auswahl. */
	private Person loesePerson(String personId) {
		if (personId == null || personId.isBlank()) {
			return null;
		}
		try {
			return personRepository.findById(Long.valueOf(personId)).orElse(null);
		} catch (NumberFormatException e) {
			return null;
		}
	}
}
