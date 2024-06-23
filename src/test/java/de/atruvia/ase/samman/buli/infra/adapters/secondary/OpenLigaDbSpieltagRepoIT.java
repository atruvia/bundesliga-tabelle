package de.atruvia.ase.samman.buli.infra.adapters.secondary;

import static de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp.BEENDET;
import static de.atruvia.ase.samman.buli.domain.Team.TeamIdentifier.teamIdentifier;
import static java.net.URI.create;
import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ExpectedToFail;
import org.springframework.web.client.RestTemplate;

import de.atruvia.ase.samman.buli.domain.Paarung;
import de.atruvia.ase.samman.buli.domain.Paarung.Entry;
import de.atruvia.ase.samman.buli.domain.Team;
import de.atruvia.ase.samman.buli.infra.internal.AvailableLeagueRepo;
import de.atruvia.ase.samman.buli.infra.internal.DefaultOpenLigaDbResultinfoRepo;

class OpenLigaDbSpieltagRepoIT {

	int idFrankfurt = 91;
	int idMuenchen = 40;
	int idBremen = 134;

	String teamMuenchen = "FC Bayern München";
	String teamFrankfurt = "Eintracht Frankfurt";
	String teamBremen = "Werder Bremen";

	URI wappenFrankfurt = create("https://i.imgur.com/X8NFkOb.png");
	URI wappenMuenchen = create("https://i.imgur.com/jJEsJrj.png");
	URI wappenBremen = create("https://upload.wikimedia.org/wikipedia/commons/thumb/b/be/"
			+ "SV-Werder-Bremen-Logo.svg/681px-SV-Werder-Bremen-Logo.svg.png");

	@Test
	void canRetrieveDataOf2022() {
		List<Paarung> paarungen = repo().lade("bl1", "2022");
		Paarung expected0 = Paarung.builder() //
				.ergebnisTyp(BEENDET) //
				.heim(new Entry(Team.builder().identifier(teamIdentifier(idFrankfurt)).name(teamFrankfurt)
						.wappen(wappenFrankfurt).build(), 1)) //
				.gast(new Entry(Team.builder().identifier(teamIdentifier(idMuenchen)).name(teamMuenchen)
						.wappen(wappenMuenchen).build(), 6)) //
				.build();
		assertThat(paarungen).hasSize(matchesOfFullSeason(18)).element(0).isEqualTo(expected0);
	}

	@Test
	void canRetrieveDataOf2023() {
		List<Paarung> paarungen = repo().lade("bl1", "2023");
		Paarung expected0 = Paarung.builder() //
				.ergebnisTyp(BEENDET) //
				.heim(new Entry(Team.builder().identifier(teamIdentifier(idBremen)).name(teamBremen)
						.wappen(wappenBremen).build(), 0)) //
				.gast(new Entry(Team.builder().identifier(teamIdentifier(idMuenchen)).name(teamMuenchen)
						.wappen(wappenMuenchen).build(), 4)) //
				.build();
		assertThat(paarungen).hasSize(matchesOfFullSeason(18)).element(0).isEqualTo(expected0);
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

	int matchesOfFullSeason(int teams) {
		return matchesPerMatchday(teams) * matchdays(teams);
	}

	int matchdays(int teams) {
		return (teams - 1) * 2;
	}

	int matchesPerMatchday(int teams) {
		return teams / 2;
	}

}
