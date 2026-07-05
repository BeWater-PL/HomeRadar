package de.bewater.homeradar.scanner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import de.bewater.homeradar.model.ArpEintrag;

/**
 * Liest die Windows-ARP-Tabelle ueber "arp -a" aus, parst IP- und
 * MAC-Adressen und filtert Multicast- sowie Broadcast-Eintraege heraus.
 */
@Component
public class NetworkScanner {

	/** Windows-Konsolenausgabe ist in Westeuropa typischerweise Cp850. */
	private static final Charset ARP_CHARSET = Charset.forName("Cp850");

	/**
	 * Extrahiert IP (Gruppe 1) und MAC im Format xx-xx-xx-xx-xx-xx (Gruppe 2)
	 * aus einer ARP-Zeile.
	 */
	private static final Pattern ARP_PATTERN = Pattern.compile(
			"(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})\\s+"
					+ "([0-9a-fA-F]{2}(?:-[0-9a-fA-F]{2}){5})");

	private static final String BROADCAST_MAC = "ff:ff:ff:ff:ff:ff";
	private static final String MULTICAST_MAC_PREFIX = "01:00:5e";

	/**
	 * Fuehrt "arp -a" aus und liefert die gefilterten Eintraege zurueck.
	 *
	 * @return Liste der gefundenen Geraete (IP + normalisierte MAC)
	 */
	public List<ArpEintrag> scan() {
		List<ArpEintrag> eintraege = new ArrayList<>();
		ProcessBuilder builder = new ProcessBuilder("arp", "-a");
		builder.redirectErrorStream(true);
		try {
			Process process = builder.start();
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream(), ARP_CHARSET))) {
				String line;
				while ((line = reader.readLine()) != null) {
					ArpEintrag eintrag = parseLine(line);
					if (eintrag != null) {
						eintraege.add(eintrag);
					}
				}
			}
			process.waitFor();
		} catch (IOException e) {
			// ARP-Tabelle konnte nicht gelesen werden -> leere/teilweise Liste
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return eintraege;
	}

	/**
	 * Parst eine einzelne ARP-Zeile und liefert einen Eintrag, sofern die Zeile
	 * eine gueltige, nicht gefilterte IP/MAC-Kombination enthaelt, sonst null.
	 */
	private ArpEintrag parseLine(String line) {
		Matcher matcher = ARP_PATTERN.matcher(line);
		if (!matcher.find()) {
			return null;
		}
		String ip = matcher.group(1);
		String mac = normalizeMac(matcher.group(2));

		if (isMulticastOrBroadcastIp(ip)) {
			return null;
		}
		if (isMulticastOrBroadcastMac(mac)) {
			return null;
		}
		return new ArpEintrag(ip, mac);
	}

	/** Normalisiert xx-xx-... auf Kleinbuchstaben mit Doppelpunkt-Trennung. */
	private String normalizeMac(String mac) {
		return mac.toLowerCase().replace('-', ':');
	}

	/** Filtert Multicast (224.-239.) und Broadcast (.255 / 255.255.255.255). */
	private boolean isMulticastOrBroadcastIp(String ip) {
		if ("255.255.255.255".equals(ip)) {
			return true;
		}
		if (ip.endsWith(".255")) {
			return true;
		}
		int firstOctet = Integer.parseInt(ip.substring(0, ip.indexOf('.')));
		return firstOctet >= 224 && firstOctet <= 239;
	}

	/** Filtert Multicast-MACs (01-00-5e...) und die Broadcast-MAC. */
	private boolean isMulticastOrBroadcastMac(String mac) {
		return mac.startsWith(MULTICAST_MAC_PREFIX) || BROADCAST_MAC.equals(mac);
	}
}
