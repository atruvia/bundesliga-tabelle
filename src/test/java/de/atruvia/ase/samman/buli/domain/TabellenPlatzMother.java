package de.atruvia.ase.samman.buli.domain;

import static de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp.BEENDET;
import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.HEIM;
import static de.atruvia.ase.samman.buli.domain.PaarungMother.paarung;
import static java.util.Arrays.asList;
import static lombok.AccessLevel.PRIVATE;

import java.util.List;
import java.util.stream.Stream;

import de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis;
import de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class TabellenPlatzMother {

	/**
	 * Erzeugt die Liste der Ergebnisse, wie sie {@link Tabelle} auch erzeugt. (TODO
	 * sollten wir das mit einem Spy sicherstellen?)
	 * 
	 * @param ergebnisse Ergebnistypen die über die merge Funktion zu einer Liste
	 *                   zusammengefasst werden sollen
	 * @return Liste der Ergebnisse des gemergten Tabellenplatzes
	 */
	public static TabellenPlatz platzWith(Ergebnis... ergebnisse) {
		return platzWith(asList(ergebnisse));
	}

	/**
	 * Erzeugt die Liste der Ergebnisse, wie sie {@link Tabelle} auch erzeugt. (TODO
	 * sollten wir das mit einem Spy sicherstellen?)
	 * 
	 * @param ergebnisse Ergebnistypen die über die merge Funktion zu einer Liste
	 *                   zusammengefasst werden sollen
	 * @return Liste der Ergebnisse des gemergten Tabellenplatzes
	 */
	public static TabellenPlatz platzWith(List<Ergebnis> ergebnisse) {
		// wir könnten hier direkt die Ergebnisse ablegen, allerdings bestünde damit die
		// Gefahr, dass wir Tabellenplätze anders erzeugen, als es die echte Tabelle
		// tut (z.B. Tabelle könnte bei Spielen mit "SIEG", "UNENTSCHIEDEN",
		// "NIEDERLAGE" Ergebnisse "NIEDERLAGE", "UNENTSCHIEDEN", "SIEG" erzeugen und
		// wir würden hier "SIEG", "UNENTSCHIEDEN", "NIEDERLAGE" ablegen).
		// Wir verlassen uns allerdings hier auch darauf, dass die echte Tabelle
		// TabellenPlatz::merge nutzt.
		return merge(ergebnisse.stream().map(TabellenPlatzMother::platzWith));
	}

	public static TabellenPlatz merge(Stream<TabellenPlatz> tabellenPlaetze) {
		return tabellenPlaetze.reduce(TabellenPlatz::mergeWith).orElseGet(() -> TabellenPlatz.builder().build());
	}

	private static TabellenPlatz platzWith(Ergebnis ergebnis) {
		return platzWith(ergebnis, BEENDET);
	}

	public static TabellenPlatz platzWith(Ergebnis ergebnis, ErgebnisTyp ergebnisTyp) {
		var paarung = paarung("same object for all", "opposite id", ergebnis, BEENDET);
		return TabellenPlatz.builder().identifier(paarung.heim().team().identifier()).paarung(paarung.viewForTeam(HEIM))
				.build();
	}

}
