package de.atruvia.ase.samman.buli.domain;

import org.jmolecules.ddd.annotation.ValueObject;

import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Accessors(fluent = true)
@ValueObject
public class Tore implements Comparable<Tore> {

	public static final Tore NULL = new Tore(0);

	int anzahl;

	public static Tore tore(int anzahl) {
		if (anzahl < 0) {
			throw new IllegalArgumentException("Value must not be negative bus was " + anzahl);
		}
		return new Tore(anzahl);
	}

	public Tore add(Tore other) {
		return new Tore(anzahl + other.anzahl);
	}

	public Tore minus(Tore other) {
		return new Tore(anzahl - other.anzahl);
	}

	@Override
	public int compareTo(Tore other) {
		return Integer.compare(anzahl, other.anzahl);
	}

}
