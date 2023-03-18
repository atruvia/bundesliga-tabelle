package de.atruvia.ase.samman.buli.domain.ports.primary;

import static de.atruvia.ase.samman.buli.infra.adapters.secondary.OpenLigaDbSpieltagRepoMother.spieltagFsRepo;
import static java.util.stream.Collectors.joining;
import static org.approvaltests.Approvals.verify;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.jupiter.api.Test;

import de.atruvia.ase.samman.buli.domain.Paarung.Ergebnis;
import de.atruvia.ase.samman.buli.domain.TabellenPlatz;

class DefaultTabellenServiceTest {

	@Test
	void tabelleBl12022Spieltag24() {
		TabellenService sut = new DefaultTabellenService(spieltagFsRepo());
		List<TabellenPlatz> erstelleTabelle = sut.erstelleTabelle("bl1", "2022");
		String tabelle = erstelleTabelle.stream().map(f -> print(f, longestTeamName(erstelleTabelle)))
				.collect(joining("\n"));
		verify(tabelle);
	}

	private int longestTeamName(List<TabellenPlatz> tabellenPlaetze) {
		return tabellenPlaetze.stream().map(TabellenPlatz::getTeam).mapToInt(String::length).max().orElse(0);
	}

	private String print(TabellenPlatz tabellenPlatz, int length) {
		return Arrays
				.asList(stringFormat(length, tabellenPlatz.getTeam()), tabellenPlatz.getSpiele(),
						tabellenPlatz.getSiege(), tabellenPlatz.getUnentschieden(), tabellenPlatz.getNiederlagen(),
						tabellenPlatz.getTore(), tabellenPlatz.getGegentore(), tabellenPlatz.getTorDifferenz(),
						tabellenPlatz.getPunkte(), firstCharOf(tabellenPlatz.getLetzte(5)), tabellenPlatz.getWappen())
				.stream().map(this::format).collect(joining(" | "));
	}

	private String firstCharOf(List<Ergebnis> ergebnisse) {
		return ergebnisse.stream().map(e -> e.name().substring(0, 1)).collect(joining());
	}

	private String stringFormat(int length, String team) {
		return String.format("%-" + (length + 1) + "s", team);
	}

	private String format(Object o) {
		return o instanceof Number ? String.format("%3d", o) : Objects.toString(o);
	}

}
