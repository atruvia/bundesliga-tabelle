package de.atruvia.ase.samman.buli.infra.adapters.secondary;

import static de.atruvia.ase.samman.buli.domain.Paarung.ErgebnisTyp.BEENDET;
import static de.atruvia.ase.samman.buli.domain.Team.TeamIdentifier.teamIdentifier;
import static de.atruvia.ase.samman.buli.infra.adapters.secondary.OpenLigaDbSpieltagRepoMother.resultinfoProvider;
import static de.atruvia.ase.samman.buli.infra.adapters.secondary.OpenLigaDbSpieltagRepoMother.spieltagFsRepo;
import static de.atruvia.ase.samman.buli.springframework.RestTemplateMock.restTemplateMock;
import static java.net.URI.create;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import de.atruvia.ase.samman.buli.domain.Paarung;
import de.atruvia.ase.samman.buli.domain.Paarung.Entry;
import de.atruvia.ase.samman.buli.domain.Team;

class OpenLigaDbSpieltagRepoTest {

	int idFrankfurt = 91;
	int idMuenchen = 40;
	int idBremen = 134;

	String teamFrankfurt = "Eintracht Frankfurt";
	String teamMuenchen = "FC Bayern München";
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
		assertThat(paarungen).hasSize(306).element(0).isEqualTo(expected0);
	}

	@Test
	void canRetrieveDataOf2023() {
		// 2022: "endergebnis" was first element of array, 2023 it was last -> filter
		// "resultTypeID" = 2 for now
		List<Paarung> paarungen = repo().lade("bl1", "2023");
		Paarung expected0 = Paarung.builder() //
				.ergebnisTyp(BEENDET) //
				.heim(new Entry(Team.builder().identifier(teamIdentifier(idBremen)).name(teamBremen)
						.wappen(wappenBremen).build(), 0)) //
				.gast(new Entry(Team.builder().identifier(teamIdentifier(idMuenchen)).name(teamMuenchen)
						.wappen(wappenMuenchen).build(), 4)) //
				.build();
		assertThat(paarungen).hasSize(9).element(0).isEqualTo(expected0);
	}

	@Test
	void throwsExceptionIfThereAreMatchesWithMultipleFinalResults() {
		RestTemplate restTemplate = restTemplateMock(__ -> """
				[
				  {
					"team1": { "teamName": "Team 1", "teamIconUrl": "teamIconUrl1" },
					"team2": { "teamName": "Team 2", "teamIconUrl": "teamIconUrl2" },
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
