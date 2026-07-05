package de.bewater.homeradar.model;

/**
 * Klassifizierung eines Geraets fuer die Intrusion-Erkennung.
 */
public enum GeraeteStatus {

	/** Einem Bewohner zugeordnetes, vertrauenswuerdiges Geraet. */
	BEWOHNER,

	/** Fest installierte Netzwerk-Infrastruktur (Router, Drucker, ...). */
	INFRASTRUKTUR,

	/** Noch nicht eingeordnetes Geraet. */
	UNBEKANNT
}
