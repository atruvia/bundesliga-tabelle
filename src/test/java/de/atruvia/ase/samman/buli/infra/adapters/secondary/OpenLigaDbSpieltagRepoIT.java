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

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import de.atruvia.ase.samman.buli.domain.Paarung;
import de.atruvia.ase.samman.buli.domain.Paarung.Entry;
import de.atruvia.ase.samman.buli.domain.Team;
import de.atruvia.ase.samman.buli.infra.internal.AvailableLeagueRepo;
import de.atruvia.ase.samman.buli.infra.internal.DefaultOpenLigaDbResultinfoRepo;
import de.atruvia.ase.samman.buli.infra.internal.RestClient;

class OpenLigaDbSpieltagRepoIT {

	@Test
	void canRetrieveDataOf2022() {
		var paarungen = repo().lade("bl1", "2022");
		checkPropertiesOfFullSeason(paarungen, 18);
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
		checkPropertiesOfFullSeason(paarungen, 18);
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
		checkPropertiesOfFullSeason(paarungen, 18);
		var expected0 = Paarung.builder() //
				.ergebnisTyp(BEENDET) //
				.heim(entry(teamDortmund, 2)) //
				.gast(entry(teamFrankfurt, 0)) //
				.build();
		assertThat(paarungen).hasSize(matchesOfFullSeasonOfTeams(18)).element(6).isEqualTo(expected0);
	}

	void checkPropertiesOfFullSeason(List<Paarung> paarungen, int teams) {
		assertThat(paarungen).hasSize(matchesOfFullSeasonOfTeams(teams));
		var byHeim = paarungen.stream().collect(groupingBy(p -> p.heim().team()));
		var byGast = paarungen.stream().collect(groupingBy(p -> p.gast().team()));
		assertThat(byHeim.keySet()).containsExactlyInAnyOrderElementsOf(byGast.keySet());
		assertThat(byHeim).hasSize(teams).allSatisfy((k, v) -> assertThat(v).hasSize(matchdaysPerRound(teams)));
		assertThat(byGast).hasSize(teams).allSatisfy((k, v) -> assertThat(v).hasSize(matchdaysPerRound(teams)));
	}

	void matchIs(Paarung paarung, Team expectedHeim, Team expectedGast) {
		assertSoftly(s -> {
			teamIs(paarung.heim(), expectedHeim);
			teamIs(paarung.gast(), expectedGast);
		});
	}

	void teamIs(Entry entry, Team expectedTeam) {
		assertThat(entry.team()).isEqualTo(expectedTeam);
	}

	OpenLigaDbSpieltagRepo repo() {
		RestClient restClient = new RestClient(new RestTemplate());
		return new OpenLigaDbSpieltagRepo(restClient,
				new DefaultOpenLigaDbResultinfoRepo(restClient, new AvailableLeagueRepo(restClient)));
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
