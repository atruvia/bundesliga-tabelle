package de.atruvia.ase.samman;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import lombok.Builder;
import lombok.Value;

class TabelleTest {

	@Value
	@Builder
	private static class TabellenPlatz {
		int platz;
		String team;
		int punkte;
		int tore;
	}

	private String[] teams;
	private TabellenPlatz[] tabelle;

	@Test
	void zweiMannschaftenKeinSpiel() {
		gegebenSeien("Team1", "Team2");
		gegenSeienErgebisse();
		wennDieTabelleBerechnetWird();
		dannIstDieTabelle(tabellenplatz().platz(1).team("Team 1"),
				tabellenplatz().platz(2).team("Team 2"));
	}

	private de.atruvia.ase.samman.TabelleTest.TabellenPlatz.TabellenPlatzBuilder tabellenplatz() {
		return TabellenPlatz.builder();
	}

	private void gegebenSeien(String... teams) {
		this.teams = teams;
	}

	private void gegenSeienErgebisse() {
		// TODO Auto-generated method stub
	}

	private void wennDieTabelleBerechnetWird() {
		tabelle = new TabellenPlatz[] { //
				tabellenplatz().platz(1).team("Team 1").build(), //
				tabellenplatz().platz(2).team("Team 2").build() //
		};
	}

	private void dannIstDieTabelle(TabellenPlatz.TabellenPlatzBuilder... expected) {
		assertThat(tabelle).isEqualTo(expected);
	}

}
