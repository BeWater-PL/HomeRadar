package de.bewater.homeradar.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.bewater.homeradar.controller.dto.AnwesendePersonDto;
import de.bewater.homeradar.controller.dto.EreignisDto;
import de.bewater.homeradar.controller.dto.GeraetStatusDto;
import de.bewater.homeradar.controller.dto.PersonAnwesendDto;
import de.bewater.homeradar.service.AnwesenheitsService;
import de.bewater.homeradar.service.IntrusionService;
import de.bewater.homeradar.model.Person;
import de.bewater.homeradar.repository.PersonRepository;

/**
 * REST-Schnittstelle fuer Anwesenheits- und Geraetestatus. Liefert
 * ausschliesslich DTOs, um die bidirektionale Person/Geraet-Beziehung nicht
 * in die JSON-Serialisierung zu ziehen.
 */
@RestController
@RequestMapping("/api")
public class ApiController {

	private final StatusAssembler statusAssembler;
	private final AnwesenheitsService anwesenheitsService;
	private final IntrusionService intrusionService;
	private final PersonRepository personRepository;

	public ApiController(StatusAssembler statusAssembler, AnwesenheitsService anwesenheitsService,
			IntrusionService intrusionService, PersonRepository personRepository) {
		this.statusAssembler = statusAssembler;
		this.anwesenheitsService = anwesenheitsService;
		this.intrusionService = intrusionService;
		this.personRepository = personRepository;
	}

	/** Alle aktuell anwesenden Personen (Name + Anzahl online-Geraete). */
	@GetMapping("/anwesend")
	@Transactional(readOnly = true)
	public List<AnwesendePersonDto> anwesend() {
		return statusAssembler.anwesendePersonen();
	}

	/** Ob eine bestimmte Person anwesend ist; 404, wenn sie nicht existiert. */
	@GetMapping("/person/{name}/anwesend")
	@Transactional(readOnly = true)
	public ResponseEntity<PersonAnwesendDto> personAnwesend(@PathVariable String name) {
		return personRepository.findByName(name)
				.map(this::toAnwesendDto)
				.map(ResponseEntity::ok)
				.orElseGet(() -> ResponseEntity.notFound().build());
	}

	/** Alle Geraete mit Online-Status. */
	@GetMapping("/geraete")
	@Transactional(readOnly = true)
	public List<GeraetStatusDto> geraete() {
		return statusAssembler.alleGeraeteMitStatus();
	}

	/** Alle offenen (nicht quittierten) Alarme, neueste zuerst. */
	@GetMapping("/alarme")
	@Transactional(readOnly = true)
	public List<EreignisDto> alarme() {
		return statusAssembler.offeneAlarme();
	}

	/** Quittiert einen Alarm; 404, wenn er nicht existiert. */
	@PostMapping("/alarme/{id}/quittieren")
	public ResponseEntity<Void> quittiere(@PathVariable Long id) {
		return intrusionService.quittiere(id)
				? ResponseEntity.noContent().build()
				: ResponseEntity.notFound().build();
	}

	private PersonAnwesendDto toAnwesendDto(Person person) {
		return new PersonAnwesendDto(person.getName(), anwesenheitsService.istPersonAnwesend(person));
	}
}
