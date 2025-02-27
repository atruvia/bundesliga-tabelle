package de.atruvia.ase.samman.buli.infra.adapters.secondary;

import static de.atruvia.ase.samman.buli.domain.Paarung.Entry.entry;
import static de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp.BEENDET;
import static de.atruvia.ase.samman.buli.domain.TeamMother.teamBremen;
import static de.atruvia.ase.samman.buli.domain.TeamMother.teamDortmund;
import static de.atruvia.ase.samman.buli.domain.TeamMother.teamFrankfurt;
import static de.atruvia.ase.samman.buli.domain.TeamMother.teamMuenchen;
import static java.util.stream.Collectors.groupingBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import de.atruvia.ase.samman.buli.domain.Paarung;
import de.atruvia.ase.samman.buli.domain.Paarung.Entry;
import de.atruvia.ase.samman.buli.domain.Team;
import de.atruvia.ase.samman.buli.infra.internal.AvailableLeagueRepo;
import de.atruvia.ase.samman.buli.infra.internal.DefaultOpenLigaDbResultinfoRepo;
import de.atruvia.ase.samman.buli.infra.internal.RestClient;

class OpenLigaDbSpieltagRepoIT {

	private final OpenLigaDbSpieltagRepo sut = repo();

	private static OpenLigaDbSpieltagRepo repo() {
		RestClient restClient = new RestClient(new RestTemplate());
		return new OpenLigaDbSpieltagRepo(restClient,
				new DefaultOpenLigaDbResultinfoRepo(restClient, new AvailableLeagueRepo(restClient)));
	}

	@Test
	void canRetrieveDataOf2022() {
		var paarungen = sut.lade("bl1", "2022");
		checkPropertiesOfFullSeason(paarungen);
		var expected = Paarung.builder() //
				.ergebnisTyp(BEENDET) //
				.heim(entry(teamFrankfurt, 1)) //
				.gast(entry(teamMuenchen, 6)) //
				.build();
		assertThat(paarungen).hasSize(matchesOfFullSeasonOfTeams(18)).element(0).isEqualTo(expected);
	}

	@Test
	void canRetrieveDataOf2023() {
		var paarungen = sut.lade("bl1", "2023");
		checkPropertiesOfFullSeason(paarungen);
		var expected = Paarung.builder() //
				.ergebnisTyp(BEENDET) //
				.heim(entry(teamBremen, 0)) //
				.gast(entry(teamMuenchen, 4)) //
				.build();
		assertThat(paarungen).hasSize(matchesOfFullSeasonOfTeams(18)).element(0).isEqualTo(expected);
	}

	@Test
	void canRetrieveDataOf2024() {
		var paarungen = sut.lade("bl1", "2024");
		checkPropertiesOfFullSeason(paarungen);
		var expected = Paarung.builder() //
				.ergebnisTyp(BEENDET) //
				.heim(entry(teamDortmund, 2)) //
				.gast(entry(teamFrankfurt, 0)) //
				.build();
		assertThat(paarungen).hasSize(matchesOfFullSeasonOfTeams(18)).element(6).isEqualTo(expected);
	}

	void checkPropertiesOfFullSeason(List<Paarung> paarungen) {
		int expectedTeams = 18;
		assertThat(paarungen).hasSize(matchesOfFullSeasonOfTeams(expectedTeams));
		var byHeim = teams(paarungen, p -> p.heim().team());
		var byGast = teams(paarungen, p -> p.gast().team());
		assertThat(byHeim.keySet()).containsExactlyInAnyOrderElementsOf(byGast.keySet());
		assertThat(byHeim).hasSize(expectedTeams).allSatisfy((k, v) -> assertThat(v).hasSize(matchdaysPerRound(expectedTeams)));
		assertThat(byGast).hasSize(expectedTeams).allSatisfy((k, v) -> assertThat(v).hasSize(matchdaysPerRound(expectedTeams)));
	}

	void matchIs(Paarung paarung, Team expectedHeim, Team expectedGast) {
		assertSoftly(s -> {
			teamIs(paarung.heim(), expectedHeim);
			teamIs(paarung.gast(), expectedGast);
		});
	}

	Map<Team, List<Paarung>> teams(List<Paarung> paarungen, Function<Paarung, Team> classifier) {
		return paarungen.stream().collect(groupingBy(classifier));
	}

	void teamIs(Entry entry, Team expectedTeam) {
		assertThat(entry.team()).isEqualTo(expectedTeam);
	}

	int matchesOfFullSeasonOfTeams(int teams) {
		return matchesPerMatchday(teams) * matchdaysPerSeason(teams);
	}

	int matchdaysPerSeason(int teams) {
		return matchdaysPerRound(teams) * 2;
	}

	int matchdaysPerRound(int teams) {
		return teams - 1;
	}

	int matchesPerMatchday(int teams) {
		return teams / 2;
	}

}
