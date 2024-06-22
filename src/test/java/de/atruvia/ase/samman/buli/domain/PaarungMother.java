package de.atruvia.ase.samman.buli.domain;

import static de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp.BEENDET;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import static java.util.function.Predicate.not;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;

import java.net.URI;
import java.util.List;

import de.atruvia.ase.samman.buli.domain.Paarung.Entry;
import de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis;
import de.atruvia.ase.samman.buli.domain.Paarung.PaarungBuilder;

public final class PaarungMother {

	private PaarungMother() {
		super();
	}

	public static List<Paarung> createPaarungen(String firstTeam, Ergebnis... ergebnisse) {
		List<String> opponents = opponents(firstTeam, ergebnisse.length);
		return range(0, ergebnisse.length)
				.mapToObj(i -> swapIfOdd(i, paarung(firstTeam, opponents.get(i), ergebnisse[i]))).toList();
	}

	private static Paarung swapIfOdd(int index, Paarung paarung) {
		if (index % 2 == 0) {
			return paarung;
		}
		return paarung.toBuilder().heim(paarung.gast()).gast(paarung.heim()).build();
	}

	public static Paarung paarung(String heimTeam, String gastTeam, Ergebnis ergebnis) {
		PaarungBuilder builder = PaarungBuilder.paarung(heimTeam, gastTeam);
		return (switch (ergebnis) {
		case SIEG -> builder.endergebnis(MAX_VALUE, MIN_VALUE);
		case UNENTSCHIEDEN -> builder.endergebnis(MAX_VALUE, MAX_VALUE);
		case NIEDERLAGE -> builder.endergebnis(MIN_VALUE, MAX_VALUE);
		}).build();
	}

	public static Paarung paarungWithAllAttributesSet() {
		return new Paarung(BEENDET, //
				new Entry("IdentifierHeim", Team.builder().name("Heim").wappen(URI.create("WappenHeim")).build(), 1), //
				new Entry("IdentifierGast", Team.builder().name("Gast").wappen(URI.create("WappenGast")).build(), 2) //
		);
	}

	private static List<String> opponents(String firstTeam, int count) {
		return rangeClosed(1, MAX_VALUE).mapToObj(i -> "Opponent-" + i).filter(not(firstTeam::equals)).limit(count)
				.toList();
	}

}
