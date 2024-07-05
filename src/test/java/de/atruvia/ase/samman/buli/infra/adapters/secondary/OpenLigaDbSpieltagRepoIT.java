package de.atruvia.ase.samman.buli.infra.adapters.secondary;

import static de.atruvia.ase.samman.buli.domain.Paarung.Entry.entry;
import static de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp.BEENDET;
import static de.atruvia.ase.samman.buli.domain.TeamMother.teamBremen;
import static de.atruvia.ase.samman.buli.domain.TeamMother.teamFrankfurt;
import static de.atruvia.ase.samman.buli.domain.TeamMother.teamMuenchen;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import de.atruvia.ase.samman.buli.domain.Paarung;
import de.atruvia.ase.samman.buli.domain.Team;
import de.atruvia.ase.samman.buli.infra.internal.AvailableLeagueRepo;
import de.atruvia.ase.samman.buli.infra.internal.DefaultOpenLigaDbResultinfoRepo;
import de.atruvia.ase.samman.buli.infra.internal.RestClient;

class OpenLigaDbSpieltagRepoIT {

	@Test
	void canRetrieveDataOf2022() {
		var paarungen = repo().lade("bl1", "2022");
		var expected0 = Paarung.builder() //
				.ergebnisTyp(BEENDET) //
				.heim(entry(teamFrankfurt, 1)) //
				.gast(entry(teamMuenchen, 6)) //
				.build();
		assertThat(paarungen).hasSize(matchesOfFullSeasonOfTeams(18)).element(0).isEqualTo(expected0);
	}

	@Test
	void canRetrieveDataOf2023() {
		var paarungen = repo().lade("bl1", "2023");
		var expected0 = Paarung.builder() //
				.ergebnisTyp(BEENDET) //
				.heim(entry(teamBremen, 0)) //
				.gast(entry(teamMuenchen, 4)) //
				.build();
		assertThat(paarungen).hasSize(matchesOfFullSeasonOfTeams(18)).element(0).isEqualTo(expected0);
	}

	@Test
	void canRetrieveDataOf2024() {
		var paarungen = repo().lade("bl1", "2024");
		assertThat(paarungen).hasSize(matchesOfFullSeasonOfTeams(18));
		assertThat(paarungen).element(94).satisfies(p -> matchIs(p, teamFrankfurt, teamBremen));
		assertThat(paarungen).element(245).satisfies(p -> matchIs(p, teamBremen, teamFrankfurt));
	}

	void matchIs(Paarung paarung, Team expectedHeim, Team expectedGast) {
		assertSoftly(s -> {
			assertThat(paarung.heim().team()).isEqualTo(expectedHeim);
			assertThat(paarung.gast().team()).isEqualTo(expectedGast);
		});
	}

	OpenLigaDbSpieltagRepo repo() {
		RestClient restClient = new RestClient(new RestTemplate());
		return new OpenLigaDbSpieltagRepo(restClient,
				new DefaultOpenLigaDbResultinfoRepo(restClient, new AvailableLeagueRepo(restClient)));
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
