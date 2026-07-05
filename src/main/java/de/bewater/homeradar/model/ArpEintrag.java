package de.bewater.homeradar.model;

/**
 * Ein Eintrag aus der ARP-Tabelle: eine IP-Adresse mit zugehoeriger
 * MAC-Adresse (normalisiert als xx:xx:xx:xx:xx:xx in Kleinbuchstaben).
 */
public record ArpEintrag(String ip, String mac) {
}
