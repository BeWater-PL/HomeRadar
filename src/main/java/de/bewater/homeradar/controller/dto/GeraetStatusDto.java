package de.bewater.homeradar.controller.dto;

import java.time.LocalDateTime;

import de.bewater.homeradar.model.GeraeteStatus;

/**
 * Statussicht auf ein Geraet fuer API und Dashboard.
 *
 * @param id             ID des Geraets (fuer die Klassifizier-Formulare)
 * @param mac            MAC-Adresse (normalisiert)
 * @param name           frei vergebener Geraetename, ggf. null
 * @param anzeigeName    Name fuer die Anzeige (Fallback ueber Hersteller)
 * @param ipZuletzt      zuletzt gesehene IP-Adresse, ggf. null
 * @param zuletztGesehen Zeitpunkt der letzten Sichtung, ggf. null
 * @param online         ob das Geraet aktuell als online gilt
 * @param personId       ID der zugeordneten Person, oder null
 * @param personName     Name der zugeordneten Person, oder null
 * @param status         Klassifizierung (BEWOHNER/INFRASTRUKTUR/UNBEKANNT)
 */
public record GeraetStatusDto(
		Long id,
		String mac,
		String name,
		String anzeigeName,
		String ipZuletzt,
		LocalDateTime zuletztGesehen,
		boolean online,
		Long personId,
		String personName,
		GeraeteStatus status) {
}
