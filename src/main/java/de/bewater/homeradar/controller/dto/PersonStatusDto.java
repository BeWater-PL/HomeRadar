package de.bewater.homeradar.controller.dto;

/**
 * Statuszeile fuer einen Bewohner im Dashboard: Name, Anwesenheit und
 * Anzahl aktuell online-Geraete.
 */
public record PersonStatusDto(String name, boolean anwesend, long onlineGeraete) {
}
