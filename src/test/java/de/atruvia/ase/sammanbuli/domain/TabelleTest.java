package de.atruvia.ase.sammanbuli.domain;

import static de.atruvia.ase.sammanbuli.domain.Paarung.Ergebnis.NIEDERLAGE;
import static de.atruvia.ase.sammanbuli.domain.Paarung.Ergebnis.SIEG;
import static de.atruvia.ase.sammanbuli.domain.Paarung.Ergebnis.UNENTSCHIEDEN;
import static java.util.Collections.emptyMap;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import de.atruvia.ase.sammanbuli.domain.Paarung.Ergebnis;
import de.atruvia.ase.sammanbuli.domain.TabelleTest.TabellenPlatz;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.Value;
import lombok.With;

class TabelleTest {

	@RequiredArgsConstructor
	@ToString
	public static class OrdnungsElement implements Comparable<OrdnungsElement> {

//	    MIS: Head-to-head goal difference: The goal difference in the matches played between the tied teams.
//	    MIS: Head-to-head away goals scored: The total number of away goals scored by the tied teams in the matches played between them.
//	    HIT: Overall goal difference: The difference between the number of goals scored and the number of goals conceded in all matches played.
//	    HIT: Overall goals scored: The total number of goals scored in all matches played.
//	    HIT: Overall away goals scored: The total number of goals scored by the tied teams in matches played away from their home stadium.
//	    MIS: Overall away goal difference: The difference between the number of away goals scored and the number of away goals conceded by the tied teams in matches played away from their home stadium.
//		If two or more teams have the same rank in the Bundesliga and there is no other criteria that can be used to separate them, then the teams will be listed in alphabetical order according to their full club name.

		private static final Comparator<OrdnungsElement> comparator = comparing(
				(OrdnungsElement e) -> e.tabellenPlatz.getPunkte())
				.thenComparing(comparing(e -> e.tabellenPlatz.getTorDifferenz()))
				.thenComparing(comparing(e -> e.tabellenPlatz.getTore()))
				.thenComparing(comparing(e -> e.tabellenPlatz.getToreAuswaerts())) //
				.reversed();

		private final TabellenPlatz tabellenPlatz;

		@Override
		public int hashCode() {
			return Objects.hash(tabellenPlatz.getPunkte(), tabellenPlatz.getTorDifferenz(), tabellenPlatz.getTore(),
					tabellenPlatz.getToreAuswaerts());
		}

		@Override
		public boolean equals(Object other) {
			return this == other || (other != null && getClass() == other.getClass()
					&& comparator.compare(this, (OrdnungsElement) other) == 0);
		}

		@Override
		public int compareTo(OrdnungsElement other) {
			return comparator.thenComparing(comparing(e -> e.tabellenPlatz.getTeam())).compare(this, other);
		}

	}

	@Value
	@Builder
	static class TabellenPlatz {

		static TabellenPlatz NULL = new TabellenPlatz(0, "", 0, emptyMap(), 0, 0, 0, 0, 0);

		@With
		int platz;
		@With
		String team;
		@Builder.Default
		int spiele = 1;
		Map<Ergebnis, Integer> ergebnisse;
		int punkte;
		int toreHeim;
		int toreAuswaerts;
		int gegentoreHeim;
		int gegentoreAuswaerts;

		int getTore() {
			return toreHeim + toreAuswaerts;
		}

		int getGegentore() {
			return gegentoreHeim + gegentoreAuswaerts;
		}

		static class TabellenPlatzBuilder {

			TabellenPlatzBuilder() {
				ergebnisse = new HashMap<>();
			}

			public TabellenPlatz.TabellenPlatzBuilder ergebnis(Ergebnis ergebnis) {
				ergebnisse.merge(ergebnis, 1, (a, b) -> a + b);
				return this;
			}

		}

		public int getTorDifferenz() {
			return getTore() - getGegentore();
		}

		public TabellenPlatz merge(TabellenPlatz other) {
			return TabellenPlatz.builder() //
					.ergebnisse(merge(this.ergebnisse, other.ergebnisse)) //
					.spiele(this.spiele + other.spiele) //
					.punkte(this.punkte + other.punkte) //
					.toreHeim(this.toreHeim + other.toreHeim) //
					.toreAuswaerts(this.toreAuswaerts + other.toreAuswaerts) //
					.gegentoreHeim(this.gegentoreHeim + other.gegentoreHeim) //
					.gegentoreAuswaerts(this.gegentoreAuswaerts + other.gegentoreAuswaerts) //
					.build();
		}

		private static Map<Ergebnis, Integer> merge(Map<Ergebnis, Integer> map1, Map<Ergebnis, Integer> map2) {
			Map<Ergebnis, Integer> map = new HashMap<>();
			map.putAll(map1);
			map.putAll(map2);
			return map;
		}

		public int getGewonnen() {
			return ergebnis(SIEG);
		}

		public int getUnentschieden() {
			return ergebnis(UNENTSCHIEDEN);
		}

		public int getVerloren() {
			return ergebnis(NIEDERLAGE);
		}

		private int ergebnis(Ergebnis type) {
			return ergebnisse.getOrDefault(type, 0);
		}

	}

	private Paarung[] paarungen;
	private Tabelle sut = new Tabelle();

	@Test
	void zweiMannschaftenKeinSpiel() {
		gegenSeienDiePaarungen(paarung("Team 1", "Team 2"), paarung("Team 2", "Team 1"));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle("""
				1|Team 1|0|0|0|0|0|0|0|0
				1|Team 2|0|0|0|0|0|0|0|0""");
	}

	@Test
	void zweiMannschaftenEinSpielKeineTore() {
		gegenSeienDiePaarungen(paarung("Team 1", "Team 2").ergebnis(0, 0), paarung("Team 2", "Team 1"));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle("""
				1|Team 1|1|0|1|0|1|0|0|0
				1|Team 2|1|0|1|0|1|0|0|0""");
	}

	@Test
	void mannschaftMitMehrPunktenIstWeiterOben() {
		gegenSeienDiePaarungen(paarung("Team 1", "Team 2").ergebnis(0, 1), paarung("Team 2", "Team 1"));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle("""
				1|Team 2|1|1|0|0|3|1|0|1
				2|Team 1|1|0|0|1|0|0|1|-1""");
	}

	@Test
	void zweiMannschaftenZweiSpieleMitToren() {
		gegenSeienDiePaarungen(paarung("Team 1", "Team 2").ergebnis(1, 0), paarung("Team 2", "Team 1").ergebnis(1, 0));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle("""
				1|Team 1|2|1|0|1|3|1|1|0
				1|Team 2|2|1|0|1|3|1|1|0""");
	}

	@Test
	void punktUndTorGleichAberMehrAUswärtsTore() {
		gegenSeienDiePaarungen(paarung("Team 1", "Team 2").ergebnis(1, 2), paarung("Team 2", "Team 1").ergebnis(0, 1));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle("""
				1|Team 2|2|1|0|1|3|2|2|0
				2|Team 1|2|1|0|1|3|2|2|0""");
	}

	private Paarung.PaarungBuilder paarung(String team1, String team2) {
		return Paarung.builder().team1(team1).team2(team2);
	}

	private void gegenSeienDiePaarungen(Paarung.PaarungBuilder... paarungen) {
		this.paarungen = Arrays.stream(paarungen).map(Paarung.PaarungBuilder::build).toArray(Paarung[]::new);
	}

	private void wennDieTabelleBerechnetWird() {
		Arrays.stream(this.paarungen).forEach(sut::add);
	}

	private void dannIstDieTabelle(String expected) {
		assertThat(print(sut.getEntries())).isEqualTo(expected);
	}

	private String print(List<TabellenPlatz> plaetze) {
		return plaetze.stream().map(this::print).collect(joining("\n"));
	}

	private String print(TabellenPlatz platz) {
		return Arrays
				.asList(platz.getPlatz(), platz.getTeam(), platz.getSpiele(), platz.getGewonnen(),
						platz.getUnentschieden(), platz.getVerloren(), platz.getPunkte(), platz.getTore(),
						platz.getGegentore(), platz.getTorDifferenz())
				.stream().map(Objects::toString).collect(joining("|"));
	}

}
