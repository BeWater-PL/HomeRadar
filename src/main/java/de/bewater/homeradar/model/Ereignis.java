package de.bewater.homeradar.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Alarm-Historie: ein potenzieller Eindringling (unbekanntes Geraet bei
 * abwesenden Bewohnern). Kann vom Nutzer als quittiert markiert werden.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Ereignis {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	private Geraet geraet;

	private LocalDateTime zeitpunkt;

	private String ipAdresse;

	/** true, sobald der Nutzer den Alarm als gesehen/abgehakt markiert hat. */
	private boolean quittiert = false;
}
