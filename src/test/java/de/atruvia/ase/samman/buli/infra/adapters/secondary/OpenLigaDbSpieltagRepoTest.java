package de.atruvia.ase.samman.buli.infra.adapters.secondary;

import static de.atruvia.ase.samman.buli.domain.Paarung.PaarungBuilder.paarung;
import static de.atruvia.ase.samman.buli.domain.Team.TeamId.teamId;
import static de.atruvia.ase.samman.buli.domain.TeamMother.teamFrankfurt;
import static de.atruvia.ase.samman.buli.infra.adapters.secondary.OpenLigaDbSpieltagRepoMother.resultinfoProvider;
import static de.atruvia.ase.samman.buli.infra.adapters.secondary.OpenLigaDbSpieltagRepoMother.spieltagFsRepo;
import static de.atruvia.ase.samman.buli.springframework.RestTemplateMock.restClient;
import static java.net.URI.create;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpResponse;

import de.atruvia.ase.samman.buli.domain.Team;
import de.atruvia.ase.samman.buli.domain.TeamMother;
import de.atruvia.ase.samman.buli.infra.internal.RestClient;

class OpenLigaDbSpieltagRepoTest {

	static final Team teamMuenchen = TeamMother.teamMuenchen.withWappen(create("https://i.imgur.com/jJEsJrj.png"));
	static final Team teamBremen = TeamMother.teamBremen.withName("Werder Bremen");

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
	void returnsEmptyListIfRestClientAnswered404() {
		RestClient restClient = restClient(() -> new MockClientHttpResponse(new byte[0], NOT_FOUND));
		assertThat(new OpenLigaDbSpieltagRepo(restClient, resultinfoProvider(2)).lade("any", "any")).isEmpty();
	}

	@Test
	void throwsExceptionIfErrorButErrorIsNot404() {
		HttpStatus notOkNorNotFound = httpStatusOtherThan(OK, NOT_FOUND);
		RestClient restClient = restClient(() -> new MockClientHttpResponse(new byte[0], notOkNorNotFound));
		OpenLigaDbSpieltagRepo sut = new OpenLigaDbSpieltagRepo(restClient, resultinfoProvider(2));
		assertThatRuntimeException().isThrownBy(() -> sut.lade("any", "any"))
				.withMessageContaining(notOkNorNotFound.getReasonPhrase());
	}

	@Test
	void throwsExceptionIfResultInfoProviderThrowsException() {
		String message = "Cannot load";
		RestClient restClient = restClient(() -> new MockClientHttpResponse(new byte[0], OK));
		OpenLigaDbSpieltagRepo sut = new OpenLigaDbSpieltagRepo(restClient, (__, ___) -> {
			throw new IllegalStateException(message);
		});
		assertThatRuntimeException().isThrownBy(() -> sut.lade("any", "any")).withMessageContaining(message);
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
		OpenLigaDbSpieltagRepo repo = new OpenLigaDbSpieltagRepo(restClient, resultinfoProvider(2));
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
		var paarungen = new OpenLigaDbSpieltagRepo(restClient, resultinfoProvider(2)).lade("any", "any");
		var heim = Team.builder().id(teamId(42)).name("Team-A").kurzname("Team-A").build();
		var gast = Team.builder().id(teamId(43)).name("Team-B").kurzname("Team-B").build();
		var expected0 = paarung(heim, gast).zwischenergebnis(98, 99).build();
		assertThat(paarungen).hasSize(1).element(0).isEqualTo(expected0);
	}

	OpenLigaDbSpieltagRepo repo() {
		return spieltagFsRepo();
	}

	private static HttpStatus httpStatusOtherThan(HttpStatus... otherThan) {
		HttpStatus status = METHOD_NOT_ALLOWED;
		assert !Arrays.asList(otherThan).contains(status);
		return status;
	}
}
