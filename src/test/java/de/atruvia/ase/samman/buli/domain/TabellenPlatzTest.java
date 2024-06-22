package de.atruvia.ase.samman.buli.domain;

import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.AUSWAERTS;
import static de.atruvia.ase.samman.buli.domain.Paarung.ViewDirection.HEIM;
import static de.atruvia.ase.samman.buli.domain.Team.TeamIdentifier.teamIdentifier;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;

import org.junit.jupiter.api.Test;

class TabellenPlatzTest {

	TabellenPlatz anyPlatz = TabellenPlatz.builder().team(teamIdentifier("anyIdentifier"), "team1")
			.wappen(URI.create("proto://wappen1")).build();

	/**
	 * Dieser Test ist ein Detailtest, welcher nicht nach TDD outside-in entstanden
	 * ist. Er dient dazu, die {@link TabellenPlatz#mergeWith(TabellenPlatz)}
	 * Methode für Lesende zu veranschaulichen.
	 */
	@Test
	void testMergeWith() {
		TabellenPlatz entry1 = anyPlatz.toBuilder().spiele(1).punkte(24) //
				.withTore(AUSWAERTS, 3).withGegentore(AUSWAERTS, 4) //
				.withTore(HEIM, 5).withGegentore(HEIM, 6) //
				.build();
		TabellenPlatz entry2 = anyPlatz.toBuilder().spiele(2).punkte(1) //
				.withTore(AUSWAERTS, 7).withGegentore(AUSWAERTS, 8) //
				.build();
		TabellenPlatz expected = anyPlatz.toBuilder().spiele(1 + 2).punkte(24 + 1) //
				.withTore(AUSWAERTS, 3 + 7).withGegentore(AUSWAERTS, 4 + 8) //
				.withTore(HEIM, 5).withGegentore(HEIM, 6) //
				.build();
		assertThat(entry1.mergeWith(entry2)).isEqualTo(expected);
	}

}
