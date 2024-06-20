package de.atruvia.ase.samman.buli.domain.ports.primary;

import static de.atruvia.ase.samman.buli.infra.adapters.secondary.OpenLigaDbSpieltagRepoMother.spieltagFsRepo;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static org.approvaltests.Approvals.verify;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
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
		Object[] headers = { "Logo", "Verein", "Sp", "S", "U", "N", "T",
				"GT", "TD", "Pkte", "Letzte 5", "Spiel" };
		var header = Stream.of(markdownRow(headers));
		var separator = Stream.of(markdownSeparator(headers));
		var content = tabelle.stream().map(DefaultTabellenServiceTest::print);
		verify(concat(header, separator, content).collect(joining("\n")), markdown());
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

	private static Options markdown() {
		return new FileOptions(Map.of()).withExtension(".md");
	}

	private static String print(TabellenPlatz tabellenPlatz) {
		return markdownRow( //
				image(tabellenPlatz.wappen(), 32), //
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
				toString(tabellenPlatz.laufendesSpiel()) //
		);
	}

	private static Object image(URI uri, int height) {
		return format("<img src=\"%s\" height=\"" + height + "\" />", uri.toASCIIString());
	}

	private static String markdownSeparator(Object[] headers) {
		return Stream.of(addFirstAndLast(headers, "")).map(Object::toString).map(s -> s.replaceAll(".", "-"))
				.collect(joiner());
	}

	@SafeVarargs
	private static <T> Stream<? extends T> concat(Stream<? extends T>... streams) {
		return Arrays.stream(streams).reduce(Stream::concat).orElse(Stream.empty());
	}

	private static Object[] addFirstAndLast(Object[] objects, String firstAndLastElement) {
		return concat( //
				Stream.of(firstAndLastElement), //
				Stream.of(objects), //
				Stream.of(firstAndLastElement) //
		).toArray();
	}

	private static String markdownRow(Object... values) {
		return Stream.of(addFirstAndLast(values, "")).map(Object::toString).collect(joiner());
	}

	private static Collector<CharSequence, ?, String> joiner() {
		return joining("|");
	}

	private static String toString(Tendenz tendenz) {
		return tendenz.ergebnisse().stream().map(Ergebnis::name).map(n -> n.substring(0, 1)).collect(joining());
	}

	private static String toString(PaarungView laufendesSpiel) {
		if (laufendesSpiel == null) {
			return "";
		}
		return format("%d:%d (%s)", laufendesSpiel.tore(), laufendesSpiel.gegentore(), laufendesSpiel.gegner().team());
	}

}
