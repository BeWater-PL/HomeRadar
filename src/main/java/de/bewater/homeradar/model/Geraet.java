package de.bewater.homeradar.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ein im Netzwerk erkanntes Geraet, eindeutig ueber seine MAC-Adresse.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Geraet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(unique = true, nullable = false)
	private String mac;

	private String name;

	private String typ;

	private LocalDateTime zuletztGesehen;

	private LocalDateTime erstmalsGesehen;

	/** Zugeordnete Person; null bei noch nicht zugeordneten Geraeten. */
	@ManyToOne
	@JoinColumn(name = "person_id")
	private Person person;

	/** Klassifizierung des Geraets; neue Geraete starten als UNBEKANNT. */
	@Enumerated(EnumType.STRING)
	private GeraeteStatus status = GeraeteStatus.UNBEKANNT;
}
