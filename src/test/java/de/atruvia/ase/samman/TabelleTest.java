package de.atruvia.ase.samman;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import lombok.Builder;
import lombok.Value;

class TabelleTest {

	private static final String TEAM_1 = "Team 1";
	private static final String TEAM_2 = "Team 2";

	@Value
	@Builder
	private static class TabellenPlatz {
		int platz;
		String team;
		int punkte;
		int tore;
		int gegentore;
	}

	@Value
	@Builder(toBuilder = true)
	private static class Paarung {
		boolean gespielt;
		String team1, team2;
		int tore, gegentore;

		int punkte() {
			if (!gespielt)
				return 0;
			return tore > gegentore ? 3 : tore == gegentore ? 1 : 0;
		}

		private Paarung reverse() {
			return toBuilder().team1(team2).team2(team1).tore(gegentore).gegentore(tore).build();
		}

		private static class PaarungBuilder {

			public PaarungBuilder score(int tore, int gegentore) {
				this.gespielt = true;
				this.tore = tore;
				this.gegentore = gegentore;
				return this;
			}

		}

	}

	private Paarung[] paarungen;
	private TabellenPlatz[] tabelle;

	@Test
	void zweiMannschaftenKeinSpiel() {
		gegenSeienDiePaarungen(paarung(TEAM_1, TEAM_2), paarung(TEAM_2, TEAM_1));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle(tabellenplatz().platz(1).team(TEAM_1), tabellenplatz().platz(1).team(TEAM_2));
	}

	@Test
	void zweiMannschaftenEinSpielKeineTore() {
		gegenSeienDiePaarungen(paarung(TEAM_1, TEAM_2).score(0, 0), paarung(TEAM_2, TEAM_1));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				tabellenplatz().platz(1).team(TEAM_1).punkte(1), //
				tabellenplatz().platz(1).team(TEAM_2).punkte(1) //
		);
	}

	@Test
	void zweiMannschaftenZweiSpieleMitToren() {
		gegenSeienDiePaarungen(paarung(TEAM_1, TEAM_2).score(1, 0), paarung(TEAM_2, TEAM_1).score(1, 0));
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle( //
				tabellenplatz().platz(1).team(TEAM_1).punkte(3).tore(1).gegentore(1), //
				tabellenplatz().platz(1).team(TEAM_2).punkte(3).tore(1).gegentore(1) //
		);
	}

	private de.atruvia.ase.samman.TabelleTest.Paarung.PaarungBuilder paarung(String team1, String team2) {
		return Paarung.builder().team1(team1).team2(team2);
	}

	private de.atruvia.ase.samman.TabelleTest.TabellenPlatz.TabellenPlatzBuilder tabellenplatz() {
		return TabellenPlatz.builder();
	}

	private void gegenSeienDiePaarungen(Paarung.PaarungBuilder... paarungen) {
		this.paarungen = Arrays.stream(paarungen).map(Paarung.PaarungBuilder::build).toArray(Paarung[]::new);
	}

	private static class T {

		@Value
		@Builder
		static class Entry {
			int punkte;
			int tore;
			int gegentore;

			public Entry merge(Entry other) {
				return Entry.builder() //
						.punkte(this.punkte + other.punkte) //
						.tore(this.tore + other.tore) //
						.gegentore(this.gegentore + other.gegentore) //
						.build();
			}

		}

		private final Map<String, Entry> entries = new HashMap<>();

		void add(Paarung paarung) {
			addInternal(paarung);
			addInternal(paarung.reverse());
		}

		private void addInternal(Paarung paarung) {
			entries.merge(paarung.getTeam1(), new Entry(paarung.punkte(), paarung.tore, paarung.gegentore),
					Entry::merge);
		}

		public List<TabellenPlatz> getEntries() {
			return entries.entrySet().stream().map(e -> TabellenPlatz.builder().platz(1).team(e.getKey()) //
					.punkte(e.getValue().punkte) //
					.tore(e.getValue().tore) //
					.gegentore(e.getValue().gegentore) //
					.build()).collect(toList());
		}

	}

	private void wennDieTabelleBerechnetWird() {
		T t = new T();
		Arrays.stream(this.paarungen).forEach(t::add);
		tabelle = t.getEntries().toArray(TabellenPlatz[]::new);
	}

	private void dannIstDieTabelle(TabellenPlatz.TabellenPlatzBuilder... expected) {
		assertThat(tabelle).isEqualTo(
				Arrays.stream(expected).map(TabellenPlatz.TabellenPlatzBuilder::build).toArray(TabellenPlatz[]::new));
	}

}
