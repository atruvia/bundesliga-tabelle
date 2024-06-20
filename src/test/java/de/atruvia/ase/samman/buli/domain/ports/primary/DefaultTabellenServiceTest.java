package de.atruvia.ase.samman.buli.domain.ports.primary;

import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.NIEDERLAGE;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.SIEG;
import static de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis.UNENTSCHIEDEN;
import static de.atruvia.ase.samman.buli.infra.adapters.secondary.OpenLigaDbSpieltagRepoMother.spieltagFsRepo;
import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.empty;
import static org.approvaltests.Approvals.verify;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.net.URI;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.approvaltests.core.Options;
import org.approvaltests.core.Options.FileOptions;
import org.junit.jupiter.api.Test;

import de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis;
import de.atruvia.ase.samman.buli.domain.Paarung.PaarungView;
import de.atruvia.ase.samman.buli.domain.TabellenPlatz;

class DefaultTabellenServiceTest {

	private static final Map<String, Function<TabellenPlatz, Object>> extractors = extractors();

	private static Map<String, Function<TabellenPlatz, Object>> extractors() {
		Map<String, Function<TabellenPlatz, Object>> content = new LinkedHashMap<>();
		content.put("Platz", TabellenPlatz::platz);
		content.put("Logo", t -> image(t.wappen(), 32));
		content.put("Verein", TabellenPlatz::teamName);
		content.put("Sp", TabellenPlatz::spiele);
		content.put("S", TabellenPlatz::siege);
		content.put("U", TabellenPlatz::unentschieden);
		content.put("N", TabellenPlatz::niederlagen);
		content.put("T", TabellenPlatz::gesamtTore);
		content.put("GT", TabellenPlatz::gesamtGegentore);
		content.put("TD", TabellenPlatz::torDifferenz);
		content.put("Pkte", TabellenPlatz::punkte);
		content.put("Letzte 5", DefaultTabellenServiceTest::tendenz);
		content.put("Spiel", t -> laufendesSpiel(t));
		return content;
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

	private static void verifyTabelle(List<TabellenPlatz> tabelle) {
		var headers = extractors.keySet().toArray();
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
		return extractors.values().stream().map(c -> c.apply(tabellenPlatz)).map(Object::toString).collect(joiner());
	}

	private static Object image(URI uri, int height) {
		return format("<img src=\"%s\" height=\"" + height + "\" />", uri.toASCIIString());
	}

	private static String markdownSeparator(Object[] headers) {
		return Stream.of(headers).map(Object::toString).map(s -> s.replaceAll(".", "-")).collect(joiner());
	}

	@SafeVarargs
	private static <T> Stream<? extends T> concat(Stream<? extends T>... streams) {
		return stream(streams).reduce(Stream::concat).orElse(empty());
	}

	private static String markdownRow(Object... values) {
		return Stream.of(values).map(Object::toString).collect(joiner());
	}

	private static Collector<CharSequence, ?, String> joiner() {
		return joining("|", "|", "|");
	}

	private static String tendenz(TabellenPlatz tabellenPlatz) {
		return tabellenPlatz.tendenz().ergebnisse().stream().map(tendenzMap::get).map(String::valueOf)
				.collect(joining());
	}

	private static String laufendesSpiel(TabellenPlatz tabellenPlatz) {
		PaarungView laufendesSpiel = tabellenPlatz.laufendesSpiel();
		return laufendesSpiel == null //
				? "" //
				: format("%d:%d (%s)", laufendesSpiel.tore(), laufendesSpiel.gegentore(),
						laufendesSpiel.gegner().team());
	}

}
