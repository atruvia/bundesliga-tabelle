package de.atruvia.ase.samman.buli.infra.adapters.secondary;

import static de.atruvia.ase.samman.buli.domain.Team.TeamId.teamId;
import static java.net.URI.create;
import static java.util.Arrays.stream;
import static lombok.AccessLevel.PUBLIC;

import java.net.URI;
import java.util.List;

import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import de.atruvia.ase.samman.buli.domain.Team;
import de.atruvia.ase.samman.buli.domain.Team.TeamId;
import de.atruvia.ase.samman.buli.domain.ports.secondary.TeamRepo;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Repository
@RequiredArgsConstructor
@SecondaryAdapter
class OpenLigaDbTeamRepo implements TeamRepo {

	private static final String SERVICE_URI = "https://api.openligadb.de/getavailableteams/{league}/{season}";

	private final RestTemplate restTemplate;

	@ToString
	@FieldDefaults(level = PUBLIC)
	@SecondaryAdapter
	// the structure of teams are identical in the Team- and the MatchRepos.
	// Copy/duplicate this class if they start diverging!
	static class OpenligaDbTeam {
		Number teamId;
		String teamName;
		String teamIconUrl;

		Team toDomain() {
			return Team.builder().id(toTeamId(teamId)) //
					.name(teamName) //
					.wappen(toURI(teamIconUrl)) //
					.build();
		}

		private static TeamId toTeamId(Number teamId) {
			return teamId == null ? null : teamId(teamId.toString());
		}

		private static URI toURI(String wappen) {
			return wappen == null ? null : create(wappen);
		}

	}

	@Override
	public List<Team> getTeams(String league, String season) {
		return stream(restTemplate.getForObject(SERVICE_URI, OpenligaDbTeam[].class, league, season)) //
				.map(OpenligaDbTeam::toDomain) //
				.toList();
	}

}
