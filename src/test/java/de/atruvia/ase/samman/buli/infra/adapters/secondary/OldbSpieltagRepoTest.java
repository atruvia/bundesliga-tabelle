package de.atruvia.ase.samman.buli.infra.adapters.secondary;

import static de.atruvia.ase.samman.buli.domain.Paarung.PaarungBuilder.paarung;
import static de.atruvia.ase.samman.buli.domain.Team.TeamId.teamId;
import static de.atruvia.ase.samman.buli.domain.TeamMother.teamBremen;
import static de.atruvia.ase.samman.buli.domain.TeamMother.teamFrankfurt;
import static de.atruvia.ase.samman.buli.domain.TeamMother.teamMuenchen;
import static de.atruvia.ase.samman.buli.infra.adapters.secondary.OldbSpieltagRepoMother.resultInfoProvider;
import static de.atruvia.ase.samman.buli.infra.adapters.secondary.OldbSpieltagRepoMother.spieltagFsRepo;
import static de.atruvia.ase.samman.buli.springframework.RestTemplateMock.restClient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import de.atruvia.ase.samman.buli.domain.Team;
import de.atruvia.ase.samman.buli.infra.internal.RestClient;

class OldbSpieltagRepoTest {

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
		RestClient restClient = restClient(__ -> """
				[
				  {
					"team1": { "teamId": 42, "teamName": "Team-A", "teamIconUrl": "teamIconUrl1" },
					"team2": { "teamId": 43, "teamName": "Team-B", "teamIconUrl": "teamIconUrl2" },
					"matchIsFinished": true,
				    "matchResults": [ { "resultTypeID": 2 }, { "resultTypeID": 2 } ]
				  }
				 ]
				""");
		OldbSpieltagRepo repo = new OldbSpieltagRepo(restClient, resultInfoProvider(2));
		assertThatThrownBy(() -> repo.lade("any", "any")).hasMessageContaining("at most one element");
	}

	@Test
	void goalsInSameMinuteGetSortedUsingGoalId() {
		RestClient restClient = restClient(__ -> """
				[
				  {
					"team1": { "teamId": 42, "teamName": "Team-A" },
					"team2": { "teamId": 43, "teamName": "Team-B" },
				    "matchResults": [ { "resultTypeID": 2 } ],
				    "goals": [
						{ "matchMinute": 42, "goalID": 2, "scoreTeam1": 98, "scoreTeam2": 99 },
						{ "matchMinute": 42, "goalID": 1, "scoreTeam1":  1, "scoreTeam2":  1 }
					]
				  }
				 ]
				""");
		var paarungen = new OldbSpieltagRepo(restClient, resultInfoProvider(2)).lade("any", "any");
		var heim = Team.builder().id(teamId(42)).name("Team-A").build();
		var gast = Team.builder().id(teamId(43)).name("Team-B").build();
		var expected0 = paarung(heim, gast).zwischenergebnis(98, 99).build();
		assertThat(paarungen).hasSize(1).element(0).isEqualTo(expected0);
	}

	private OldbSpieltagRepo repo() {
		return spieltagFsRepo();
	}

}
