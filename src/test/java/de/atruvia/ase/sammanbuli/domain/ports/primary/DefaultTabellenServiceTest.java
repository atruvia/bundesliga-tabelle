package de.atruvia.ase.sammanbuli.domain.ports.primary;

import static de.atruvia.ase.sammanbuli.infra.adapters.secondary.OpenLigaDbSpieltagRepoMother.readFromLocalFilesystemRepo;
import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.OptionalInt;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.Test;

import de.atruvia.ase.sammanbuli.domain.TabellenPlatz;

class DefaultTabellenServiceTest {

	@Test
	void test() {
		TabellenService sut = new DefaultTabellenService(readFromLocalFilesystemRepo());
		List<TabellenPlatz> erstelleTabelle = sut.erstelleTabelle("bl1", "2022");

		int longestTeamName = longestTeamName(erstelleTabelle);
		
		String s = erstelleTabelle.stream().map(f -> print(f, longestTeamName)).collect(joining("\n"));

		System.out.println(s);

		assertThat(s).isEqualTo("");
	}

	private int longestTeamName(List<TabellenPlatz> erstelleTabelle) {
		return erstelleTabelle.stream().map(p -> p.getTeam()).mapToInt(String::length).max().orElse(0);
	}

	private String print(TabellenPlatz tabellenPlatz, String length) {
		
		return String.format("%1$" + length + "s", inputString).replace(' ', '0');
		
		return java.util.Arrays
				.asList(tabellenPlatz.getTeam(), tabellenPlatz.getSpiele(), tabellenPlatz.getGewonnen(),
						tabellenPlatz.getUnentschieden(), tabellenPlatz.getVerloren(), tabellenPlatz.getTore(),
						tabellenPlatz.getGegentore(), tabellenPlatz.getTorDifferenz(), tabellenPlatz.getPunkte())
				.stream().map(Object::toString).collect(joining("\t|"));
	}

}
