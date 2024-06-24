package de.atruvia.ase.samman.buli.infra.adapters.secondary;

import static de.atruvia.ase.samman.buli.domain.TeamMother.idFrankfurt;
import static de.atruvia.ase.samman.buli.domain.TeamMother.idMuenchen;
import static de.atruvia.ase.samman.buli.infra.adapters.secondary.OpenLigaDbSpieltagRepoMother.teamFsRepo;
import static java.net.URI.create;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import de.atruvia.ase.samman.buli.domain.Team;
import de.atruvia.ase.samman.buli.domain.Team.TeamId;

class OpenLigaDbTeamRepoTest {

	@Test
	void canRetrieveDataOf2022() {
		var teams = teams("bl1", "2022");
		assertThat(teamByIdentifier(teams, idFrankfurt)).hasValueSatisfying(t -> {
			assertThat(t.name()).isEqualTo("Eintracht Frankfurt");
			assertThat(t.wappen()).isEqualTo(create("https://i.imgur.com/X8NFkOb.png"));
		});
		assertThat(teamByIdentifier(teams, idMuenchen)).hasValueSatisfying(t -> {
			assertThat(t.name()).isEqualTo("FC Bayern München");
			assertThat(t.wappen()).isEqualTo(create("https://i.imgur.com/jJEsJrj.png"));
		});
	}

	Optional<Team> teamByIdentifier(List<Team> teams, TeamId teamId) {
		return teams.stream().filter(t -> teamId.equals(t.id())).findFirst();
	}

	List<Team> teams(String league, String season) {
		return repo().getTeams(league, season);
	}

	OpenLigaDbTeamRepo repo() {
		return teamFsRepo();
	}

}
