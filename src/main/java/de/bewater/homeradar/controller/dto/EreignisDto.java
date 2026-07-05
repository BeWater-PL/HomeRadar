package de.bewater.homeradar.controller.dto;

import java.time.LocalDateTime;

/**
 * Web-Sicht auf einen offenen Alarm.
 *
 * @param id         ID des Ereignisses (zum Quittieren)
 * @param mac        MAC-Adresse des ausloesenden Geraets
 * @param geraetName frei vergebener Geraetename, ggf. null
 * @param ipAdresse  IP-Adresse zum Zeitpunkt des Alarms
 * @param zeitpunkt  Zeitpunkt des Alarms
 */
public record EreignisDto(
		Long id,
		String mac,
		String geraetName,
		String ipAdresse,
		LocalDateTime zeitpunkt) {
}
