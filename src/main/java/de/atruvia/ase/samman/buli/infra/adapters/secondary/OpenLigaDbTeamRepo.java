package de.atruvia.ase.samman.buli.infra.adapters.secondary;

import static de.atruvia.ase.samman.buli.domain.Team.TeamIdentifier.teamIdentifier;
import static java.util.Arrays.stream;
import static lombok.AccessLevel.PUBLIC;

import java.net.URI;
import java.util.List;

import org.jmolecules.architecture.hexagonal.SecondaryAdapter;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import de.atruvia.ase.samman.buli.domain.Team;
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
	private static class JsonTeam {
		long teamId;
		String teamName;
		String teamIconUrl;

		Team toDomain() {
			return Team.builder().identifier(teamIdentifier(teamId)).name(teamName).wappen(toURI(teamIconUrl)).build();
		}

		private static URI toURI(String wappen) {
			return wappen == null ? null : URI.create(wappen);
		}

	}

	@Override
	public List<Team> getTeams(String league, String season) {
		return stream(restTemplate.getForObject(SERVICE_URI, JsonTeam[].class, league, season)).map(JsonTeam::toDomain)
				.toList();
	}

}
