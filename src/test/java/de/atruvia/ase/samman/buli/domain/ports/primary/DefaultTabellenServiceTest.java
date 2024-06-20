package de.atruvia.ase.samman.buli.domain.ports.primary;

import static de.atruvia.ase.samman.buli.infra.adapters.secondary.OpenLigaDbSpieltagRepoMother.spieltagFsRepo;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.concat;
import static org.approvaltests.Approvals.verify;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.approvaltests.core.Options;
import org.approvaltests.core.Options.FileOptions;
import org.junit.jupiter.api.Test;

import de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis;
import de.atruvia.ase.samman.buli.domain.Paarung.PaarungView;
import de.atruvia.ase.samman.buli.domain.TabellenPlatz;
import de.atruvia.ase.samman.buli.domain.TabellenPlatz.Tendenz;

class DefaultTabellenServiceTest {

	@Test
	void tabelleBl12022Spieltag24() {
		TabellenService sut = new DefaultTabellenService(spieltagFsRepo());
		verifyTabelle(sut.erstelleTabelle("bl1", "2022"));
	}

	@Test
	void tabelleBl12023Spieltag27_gamesRunning_goalsButFinalResultsAre_0_0() {
		TabellenService sut = new DefaultTabellenService(spieltagFsRepo());
		verifyTabelle(sut.erstelleTabelle("bl1", "2023-games-running"));
	}

	@Test
	void tabelleBl12023Spieltag27_gamesRunning_goalsAndFinalResultsAreCorrect() {
		TabellenService sut = new DefaultTabellenService(spieltagFsRepo());
		verifyTabelle(sut.erstelleTabelle("bl1", "2023-games-running-correct-final-result"));
	}

	private static void verifyTabelle(List<TabellenPlatz> tabelle) {
		var header = Stream.of(Stream.of( //
				"Mannschaft", "Spiele", "Siege", "Unentschieden", "Niederlagen", "Tore", "Gegentore", "Tordifferenz",
				"Punkte", "Tendenz", "Laufendes Spiel", "Wappen").collect(joining(",")));
		var content = tabelle.stream().map(f -> print(f, longest(tabelle, TabellenPlatz::teamName)));
		verify(concat(header, content).collect(joining("\n")), csv());
	}

	static Options csv() {
		return new FileOptions(Map.of()).withExtension(".csv");
	}

	@Test
	void whenRepoThrowsExceptionThenTheServiceThrowsTheException() {
		String message = "some data load error";
		TabellenService sut = new DefaultTabellenService((__i1, __i2) -> {
			throw new RuntimeException(message);
		});
		assertThatExceptionOfType(RuntimeException.class).isThrownBy(() -> sut.erstelleTabelle("bl1", "2022"))
				.withFailMessage(message);
	}

	static int longest(List<TabellenPlatz> tabellenPlaetze, Function<TabellenPlatz, String> attribute) {
		return tabellenPlaetze.stream().map(attribute).mapToInt(String::length).max().orElse(0);
	}

	static String print(TabellenPlatz tabellenPlatz, int length) {
		return Stream.of( //
				tabellenPlatz.teamName(), //
				tabellenPlatz.spiele(), //
				tabellenPlatz.siege(), //
				tabellenPlatz.unentschieden(), //
				tabellenPlatz.niederlagen(), //
				tabellenPlatz.gesamtTore(), //
				tabellenPlatz.gesamtGegentore(), //
				tabellenPlatz.torDifferenz(), //
				tabellenPlatz.punkte(), //
				toString(tabellenPlatz.tendenz()), //
				toString(tabellenPlatz.laufendesSpiel()), //
				tabellenPlatz.wappen() //
		).map(Object::toString).collect(joining(","));
	}

	static String toString(Tendenz tendenz) {
		return tendenz.ergebnisse().stream().map(Ergebnis::name).map(n -> n.substring(0, 1)).collect(joining());
	}

	static String toString(PaarungView laufendesSpiel) {
		if (laufendesSpiel == null) {
			return "";
		}
		return format("%d:%d (%s)", laufendesSpiel.tore(), laufendesSpiel.gegentore(), laufendesSpiel.gegner().team());
	}

}
