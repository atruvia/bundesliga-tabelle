package de.atruvia.ase.samman.buli.domain.ports.primary;

import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.NIEDERLAGE;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.SIEG;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.UNENTSCHIEDEN;
import static de.atruvia.ase.samman.buli.infra.adapters.secondary.OpenLigaDbSpieltagRepoMother.spieltagFsRepo;
import static de.atruvia.ase.samman.buli.util.Streams.concat;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.Map.entry;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.approvaltests.Approvals.verify;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.net.URI;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Stream;

import org.approvaltests.core.Options;
import org.approvaltests.core.Options.FileOptions;
import org.junit.jupiter.api.Test;

import de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis;
import de.atruvia.ase.samman.buli.domain.Tabelle;
import de.atruvia.ase.samman.buli.domain.TabellenPlatz;

public class DefaultTabellenServiceTest {

	private static final Map<String, Function<TabellenPlatz, Object>> attributes = attributeExtractors()
			.collect(toMap(Entry::getKey, Entry::getValue, (x, y) -> y, LinkedHashMap::new));

	private static Stream<Entry<String, Function<TabellenPlatz, Object>>> attributeExtractors() {
		return Stream.of( //
				entry("Platz", TabellenPlatz::platz), //
				entry("Logo", t -> image(t.wappen(), 32)), //
				entry("Verein", TabellenPlatz::teamName), //
				entry("Sp", TabellenPlatz::spiele), //
				entry("S", TabellenPlatz::siege), //
				entry("U", TabellenPlatz::unentschieden), //
				entry("N", TabellenPlatz::niederlagen), //
				entry("T", TabellenPlatz::gesamtTore), //
				entry("GT", TabellenPlatz::gesamtGegentore), //
				entry("TD", TabellenPlatz::torDifferenz), //
				entry("Pkte", TabellenPlatz::punkte), //
				entry("Letzte 5", DefaultTabellenServiceTest::tendenz), //
				entry("Spiel", DefaultTabellenServiceTest::laufendesSpiel) //
		);
	}

	private static final Map<Ergebnis, Character> tendenzMap = new EnumMap<>(Map.of( //
			SIEG, '✅', //
			UNENTSCHIEDEN, '➖', //
			NIEDERLAGE, '❌' //
	));

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

	private static void verifyTabelle(Tabelle tabelle) {
		var headerNames = attributes.keySet().toArray(String[]::new);
		var headerRow = Stream.of(markdownRow(headerNames));
		var separatorRow = Stream.of(markdownSeparator(headerNames));
		var contentRows = tabelle.getEntries().stream().map(DefaultTabellenServiceTest::print);
		verify(concat(headerRow, separatorRow, contentRows).collect(joining("\n")), markdown());
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
		return collectToMarkdownString(valuesOf(tabellenPlatz));
	}

	private static Stream<Object> valuesOf(TabellenPlatz tabellenPlatz) {
		return attributes.values().stream().map(c -> c.apply(tabellenPlatz));
	}

	private static Object image(URI uri, int height) {
		return format("<p align=\"center\"><img src=\"%s\" height=\"%d\"/></p>", uri.toASCIIString(), height);
	}

	private static String markdownSeparator(String[] headers) {
		return collectToMarkdownString(Stream.of(headers).map(s -> s.replaceAll(".", "-")));
	}

	private static String markdownRow(String... values) {
		return collectToMarkdownString(stream(values));
	}

	private static String collectToMarkdownString(Stream<Object> values) {
		return values.map(Object::toString).collect(joining("|", "|", "|"));
	}

	private static String tendenz(TabellenPlatz tabellenPlatz) {
		return tabellenPlatz.tendenz().ergebnisse().stream().map(tendenzMap::get).map(String::valueOf)
				.collect(joining());
	}

	private static String laufendesSpiel(TabellenPlatz tabellenPlatz) {
		var laufendesSpiel = tabellenPlatz.laufendesSpiel();
		return laufendesSpiel == null //
				? "" //
				: format("%d:%d (%s)", laufendesSpiel.tore(), laufendesSpiel.gegentore(),
						laufendesSpiel.gegner().team().name());
	}

}
