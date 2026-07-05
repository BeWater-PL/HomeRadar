package de.bewater.homeradar.controller.dto;

/**
 * Kompakte Sicht auf eine anwesende Person: Name und Anzahl online-Geraete.
 */
public record AnwesendePersonDto(String name, long onlineGeraete) {
}
