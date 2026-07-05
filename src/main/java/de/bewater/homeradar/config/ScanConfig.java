package de.bewater.homeradar.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Konfiguration des Netzwerk-Scans, gebunden an den Prefix "homeradar".
 */
@ConfigurationProperties(prefix = "homeradar")
@Getter
@Setter
public class ScanConfig {

	/** Toleranz in Minuten, innerhalb derer ein Geraet als online gilt. */
	private int toleranzMinuten = 5;

	/** Zu scannendes /24-Subnetz (erste drei Oktette). */
	private String subnetz = "192.168.178";
}
