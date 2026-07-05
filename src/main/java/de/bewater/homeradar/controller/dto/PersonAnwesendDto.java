package de.bewater.homeradar.controller.dto;

/**
 * Antwort auf die Frage, ob eine bestimmte Person anwesend ist.
 */
public record PersonAnwesendDto(String name, boolean anwesend) {
}
