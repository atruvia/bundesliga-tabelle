package de.atruvia.ase.samman.buli.domain;

import static de.atruvia.ase.samman.buli.domain.Paarung.Entry.entry;
import static de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp.BEENDET;
import static de.atruvia.ase.samman.buli.domain.Team.TeamId.teamId;
import static java.lang.Integer.MAX_VALUE;
import static java.util.function.Predicate.not;
import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;

import java.net.URI;
import java.util.List;

import de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis;
import de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp;
import de.atruvia.ase.samman.buli.domain.Paarung.PaarungBuilder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaarungMother {

	public static List<Paarung> createPaarungen(String firstTeam, Ergebnis... ergebnisse) {
		List<String> opponents = opponents(firstTeam, ergebnisse.length);
		return range(0, ergebnisse.length)
				.mapToObj(i -> swapIfOdd(i, paarung(firstTeam, opponents.get(i), ergebnisse[i], BEENDET))).toList();
	}

	private static Paarung swapIfOdd(int index, Paarung paarung) {
		if (index % 2 == 0) {
			return paarung;
		}
		return paarung.toBuilder().heim(paarung.gast()).gast(paarung.heim()).build();
	}

	public static Paarung paarung(String heimTeam, String gastTeam, Ergebnis ergebnis, ErgebnisTyp ergebnisTyp) {
		PaarungBuilder builder = PaarungBuilder.paarung(heimTeam, gastTeam);
		return (switch (ergebnis) {
		case SIEG -> builder.ergebnis(ergebnisTyp, MAX_VALUE, 0);
		case UNENTSCHIEDEN -> builder.ergebnis(ergebnisTyp, MAX_VALUE, MAX_VALUE);
		case NIEDERLAGE -> builder.ergebnis(ergebnisTyp, 0, MAX_VALUE);
		}).build();
	}

	public static Paarung paarungWithAllAttributesSet() {
		return new Paarung(BEENDET, //
				entry(Team.builder().id(teamId("Heim")).name("Heim").wappen(URI.create("WappenHeim")).build(), 1), //
				entry(Team.builder().id(teamId("Gast")).name("Gast").wappen(URI.create("WappenGast")).build(), 2) //
		);
	}

	private static List<String> opponents(String firstTeam, int count) {
		return rangeClosed(1, MAX_VALUE).mapToObj(i -> "Opponent-" + i).filter(not(firstTeam::equals)).limit(count)
				.toList();
	}

}
