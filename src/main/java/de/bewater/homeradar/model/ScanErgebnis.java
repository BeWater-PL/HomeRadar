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
 * Historieneintrag: haelt fest, dass ein Geraet zu einem bestimmten
 * Zeitpunkt unter einer bestimmten IP-Adresse gesehen wurde.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class ScanErgebnis {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	private Geraet geraet;

	private LocalDateTime zeitpunkt;

	private String ipAdresse;
}
