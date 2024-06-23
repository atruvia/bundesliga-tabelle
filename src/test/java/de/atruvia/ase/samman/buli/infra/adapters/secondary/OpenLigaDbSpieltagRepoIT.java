package de.atruvia.ase.samman.buli.infra.adapters.secondary;

import static de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp.BEENDET;
import static de.atruvia.ase.samman.buli.domain.TeamMother.teamBremen;
import static de.atruvia.ase.samman.buli.domain.TeamMother.teamFrankfurt;
import static de.atruvia.ase.samman.buli.domain.TeamMother.teamMuenchen;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ExpectedToFail;
import org.springframework.web.client.RestTemplate;

import de.atruvia.ase.samman.buli.domain.Paarung;
import de.atruvia.ase.samman.buli.domain.Paarung.Entry;
import de.atruvia.ase.samman.buli.infra.internal.AvailableLeagueRepo;
import de.atruvia.ase.samman.buli.infra.internal.DefaultOpenLigaDbResultinfoRepo;

class OpenLigaDbSpieltagRepoIT {

	@Test
	void canRetrieveDataOf2022() {
		List<Paarung> paarungen = repo().lade("bl1", "2022");
		Paarung expected0 = Paarung.builder() //
				.ergebnisTyp(BEENDET) //
				.heim(new Entry(teamFrankfurt, 1)) //
				.gast(new Entry(teamMuenchen, 6)) //
				.build();
		assertThat(paarungen).hasSize(matchesOfFullSeasonOfTeams(18)).element(0).isEqualTo(expected0);
	}

	@Test
	void canRetrieveDataOf2023() {
		List<Paarung> paarungen = repo().lade("bl1", "2023");
		Paarung expected0 = Paarung.builder() //
				.ergebnisTyp(BEENDET) //
				.heim(new Entry(teamBremen, 0)) //
				.gast(new Entry(teamMuenchen, 4)) //
				.build();
		assertThat(paarungen).hasSize(matchesOfFullSeasonOfTeams(18)).element(0).isEqualTo(expected0);
	}

	@Test
	@ExpectedToFail("no data for 2024/25 yet --- add assertions for match #0 when available")
	void canRetrieveDataOf2024() {
		assertThat(repo().lade("bl1", "2024")).isNotEmpty();
	}

	OpenLigaDbSpieltagRepo repo() {
		RestTemplate restTemplate = new RestTemplate();
		return new OpenLigaDbSpieltagRepo(restTemplate,
				new DefaultOpenLigaDbResultinfoRepo(restTemplate, new AvailableLeagueRepo(restTemplate)));
	}

	int matchesOfFullSeasonOfTeams(int teams) {
		return matchesPerMatchday(teams) * matchdays(teams);
	}

	int matchdays(int teams) {
		return (teams - 1) * 2;
	}

	int matchesPerMatchday(int teams) {
		return teams / 2;
	}

}
