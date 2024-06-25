package de.atruvia.ase.samman.buli.infra.adapters.secondary;

import static de.atruvia.ase.samman.buli.domain.Paarung.PaarungBuilder.paarung;
import static de.atruvia.ase.samman.buli.domain.TeamMother.teamBremen;
import static de.atruvia.ase.samman.buli.domain.TeamMother.teamFrankfurt;
import static de.atruvia.ase.samman.buli.domain.TeamMother.teamMuenchen;
import static de.atruvia.ase.samman.buli.infra.adapters.secondary.OpenLigaDbSpieltagRepoMother.resultinfoProvider;
import static de.atruvia.ase.samman.buli.infra.adapters.secondary.OpenLigaDbSpieltagRepoMother.spieltagFsRepo;
import static de.atruvia.ase.samman.buli.springframework.RestTemplateMock.restTemplateMock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

class OpenLigaDbSpieltagRepoTest {

	@Test
	void canRetrieveDataOf2022() {
		var paarungen = repo().lade("bl1", "2022");
		var expected0 = paarung(teamFrankfurt, teamMuenchen).endergebnis(1, 6).build();
		assertThat(paarungen).hasSize(306).element(0).isEqualTo(expected0);
	}

	@Test
	void canRetrieveDataOf2023() {
		// 2022: "endergebnis" was first element of array, 2023 it was last -> filter
		// "resultTypeID" = 2 for now
		var paarungen = repo().lade("bl1", "2023");
		var expected0 = paarung(teamBremen, teamMuenchen).endergebnis(0, 4).build();
		assertThat(paarungen).hasSize(9).element(0).isEqualTo(expected0);
	}

	@Test
	void throwsExceptionIfThereAreMatchesWithMultipleFinalResults() {
		RestTemplate restTemplate = restTemplateMock(__ -> """
				[
				  {
					"team1": { "teamId": 42, "teamName": "Team 1", "teamIconUrl": "teamIconUrl1" },
					"team2": { "teamId": 43, "teamName": "Team 2", "teamIconUrl": "teamIconUrl2" },
					"matchIsFinished": true,
				    "matchResults": [ { "resultTypeID": 2 }, { "resultTypeID": 2 } ]
				  }
				 ]
				""");
		OpenLigaDbSpieltagRepo repo = new OpenLigaDbSpieltagRepo(restTemplate, resultinfoProvider(2));
		assertThatThrownBy(() -> repo.lade("any", "any")).hasMessageContaining("at most one element");
	}

	OpenLigaDbSpieltagRepo repo() {
		return spieltagFsRepo();
	}

}
