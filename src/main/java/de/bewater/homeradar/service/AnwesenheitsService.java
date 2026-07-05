package de.bewater.homeradar.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import de.bewater.homeradar.config.ScanConfig;
import de.bewater.homeradar.model.Geraet;
import de.bewater.homeradar.model.Person;
import de.bewater.homeradar.repository.PersonRepository;

/**
 * Leitet aus der Sichtbarkeit der Geraete die Anwesenheit von Personen ab.
 */
@Service
public class AnwesenheitsService {

	private final ScanConfig scanConfig;
	private final PersonRepository personRepository;

	public AnwesenheitsService(ScanConfig scanConfig, PersonRepository personRepository) {
		this.scanConfig = scanConfig;
		this.personRepository = personRepository;
	}

	/**
	 * Ein Geraet gilt als online, wenn es zuletzt innerhalb der konfigurierten
	 * Toleranz gesehen wurde.
	 */
	public boolean istGeraetOnline(Geraet g) {
		LocalDateTime zuletztGesehen = g.getZuletztGesehen();
		if (zuletztGesehen == null) {
			return false;
		}
		LocalDateTime grenze = LocalDateTime.now().minusMinutes(scanConfig.getToleranzMinuten());
		return zuletztGesehen.isAfter(grenze);
	}

	/**
	 * Eine Person ist anwesend, wenn mindestens eines ihrer Geraete online ist.
	 */
	public boolean istPersonAnwesend(Person p) {
		return p.getGeraete().stream().anyMatch(this::istGeraetOnline);
	}

	/**
	 * Liefert alle aktuell anwesenden Personen.
	 */
	public List<Person> getAnwesendePersonen() {
		return personRepository.findAll().stream()
				.filter(this::istPersonAnwesend)
				.toList();
	}
}
