package de.bewater.homeradar.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

/**
 * Ermittelt anhand des OUI-Praefixes einer MAC-Adresse den Hersteller.
 * Grundlage ist die IEEE-MA-L-Liste (oui.csv im Classpath).
 */
@Service
@Slf4j
public class OuiLookupService {

	/** Name der OUI-Liste im Classpath. */
	private static final String OUI_DATEI = "oui.csv";

	/**
	 * Erfasst MA-L-Zeilen: Gruppe 1 = 6-stelliges OUI-Praefix, Gruppe 2 = in
	 * Anfuehrungszeichen stehender Name (mit Kommas), Gruppe 3 = einfacher Name.
	 */
	private static final Pattern MA_L_ZEILE = Pattern.compile(
			"^MA-L,([0-9A-Fa-f]{6}),(?:\"([^\"]*)\"|([^,]*))");

	/**
	 * Zweites Hex-Zeichen einer lokal verwalteten (randomisierten) MAC. Bei
	 * gesetztem Bit 2 des ersten Bytes ergeben sich diese Nibble-Werte; solche
	 * MACs sind nicht in der IEEE-Liste enthalten.
	 */
	private static final Set<Character> LOKAL_VERWALTET = Set.of('2', '6', 'a', 'e');

	/** OUI-Praefix (kleingeschrieben, ohne Trenner) -> Herstellername. */
	private final Map<String, String> herstellerNachPraefix = new HashMap<>();

	/** Laedt die OUI-Liste einmalig beim Start; scheitert fail-soft. */
	@PostConstruct
	void ladeOuiListe() {
		ClassPathResource resource = new ClassPathResource(OUI_DATEI);
		if (!resource.exists()) {
			log.warn("OUI-Datei {} nicht gefunden - Herstellererkennung ist deaktiviert.", OUI_DATEI);
			return;
		}
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
			String zeile;
			while ((zeile = reader.readLine()) != null) {
				verarbeiteZeile(zeile);
			}
			log.info("OUI-Liste geladen: {} Eintraege.", herstellerNachPraefix.size());
		} catch (Exception e) {
			log.error("Fehler beim Laden der OUI-Liste {}.", OUI_DATEI, e);
		}
	}

	/** Parst eine einzelne CSV-Zeile und uebernimmt gueltige MA-L-Eintraege. */
	private void verarbeiteZeile(String zeile) {
		Matcher matcher = MA_L_ZEILE.matcher(zeile);
		if (!matcher.find()) {
			return;
		}
		String praefix = matcher.group(1).toLowerCase();
		String name = matcher.group(2) != null ? matcher.group(2) : matcher.group(3);
		if (name != null && !name.isBlank()) {
			// Bei doppelten Praefixen gilt der erste Eintrag.
			herstellerNachPraefix.putIfAbsent(praefix, name.trim());
		}
	}

	/**
	 * Bildet den Anzeigenamen fuer ein Geraet: den gepflegten Namen, falls
	 * gesetzt, sonst einen Fallback mit erkanntem Hersteller.
	 *
	 * @param name gepflegter Geraetename (darf null/leer sein)
	 * @param mac  MAC-Adresse fuer die Herstellererkennung
	 * @return "&lt;name&gt;", sonst "Unbekanntes Ger&auml;t (&lt;Hersteller&gt;)"
	 *         bzw. "Unbekanntes Ger&auml;t"
	 */
	public String anzeigeName(String name, String mac) {
		if (name != null && !name.isBlank()) {
			return name;
		}
		return findeHersteller(mac)
				.map(hersteller -> "Unbekanntes Gerät (" + hersteller + ")")
				.orElse("Unbekanntes Gerät");
	}

	/**
	 * Ermittelt den Hersteller zu einer MAC-Adresse.
	 *
	 * @param mac MAC-Adresse mit ":"- oder "-"-Trennern (darf null sein)
	 * @return der Herstellername, oder leer bei unbekanntem Praefix, ungueltiger
	 *         Eingabe oder lokal verwalteter (randomisierter) MAC
	 */
	public Optional<String> findeHersteller(String mac) {
		if (mac == null) {
			return Optional.empty();
		}
		String normalisiert = mac.replace(":", "").replace("-", "").toLowerCase();
		if (normalisiert.length() < 6) {
			return Optional.empty();
		}
		// Randomisierte Smartphone-MACs stehen nie in der IEEE-Liste.
		if (LOKAL_VERWALTET.contains(normalisiert.charAt(1))) {
			return Optional.empty();
		}
		String praefix = normalisiert.substring(0, 6);
		return Optional.ofNullable(herstellerNachPraefix.get(praefix));
	}
}
